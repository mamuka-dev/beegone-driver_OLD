package com.gotaxi.driver.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;
import com.gotaxi.driver.API.RetrofitClient;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.ConnectionHelper;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.Models.UserResponse;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.Utilities;
import com.gotaxi.driver._base.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class LoginOtpScreen extends BaseActivity {
    String emilto;
    JSONObject json;
    ConnectionHelper helper;
    Boolean isInternet;
    CustomDialog customDialog;
    public Context context = LoginOtpScreen.this;
    String TAG = "BEGINSCREEN";
    String device_token, device_UDID;
    Utilities utils = new Utilities();

    ProgressDialog mProg;
    FirebaseAuth mAuth;
    String phone;
    //String
    String verificationId;
    //EditText
    EditText Phonenum;
    //Button
    Button verifyCodeButton;
    //TextView
    TextView changePasswordTxt;
    //Layout
    CardView phone_layout;
    //Pinview
    EditText verifyCodeET;
     CountryCodePicker cpp;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_otp_screen_activity);
        customDialog = new CustomDialog(this);
        mProg = new ProgressDialog(LoginOtpScreen.this);
        mAuth = FirebaseAuth.getInstance();
        verifyCodeButton = findViewById(R.id.submit);
        verifyCodeET = findViewById(R.id.pinView);
        Phonenum = findViewById(R.id.idphone);
        phone_layout = findViewById(R.id.phone_layout);
        changePasswordTxt = findViewById(R.id.changePasswordTxt);
        back = findViewById(R.id.backArrow);
        helper = new ConnectionHelper(LoginOtpScreen.this);
        isInternet = helper.isConnectingToInternet();
         cpp = findViewById(R.id.mcpp);
        GetToken();
        changePasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginOtpScreen.this, ResetPasswordActivity.class));
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Phonenum.getText().toString().trim().equals("")) {
                    if (!verifyCodeET.getText().toString().trim().equals("")) {
                        customDialog.show();
                        phone = Phonenum.getText().toString().trim();
                        Call<UserResponse> call = RetrofitClient.getInstance().getApi().userlogin(cpp.getSelectedCountryCodeWithPlus() + Phonenum.getText().toString());
                        call.enqueue(new Callback<UserResponse>() {
                            @Override
                            public void onResponse(Call<UserResponse> call, retrofit2.Response<UserResponse> response) {
                                if (response.isSuccessful()) {
                                    if (!response.body().isError()) {
                                        UserResponse userResponse = response.body();
                                        emilto = userResponse.getUser().getEmail();
                                        hideKeyboard(v);
                                        signIn(emilto);

                                    } else {
                                        customDialog.dismiss();
                                        Toast.makeText(LoginOtpScreen.this, "Please Register: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    customDialog.dismiss();
                                    Toast.makeText(LoginOtpScreen.this, "Please Register: driver Not Registered", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onFailure(Call<UserResponse> call, Throwable t) {
                                customDialog.dismiss();
                            }
                        });
                    } else {
                        verifyCodeET.setError(getResources().getString(R.string.please_enter_password));
                    }
                } else {
                    Phonenum.setError(getResources().getString(R.string.please_enter_your_phone));
                }
            }
        });
        Phonenum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                hideKeyboard(view);
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                utils.print(TAG, "GCM Registration Token: " + device_token);
            } else {
//                device_token = "" + FirebaseInstanceId.getInstance().getToken();
//                SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
//                utils.print(TAG, "Failed to complete token refresh: " + device_token);
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isComplete()) {
// TODO: 23/01/2022 check it's getting token or not
                            device_token = "" + task.getResult();
                            SharedHelper.putKey(context, "device_token", "" + task.getResult());
                            utils.print(TAG, "Failed to complete token refresh: " + device_token);
                        }
                    }
                });
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            utils.print(TAG, "Failed to complete token refresh");
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            utils.print(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            utils.print(TAG, "Failed to complete device UDID");
        }
    }

    private void signIn(String emilto) {
        if (isInternet) {
            JSONObject object = new JSONObject();
            try {
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                object.put("email", emilto);
                object.put("password", verifyCodeET.getText().toString());
                utils.print("InputToLoginAPI", "" + object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //  customDialog.dismiss();
                    utils.print("SignUpResponse", response.toString());
                    SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                    SharedHelper.putKey(context, "token_type", response.optString("token_type"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    getProfile();
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

                    if (response != null && response.data != null) {
                        utils.print("MyTestError1", "" + response.statusCode);
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));
                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                displayMessage(getString(R.string.something_went_wrong));
                            } else if (response.statusCode == 401) {
                                displayMessage(getString(R.string.invalid_credentials));
                            } else if (response.statusCode == 422) {
                                json = DriverApplication.trimMessage(new String(response.data));
                                if (json != "" && json != null) {
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
                            signIn(emilto);
                        }
                    }

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }
            };

            DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }

    public void getProfile() {

        if (isInternet) {

            if (customDialog == null) {
                customDialog = new CustomDialog(context);
                customDialog.show();
            }
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API + "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //customDialog.dismiss();
                    utils.print("GetProfile", response.toString());
                    SharedHelper.putKey(context, "id", response.optString("id"));
                    SharedHelper.putKey(context, "first_name", response.optString("first_name"));
                    SharedHelper.putKey(context, "last_name", response.optString("last_name"));
                    SharedHelper.putKey(context, "email", response.optString("email"));
                    SharedHelper.putKey(context, "gender", "" + response.optString("gender"));
                    SharedHelper.putKey(context, "mobile", response.optString("mobile"));
                    SharedHelper.putKey(context, "approval_status", response.optString("status"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    SharedHelper.putKey(context, "loggedIn", getString(R.string.True));
                    if (response.optString("avatar").startsWith("http"))
                        SharedHelper.putKey(context, "picture", response.optString("avatar").replace("app/public",""));
                    else
                        SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar").replace("app/public",""));

                    SharedHelper.getKey(context, "picture");

                    if (response.optJSONObject("service") != null) {
                        try {
                            JSONObject service = response.optJSONObject("service");
                            if (service.optJSONObject("service_type") != null) {
                                JSONObject serviceType = service.optJSONObject("service_type");
                                SharedHelper.putKey(context, "service", serviceType.optString("name"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    SharedHelper.putKey(context, "sos", response.optString("sos"));
                    SharedHelper.putKey(LoginOtpScreen.this, "login_by", "manual");
                    GoToMainActivity();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customDialog.dismiss();
                    String json = null;
                    String Message;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));

                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                displayMessage(getString(R.string.something_went_wrong));
                            } else if (response.statusCode == 401) {
                                SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                                GoToBeginActivity();
                            } else if (response.statusCode == 422) {
                                json = DriverApplication.trimMessage(new String(response.data));
                                if (json != "" && json != null) {
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
                    headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }
            };

            DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }


    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(context, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    public void displayMessage(String toastString) {
        Log.e("displayMessage", "" + toastString);

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

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

}
