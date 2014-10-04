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

package net.frakbot.FWeather.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import net.frakbot.FWeather.R;
import net.frakbot.FWeather.updater.UpdaterService;
import net.frakbot.global.Const;

/**
 * Implementation of android.preference.DialogPreference that
 * handles the logout by asking the user first.
 *
 * @author Francesco Pontillo
 */
public class AuthorsDialog extends FragmentActivity {

    private static final String TAG = AuthorsDialog.class.getSimpleName();

    private static final String VERSION_UNAVAILABLE = "N/A";

    PackageManager pm;
    String packageName;
    String versionName;

    TextView nameAndVersionView;
    TextView aboutAuthorsView;
    Button donateFrakbotBtn;
    Button authenticWeatherBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // This sets the window size, while working around the IllegalStateException thrown by ActionBarView
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        // Get app version
        pm = getPackageManager();
        packageName = getPackageName();

        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
        }

        // Build the about body view and append the link to see OSS licenses
        setContentView(R.layout.dialog_about);

        nameAndVersionView = (TextView) findViewById(R.id.app_name_and_version);
        aboutAuthorsView = (TextView) findViewById(R.id.about_authors);

        donateFrakbotBtn = (Button) findViewById(R.id.btn_donate_frakbot);
        authenticWeatherBtn = (Button) findViewById(R.id.btn_authentic_weather);

        // Get the current widget locale (used to show the correct translator name)
        Locale locale = UpdaterService.getUserSelectedLocale(this);

        nameAndVersionView.setText(
                Html.fromHtml(getString(R.string.app_name_and_version, versionName)));
        if (locale != null) {
            aboutAuthorsView.setText(
                    Html.fromHtml(getString(R.string.about_developers,
                            getLocalizedString(this, R.string.translator_name, locale))));
        } else {
            aboutAuthorsView.setText(
                    Html.fromHtml(getString(R.string.about_developers,
                            getString(R.string.translator_name))));
        }

        // Setup the donation buttons
        donateFrakbotBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        Intent.ACTION_VIEW, Uri.parse(Const.Urls.DONATE_FRAKBOT)));
            }
        });

        authenticWeatherBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        Intent.ACTION_VIEW, Uri.parse(Const.Urls.AUTHENTIC_WEATHER)));
            }
        });
    }

    /**
     * Gets a string resource, localized in the desired language.
     *
     * @param context    The base Context
     * @param stringId   The ID of the resource string to retrieve
     * @param destLocale The locale to load the string for
     * @return Returns the string resource in the desired locale
     */
    private static CharSequence getLocalizedString(Context context, int stringId, Locale destLocale) {
        Locale defaultLocale = null;
        if (destLocale != null) {
            defaultLocale = UpdaterService.switchLocale(context, destLocale);
        }

        String locString = context.getString(stringId);
        UpdaterService.switchLocale(context, defaultLocale);

        return locString;
    }
}
