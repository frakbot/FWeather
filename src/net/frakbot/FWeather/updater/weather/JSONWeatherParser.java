/**
 * This is a tutorial source code 
 * provided "as is" and without warranties.
 *
 * For any question please visit the web site
 * http://www.survivingwithandroid.com
 *
 * or write an email to
 * survivingwithandroid@gmail.com
 *
 */
package net.frakbot.FWeather.updater.weather;

import net.frakbot.FWeather.updater.weather.model.Location;
import net.frakbot.FWeather.updater.weather.model.Weather;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/*
 * Copyright (C) 2013 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class JSONWeatherParser {

    public static Weather getWeather(String data) throws JSONException {
        Weather weather = new Weather();

        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        // We start extracting the info
        Location loc = new Location();

        JSONObject coordObj = jObj.getJSONObject("coord");
        loc.setLatitude((float) coordObj.getDouble("lat"));
        loc.setLongitude((float) coordObj.getDouble("lon"));

        JSONObject sysObj = jObj.getJSONObject("sys");
        loc.setCountry(sysObj.getString("country"));
        loc.setSunrise(sysObj.getInt("sunrise"));
        loc.setSunset(sysObj.getInt("sunset"));
        loc.setCity(jObj.getString("name"));
        weather.mLocation = loc;

        // We get weather info (This is an array)
        JSONArray jArr = jObj.getJSONArray("weather");

        // We use only the first value
        JSONObject JSONWeather = jArr.getJSONObject(0);
        weather.mCurrentCondition.setWeatherId(JSONWeather.getInt("id"));
        weather.mCurrentCondition.setDescr(JSONWeather.getString("description"));
        weather.mCurrentCondition.setCondition(JSONWeather.getString("main"));
        weather.mCurrentCondition.setIcon(JSONWeather.getString("icon"));

        JSONObject mainObj = jObj.getJSONObject("main");
        weather.mCurrentCondition.setHumidity(mainObj.getInt("humidity"));
        weather.mCurrentCondition.setPressure(mainObj.getInt("pressure"));
        weather.mTemperature.setMaxTemp((float) mainObj.getDouble("temp_max"));
        weather.mTemperature.setMinTemp((float) mainObj.getDouble("temp_min"));
        weather.mTemperature.setTemp((float) mainObj.getDouble("temp"));

        // Wind
        JSONObject wObj = jObj.getJSONObject("wind");
        weather.mWind.setSpeed((float) wObj.getDouble("speed"));
        weather.mWind.setDeg((float) wObj.getDouble("deg"));

        // Clouds
        JSONObject cObj = jObj.getJSONObject("clouds");
        weather.mClouds.setPerc(cObj.getInt("all"));

        return weather;
    }
}