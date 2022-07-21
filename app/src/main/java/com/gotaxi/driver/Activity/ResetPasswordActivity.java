package com.gotaxi.driver.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.gotaxi.driver.API.RetrofitClient;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.ConnectionHelper;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.Model.CityResponse;
import com.gotaxi.driver.Models.UserResponse;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.MyEditText;
import com.gotaxi.driver.Utilities.Utilities;
import com.gotaxi.driver._base.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;

import static com.gotaxi.driver.DriverApplication.trimMessage;

public class ResetPasswordActivity extends BaseActivity {
    String verificationCodeBySystem;
    FirebaseAuth mAuth;
    final String[] phone = new String[1];
    CountryCodePicker cpp;
    TextView mPhone, sendotpagain;
    EditText mPin;
    PinView otpcode;
    Button verifiy_btn, get_otp, changePasswordBtn;
    LinearLayout view_beforeverify, view_afterverify;
    public Context context = ResetPasswordActivity.this;
    public Activity activity = ResetPasswordActivity.this;
    ImageView backArrow;
    CustomDialog customDialog;
    ConnectionHelper helper;
    Boolean isInternet;
    TextView sendto;
    LinearLayout phoneverificationlayout;
    String emailto, id;
    MyEditText new_password, confirm_password;
    boolean isverificationrequired = true;
    Utilities utils = new Utilities();


    public static int APP_REQUEST_CODE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        customDialog = new CustomDialog(ResetPasswordActivity.this);
        mAuth = FirebaseAuth.getInstance();

        findViewById();

        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.hideKeyboard(ResetPasswordActivity.this);
                onBackPressed();
            }
        });
        verifiy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otpcode.length() < 6) {
                    Toast.makeText(context, "Wrong OTP Code", Toast.LENGTH_SHORT).show();
                } else {
                    Utilities.hideKeyboard(ResetPasswordActivity.this);
                    customDialog.show();
                    verifyCode(String.valueOf(otpcode.getText()));
                }
            }
        });
        sendotpagain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendotpagain.setEnabled(false);
                customDialog.show();
                sendVerificationCodeToUser(mPhone.getText().toString());
            }
        });
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new_password.getText().toString().trim().equals("")) {
                    new_password.setError("Please Enter Password");
                } else if (confirm_password.getText().toString().trim().equals("")) {
                    confirm_password.setError("Please Confirm Password");
                } else if (!new_password.getText().toString().trim().equals(confirm_password.getText().toString().trim())) {
                    displayMessage("Password Doesn't Match");
                } else {
                    resetpassword();
                }
            }
        });
        get_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isverificationrequired) {
                    if (mPhone.getText().toString().equals("")) {
                        displayMessage("Please Enter Your Phone Number");
                    } else {
                        new CountDownTimer(60000, 1000) {


                            @Override
                            public void onTick(long millisUntilFinished) {
                                sendotpagain.setText(getResources().getString(R.string.send_otp_again) + " (" + millisUntilFinished / 1000 + ")");
                            }

                            public void onFinish() {
                                sendotpagain.setEnabled(true);
                                v.invalidate();
                            }
                        }.start();
                        Utilities.hideKeyboard(ResetPasswordActivity.this);
                        customDialog.show();
                        sendVerificationCodeToUser(mPhone.getText().toString());
                    }
                } else {
                    Utilities.hideKeyboard(ResetPasswordActivity.this);
                    getemail();
                }

            }
        });

        mPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String x = s.toString();
                if (x.startsWith("0")) {
                    mPhone.setText("");
                }
            }
        });

        checkverification();
    }

    private void checkverification() {
        customDialog.show();
        Call<CityResponse> call = RetrofitClient.getInstance().getApi().getcity();
        call.enqueue(new Callback<CityResponse>() {
            @Override
            public void onResponse(Call<CityResponse> call, retrofit2.Response<CityResponse> response) {
                if (response.isSuccessful()) {
                    isverificationrequired = response.body().isVerification();
                }
                if (customDialog.isShowing())
                    customDialog.dismiss();
            }

            @Override
            public void onFailure(Call<CityResponse> call, Throwable t) {
                if (customDialog.isShowing())
                    customDialog.dismiss();
            }
        });
    }

    public void findViewById() {
        otpcode = findViewById(R.id.otppin);
        verifiy_btn = findViewById(R.id.verify_btn);
        get_otp = findViewById(R.id.get_otp);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        view_beforeverify = findViewById(R.id.view_beforeverification);
        view_afterverify = findViewById(R.id.view_afterverify);
        mPin = findViewById(R.id.pinView);
        mPhone = findViewById(R.id.idphone);
        cpp = findViewById(R.id.mcpp);
        backArrow = findViewById(R.id.backArrow);
        sendotpagain = findViewById(R.id.sendotpagain);
        sendto = findViewById(R.id.senttotext);
        new_password = findViewById(R.id.new_password);
        confirm_password = findViewById(R.id.confirm_password);
        helper = new ConnectionHelper(context);
        phoneverificationlayout = findViewById(R.id.phoneverificationlayout);
        isInternet = helper.isConnectingToInternet();
    }

    private void sendVerificationCodeToUser(String phoneNo) {


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(cpp.getSelectedCountryCodeWithPlus() + phoneNo)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
            if (customDialog.isShowing())
                customDialog.dismiss();
            mPhone.setEnabled(false);
            sendto.setText(getString(R.string.sent_to) + " " + cpp.getSelectedCountryCodeWithPlus() + mPhone.getText().toString());
            phoneverificationlayout.setVisibility(View.GONE);
            view_beforeverify.setVisibility(View.VISIBLE);
        }


        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }


        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(ResetPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    private void verifyCode(String codeByUser) {


        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredentials(credential);


    }


    private void signInTheUserByCredentials(PhoneAuthCredential credential) {


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(ResetPasswordActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getemail();
                        } else {
                            if (customDialog.isShowing())
                                customDialog.dismiss();
                            Toast.makeText(ResetPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }


                    }
                });

    }

    private void getemail() {
        Call<UserResponse> call = RetrofitClient
                .getInstance().getApi().userlogin(cpp.getSelectedCountryCodeWithPlus() + mPhone.getText().toString());
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, retrofit2.Response<UserResponse> response) {
                if (!response.body().isError()) {
                    phoneverificationlayout.setVisibility(View.GONE);
                    UserResponse userResponse = response.body();
                    emailto = userResponse.getUser().getEmail();
                    forgetPassword();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "This Phone Number is not registered", Toast.LENGTH_SHORT).show();
                }

                customDialog.dismiss();

            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                customDialog.dismiss();
            }
        });
    }

    private void forgetPassword() {
        JSONObject object = new JSONObject();
        try {

            object.put("email", emailto);
            Log.e("ForgetPassword", "" + object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.FORGET_PASSWORD, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                customDialog.dismiss();
                JSONObject userObject = response.optJSONObject("provider");
                id = String.valueOf(userObject.optInt("id"));
                view_beforeverify.setVisibility(View.GONE);
                view_afterverify.setVisibility(View.VISIBLE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    Log.e("MyTest", "" + error);
                    Log.e("MyTestError", "" + error.networkResponse);
                    Log.e("MyTestError1", "" + response.statusCode);
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage("Something went wrong.");
                            }
                        } else if (response.statusCode == 401) {
                            try {
                                if (errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                    Log.v("code", "code");
                                } else {
                                    displayMessage(errorObj.optString("message"));
                                }
                            } catch (Exception e) {
                                displayMessage("Something went wrong.");
                            }

                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (!json.equals("") && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage("Please try again.");
                            }

                        } else {
                            displayMessage("Please try again.");
                        }

                    } catch (Exception e) {
                        displayMessage("Something went wrong.");
                    }
                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        forgetPassword();
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

    }

    private void resetpassword() {
        customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("password", new_password.getText().toString());
            object.put("password_confirmation", confirm_password.getText().toString());
            Log.e("ResetPassword", "" + object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.RESET_PASSWORD, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                Log.v("ResetPasswordResponse", response.toString());
                try {
                    JSONObject object1 = new JSONObject(response.toString());
                    Toast.makeText(context, object1.optString("message"), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ResetPasswordActivity.this, LoginOtpScreen.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                Log.e("MyTest", "" + error);
                Log.e("MyTestError", "" + error.networkResponse);
                Log.e("MyTestError1", "" + response.statusCode);
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage("Something went wrong.");
                            }
                        } else if (response.statusCode == 401) {
                            try {
                                if (errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                    //Call Refresh token
                                } else {
                                    displayMessage(errorObj.optString("message"));
                                }
                            } catch (Exception e) {
                                displayMessage("Something went wrong.");
                            }

                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (!json.equals("") && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage("Please try again.");
                            }

                        } else {
                            displayMessage("Please try again.");
                        }

                    } catch (Exception e) {
                        displayMessage("Something went wrong.");
                    }


                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        resetpassword();
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

}
