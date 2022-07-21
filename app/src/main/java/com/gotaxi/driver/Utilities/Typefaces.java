package com.gotaxi.driver.Utilities;


import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;


public class Typefaces {


    private static final HashMap<String, Typeface> cache = new HashMap<>();


    public static Typeface get(Context c, String name) {

        synchronized (cache) {

            if (!cache.containsKey(name)) {

                Typeface t = Typeface.createFromAsset(c.getResources().getAssets(), String.format("%s", name));

                cache.put(name, t);

            }

            return cache.get(name);

        }


    }

}
