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
    private int mainTextArrayId;
    private int mainTextPosition;
    private int secondaryTextArrayId;
    private int secondaryTextPosition;
    private int textColorId;
    private int wearTextColorId;
    private int imageId;
    private int wearImageId;
    private int mainLightColorId;
    private int mainDarkColorId;
    private int secondaryLightColorId;
    private int secondaryDarkColorId;

    public int getMainTextArrayId() {
        return mainTextArrayId;
    }

    public void setMainTextArrayId(int mainTextArrayId) {
        this.mainTextArrayId = mainTextArrayId;
    }

    public int getMainTextPosition() {
        return mainTextPosition;
    }

    public void setMainTextPosition(int mainTextPosition) {
        this.mainTextPosition = mainTextPosition;
    }

    public int getSecondaryTextArrayId() {
        return secondaryTextArrayId;
    }

    public void setSecondaryTextArrayId(int secondaryTextArrayId) {
        this.secondaryTextArrayId = secondaryTextArrayId;
    }

    public int getSecondaryTextPosition() {
        return secondaryTextPosition;
    }

    public void setSecondaryTextPosition(int secondaryTextPosition) {
        this.secondaryTextPosition = secondaryTextPosition;
    }

    public int getTextColorId() {
        return textColorId;
    }

    public void setTextColorId(int textColorId) {
        this.textColorId = textColorId;
    }

    public int getWearTextColorId() {
        return wearTextColorId;
    }

    public void setWearTextColorId(int wearTextColorId) {
        this.wearTextColorId = wearTextColorId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getWearImageId() {
        return wearImageId;
    }

    public void setWearImageId(int wearImageId) {
        this.wearImageId = wearImageId;
    }

    public int getMainLightColorId() {
        return mainLightColorId;
    }

    public void setMainLightColorId(int mainLightColorId) {
        this.mainLightColorId = mainLightColorId;
    }

    public int getMainDarkColorId() {
        return mainDarkColorId;
    }

    public void setMainDarkColorId(int mainDarkColorId) {
        this.mainDarkColorId = mainDarkColorId;
    }

    public int getSecondaryLightColorId() {
        return secondaryLightColorId;
    }

    public void setSecondaryLightColorId(int secondaryLightColorId) {
        this.secondaryLightColorId = secondaryLightColorId;
    }

    public int getSecondaryDarkColorId() {
        return secondaryDarkColorId;
    }

    public void setSecondaryDarkColorId(int secondaryDarkColorId) {
        this.secondaryDarkColorId = secondaryDarkColorId;
    }
}
