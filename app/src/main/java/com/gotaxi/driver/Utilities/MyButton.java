package com.gotaxi.driver.Utilities;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;


@SuppressLint("AppCompatCustomView")
public class MyButton extends Button {
    public MyButton(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
//        Typeface customFont = FontCache.getTypeface("fonts/ClanPro-NarrNews.otf", context);
//        setTypeface(customFont);
        setTextSize(TypedValue.COMPLEX_UNIT_SP,13);

    }
}
