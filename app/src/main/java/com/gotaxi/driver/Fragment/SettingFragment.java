package com.gotaxi.driver.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.accountkit.AccountKit;
import com.facebook.login.LoginManager;
import com.google.android.material.snackbar.Snackbar;
import com.gotaxi.driver.Activity.WelcomeScreenActivity;
import com.gotaxi.driver.Activity.custom_webview;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.LanguageData;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.R;
import com.gotaxi.driver.adapter.LanguageAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.gotaxi.driver.DriverApplication.trimMessage;

public class SettingFragment extends Fragment {
    private Dialog alertDialog;
    private LanguageData languageData;
    private LinearLayout share, logout, accountmanager, s_tmeter, s_language, terms, privacy;
    private ImageView backbtn;

    public SettingFragment() {
    }


    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        share = view.findViewById(R.id.s_share);
        logout = view.findViewById(R.id.s_logout);
        accountmanager = view.findViewById(R.id.s_account);
        s_tmeter = view.findViewById(R.id.s_tmeter);
        s_language = view.findViewById(R.id.s_language);
        privacy = view.findViewById(R.id.privacy);
        terms = view.findViewById(R.id.terms);
        backbtn = view.findViewById(R.id.backArrow);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new HomeFragment();

                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
            }
        });
        s_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                language_alert_view(getActivity());
            }
        });
        s_tmeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new TaxiMeterFragment();

                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
            }
        });
        accountmanager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new ManageBankAccountFragment();

                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToShareScreen(URLHelper.APP_URL + getActivity().getPackageName());
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialog();
            }
        });
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), custom_webview.class);
                intent.putExtra("page", "privacy_page");
                intent.putExtra("page_name", "Privacy Policy");
                startActivity(intent);
            }
        });
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), custom_webview.class);
                intent.putExtra("page", "terms_page");
                intent.putExtra("page_name", "Terms & Conditions");
                startActivity(intent);
            }
        });
        return view;
    }

    public void navigateToShareScreen(String shareUrl) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl + " -via " + getString(R.string.app_name));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void showLogoutDialog() {
        if (!getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(this.getString(R.string.app_name))
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage(getString(R.string.logout_alert));
            builder.setPositiveButton(R.string.yes, (dialog, which) -> logout());
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                //Reset to previous seletion menu in navigation
                dialog.dismiss();
            });
            builder.setCancelable(false);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(arg -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            });
            dialog.show();
        }
    }

    CustomDialog customDialog = null;

    void handleLoaderDialog(boolean isShow) {
        if (customDialog == null)
            customDialog = new CustomDialog(getActivity());
        if (customDialog != null)
            if (isShow)
                customDialog.show();
            else customDialog.dismiss();


    }

    public void logout() {
        handleLoaderDialog(true);

        JSONObject object = new JSONObject();
        try {
            object.put("id", SharedHelper.getKey(getActivity(), "id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.LOGOUT, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                handleLoaderDialog(false);

                if (SharedHelper.getKey(getActivity(), "login_by").equals("facebook"))
                    LoginManager.getInstance().logOut();
                if (SharedHelper.getKey(getActivity(), "login_by").equals("google"))

                    if (!SharedHelper.getKey(getActivity(), "account_kit_token").equalsIgnoreCase("")) {
                        Log.e("setting", "Account kit logout: " + SharedHelper.getKey(getActivity(), "account_kit_token"));
                        AccountKit.logOut();
                        SharedHelper.putKey(getActivity(), "account_kit_token", "");
                    }
                SharedHelper.putKey(getActivity(), "current_status", "");
                SharedHelper.putKey(getActivity(), "loggedIn", getString(R.string.False));
                SharedHelper.putKey(getActivity(), "email", "");
                Intent goToLogin = new Intent(getActivity(), WelcomeScreenActivity.class);
                goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(goToLogin);
                getActivity().finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleLoaderDialog(false);
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
                        logout();
                    }
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                Log.e("getHeaders: Token", SharedHelper.getKey(getActivity(), "access_token") + SharedHelper.getKey(getActivity(), "token_type"));
                headers.put("Authorization", "" + "Bearer" + " " + SharedHelper.getKey(getActivity(), "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void language_alert_view(final Context mContext) {
        final ArrayList<LanguageData> languageDataArrayList = new ArrayList<>();
        if (alertDialog != null)
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        final View view = View.inflate(mContext, R.layout.language_lay, null);

        alertDialog = new Dialog(mContext, R.style.dialogwinddow);
        alertDialog.setContentView(view);
        alertDialog.setCancelable(true);
        alertDialog.show();

        RecyclerView langList = alertDialog.findViewById(R.id.langList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        langList.setHasFixedSize(true);
        langList.setLayoutManager(linearLayoutManager);
        try {
            for (int i = 0; i < getResources().getStringArray(R.array.languagelist).length; i++) {

                String lang_name = (getResources().getStringArray(R.array.languagelist))[i];
                String lang_code = (getResources().getStringArray(R.array.languagecode))[i];
                String lang_country_code = (getResources().getStringArray(R.array.languagecountrycode))[i];
                languageData = new LanguageData(lang_name, lang_code, lang_country_code);
                languageDataArrayList.add(languageData);
            }

            LanguageAdapter detailsSizeAdapter = new LanguageAdapter(getActivity(), languageDataArrayList);
            langList.setAdapter(detailsSizeAdapter);


            detailsSizeAdapter.setOnItemClickListener(new LanguageAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {

                    alertDialog.dismiss();


                    updateLocale(languageDataArrayList.get(position));
                    /*
                    restart the activity
                     */
                    Intent intent = getActivity().getIntent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void updateLocale(LanguageData languageData) {

        SharedHelper.putKey(getActivity(), "language", languageData.getLanguageCode());

        setLanguage();

    }

    private void setLanguage() {
        String languageCode = SharedHelper.getKey(getActivity(), "language");
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getActivity().getBaseContext().getResources().updateConfiguration(configuration, getActivity().getBaseContext().getResources().getDisplayMetrics());
//        LocaleUtils.setLocale(this, languageCode);
    }

    public void displayMessage(String toastString) {
        Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).setTextColor(Color.WHITE).show();
    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }
}
