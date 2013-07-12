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

import android.content.Context;
import android.location.Location;
import net.frakbot.FWeather.FWeatherApplication;
import net.frakbot.FWeather.util.FLog;

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

    private final static String CITY_BASE_URL = "http://api.openweathermap.org/data/2.5/" +
                                                "weather?q=%1$s&mode=json&lang=en&units=metric&APPID=" +
                                                FWeatherApplication.getApiKey();
    private final static String LATLON_BASE_URL = "http://api.openweathermap.org/data/2.5/" +
                                                  "weather?lat=%1$f&lon=%2$f&mode=json&lang=en&units=metric&APPID=" +
                                                  FWeatherApplication.getApiKey();
    private final static String IMG_URL = "http://openweathermap.org/img/w/";

    private Context mContext;

    public WeatherHttpClient(Context context) {
        mContext = context;
    }

    /**
     * Gets the JSON weather data for the specified city.
     *
     * @param cityName The name of the city to get the weather of
     *
     * @return Returns the weather JSON data, or null if there was any
     *         error
     */
    public String getCityWeatherJsonData(String cityName) throws IOException {
        HttpURLConnection con = null;
        InputStream is = null;

        try {
            FLog.i(mContext, "FWeatherHttp", "City weather update started: " + cityName);
            con = openConnection(String.format(CITY_BASE_URL, cityName));

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

            FLog.d(mContext, "FWeatherHttp", "City weather update done. Length: " + buffer.length());

            return buffer.toString();
        }
        catch (IOException e) {
            FLog.e(mContext, "FWeatherHttp", "Error while fetching the weather", e);
            throw e;
        }
        finally {
            cleanup(con, is);
        }
    }

    /**
     * Gets the JSON weather data for the specified location.
     *
     * @param location The location to get the weather of
     *
     * @return Returns the weather JSON data, or null if there was any
     *         error
     */
    public String getLocationWeatherJsonData(Location location) throws IOException {
        HttpURLConnection con = null;
        InputStream is = null;

        try {
            FLog.i(mContext, "FWeatherHttp", "Loaction weather update started: " + location.toString());
            // We specify the locale to avoid passing latlong data with the wrong decimal separators
            con = openConnection(String.format(Locale.US, LATLON_BASE_URL, location.getLatitude(),
                                               location.getLongitude()));

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

            FLog.d(mContext, "FWeatherHttp", "Location weather update done. Length: " + buffer.length());

            return buffer.toString();
        } catch (IOException e) {
            FLog.e(mContext, "FWeatherHttp", "Error while fetching the weather", e);
            throw e;
        } finally {
            cleanup(con, is);
        }
    }

    /**
     * Tries to close the connection resources.
     *
     * @param con The HTTP connection to close
     * @param is  The InputStream to close
     */
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

    /**
     * Opens a connection to the specified URL and returns it.
     *
     * @param url The URL to connect to
     *
     * @return Returns the connection
     * @throws IOException Thrown if an error occurs while connecting to the URL
     */
    private HttpURLConnection openConnection(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
        con.setRequestMethod("GET");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.connect();
        return con;
    }

    /**
     * Gets the weather image data for the specified weather code.
     *
     * @param code The code to get the image of
     *
     * @return Returns the weather image data, or null if there was any
     *         error
     */
    public byte[] getImage(String code) {
        HttpURLConnection con = null;
        InputStream is = null;

        try {
            FLog.i(mContext, "FWeatherHttp", "Image DL started. Code: " + code);

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

            FLog.i(mContext, "FWeatherHttp", "Image DL done. Size: " + baos.size());
            return baos.toByteArray();
        }
        catch (Throwable t) {
            FLog.e(mContext, "FWeatherHttp", "Error while fetching the weather image", t);
        }
        finally {
            cleanup(con, is);
        }

        return null;
    }
}