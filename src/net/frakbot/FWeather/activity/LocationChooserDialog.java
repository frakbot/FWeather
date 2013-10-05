package net.frakbot.FWeather.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.updater.weather.YahooWeatherApiClient;
import net.frakbot.FWeather.util.WeatherLocationPreference;
import net.frakbot.util.log.FLog;

import java.util.ArrayList;
import java.util.List;

import static net.frakbot.FWeather.updater.weather.YahooWeatherApiClient.LocationSearchResult;

/**
 * Dialog that pops up when touching the Location preference.
 */
public class LocationChooserDialog extends SherlockFragmentActivity
    implements TextWatcher, LoaderManager.LoaderCallbacks<List<LocationSearchResult>> {

    /**
     * Time between search queries while typing.
     */
    private static final int QUERY_DELAY_MILLIS = 500;
    private static final String TAG = LocationChooserDialog.class.getSimpleName();

    private SearchResultsListAdapter mSearchResultsAdapter;
    private ListView mSearchResultsList;
    private boolean mSelectedValue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_weather_location_chooser);

        TextView searchView = (TextView) findViewById(R.id.location_query);
        searchView.addTextChangedListener(this);

        // Set up apps
        mSearchResultsList = (ListView) findViewById(android.R.id.list);
        mSearchResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long itemId) {
                String value = mSearchResultsAdapter.getPrefValueAt(position);
                FLog.d(TAG, "User has selected an item: " + value);
                try {
                    sendBroadcast(new Intent(WeatherLocationPreference.ACTION_SET_VALUE)
                                      .putExtra(WeatherLocationPreference.EXTRA_VALUE, value)
                                      .setPackage(getPackageName()));
                }
                catch (Exception e) {
                    FLog.w(TAG, "Unable to send the SET_VALUE broadcast", e);
                }
                mSelectedValue = true;
                finish();
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        tryBindList();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!mSelectedValue) {
            // If no value was selected, this has been canceled
            try {
                sendBroadcast(new Intent(WeatherLocationPreference.ACTION_CANCELED)
                                  .setPackage(getPackageName()));
            }
            catch (Exception e) {
                FLog.w(TAG, "Unable to send the CANCELED broadcast", e);
            }
        }
    }

    private void tryBindList() {
        if (mSearchResultsAdapter == null) {
            mSearchResultsAdapter = new SearchResultsListAdapter();
        }

        if (mSearchResultsList != null) {
            mSearchResultsList.setAdapter(mSearchResultsAdapter);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        mQuery = charSequence.toString();
        if (mRestartLoaderHandler.hasMessages(0)) {
            return;
        }

        mRestartLoaderHandler.sendMessageDelayed(
            mRestartLoaderHandler.obtainMessage(0),
            QUERY_DELAY_MILLIS);
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private String mQuery;

    private Handler mRestartLoaderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle args = new Bundle();
            args.putString("query", mQuery);
            getSupportLoaderManager().restartLoader(0, args, LocationChooserDialog.this);
        }
    };

    @Override
    public Loader<List<LocationSearchResult>> onCreateLoader(int id, Bundle args) {
        final String query = args.getString("query");
        return new ResultsLoader(query, this);
    }

    @Override
    public void onLoadFinished(Loader<List<LocationSearchResult>> loader,
                               List<LocationSearchResult> results) {
        mSearchResultsAdapter.changeArray(results);
    }

    @Override
    public void onLoaderReset(Loader<List<LocationSearchResult>> loader) {
        mSearchResultsAdapter.changeArray(null);
    }

    private class SearchResultsListAdapter extends BaseAdapter {
        private List<LocationSearchResult> mResults;

        private SearchResultsListAdapter() {
            mResults = new ArrayList<LocationSearchResult>();
        }

        public void changeArray(List<LocationSearchResult> results) {
            if (results == null) {
                results = new ArrayList<LocationSearchResult>();
            }

            mResults = results;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return Math.max(1, mResults.size());
        }

        @Override
        public Object getItem(int position) {
            if (position == 0 && mResults.size() == 0) {
                return null;
            }

            return mResults.get(position);
        }

        public String getPrefValueAt(int position) {
            if (position == 0 && mResults.size() == 0) {
                return "";
            }

            LocationSearchResult result = mResults.get(position);
            return result.woeid + "," + result.displayName;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0 && mResults.size() == 0) {
                return -1;
            }

            return mResults.get(position).woeid.hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(LocationChooserDialog.this)
                                            .inflate(R.layout.list_item_weather_location_result, container, false);
            }

            if (position == 0 && mResults.size() == 0) {
                ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(R.string.pref_weather_location_automatic);
                ((TextView) convertView.findViewById(android.R.id.text2))
                    .setText(R.string.pref_weather_location_automatic_description);
            }
            else {
                LocationSearchResult result = mResults.get(position);
                ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(result.displayName);
                ((TextView) convertView.findViewById(android.R.id.text2))
                    .setText(result.country);
            }

            return convertView;
        }
    }

    /**
     * Loader that fetches location search results from {@link net.frakbot.FWeather.updater.weather
     * .YahooWeatherApiClient}.
     */
    private static class ResultsLoader extends AsyncTaskLoader<List<LocationSearchResult>> {
        private String mQuery;
        private List<LocationSearchResult> mResults;

        public ResultsLoader(String query, Context context) {
            super(context);
            mQuery = query;
        }

        @Override
        public List<LocationSearchResult> loadInBackground() {
            return YahooWeatherApiClient.findLocationsAutocomplete(mQuery);
        }

        @Override
        public void deliverResult(List<LocationSearchResult> apps) {
            mResults = apps;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(apps);
            }
        }

        @Override
        protected void onStartLoading() {
            if (mResults != null) {
                deliverResult(mResults);
            }

            if (takeContentChanged() || mResults == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        protected void onReset() {
            super.onReset();
            onStopLoading();
        }
    }
}