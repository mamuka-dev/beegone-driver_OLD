package com.gotaxi.driver.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.R;

public class custom_webview extends AppCompatActivity {
    private WebView webView;
    private CustomDialog customDialog;
    private ImageView back;
    private TextView custompagename;
    Intent mainintent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_webview);
        mainintent = getIntent();

        customDialog = new CustomDialog(this);
        customDialog.show();

        webView = findViewById(R.id.livechatwebview);
        back = findViewById(R.id.backArrow);
        custompagename = findViewById(R.id.custompagename);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);

        custompagename.setText(mainintent.getStringExtra("page_name"));
        webView.loadData(SharedHelper.getKey(this, mainintent.getStringExtra("page")), "text/html", "UTF-8");
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                if (customDialog.isShowing())
                    customDialog.dismiss();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        webView.clearHistory();
        finish();
    }
}
