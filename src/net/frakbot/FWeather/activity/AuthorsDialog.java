/*
 * Copyright 2013 Sebastiano Poggi and Francesco Pontillo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.frakbot.FWeather.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.global.Const;

/**
 * Implementation of android.preference.DialogPreference that
 * handles the logout by asking the user first.
 * @author Francesco Pontillo
 */
public class AuthorsDialog extends SherlockFragmentActivity {

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

        // Get app version
        pm = getPackageManager();
        packageName = getPackageName();

        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
        }

        // Build the about body view and append the link to see OSS licenses
        setContentView(R.layout.dialog_about);

        nameAndVersionView = (TextView) findViewById(R.id.app_name_and_version);
        aboutAuthorsView = (TextView) findViewById(R.id.about_authors);

        donateFrakbotBtn = (Button) findViewById(R.id.btn_donate_frakbot);
        authenticWeatherBtn = (Button) findViewById(R.id.btn_authentic_weather);

        nameAndVersionView.setText(Html.fromHtml(
            getString(R.string.app_name_and_version, versionName)));
        aboutAuthorsView.setText(Html.fromHtml(
            getString(R.string.about_developers)));

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
}
