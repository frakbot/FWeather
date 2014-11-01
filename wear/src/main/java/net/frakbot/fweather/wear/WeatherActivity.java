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
package net.frakbot.fweather.wear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.CardScrollView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.frakbot.fweather.wear.stuff.image.magic.wellnotreally.ImageMagician;

public class WeatherActivity extends Activity {

    public static final String EXTRA_PRIMARY_TEXT = "important_shit";
    public static final String EXTRA_SECONDARY_TEXT = "other_stuff";
    public static final String EXTRA_IMAGE = "dem_pixels";
    public static final String EXTRA_ACCENT_COLOR = "i_see_all_the_colors_accentuated";
    public static final String EXTRA_UNNECESSARY_EXTRA = "nobody_uses_me_#sadface";

    private TextView mPrimary;
    private TextView mSecondary;
    private ImageView mImage;
    private CardScrollView cardScrollView;
    private ImageMagician imageMagician;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        cardScrollView = (CardScrollView) findViewById(R.id.card_scroll_view);
        cardScrollView.setCardGravity(Gravity.BOTTOM);

        mPrimary = (TextView) findViewById(R.id.weather_title);
        mSecondary = (TextView) findViewById(R.id.weather_description);
        mImage = (ImageView) findViewById(R.id.weather_src);

        imageMagician = new ImageMagician();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        CharSequence primary = intent.getCharSequenceExtra(EXTRA_PRIMARY_TEXT);
        CharSequence secondary = intent.getCharSequenceExtra(EXTRA_SECONDARY_TEXT);
        String imagePath  = intent.getStringExtra(EXTRA_IMAGE);
        int accentColor  = intent.getIntExtra(EXTRA_ACCENT_COLOR, 0);

        updateUI(primary, secondary, imagePath, accentColor);
    }

    private void updateUI(CharSequence primary, CharSequence secondary, String imagePath, int accentColor) {
        mPrimary.setText(primary);

        if (secondary != null) {
            mSecondary.setText(secondary);
            mSecondary.setVisibility(View.VISIBLE);
        } else {
            mSecondary.setVisibility(View.GONE);
        }

        mImage.setImageBitmap(imageMagician.loadStuffFrom(imagePath));
        mPrimary.setText(primary);
        cardScrollView.setBackgroundColor(accentColor);
    }

}
