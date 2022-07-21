package com.gotaxi.driver.Activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.R;

public class chat_support extends AppCompatActivity {
    private WebView webView;
    private CustomDialog customDialog;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_support);
        customDialog = new CustomDialog(this);
        customDialog.show();

        webView = findViewById(R.id.livechatwebview);
        back = findViewById(R.id.backArrow);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);


        webView.loadUrl("https://tawk.to/chat/" + SharedHelper.getKey(this, "tawk_live") + "/default");
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
