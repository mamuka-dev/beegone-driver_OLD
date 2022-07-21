package com.gotaxi.driver.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.chaos.view.PinView;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gotaxi.driver._base.BaseActivity;
import com.hbb20.CountryCodePicker;
import com.gotaxi.driver.API.RetrofitClient;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.ConnectionHelper;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.Model.CityResponse;
import com.gotaxi.driver.Models.City;
import com.gotaxi.driver.Models.Servicelist;
import com.gotaxi.driver.Models.Servicelistresponse;
import com.gotaxi.driver.Models.UserResponse;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.MyCheckbox;
import com.gotaxi.driver.Utilities.MyEditText;
import com.gotaxi.driver.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;

import static com.gotaxi.driver.DriverApplication.trimMessage;


public class RegisterActivity extends BaseActivity {
    private String[] totalcities;
    private String cityname;
    private String[] totalservices;
    private String[] totalservicesid;
    private String servicename;
    private String serviceid;
    private boolean isPhoneVerification = false;
    TextView sendto;
    LinearLayout phoneverificationlayout;
    CountryCodePicker cpp;
    Dialog myDialog;
    MyCheckbox checkbox;
    TextView terms, sendotpagain;
    MyEditText adddress;
    Spinner city, service;
    PinView otpcode;
    Button verifiy_btn, get_otp;
    LinearLayout view_beforeverify, view_afterverify;
    TextView mPhone;
    EditText mPin;
    RadioButton rbFemale;
    CountryCodePicker country;
    String verificationCodeBySystem;
    public Context context = RegisterActivity.this;
    public Activity activity = RegisterActivity.this;
    String TAG = "RegisterActivity";
    String device_token, device_UDID;
    ImageView backArrow;
    Button signupBtn;
    EditText email, first_name, last_name, mobile_no, password;
    CustomDialog customDialog;
    ConnectionHelper helper;
    Boolean isInternet;
    Boolean fromActivity = false;
    String strViewPager = "";
    EditText vmodel, vnumber;
    public static int APP_REQUEST_CODE = 99;

    private String blockCharacterSet = "~#^|$%&*!()_-*.,@/";
    Utilities utils = new Utilities();

    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        customDialog = new CustomDialog(RegisterActivity.this);
        myDialog = new Dialog(this);
        try {
            Intent intent = getIntent();
            if (intent != null) {

                if (getIntent().getExtras().containsKey("viewpager")) {
                    strViewPager = getIntent().getExtras().getString("viewpager");
                }

                if (getIntent().getExtras().getBoolean("isFromMailActivity")) {
                    fromActivity = true;
                } else if (!getIntent().getExtras().getBoolean("isFromMailActivity")) {
                    fromActivity = false;
                } else {
                    fromActivity = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fromActivity = false;
        }
        findViewById();
        GetToken();

        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (first_name.getText().toString().equals("") || first_name.getText().toString().equalsIgnoreCase(getString(R.string.first_name))) {
                    displayMessage(getString(R.string.first_name_empty));
                } else if (last_name.getText().toString().equals("") || last_name.getText().toString().equalsIgnoreCase(getString(R.string.last_name))) {
                    displayMessage(getString(R.string.last_name_empty));
                } else if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                    displayMessage(getString(R.string.email_validation));
                } else if (!Utilities.isValidEmail(email.getText().toString())) {
                    displayMessage(getString(R.string.not_valid_email));
                } else if (vmodel.getText().toString().equals("")) {
                    displayMessage(getResources().getString(R.string.enter_vehicle_model));
                } else if (vnumber.getText().toString().equals("")) {
                    displayMessage(getResources().getString(R.string.enter_vehicle_number));
                } else if (mPhone.getText().toString().equals("")) {
                    displayMessage(getResources().getString(R.string.enter_phone_number));
                } else if (mPin.getText().toString().equals("")) {
                    displayMessage(getResources().getString(R.string.enter_passpin));
                }
                else if( mPin.getText().toString().trim().length()<6){
                    displayMessage(getResources().getString(R.string.wrong_pass_len));

                }
                else if (!rbFemale.isChecked()) {
                    displayMessage(getString(R.string.sorry_note_reg));
                } else if (!checkbox.isChecked()) {
                    displayMessage(getResources().getString(R.string.accept_terms_condition));
                } else {
                    if (isInternet) {
                        checkMailAlreadyExit(view);
                    } else {
                        displayMessage(getString(R.string.something_went_wrong_net));
                    }
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.hideKeyboard(RegisterActivity.this);
                onBackPressed();
            }
        });


        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.hideKeyboard(RegisterActivity.this);
                Intent intent = new Intent(RegisterActivity.this, custom_webview.class);
                intent.putExtra("page", "privacy_page");
                intent.putExtra("page_name", "Privacy Policy");
                startActivity(intent);
            }
        });

        verifiy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otpcode.length() < 6) {
                    Toast.makeText(context, "Wrong OTP Code", Toast.LENGTH_SHORT).show();
                } else {
                    Utilities.hideKeyboard(RegisterActivity.this);
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
        get_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhone.getText().toString().equals("") || mPhone.getText().toString().length() == 0) {
                    displayMessage(getString(R.string.mobile_number_empty));
                } else {
                    customDialog.show();
                    Call<UserResponse> call =
                            RetrofitClient.getInstance().getApi().userlogin(
                                    //getString(R.string.country_code)
                                    cpp.getSelectedCountryCodeWithPlus()
                                            + mPhone.getText().toString());
                    call.enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call, retrofit2.Response<UserResponse> response) {
                            if (response.isSuccessful()) {
                                if (response.body().isError()) {
                                    customDialog.dismiss();
                                    getotp();
                                } else {
                                    customDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Please Login: User Already Registered", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                customDialog.dismiss();
                                getotp();
                            }

                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {
                            customDialog.dismiss();
                        }
                    });

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

        getcity();
        getservices();
    }

    private void getotp() {
        if (isPhoneVerification) {
            new CountDownTimer(60000, 1000) {


                @SuppressLint("SetTextI18n")
                @Override
                public void onTick(long millisUntilFinished) {
                    sendotpagain.setText(getResources().getString(R.string.send_otp_again) + " (" + millisUntilFinished / 1000 + ")");
                }

                public void onFinish() {
                    sendotpagain.setEnabled(true);
                }
            }.start();
            Utilities.hideKeyboard(RegisterActivity.this);
            sendVerificationCodeToUser(mPhone.getText().toString());
        } else {
            Utilities.hideKeyboard(RegisterActivity.this);
            phoneverificationlayout.setVisibility(View.GONE);
            view_afterverify.setVisibility(View.VISIBLE);
        }

    }

    private void getservices() {
        Call<Servicelistresponse> call = RetrofitClient.getInstance().getApi().getallservices();
        call.enqueue(new Callback<Servicelistresponse>() {
            @Override
            public void onResponse(Call<Servicelistresponse> call, retrofit2.Response<Servicelistresponse> response) {
                if (response.isSuccessful()) {
                    List<Servicelist> servicelists;
                    Servicelistresponse servicelistresponse = response.body();
                    servicelists = servicelistresponse.getServiceslist();
                    totalservices = new String[servicelists.size()];
                    totalservicesid = new String[servicelists.size()];
                    for (int i = 0; i < servicelists.size(); i++) {
                        totalservices[i] = servicelists.get(i).getName();
                        totalservicesid[i] = servicelists.get(i).getId();
                        ArrayAdapter<String> adapter;
                        adapter = new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_list_item_1, totalservices);
                        service.setAdapter(adapter);
                        serviceid = totalservicesid[0];
                    }
                }

            }

            @Override
            public void onFailure(Call<Servicelistresponse> call, Throwable t) {
                Log.v("TAG", t.getLocalizedMessage().toString());
            }
        });
        service.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serviceid = totalservicesid[position];
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getcity() {
        customDialog.show();
        Call<CityResponse> call = RetrofitClient.getInstance().getApi().getcity();
        call.enqueue(new Callback<CityResponse>() {
            @Override
            public void onResponse(Call<CityResponse> call, retrofit2.Response<CityResponse> response) {
                if (response.isSuccessful()) {
                    isPhoneVerification = response.body().isVerification();
                    List<City> cities;
                    CityResponse cityResponse = response.body();
                    cities = cityResponse.getCity();
                    totalcities = new String[cities.size()];
                    for (int i = 0; i < cities.size(); i++) {
                        totalcities[i] = cities.get(i).getName();
                    }

                    ArrayAdapter<String> adapter;
                    adapter = new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_list_item_1, totalcities);
                    city.setAdapter(adapter);
                    cityname = totalcities[0];

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
        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cityname = totalcities[position];
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void findViewById() {
        sendto = findViewById(R.id.senttotext);
        sendotpagain = findViewById(R.id.sendotpagain);
        verifiy_btn = findViewById(R.id.verify_btn);
        get_otp = findViewById(R.id.get_otp);
        checkbox = findViewById(R.id.checkedterms);
        view_beforeverify = findViewById(R.id.view_beforeverification);
        view_afterverify = findViewById(R.id.view_afterverification);
        terms = findViewById(R.id.termms);
        city = findViewById(R.id.city_spinner);
        service = findViewById(R.id.service_list);
        mPin = findViewById(R.id.pinView);
        rbFemale = findViewById(R.id.rb_female);
        mPhone = findViewById(R.id.idphone);
        cpp = findViewById(R.id.mcpp);
        otpcode = findViewById(R.id.otppin);
        email = findViewById(R.id.email);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        mobile_no = findViewById(R.id.mobile_no);
        vmodel = findViewById(R.id.vmodel);
        vnumber = findViewById(R.id.vnumber);
        signupBtn = findViewById(R.id.nextBtn);
        phoneverificationlayout = findViewById(R.id.phoneverificationlayout);
        backArrow = findViewById(R.id.backArrow);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        email.setText(SharedHelper.getKey(context, "email"));
        first_name.setFilters(new InputFilter[]{filter});
        last_name.setFilters(new InputFilter[]{filter});
    }


    public void checkMailAlreadyExit(View view) {
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("email", email.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CHECK_MAIL_ALREADY_REGISTERED, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                registerAPI();
                //  phoneLogin();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                String json = null;
                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {
                    utils.print("MyTest", "" + error);
                    utils.print("MyTestError", "" + error.networkResponse);
                    utils.print("MyTestError1", "" + response.statusCode);
                    try {
                        if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (!json.equals("") && json != null) {
                                if (json.startsWith("The email has already been taken")) {
                                    displayMessage(getString(R.string.email_exist));
                                } else {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                                //displayMessage(json);
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
                        checkMailAlreadyExit(view);
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

    private void registerAPI() {


        if (customDialog == null) {
            customDialog = new CustomDialog(context);
            customDialog.show();
        }
        JSONObject object = new JSONObject();
        try {
            object.put("device_type", "android");
            object.put("device_id", device_UDID);
            object.put("device_token", device_token);
            object.put("login_by", "manual");
            object.put("first_name", first_name.getText().toString());
            object.put("last_name", last_name.getText().toString());
            object.put("email", email.getText().toString());
            object.put("city", cityname);
            object.put("password", mPin.getText().toString());
            object.put("password_confirmation", mPin.getText().toString());
            object.put("vmodel", vmodel.getText().toString());
            object.put("vnumber", vnumber.getText().toString());
            object.put("vtype", serviceid);
            //  object.put("mobile", getString(R.string.country_code) + mPhone.getText().toString().trim());
            object.put("mobile", cpp.getSelectedCountryCodeWithPlus() + mPhone.getText().toString().trim());
//            object.put("picture","");
//            object.put("social_unique_id","");
            utils.print("InputToRegisterAPI", "" + object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.register, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                utils.print("SignInResponse", response.toString());
                SharedHelper.putKey(RegisterActivity.this, "email", email.getText().toString());
                SharedHelper.putKey(RegisterActivity.this, "password", mPin.getText().toString());
                signIn();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (customDialog != null && customDialog.isShowing())
                    customDialog.dismiss();
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    utils.print("MyTestError1", "" + response.statusCode);
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));
                        utils.print("ErrorInRegisterAPI", "" + errorObj.toString());

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("error"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            try {
                                if (errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                    //Call Refresh token
                                } else {
                                    displayMessage(errorObj.optString("message"));
                                }
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }

                        } else if (response.statusCode == 422) {
                            json = trimMessage(new String(response.data));
                            if (!json.equals("") && json != null) {
                                if (json.startsWith("The email has already been taken")) {
                                    displayMessage(getString(R.string.email_exist));
                                } else {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                                //displayMessage(json);
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
                        registerAPI();
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

    public void signIn() {
        if (isInternet) {
            if (customDialog == null) {
                customDialog = new CustomDialog(RegisterActivity.this);
                if (customDialog != null)
                    customDialog.show();
            }
            JSONObject object = new JSONObject();
            try {
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                object.put("email", SharedHelper.getKey(RegisterActivity.this, "email"));
                object.put("password", SharedHelper.getKey(RegisterActivity.this, "password"));
                utils.print("InputToLoginAPI", "" + object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    utils.print("SignUpResponse", response.toString());
                    SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    getProfile();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    String json = null;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));
                            utils.print("ErrorInLoginAPI", "" + errorObj.toString());

                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500 || response.statusCode == 401) {
                                displayMessage(getString(R.string.something_went_wrong));
                            } else if (response.statusCode == 422) {
                                json = trimMessage(new String(response.data));
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
                            signIn();
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
                customDialog = new CustomDialog(RegisterActivity.this);
                if (customDialog != null)
                    customDialog.show();
            }
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    utils.print("GetProfile", response.toString());
                    SharedHelper.putKey(RegisterActivity.this, "id", response.optString("id"));
                    SharedHelper.putKey(RegisterActivity.this, "first_name", response.optString("first_name"));
                    SharedHelper.putKey(RegisterActivity.this, "last_name", response.optString("last_name"));
                    SharedHelper.putKey(RegisterActivity.this, "email", response.optString("email"));
                    if (response.optString("avatar").startsWith("http"))
                        SharedHelper.putKey(context, "picture", response.optString("avatar"));
                    else
                        SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar"));
                    SharedHelper.putKey(RegisterActivity.this, "gender", "" + response.optString("gender"));
                    SharedHelper.putKey(RegisterActivity.this, "mobile", response.optString("mobile"));
                    SharedHelper.putKey(context, "approval_status", response.optString("status"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    SharedHelper.putKey(context, "sos", response.optString("sos"));
                    SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.True));

                    if (response.optJSONObject("service") != null) {
                        JSONObject service = response.optJSONObject("service");
                        JSONObject serviceType = service.optJSONObject("service_type");
                        SharedHelper.putKey(context, "service", serviceType.optString("name"));
                    }
                    SharedHelper.putKey(RegisterActivity.this, "login_by", "manual");
                    //GoToMainActivity();
                    GoToDocumentsActivity();
                    // Toast.makeText(context, "TST", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (customDialog != null && customDialog.isShowing())
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

                                json = trimMessage(new String(response.data));
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
                            getProfile();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + SharedHelper.getKey(RegisterActivity.this, "access_token"));
                    return headers;
                }
            };

            DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            if (data != null) {
                AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Log.e(TAG, "onSuccess: Account Kit" + account.getId());
                        Log.e(TAG, "onSuccess: Account Kit" + AccountKit.getCurrentAccessToken().getToken());
                        if (AccountKit.getCurrentAccessToken().getToken() != null) {
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", AccountKit.getCurrentAccessToken().getToken());
                            PhoneNumber phoneNumber = account.getPhoneNumber();
                            String phoneNumberString = phoneNumber.toString();
                            SharedHelper.putKey(RegisterActivity.this, "mobile", phoneNumberString);
                            registerAPI();
                        } else {
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", "");
                            SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.False));
                            SharedHelper.putKey(context, "email", "");
                            SharedHelper.putKey(context, "login_by", "");
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", "");
                            Intent goToLogin = new Intent(RegisterActivity.this, WelcomeScreenActivity.class);
                            goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(goToLogin);
                            finish();
                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Log.e(TAG, "onError: Account Kit" + accountKitError);
                        displayMessage("" + getResources().getString(R.string.social_cancel));
                    }
                });
                if (loginResult != null) {
                    SharedHelper.putKey(this, "account_kit", getString(R.string.True));
                } else {
                    SharedHelper.putKey(this, "account_kit", getString(R.string.False));
                }
                String toastMessage;
                if (loginResult.getError() != null) {
                    toastMessage = loginResult.getError().getErrorType().getMessage();
                    // showErrorActivity(loginResult.getError());
                } else if (loginResult.wasCancelled()) {
                    toastMessage = "Login Cancelled";
                } else {
                    if (loginResult.getAccessToken() != null) {
                        Log.e(TAG, "onActivityResult: Account Kit" + loginResult.getAccessToken().toString());
                        SharedHelper.putKey(this, "account_kit", loginResult.getAccessToken().toString());
                        toastMessage = "Welcome to Tranxit...";
                    } else {
                        SharedHelper.putKey(this, "account_kit", "");
                        toastMessage = String.format(
                                "Welcome to Tranxit...",
                                loginResult.getAuthorizationCode().substring(0, 10));
                    }
                }
            }
        }
    }

    private void sendVerificationCodeToUser(String phoneNo) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
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
            //  sendto.setText(getString(R.string.sent_to) + " " + getString(R.string.country_code) + mPhone.getText().toString());
            sendto.setText(getString(R.string.sent_to) + " " + cpp.getSelectedCountryCodeWithPlus() + mPhone.getText().toString());
            phoneverificationlayout.setVisibility(View.GONE);
            view_beforeverify.setVisibility(View.VISIBLE);
        }


        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }


        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    private void verifyCode(String codeByUser) {


        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredentials(credential);


    }


    private void signInTheUserByCredentials(PhoneAuthCredential credential) {


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mPhone.setEnabled(false);
                            view_beforeverify.setVisibility(View.GONE);
                            view_afterverify.setVisibility(View.VISIBLE);
                            if (customDialog.isShowing())
                                customDialog.dismiss();
                        } else {
                            if (customDialog.isShowing())
                                customDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }


                    }
                });


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


    public void GoToDocumentsActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, UploadDocumentActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //   mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        //  RegisterActivity.this.finish();
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        RegisterActivity.this.finish();
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

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        activity.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

}
