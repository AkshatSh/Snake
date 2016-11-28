package com.a461.ellen.a461snake;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        super(context);
        System.out.println("initialized ctv with c");
    }

    public CustomTextView(Context context, AttributeSet attributes) {
        super(context, attributes);
        System.out.println("initialized ctv with ca");
        FontHelper.setCustomFont(this, context, attributes);
    }

    public CustomTextView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        System.out.println("initialized ctv with cas");
        FontHelper.setCustomFont(this, context, attributes);
    }
}

class FontHelper {

    // set font on textview based on custom font attribute
    public static void setCustomFont(TextView tv, Context context, AttributeSet attributes) {
        TypedArray a = context.obtainStyledAttributes(attributes, R.styleable.CustomFont);
        String font = a.getString(R.styleable.CustomFont_font);
        System.out.println("getting font: " + font);
        setCustomFont(tv, font, context);
        a.recycle();
    }

    // sets font on textview
    public static void setCustomFont(TextView tv, String font, Context context) {
        if (font == null) {
            System.out.println("font was null");
            return;
        }

        Typeface tf = FontCache.get(font, context);
        if (tf != null) {
            System.out.println("fetched typeface");
            tv.setTypeface(tf);
        }
    }
}
