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
package net.frakbot.common;

/**
 * Contains weather resource IDs for the widget instances and the connected Wear devices.
 */
public class WeatherResources {
    private int mainTextId;
    private int secondaryTextId;
    private int textColorId;
    private int imageId;

    public int getMainTextId() {
        return mainTextId;
    }

    public void setMainTextId(int mainTextId) {
        this.mainTextId = mainTextId;
    }

    public int getSecondaryTextId() {
        return secondaryTextId;
    }

    public void setSecondaryTextId(int secondaryTextId) {
        this.secondaryTextId = secondaryTextId;
    }

    public int getTextColorId() {
        return textColorId;
    }

    public void setTextColorId(int textColorId) {
        this.textColorId = textColorId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
