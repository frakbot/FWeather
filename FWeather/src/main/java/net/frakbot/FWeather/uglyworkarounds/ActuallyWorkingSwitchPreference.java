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
package net.frakbot.FWeather.uglyworkarounds;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

/**
 * Hideous workaround for an issue where SwitchPreferences are going nuts in when scrolling
 * lists because of broken recycling logic. See AOSP issue 26194.
 */
public class ActuallyWorkingSwitchPreference extends SwitchPreference {

    public ActuallyWorkingSwitchPreference(Context context) {
        this(context, null);
    }

    public ActuallyWorkingSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.switchPreferenceStyle);
    }

    public ActuallyWorkingSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}