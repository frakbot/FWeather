/*
 * Copyright 2013 Sebastiano Poggi and Francesco Pontillo
 * Copyright 2013 Google Inc.
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

package net.frakbot.FWeather.updater.weather.model;

import net.frakbot.util.log.FLog;

/**
 * A helper class representing weather data. Derived from Roman Nurik's
 * DashClock weather extension. Used for Yahoo! Weather data.
 */
public class WeatherData {

    private static final String TAG = WeatherData.class.getSimpleName();

    // Used for serialization/deserialization
    private static final int WEATHER_DATA_VERSION = 1;
    private static final int WEATHER_DATA_FIELDS_COUNT = 9;

    public static final int INVALID_TEMPERATURE = Integer.MIN_VALUE;
    public static final int INVALID_CONDITION = -1;
    public static final int WEATHER_ID_ERR_NO_LOCATION = -10000;
    public static final int WEATHER_ID_ERR_NO_NETWORK = -10001;

    public int temperature = INVALID_TEMPERATURE;
    public int low = INVALID_TEMPERATURE;
    public int high = INVALID_TEMPERATURE;
    public int conditionCode = INVALID_CONDITION;
    public int todayForecastConditionCode = INVALID_CONDITION;
    public String conditionText;
    public String forecastText;
    public String location;

    public WeatherData() {
    }

    @Override
    public String toString() {
        return String.format("WeatherData {%s - %s (%d) - %d (min %d, max %d) - forecast: %s (%d)}",
                             location, conditionText, conditionCode, temperature, low, high, forecastText,
                             todayForecastConditionCode);
    }

    /**
     * Creates a string representation of this instance that can be easily
     * deserialize to an object instance.
     *
     * @return Returns the string representation of this instance.
     */
    public String serializeToString() {
        return String.format("%d||%s||%s||%d||%d||%d||%d||%s||%d",
                             WEATHER_DATA_VERSION, location, conditionText, conditionCode, temperature, low, high,
                             forecastText, todayForecastConditionCode);
    }

    /**
     * Creates an instance of WeatherData deserializing the passed-along
     * weather data.
     *
     * @return Returns an instance deserialized from the String, or null if
     *         the data is not valid.
     */
    public static WeatherData deserializeFromString(String serialized) {
        // Sanity checks first
        if (serialized == null) {
            FLog.v(TAG, "Deserializing failed. Null string");
            return null;
        }

        String[] tokens = serialized.split("\\|\\|");
        if (tokens.length != WEATHER_DATA_FIELDS_COUNT) {
            FLog.v(TAG, String.format("Deserializing failed. Invalid string: \"%s\"\n" +
                                      "\t>Fields found: %d, expected: %d",
                                      serialized, tokens.length, WEATHER_DATA_FIELDS_COUNT));
            return null;
        }

        // Check the version of the data (MUST be ours!)
        int tmpInt = -1;
        try {
            tmpInt = Integer.parseInt(tokens[0]);
        } catch (Throwable ignored) { }

        if (tmpInt != WEATHER_DATA_VERSION) {
            FLog.v(TAG, String.format("Deserializing failed. Wrong data version: %d (ours is: %d)",
                                      tmpInt, WEATHER_DATA_VERSION));
            return null;
        }

        // Then move to the actual deserialization
        WeatherData wd = new WeatherData();
        wd.location = tokens[1];
        wd.conditionText = tokens[2];

        try {
            wd.conditionCode = Integer.parseInt(tokens[3]);
        } catch (Throwable ignored) { }
        try {
            wd.temperature = Integer.parseInt(tokens[4]);
        } catch (Throwable ignored) { }
        try {
            wd.low = Integer.parseInt(tokens[5]);
        } catch (Throwable ignored) { }
        try {
            wd.high = Integer.parseInt(tokens[6]);
        } catch (Throwable ignored) { }

        wd.forecastText = tokens[7];
        try {
            wd.todayForecastConditionCode = Integer.parseInt(tokens[8]);
        } catch (Throwable ignored) { }

        return wd;
    }
}