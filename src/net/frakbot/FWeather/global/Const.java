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

package net.frakbot.FWeather.global;

import net.frakbot.FWeather.BuildConfig;

/**
 * Contains several global constants.
 * @author Sebastiano Poggi, Francesco Pontillo
 *
 */
public class Const {

    public static final String APP_NAME = "ItaSA";

    public static final boolean VERBOSE = BuildConfig.DEBUG;
    public static final boolean DEBUG = VERBOSE /* TODO add a preference to toggle it */;
    public static final boolean INFO = VERBOSE /* TODO add a preference to toggle it */;
    
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
        public static final String SYNC_RATE_PREFERENCE_CHANGED_ACTION = "net.italiansubs.droitasa.SYNC_RATE_PREFERENCE_CHANGED_ACTION";
    }

    public class Frags {
    }

    public class Loaders {
    }

    public class Urls {
    }
    
    /**
     * Collection of preference keys, as listed in res/xml/*.xml files.
     * Always keep in sync these constants with the ones defined in those files.
     * Use R.string.KEY where you have a context.
     * @author Francesco Pontillo
     */
    public class Preferences {
        public static final String SYNC_FREQUENCY = "sync_frequency";
        public static final String FEEDBACK = "feedback";
        public static final String ANALYTICS = "analytics";
    }
    
    /**
     * Collection of update rates, or thresholds,
     * expressed in seconds.
     * @author Francesco Pontillo
     */
    public class Thresholds {
    }

}
