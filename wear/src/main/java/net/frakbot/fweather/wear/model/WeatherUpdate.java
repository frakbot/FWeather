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
package net.frakbot.fweather.wear.model;

public class WeatherUpdate {

    private CharSequence primary;
    private CharSequence secondary;
    private String image;
    private int accentColor;

    public WeatherUpdate(CharSequence primary, CharSequence secondary, String image, int accentColor) {
        this.primary = primary;
        this.secondary = secondary;
        this.image = image;
        this.accentColor = accentColor;
    }

    public CharSequence getPrimary() {
        return primary;
    }

    public CharSequence getSecondary() {
        return secondary;
    }

    public String getImage() {
        return image;
    }

    public int getAccentColor() {
        return accentColor;
    }

}
