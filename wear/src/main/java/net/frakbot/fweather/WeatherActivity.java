/*
 * Copyright 2014 Sebastiano Poggi and Francesco Pontillo
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
package net.frakbot.FWeather;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.ImageReference;
import android.view.Gravity;

import com.mariux.teleport.lib.TeleportClient;

import net.frakbot.common.SantaLittleHelper;
import net.frakbot.fweather.wear.fragments.ShareFragment;
import net.frakbot.fweather.wear.fragments.WeatherFragment;
import net.frakbot.fweather.wear.model.WeatherUpdate;
import net.frakbot.fweather.wear.stuff.image.magic.wellnotreally.ImageMagician;

public class WeatherActivity extends Activity implements ShareFragment.OnShareClickListener {

    public static final String EXTRA_PRIMARY_TEXT = "important_shit";
    public static final String EXTRA_SECONDARY_TEXT = "other_stuff";
    public static final String EXTRA_IMAGE = "dem_pixels";
    public static final String EXTRA_ACCENT_COLOR = "i_see_all_the_colors_accentuated";
    public static final String EXTRA_UNNECESSARY_EXTRA = "nobody_uses_me_#sadface";
    public static final String EXTRA_SHARE_EXTRA = "share_the_fucking_shit";

    private GridViewPager weatherPager;
    private WeatherUpdate weatherUpdate;
    private TeleportClient mTeleportClient;
    private TransactionPagerAdapter adapter;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        CharSequence primary = intent.getCharSequenceExtra(EXTRA_PRIMARY_TEXT);
        CharSequence secondary = intent.getCharSequenceExtra(EXTRA_SECONDARY_TEXT);
        String imagePath = intent.getStringExtra(EXTRA_IMAGE);
        int accentColor = intent.getIntExtra(EXTRA_ACCENT_COLOR, 0);

        weatherUpdate = new WeatherUpdate(primary, secondary, R.drawable.sky, accentColor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mTeleportClient = new TeleportClient(this);

        // TODO: HARDCODE EVERYTHINGZ!
        CharSequence main = SantaLittleHelper.getColoredSpannedString(this, R.string.weather_rainy, R.color.primary_text_light, R.color.primary_text_dark, false);
        CharSequence second = SantaLittleHelper.getColoredSpannedString(this, R.string.temp_cold, R.color.secondary_text_light, R.color.secondary_text_light, false);

        weatherUpdate = new WeatherUpdate(main,
                second,
                R.drawable.weather_background,
                0);

        weatherPager = (GridViewPager) findViewById(R.id.weather_pager);
        adapter = new TransactionPagerAdapter(getFragmentManager());
        weatherPager.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTeleportClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTeleportClient.disconnect();
    }

    @Override
    public void onShareSelected() {
        mTeleportClient.sendMessage(EXTRA_SHARE_EXTRA, null);
    }

    private void updateWeather(){
        ((WeatherFragment) adapter.getFragment(0,0)).updateWeatherWith(weatherUpdate);
    }

    private class TransactionPagerAdapter extends FragmentGridPagerAdapter {

        public TransactionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getColumnCount(int row) {
            return 1;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public Fragment getFragment(int row, int col) {
            if (col == 0) {
                WeatherFragment fragment = WeatherFragment.create(weatherUpdate);
                fragment.setCardGravity(Gravity.BOTTOM);
                return fragment;
            } else {
                return new ShareFragment();
            }
        }

        @Override
        public ImageReference getBackground(int row, int column) {
            return ImageReference.forDrawable(R.drawable.weather_bg_others);
//            Bitmap colorImage = createColorImageForBackground();
//            return ImageReference.forBitmap(colorImage);
        }

        private Bitmap createColorImageForBackground() {
            return ImageMagician.createColorImage(weatherUpdate.getAccentColor());
        }

    }

}

