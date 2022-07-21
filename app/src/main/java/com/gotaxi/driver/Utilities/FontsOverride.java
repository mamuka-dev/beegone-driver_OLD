package com.gotaxi.driver.Utilities;


import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;


public final class FontsOverride {

    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    protected static void replaceFont(String staticTypefaceFieldName,
                                      final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


}