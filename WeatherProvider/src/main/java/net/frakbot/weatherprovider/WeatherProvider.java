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
package net.frakbot.weatherprovider;


import java.util.List;

public abstract class WeatherProvider {

    private ApiKey apiKey;
    protected boolean useImperialUnits;

    protected WeatherProvider(ApiKey apiKey, boolean useImperialUnits) {
        this.apiKey = apiKey;
        this.useImperialUnits = useImperialUnits;
    }

    protected ApiKey getApiKey() {
        return apiKey;
    }

    public abstract LocationId findLocationId(String location);

    public abstract List<LocationId> getLocationIdSuggestestions(String query);

    public abstract Weather getWeatherForLocationId(LocationId locationId);

    public abstract class Builder {

        protected ApiKey apiKey;
        protected boolean useImperialUnits;

        public Builder withApiKey(ApiKey apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder usingImperialUnits(boolean useImperialUnits) {
            this.useImperialUnits = useImperialUnits;
            return this;
        }

        public abstract WeatherProvider build();

    }

}
