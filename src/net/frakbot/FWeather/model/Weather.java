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
package net.frakbot.FWeather.model;

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
public class Weather {

    public Location mLocation;
    public CurrentCondition mCurrentCondition = new CurrentCondition();
    public Temperature mTemperature = new Temperature();
    public Wind mWind = new Wind();
    public Rain mRain = new Rain();
    public Snow mSnow = new Snow();
    public Clouds mClouds = new Clouds();

    public byte[] mIconData;

    public class CurrentCondition {
        private int mWeatherId;
        private String mCondition;
        private String mDescr;
        private String mIcon;

        private float mPressure;
        private float mHumidity;

        public int getWeatherId() {
            return mWeatherId;
        }

        public void setWeatherId(int weatherId) {
            mWeatherId = weatherId;
        }

        public String getCondition() {
            return mCondition;
        }

        public void setCondition(String condition) {
            mCondition = condition;
        }

        public String getDescr() {
            return mDescr;
        }

        public void setDescr(String descr) {
            mDescr = descr;
        }

        public String getIcon() {
            return mIcon;
        }

        public void setIcon(String icon) {
            mIcon = icon;
        }

        public float getPressure() {
            return mPressure;
        }

        public void setPressure(float pressure) {
            mPressure = pressure;
        }

        public float getHumidity() {
            return mHumidity;
        }

        public void setHumidity(float humidity) {
            mHumidity = humidity;
        }

        @Override
        public String toString() {
            return "Conditions {ID: " + mWeatherId + ", name: " + mCondition + ", descr: " + mDescr +
                   ", icon: " + mIcon + ", press: " + mPressure + ", hum: " + mHumidity + "}";
        }
    }

    public class Temperature {
        private float mTemp;
        private float mMinTemp;
        private float mMaxTemp;

        public float getTemp() {
            return mTemp;
        }

        public void setTemp(float temp) {
            mTemp = temp;
        }

        public float getMinTemp() {
            return mMinTemp;
        }

        public void setMinTemp(float minTemp) {
            mMinTemp = minTemp;
        }

        public float getMaxTemp() {
            return mMaxTemp;
        }

        public void setMaxTemp(float maxTemp) {
            mMaxTemp = maxTemp;
        }

        @Override
        public String toString() {
            return "Temperature {curr: " + mTemp + ", min: " + mMinTemp + ", max: " + mMaxTemp + "}";
        }
    }

    public class Wind {
        private float mSpeed;
        private float mDeg;

        public float getSpeed() {
            return mSpeed;
        }

        public void setSpeed(float speed) {
            mSpeed = speed;
        }

        public float getDeg() {
            return mDeg;
        }

        public void setDeg(float deg) {
            mDeg = deg;
        }

        @Override
        public String toString() {
            return "Wind {speed: " + mSpeed + ", deg: " + mDeg + "}";
        }
    }

    public class Rain {
        private String mTime;
        private float mAmount;

        public String getTime() {
            return mTime;
        }

        public void setTime(String time) {
            mTime = time;
        }

        public float getAmount() {
            return mAmount;
        }

        public void setAmount(float amount) {
            mAmount = amount;
        }

        @Override
        public String toString() {
            return "Rain {time: " + mTime + ", amount: " + mAmount + "}";
        }
    }

    public class Snow {
        private String mTime;
        private float mAmount;

        public String getTime() {
            return mTime;
        }

        public void setTime(String time) {
            mTime = time;
        }

        public float getAmount() {
            return mAmount;
        }

        public void setAmount(float amount) {
            mAmount = amount;
        }

        @Override
        public String toString() {
            return "Snow {time: " + mTime + ", amount: " + mAmount + "}";
        }
    }

    public class Clouds {
        private int mPerc;

        public int getPerc() {
            return mPerc;
        }

        public void setPerc(int perc) {
            mPerc = perc;
        }

        @Override
        public String toString() {
            return "Clouds {" + mPerc + "%}";
        }
    }

    @Override
    public String toString() {
        return "Weather {" + mLocation.toString() + " - " + mCurrentCondition.toString() +
               " - " + mTemperature.toString() + " - " + mRain.toString() +
               " - " + mSnow.toString() + " - " + mClouds.toString() +
               " - " + mWind.toString() + "}";
    }
}