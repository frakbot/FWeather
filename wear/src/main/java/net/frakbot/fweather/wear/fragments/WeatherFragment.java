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
package net.frakbot.fweather.wear.fragments;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.CardScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.frakbot.fweather.wear.R;
import net.frakbot.fweather.wear.WeatherActivity;
import net.frakbot.fweather.wear.model.WeatherUpdate;
import net.frakbot.fweather.wear.stuff.image.magic.wellnotreally.ImageMagician;

public class WeatherFragment extends CardFragment {

    private TextView mPrimary;
    private TextView mSecondary;
    private ImageView mImage;
    private CardScrollView cardScrollView;

    private ImageMagician imageMagician;

    public static WeatherFragment create(WeatherUpdate weatherUpdate) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putCharSequence(WeatherActivity.EXTRA_PRIMARY_TEXT, weatherUpdate.getPrimary());
        bundle.putCharSequence(WeatherActivity.EXTRA_SECONDARY_TEXT, weatherUpdate.getSecondary());
        bundle.putString(WeatherActivity.EXTRA_IMAGE, weatherUpdate.getImage());
        bundle.putInt(WeatherActivity.EXTRA_ACCENT_COLOR, weatherUpdate.getAccentColor());
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageMagician = new ImageMagician();
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.weather_card, null);

        cardScrollView = (CardScrollView) v.findViewById(R.id.card_scroll_view);
        mPrimary = (TextView) v.findViewById(R.id.weather_title);
        mSecondary = (TextView) v.findViewById(R.id.weather_description);
        mImage = (ImageView) v.findViewById(R.id.weather_src);

        updateUI(extractPrimary(), extractSecondary(), extractImage(), extractAccentColor());

        return v;
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

    private CharSequence extractPrimary(){
        return getArguments().getCharSequence(WeatherActivity.EXTRA_PRIMARY_TEXT);
    }

    private CharSequence extractSecondary() {
        return getArguments().getCharSequence(WeatherActivity.EXTRA_SECONDARY_TEXT);
    }

    private String extractImage() {
        return getArguments().getString(WeatherActivity.EXTRA_IMAGE);
    }

    private int extractAccentColor() {
        return getArguments().getInt(WeatherActivity.EXTRA_ACCENT_COLOR);
    }

}
