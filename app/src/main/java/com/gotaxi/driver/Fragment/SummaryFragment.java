package com.gotaxi.driver.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
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
import com.daasuu.cat.CountAnimationTextView;
import com.google.android.material.snackbar.Snackbar;
import com.gotaxi.driver.Activity.WelcomeScreenActivity;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.R;

import org.json.JSONObject;

import java.util.HashMap;

import static com.gotaxi.driver.DriverApplication.trimMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class SummaryFragment extends Fragment implements View.OnClickListener {


    AppCompatButton paynow;
    TextView paybleamout, Totalrevenue, servicecharges, cashcollected, payorcollect;


    ImageView imgBack;
    LinearLayout cardLayout;
    ImageButton bt_toggle_items;
    CountAnimationTextView noOfRideTxt;
    CountAnimationTextView scheduleTxt;
    CountAnimationTextView revenueTxt;
    CountAnimationTextView cancelTxt;

    CardView ridesCard;
    CardView cancelCard;
    CardView scheduleCard;
    CardView revenueCard;

    TextView currencyTxt;

    int rides, revenue, schedule, cancel;
    Double doubleRevenue;

    String TAG = "SummaryFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        findViewsById(view);
        setClickListeners();

        getProviderSummary();
        getProfile();
        return view;

    }

    private void setClickListeners() {
        imgBack.setOnClickListener(this);
        revenueCard.setOnClickListener(this);
        ridesCard.setOnClickListener(this);
        revenueCard.setOnClickListener(this);
        scheduleCard.setOnClickListener(this);
    }

    private void findViewsById(View view) {
        paynow = view.findViewById(R.id.amounttopaynow);
        cashcollected = view.findViewById(R.id.cashcollected);
        Totalrevenue = view.findViewById(R.id.totalravenue);
        servicecharges = view.findViewById(R.id.servicecharges);
        payorcollect = view.findViewById(R.id.payorcollect);
        paybleamout = view.findViewById(R.id.amounttopay);

        imgBack = view.findViewById(R.id.backArrow);
        cardLayout = view.findViewById(R.id.card_layout);
        noOfRideTxt = view.findViewById(R.id.no_of_rides_txt);
        scheduleTxt = view.findViewById(R.id.schedule_txt);
        cancelTxt = view.findViewById(R.id.cancel_txt);
        revenueTxt = view.findViewById(R.id.revenue_txt);
        currencyTxt = view.findViewById(R.id.currency_txt);
        revenueCard = view.findViewById(R.id.revenue_card);
        scheduleCard = view.findViewById(R.id.schedule_card);
        ridesCard = view.findViewById(R.id.rides_card);
        cancelCard = view.findViewById(R.id.cancel_card);
        bt_toggle_items = view.findViewById(R.id.bt_toggle_items);
        LinearLayout lyt_expand_items = view.findViewById(R.id.lyt_expand_items);

        bt_toggle_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean show = toggleArrow(view);
                if (show) {
                    lyt_expand_items.setVisibility(View.GONE);
                } else {

                    lyt_expand_items.setVisibility(View.VISIBLE);
                }
            }
        });

        paynow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (paybleamout.getText().toString().trim().equals("0.0")) {
                    Toast.makeText(getActivity(), "All amount already Paid", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Online Payment is not available please pay at our nearest office.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = new Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("toolbar", "true");
        switch (v.getId()) {
            case R.id.backArrow:
                Fragment fragment2 = new HomeFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content, fragment2).commit();
                break;

            case R.id.rides_card:
                fragment = new PastTrips();
                fragment.setArguments(bundle);
                transaction.replace(R.id.content, fragment, TAG);
                transaction.addToBackStack(TAG);
                transaction.commit();
                break;
            case R.id.schedule_card:
                fragment = new OnGoingTrips();
                fragment.setArguments(bundle);
                transaction.replace(R.id.content, fragment, TAG);
                transaction.addToBackStack(TAG);
                transaction.commit();
                break;
            case R.id.revenue_card:
                fragment = new EarningsFragment();
                transaction.replace(R.id.content, fragment, TAG);
                transaction.addToBackStack(TAG);
                transaction.commit();
                break;
            case R.id.cancel_card:
                fragment = new PastTrips();
                fragment.setArguments(bundle);
                transaction.replace(R.id.content, fragment, TAG);
                transaction.addToBackStack(TAG);
                transaction.commit();
                break;
        }
    }

    public boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(180);
            return false;
        } else {
            view.animate().setDuration(200).rotation(0);
            return true;
        }
    }

    private void setDetails() {
        Animation txtAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.txt_size);
        if (schedule > 0) {
            scheduleTxt.setAnimationDuration(500)
                    .countAnimation(0, schedule);
        } else {
            scheduleTxt.setText("0");
        }
        if (revenue > 0) {
            revenueTxt.setAnimationDuration(500)
                    .countAnimation(0, revenue);


        } else {
            revenueTxt.setText("0");
        }
        if (rides > 0) {
            noOfRideTxt.setAnimationDuration(500)
                    .countAnimation(0, rides);

        } else {
            noOfRideTxt.setText("0");
        }
        if (cancel > 0) {
            cancelTxt.setAnimationDuration(500)
                    .countAnimation(0, cancel);
        } else {
            cancelTxt.setText("0");
        }
        scheduleTxt.startAnimation(txtAnim);
        revenueTxt.startAnimation(txtAnim);
        noOfRideTxt.startAnimation(txtAnim);
        cancelTxt.startAnimation(txtAnim);
        currencyTxt.setText(SharedHelper.getKey(getContext(), "currency"));
    }

    public void getProviderSummary() {

            final CustomDialog customDialog = new CustomDialog(getActivity());
            customDialog.show();
            JSONObject object = new JSONObject();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.SUMMARY, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    customDialog.dismiss();
                    Log.v("summary", response.toString());
                    cardLayout.setVisibility(View.VISIBLE);
                    Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
                    cardLayout.startAnimation(slideUp);
                    rides = Integer.parseInt(response.optString("rides"));
                    schedule = Integer.parseInt(response.optString("scheduled_rides"));
                    cancel = Integer.parseInt(response.optString("cancel_rides"));
                    doubleRevenue = Double.parseDouble(response.optString("revenue"));
                    revenue = doubleRevenue.intValue();

//                    paybleamout.setText(SharedHelper.getKey(getContext(), "currency") + " " + response.optString("amountcopay"));
                    Totalrevenue.setText(SharedHelper.getKey(getContext(), "currency") + " " + response.optString("revenue"));
                    servicecharges.setText("-" + SharedHelper.getKey(getContext(), "currency") + " " + response.optString("serch"));
                    cashcollected.setText(SharedHelper.getKey(getContext(), "currency") + " " + response.optString("cashpayment"));
                    payorcollect.setText(SharedHelper.getKey(getContext(), "currency") + " " + response.optString("withdraw"));

                    //revenue = Integer.parseInt(response.optString("revenue"));

                    slideUp.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            setDetails();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });


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
                                    displayMessage(errorObj.optString("message"));
                                } catch (Exception e) {
                                    displayMessage(getString(R.string.something_went_wrong));
                                    e.printStackTrace();
                                }
                            } else if (response.statusCode == 401) {
                                GoToBeginActivity();
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
                            e.printStackTrace();
                        }

                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            getProviderSummary();
                        }
                    }
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + SharedHelper.getKey(getContext(), "access_token"));
                    Log.e("", "Access_Token" + SharedHelper.getKey(getContext(), "access_token"));
                    return headers;
                }
            };

            DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }
    public void getProfile() {

        JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API, object, new Response.Listener<JSONObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject response) {
                String currency = SharedHelper.getKey(getActivity(), "currency");
                SharedHelper.putKey(getActivity(), "today_rev", currency + Math.round(Float.parseFloat(response.optString("wallet"))));
                paybleamout.setText(currency + Math.round(Float.parseFloat(response.optString("wallet"))));
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
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                Log.e("TAG", "getHeaders: Token" + SharedHelper.getKey(getActivity(), "access_token") + SharedHelper.getKey(getActivity(), "token_type"));
                headers.put("Authorization", "" + "Bearer" + " " + SharedHelper.getKey(getActivity(), "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }
    public void displayMessage(String toastString) {
        Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
               .setAction("Action", null).setTextColor(Color.WHITE).show();
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(getContext(), "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(getContext(), WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        getActivity().finish();
    }

}
