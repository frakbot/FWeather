/*
 * Copyright 2013 Sebastiano Poggi and Francesco Pontillo
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

package net.frakbot.util.log;

/** A set of logging levels used by {@link net.frakbot.util.log.FLog} */
public enum LogLevel {
    /**
     * No logging at all.
     */
    SUPPRESS (1),
    /**
     * VERBOSE log. Matches {@link android.util.Log#VERBOSE}.
     */
    VERBOSE (2),
    /**
     * DEBUG log. Matches {@link android.util.Log#DEBUG}.
     */
    DEBUG (3),
    /**
     * INFO log. Matches {@link android.util.Log#WARN}.
     */
    INFO (4),
    /**
     * WARN log. Matches {@link android.util.Log#WARN}.
     */
    WARN (5),
    /**
     * ERROR log. Matches {@link android.util.Log#ERROR}.
     */
    ERROR (6),
    /**
     * ASSERT log. Matches {@link android.util.Log#ASSERT}.
     */
    ASSERT (7);

    private final int mIndex;

    LogLevel(int intValue) {
        if (intValue < 1 || intValue > 7) {
            throw new IllegalArgumentException("Invalid intValue. Must be in the [1, 7] range.");
        }

        mIndex = intValue;
    }

    public int toInt() {
        return mIndex;
    }

    @Override
    public String toString() {
        switch (this) {
            case SUPPRESS:
                return "SUPPRESS";
            case VERBOSE:
                return "VERBOSE";
            case DEBUG:
                return "DEBUG";
            case INFO:
                return "INFO";
            case WARN:
                return "WARN";
            case ERROR:
                return "ERROR";
            case ASSERT:
                return "ASSERT";
        }

        return super.toString();
    }
}
