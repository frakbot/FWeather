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
package net.frakbot.FWeather;

import android.location.Location;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

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
public class WeatherHttpClient {

    private static final String API_KEY = "2ab566b32d4464be0517751370e18fed";
    private final static String CITY_BASE_URL = "http://api.openweathermap.org/data/2.5/" +
                                                "weather?q=%1$s&mode=json&lang=en&units=metric&APPID=" +
                                                API_KEY;
    private final static String LATLON_BASE_URL = "http://api.openweathermap.org/data/2.5/" +
                                                  "weather?lat=%1$f&lon=%2$f&mode=json&lang=en&units=metric&APPID=" +
                                                  API_KEY;
    private final static String IMG_URL = "http://openweathermap.org/img/w/";

    /**
     * Gets the JSON weather data for the specified city.
     *
     * @param cityName The name of the city to get the weather of.
     *
     * @return Returns the weather JSON data, or null if there was any
     *         error.
     */
    public String getCityWeatherJsonData(String cityName) {
        HttpURLConnection con = null;
        InputStream is = null;

        try {
            Log.i("FWeatherHttp", "City weather update started: " + cityName);
            con = openConnection(String.format(CITY_BASE_URL, cityName), con);

            // Let's read the response
            StringBuilder buffer = new StringBuilder();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\r\n");
            }

            is.close();
            con.disconnect();

            Log.d("FWeatherHttp", "City weather update done. Length: " + buffer.length());

            return buffer.toString();
        }
        catch (Throwable t) {
            Log.e("FWeatherHttp", "Error while fetching the weather", t);
        }
        finally {
            cleanup(con, is);
        }

        return null;
    }

    /**
     * Gets the JSON weather data for the specified location.
     *
     * @param location The location to get the weather of.
     *
     * @return Returns the weather JSON data, or null if there was any
     *         error.
     */
    public String getLocationWeatherJsonData(Location location) {
        HttpURLConnection con = null;
        InputStream is = null;

        try {
            Log.i("FWeatherHttp", "Loaction weather update started: " + location.toString());
            // We specify the locale to avoid passing latlong data with the wrong decimal separators
            con = openConnection(String.format(Locale.US, LATLON_BASE_URL, location.getLatitude(),
                                               location.getLongitude()), con);

            // Let's read the response
            StringBuilder buffer = new StringBuilder();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\r\n");
            }

            is.close();
            con.disconnect();

            Log.d("FWeatherHttp", "Location weather update done. Length: " + buffer.length());

            return buffer.toString();
        }
        catch (Throwable t) {
            Log.e("FWeatherHttp", "Error while fetching the weather", t);
        }
        finally {
            cleanup(con, is);
        }

        return null;
    }

    private void cleanup(HttpURLConnection con, InputStream is) {
        try {
            is.close();
        }
        catch (Throwable ignored) {
        }
        try {
            con.disconnect();
        }
        catch (Throwable ignored) {
        }
    }

    private HttpURLConnection openConnection(String url, HttpURLConnection con) throws IOException {
        con = (HttpURLConnection) (new URL(url)).openConnection();
        con.setRequestMethod("GET");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.connect();
        return con;
    }

    public byte[] getImage(String code) {
        HttpURLConnection con = null;
        InputStream is = null;

        try {
            Log.i("FWeatherHttp", "Image DL started. Code: " + code);

            con = (HttpURLConnection) (new URL(IMG_URL + code)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            is = con.getInputStream();
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while (is.read(buffer) != -1) {
                baos.write(buffer);
            }

            Log.i("FWeatherHttp", "Image DL done. Size: " + baos.size());
            return baos.toByteArray();
        }
        catch (Throwable t) {
            Log.e("FWeatherHttp", "Error while fetching the weather image", t);
        }
        finally {
            cleanup(con, is);
        }

        return null;
    }
}