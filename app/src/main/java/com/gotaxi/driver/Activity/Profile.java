package com.gotaxi.driver.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.ConnectionHelper;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.Utilities;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.gotaxi.driver.DriverApplication.trimMessage;

public class Profile extends AppCompatActivity {
    private CircleImageView profile_img;
    private ImageView edit_profile, back;
    private TextView profile_username, profile_email, profile_phone, profile_vm, profile_vn, profile_sn, profile_sc;
    private Context context = Profile.this;
    private String TAG = "Profile: ";
    private ConnectionHelper helper;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getProfile();
        edit_profile = findViewById(R.id.edit_profile);
        back = findViewById(R.id.backArrow);
        profile_img = findViewById(R.id.profile_userimg);
        profile_username = findViewById(R.id.profile_username);
        profile_email = findViewById(R.id.profile_email);
        profile_phone = findViewById(R.id.profile_phone);
        profile_vm = findViewById(R.id.profile_vm);
        profile_vn = findViewById(R.id.profile_vn);
        profile_sn = findViewById(R.id.profile_sn);
        profile_sc = findViewById(R.id.profile_sc);
        Glide.with(this)
                .load(SharedHelper.getKey(this, "picture"))
                .placeholder(getResources().getDrawable(R.drawable.img_profile_placehodler))
                .into(profile_img);

        profile_username.setText(SharedHelper.getKey(this, "first_name") + " " + SharedHelper.getKey(this, "last_name"));
        profile_email.setText(SharedHelper.getKey(this, "email"));
        profile_phone.setText(SharedHelper.getKey(this, "mobile"));
        profile_sn.setText(SharedHelper.getKey(this, "service_name"));
        profile_sc.setText(SharedHelper.getKey(this, "service_type"));
        profile_vm.setText(SharedHelper.getKey(this, "service_model"));
        profile_vn.setText(SharedHelper.getKey(this, "service_number"));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, EditProfile.class));
            }
        });

    }

    public void getProfile() {

        JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("GetProfile", response.toString());
                SharedHelper.putKey(context, "id", response.optString("id"));
                SharedHelper.putKey(context, "first_name", response.optString("first_name"));
                SharedHelper.putKey(context, "last_name", response.optString("last_name"));
                SharedHelper.putKey(context, "email", response.optString("email"));
                SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("picture"));
                SharedHelper.putKey(context, "gender", response.optString("gender"));
                SharedHelper.putKey(context, "sos", response.optString("sos"));
                SharedHelper.putKey(context, "mobile", response.optString("mobile"));
                SharedHelper.putKey(context, "refer_code", response.optString("refer_code"));
                SharedHelper.putKey(context, "wallet_balance", response.optString("wallet_balance"));
                SharedHelper.putKey(context, "payment_mode", response.optString("payment_mode"));
                if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                    SharedHelper.putKey(context, "currency", response.optString("currency"));
                else
                    SharedHelper.putKey(context, "currency", "$");
                SharedHelper.putKey(context, "loggedIn", getString(R.string.True));
                if (response.optString("avatar").startsWith("http"))
                    SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar"));
                else
                    SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar"));

                if (response.optJSONObject("service") != null) {
                    try {
                        JSONObject service = response.optJSONObject("service");
                        if (service.optJSONObject("service_type") != null) {
                            JSONObject serviceType = service.optJSONObject("service_type");
                            SharedHelper.putKey(context, "service_name", serviceType.optString("name"));
                            SharedHelper.putKey(context, "service_type", serviceType.optString("type"));
                            SharedHelper.putKey(context, "service_number", service.optString("service_number"));
                            SharedHelper.putKey(context, "service_model", service.optString("service_model"));
                            profile_sn.setText(SharedHelper.getKey(Profile.this, "service_name"));
                            profile_sc.setText(SharedHelper.getKey(Profile.this, "service_type"));
                            profile_vm.setText(SharedHelper.getKey(Profile.this, "service_model"));
                            profile_vn.setText(SharedHelper.getKey(Profile.this, "service_number"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.getString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            /*refreshAccessToken();*/
                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (!json.equals("") && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

                        } else if (response.statusCode == 503) {
                            displayMessage(getString(R.string.server_down));
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }

                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                    }

                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getProfile();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                Log.e(TAG, "getHeaders: Token" + SharedHelper.getKey(context, "access_token") + SharedHelper.getKey(context, "token_type"));
                headers.put("Authorization", "" + "Bearer" + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    public void displayMessage(String toastString) {
        Utilities utilities = new Utilities();
        utilities.print("displayMessage", "" + toastString);
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).setTextColor(Color.WHITE).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }
}
