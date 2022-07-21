package com.gotaxi.driver.Utilities;


import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatEditText;

public class MyEditText extends AppCompatEditText {
    public MyEditText(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
//        Typeface customFont = FontCache.getTypeface("fonts/ClanPro-Book.otf", context);
//        setTypeface(customFont);
        setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
    }

}
