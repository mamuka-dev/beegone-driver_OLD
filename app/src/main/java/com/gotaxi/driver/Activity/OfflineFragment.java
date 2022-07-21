package com.gotaxi.driver.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.appcompat.widget.AppCompatButton;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.gotaxi.driver.Fragment.HomeFragment;
import com.gotaxi.driver.Helper.ConnectionHelper;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class OfflineFragment extends Fragment {
    AppCompatButton m_revenue;
    Activity activity;
    Context context;
    ConnectionHelper helper;
    Boolean isInternet;
    View rootView;
    CustomDialog customDialog;
    String token;
    Switch goOnlineBtn;
    //menu icon
    ImageView menuIcon;
    int NAV_DRAWER = 0;
    DrawerLayout drawer;

    Utilities utils = new Utilities();


    public OfflineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        ha.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
//            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
//            notificationManager.cancelAll();
            ha.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //call function
                    checkStatus();
                    ha.postDelayed(this, 3000);
                }
            }, 3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_offline, container, false);
        findViewByIdAndInitialize();
        return rootView;
    }

    public void findViewByIdAndInitialize() {
        ha = new Handler();
        helper = new ConnectionHelper(activity);
        isInternet = helper.isConnectingToInternet();
        token = SharedHelper.getKey(activity, "access_token");
        goOnlineBtn = rootView.findViewById(R.id.goOnlineBtn);
        menuIcon = rootView.findViewById(R.id.menuIcon);
        drawer = activity.findViewById(R.id.drawer_layout);
        m_revenue = rootView.findViewById(R.id.m_revenue);

        m_revenue.setText(SharedHelper.getKey(context, "today_rev"));
        goOnlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goOnlineBtn.isChecked())
                    goOnline();
            }
        });


        menuIcon.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (NAV_DRAWER == 0) {
                    drawer.openDrawer(Gravity.START);
                } else {
                    NAV_DRAWER = 0;
                    drawer.closeDrawers();
                }
            }
        });
    }

    public void online() {
        try {
            FragmentManager manager = MainActivity.fragmentManager;
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content, new HomeFragment());
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goOnline() {
        customDialog = new CustomDialog(activity);
        customDialog.show();
        JSONObject param = new JSONObject();
        try {
            param.put("service_status", "active");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.UPDATE_AVAILABILITY_API, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    try {
                        customDialog.dismiss();
                        if (response.optJSONObject("service").optString("status").equalsIgnoreCase("active")) {
//                            Intent intent = new Intent(context, MainActivity.class);
//                            context.startActivity(intent);
//                            FragmentManager manager = MainActivity.fragmentManager;
//                            FragmentTransaction transaction = manager.beginTransaction();
//                            transaction.replace(R.id.content, new Map());
//                            transaction.commitAllowingStateLoss();
                            online();
                        } else {
                            displayMessage(getString(R.string.something_went_wrong));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    customDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
                utils.print("Error", error.toString());
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {

                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
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
                        goOnline();
                    }
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
        Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
               .setAction("Action", null).setTextColor(Color.WHITE).show();
    }

    public void GoToBeginActivity() {
        ha.removeCallbacksAndMessages(null);
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    boolean showBatteryAlert = true;
    public Handler ha;

    public void checkStatus() {
        try {
            /* Battery status check */
            if (context == null) {
                context = getContext();
            }
            helper = new ConnectionHelper(context);
            if (Utilities.getBatteryLevel(context)) {
                if (showBatteryAlert) {
                    Utilities.notify(context, activity);
                    showBatteryAlert = false;
                }
            }


            if (helper.isConnectingToInternet()) {

                String url = URLHelper.base + "api/provider/trip?latitude=&longitude=";

                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String distee = "";
                        Log.e("CheckStatus", "" + response.toString());
                        if (response.optString("service_status").equals("active")) {
//                                ha.removeMessages(0);
//                    Intent intent = new Intent(activity, Offline.class);
//                    activity.startActivity(intent);
                            online();
                        }
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        utils.print("Error", error.toString());
                        //errorHandler(error);
                    }
                }) {
                    @Override
                    public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("X-Requested-With", "XMLHttpRequest");
                        headers.put("Authorization", "Bearer " + token);
                        return headers;
                    }
                };
                DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
            } else {
                displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }
}
