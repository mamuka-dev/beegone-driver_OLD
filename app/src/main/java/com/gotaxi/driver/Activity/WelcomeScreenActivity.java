package com.gotaxi.driver.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gotaxi.driver.API.RetrofitClient;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Models.ConstData;
import com.gotaxi.driver.Models.ConstDataResponse;
import com.gotaxi.driver.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WelcomeScreenActivity extends AppCompatActivity {

    Button loginButton, signUpButton;
    LinearLayout social_layout;
    TextView tnc;
    CustomDialog customDialog;
    String[] key = new String[1];
    final String[][] value = new String[1][1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }


        setContentView(R.layout.activity_welcome_screen);
        loginButton = findViewById(R.id.sign_in_btn);
        signUpButton = findViewById(R.id.sign_up_btn);
        tnc = findViewById(R.id.tnc);

        // layouts of all welcome sliders
        // add few more layouts if you want

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeScreenActivity.this, LoginOtpScreen.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
//                finish();
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeScreenActivity.this, RegisterActivity.class).putExtra("signup", true).putExtra("viewpager", "yes").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);

            }
        });


        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeScreenActivity.this);
                builder.setMessage(R.string.msg_loc_data);
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }, 2000);
        overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);

        changeStatusBarColor();
        getconstdata();
        customTextView(tnc);
    }

    private void customTextView(TextView view) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                "By continuing, you agree that you have read and accept our ");
        spanTxt.append("Term of services");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(WelcomeScreenActivity.this, custom_webview.class);
                intent.putExtra("page", "terms_page");
                intent.putExtra("page_name", "Terms & Conditions");
                startActivity(intent);
            }
        }, spanTxt.length() - "Term of services".length(), spanTxt.length(), 0);
        spanTxt.append(" and");
        spanTxt.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_span_text)), 59, 75, 0);
        spanTxt.append(" Privacy Policy");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(WelcomeScreenActivity.this, custom_webview.class);
                intent.putExtra("page", "privacy_page");
                intent.putExtra("page_name", "Privacy Policy");
                startActivity(intent);
            }
        }, spanTxt.length() - " Privacy Policy".length(), spanTxt.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_span_text)), 80, spanTxt.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    public void getconstdata() {
        customDialog = new CustomDialog(this);
        customDialog.show();
        Call<ConstDataResponse> call = RetrofitClient.getInstance().getApi().getconstdata();
        call.enqueue(new Callback<ConstDataResponse>() {
            @Override
            public void onResponse(@NotNull Call<ConstDataResponse> call, @NotNull Response<ConstDataResponse> response) {
                if (response.isSuccessful()) {
                    List<ConstData> constData;
                    ConstDataResponse constDataResponse = response.body();
                    constData = constDataResponse.getData();

                    key = new String[constData.size()];
                    value[0] = new String[constData.size()];

                    for (int i = 0; i < constData.size(); i++) {
                        key[i] = constData.get(i).getKey();
                        value[0][i] = constData.get(i).getValue();
                        if (key[i].equals("page_privacy"))
                            SharedHelper.putKey(WelcomeScreenActivity.this, "privacy_page", value[0][i]);
                        if (key[i].equals("page_terms"))
                            SharedHelper.putKey(WelcomeScreenActivity.this, "terms_page", value[0][i]);
                        Log.v("Terms", SharedHelper.getKey(WelcomeScreenActivity.this, "terms_page"));

                    }
                    if (customDialog!=null && customDialog.isShowing())
                        customDialog.dismiss();
                } else {
                    if (customDialog.isShowing())
                        customDialog.dismiss();
                    // Toast.makeText(WelcomeScreenActivity.this, "Some Error Occurred ! Please Try Again Later", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NotNull Call<ConstDataResponse> call, @NotNull Throwable t) {
                customDialog.dismiss();
                // Toast.makeText(WelcomeScreenActivity.this, "Some Error Occurred ! Please Try Again Later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }


}

