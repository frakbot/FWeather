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

package net.frakbot.global;

/**
 * Contains several global constants.
 * @author Sebastiano Poggi, Francesco Pontillo
 *
 */
public class Const {

    public static final String APP_NAME = "FWeather";

    /**
     * Holds a series of utility strings, mostly used as Bundle keys.
     * @author Francesco Pontillo
     */
    public class Bundles {
    }

    /**
     * Collection of Intents used in the app.
     * @author Francesco Pontillo
     */
    public class Intents {
        public static final String SYNC_RATE_PREFERENCE_CHANGED_ACTION = "net.frakbot.FWeather.SYNC_RATE_PREFERENCE_CHANGED_ACTION";
        public static final String ACTION_UPDATE_FRAKKING_WIDGET = "net.frakbot.FWeather.action.ACTION_UPDATE_FRAKKING_WIDGET";
        public static final String ACTION_DO_NOTHING_JON_SNOW = "net.frakbot.FWeather.action.ACTION_DO_NOTHING_JON_SNOW";
    }

    public class Frags {
    }

    public class Loaders {
    }

    public class Urls {
        public static final String DONATE_FRAKBOT = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=frakbot@gmail.com&lc=EU&currency_code=EUR&item_name=Italiansubs.net%20Android&amount=";
        public static final String AUTHENTIC_WEATHER = "http://www.behance.net/gallery/Authentic-Weather/7196565";
    }

    /**
     * Collection of preference keys, as listed in res/xml/*.xml files.
     * Always keep in sync these constants with the ones defined in those files.
     * Use R.string.KEY where you have a context.
     * @author Francesco Pontillo
     */
    public class Preferences {
        public static final String PREFERENCE = "preference";
        public static final String CHANGE = "change";
        public static final String SYNC_FREQUENCY = "sync_frequency";
        public static final String SYNC_FORCE = "sync_force";
        public static final String FEEDBACK = "feedback";
        public static final String ANALYTICS = "analytics";
        public static final String AUTHORS = "authors";
        public static final String CHANGELOG = "changelog";
        public static final String UI_TOGGLE_BUTTONS = "ui_toggle_buttons";
        public static final String UI_TOGGLE_WEATHER_ICON = "ui_toggle_weather_icon";
        public static final String UI_TOGGLE_TEMPERATURE_INFO = "ui_toggle_temperature_info";
        public static final String UI_DARKMODE = "ui_darkmode";
        public static final String UI_OVERRIDE_LANGUAGE = "ui_override_language";
        public static final String LOCATION_CACHE = "location_cache";
        public static final String LOCATION_CACHE_TIMESTAMP = "location_cache_timestamp";
        public static final String DEBUG = "debug";
    }

    /**
     * Collection of update rates, or thresholds,
     * expressed in seconds.
     * @author Francesco Pontillo
     */
    public class Thresholds {
        public static final int MAX_FETCH_WEATHER_ATTEMPTS = 3;
        public static final int MAX_FETCH_LOCATION_ATTEMPTS = 3;
    }

    public class Share {
        public static final String VIA = "#FWeather";
    }

}
