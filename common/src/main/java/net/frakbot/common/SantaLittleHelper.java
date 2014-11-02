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

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

/**
 * A little helper.
 */
public class SantaLittleHelper {

    private static final String PLACEHOLDER_COLOR = "%%COLOR%%";

    /**
     * Returns the specified spanned string with the correct highlighting color.
     *
     * @param context      The (guess what) caller {@link android.content.Context}
     * @param stringId     The Resource ID of the string
     * @param lightColorId The Resource ID of the highlight color in normal (light) mode
     * @param darkColorId  The Resource ID of the highlight color in dark mode
     * @param darkMode     True if the widget is in dark mode, false otherwise
     * @return Returns the spanned, colored string
     */
    public static Spanned getColoredSpannedString(Context context, int stringId, int lightColorId, int darkColorId, boolean darkMode) {
        int color = context.getResources().getColor(!darkMode ? lightColorId : darkColorId);
        String string = context.getString(stringId)
                .replace(PLACEHOLDER_COLOR, String.format("#%06X", (0xFFFFFF & color)));
        return Html.fromHtml(string);
    }

    public static int getStringIdInArray(Context context, int arrayId, int position) {
        String varName = context.getResources().getStringArray(arrayId)[position];
        return context.getResources().getIdentifier(varName, "string", context.getPackageName());
    }

    public static Spanned getSpannedInArray(Context context, int arrayId, int position, boolean darkMode, int lightColorId, int darkColorId) {
        int lol = getStringIdInArray(context, arrayId, position);
        return SantaLittleHelper.getColoredSpannedString(context, lol, lightColorId, darkColorId, darkMode);
    }
}
