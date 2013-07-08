/*
 * Copyright 2013 Sebastiano Poggi and Francesco Pontillo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.frakbot.FWeather.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.util.TrackerHelper;

/**
 * A simple Activity that shows the third-party licenses for the app.
 * <p/>
 * Author: Sebastiano Poggi
 * Created on: 3/11/13 Time: 11:48 PM
 * File version: 1.0
 * <p/>
 * Changelog:
 * Version 1.0
 * * Initial revision
 */
public class LicensesActivity extends SherlockFragmentActivity {

    WebView mWebView;
    ProgressBar mProgressBar;

    @Override
    protected void onStart() {
        super.onStart();
        TrackerHelper.activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TrackerHelper.activityStop(this);
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        mWebView = (WebView) findViewById(R.id.web_license);
        mProgressBar = (ProgressBar) findViewById(android.R.id.empty);

        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                // Swap progressbar and WebView visibility
                mWebView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        mWebView.loadUrl("file:///android_asset/www/license.html");

        // Sets and shows the title in the ActionBar
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            //            actionBar.setDisplayHomeAsUpEnabled(true);     TODO
            //            ((ActionBar) actionBar).setIcon(R.drawable.ab_icon_normal);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}