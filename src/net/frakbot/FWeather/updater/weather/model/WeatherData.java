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

/**
 * A helper class representing weather data. Derived from Roman Nurik's
 * DashClock weather extension. Used for Yahoo! Weather data.
 */
public class WeatherData {
    public static final int INVALID_TEMPERATURE = Integer.MIN_VALUE;
    public static final int INVALID_CONDITION = -1;
    public static final int WEATHER_ID_ERR_NO_LOCATION = 10000;

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
        return "WeatherData {" + location + " - " + conditionText + " (" + conditionCode + ")" +
               " - " + temperature + " (min " + low + ", max " + high + ")" +
               " - forecast: " + forecastText + " (" + todayForecastConditionCode + ")}";
    }
}