package com.gotaxi.driver.Activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.ConnectionHelper;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.Utilities;
import com.gotaxi.driver._base.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends BaseActivity {
    String TAG = "ChangePasswordActivity";
    public Context context = ChangePasswordActivity.this;
    public Activity activity = ChangePasswordActivity.this;
    CustomDialog customDialog;
    ConnectionHelper helper;
    Boolean isInternet;
    Button changePasswordBtn;
    ImageView backArrow;
    EditText current_password, new_password, confirm_new_password;
    Utilities utils = new Utilities();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        findViewByIdandInitialization();

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String current_password_value = current_password.getText().toString();
                String new_password_value = new_password.getText().toString();
                String confirm_password_value = confirm_new_password.getText().toString();
                if (current_password_value == null || current_password_value.equalsIgnoreCase("")) {
                    displayMessage(getString(R.string.please_enter_current_pass));
                } else if (new_password_value == null || new_password_value.equalsIgnoreCase("")) {
                    displayMessage(getString(R.string.please_enter_new_pass));
                } else if (confirm_password_value == null || confirm_password_value.equalsIgnoreCase("")) {
                    displayMessage(getString(R.string.please_enter_confirm_pass));
                } else if (new_password_value.length() < 6 || new_password_value.length() > 16) {
                    displayMessage(getString(R.string.password_validation1));
                } else if (current_password_value.equalsIgnoreCase(new_password_value)) {
                    displayMessage(getString(R.string.new_password_validation));
                } else if (!new_password_value.equals(confirm_password_value)) {
                    displayMessage(getString(R.string.different_passwords));
                } else {
                    if (helper.isConnectingToInternet()) {
                        changePassword();
                    } else {
                        displayMessage(getString(R.string.something_went_wrong_net));
                    }
                }
            }
        });

    }

    public void findViewByIdandInitialization() {
        current_password = findViewById(R.id.current_password);
        new_password = findViewById(R.id.new_password);
        confirm_new_password = findViewById(R.id.confirm_password);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        backArrow = findViewById(R.id.backArrow);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
    }


    private void changePassword() {

        customDialog = new CustomDialog(context);
        customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("password", new_password.getText().toString());
            object.put("password_confirmation", confirm_new_password.getText().toString());
            object.put("password_old", current_password.getText().toString());
            utils.print("ChangePasswordAPI", "" + object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CHANGE_PASSWORD_API, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                customDialog.dismiss();
                utils.print("SignInResponse", response.toString());
                displayMessage(response.optString("message"));
                GoToBeginActivity();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                utils.print("MyTest", "" + error);
                utils.print("MyTestError", "" + error.networkResponse);
                utils.print("MyTestError1", "" + response.statusCode);
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));
                        utils.print("ErrorChangePasswordAPI", "" + errorObj.toString());

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("error"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {
                            json = DriverApplication.trimMessage(new String(response.data));
                            if (!json.equals("") && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
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
                        changePassword();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
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
