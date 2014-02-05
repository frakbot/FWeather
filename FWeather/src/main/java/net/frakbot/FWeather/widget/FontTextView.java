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

package net.frakbot.FWeather.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.util.FontHelper;

/**
 * A TextView that offers better support for custom fonts.
 * @author: Sebastiano Poggi
 */
public class FontTextView extends TextView {

    /**
     * Initializes an instance of the FontTextView.
     *
     * @param context The Context to initialize the FontTextView into.
     */
    public FontTextView(Context context) {
        super(context);
    }

    /**
     * Initializes an instance of the FontTextView.
     *
     * @param context The Context to initialize the FontTextView into.
     * @param attrs   The attributes set to initialize te FontTextView with.
     */
    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initializes an instance of the FontTextView.
     *
     * @param context  The Context to initialize the FontTextView into.
     * @param attrs    The attributes set to initialize te FontTextView with.
     * @param defStyle The base style resource to apply to this FontTextView.
     */
    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.FontTextView_customFont && !isInEditMode()) {
                setTypeface(a.getString(attr));
            }
        }
        a.recycle();
    }

    /**
     * Sets a typeface loading it from the assets.
     *
     * @param fontName The name of the font (the name of the TTF file in the /res/assets dir)
     */
    public void setTypeface(String fontName) {
        final Typeface font = FontHelper.getFont(getContext(), fontName);
        if (font != null) {
            setTypeface(font);
        }
    }
}
