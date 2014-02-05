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

package net.frakbot.FWeather.util;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;

import java.util.HashMap;

/**
 * Helps retrieving and loading custom fonts from the app assets.
 *
 * @author Sebastiano Poggi
 */
public class FontHelper {

    private static final HashMap<String, Typeface> mFontsCache = new HashMap<String, Typeface>();

    /**
     * Gets an instance of the Roboto Light typeface (if available, or
     * a fallback on Lato Light on pre-Jelly Bean platforms).
     *
     * @return Returns the Typeface, or null if we're running on platforms
     *         earlier than Jelly Bean and ctx is null.
     */
    public static Typeface getRobotoLight(Context ctx) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            return Typeface.create("sans-serif-light", Typeface.NORMAL);
        }
        else {
            return getFont(ctx, "Roboto-Light.ttf");
        }
    }

    /**
     * Gets an instance of the Roboto Condensed typeface (if available, or
     * a fallback on the system default font on pre-Jelly Bean platforms).
     *
     * @return Returns the Typeface, or null if we're running on platforms
     *         earlier than Jelly Bean and ctx is null.
     */
    public static Typeface getRobotoCondensed(Context ctx) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            return Typeface.create("sans-serif-condensed", Typeface.NORMAL);
        }
        else {
            return getRobotoLight(ctx);
        }
    }

    /**
     * Gets an instance of the specified Typeface.
     *
     * @param ctx      The context to load the font with, if needed.
     * @param fontName The name of the font (the name of the TTF file in the /res/assets dir)
     *
     * @return Returns the Typeface, or null if it's not been loaded
     *         yet and ctx is null.
     */
    public static Typeface getFont(Context ctx, String fontName) {
        Typeface font;
        if (!isFontCached(fontName)) {
            if (ctx == null) {
                return null;   // We can't retrieve it...
            }

            font = Typeface.createFromAsset(ctx.getAssets(), fontName);
            if (font != null) {
                // Cache it if we've successfully loaded it
                mFontsCache.put(fontName, font);
            }
        }
        else {
            font = mFontsCache.get(fontName);
        }

        return font;
    }

    /**
     * Gets a value indicating if the specified font is already in the font cache.
     *
     * @param fontName The name of the font (the name of the TTF file in the /res/assets dir)
     *
     * @return Returns true if the font is in the cache, else false.
     */
    public static boolean isFontCached(String fontName) {
        for (String name : mFontsCache.keySet()) {
            if (name.equals(fontName)) {
                return true;
            }
        }
        return false;
    }
}
