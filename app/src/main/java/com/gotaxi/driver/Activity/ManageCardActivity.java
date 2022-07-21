package com.gotaxi.driver.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.ConnectionHelper;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.Models.CardDetails;
import com.gotaxi.driver.Models.CardInfo;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.Utilities;
import com.gotaxi.driver.adapter.NewPaymentListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageCardActivity extends AppCompatActivity implements NewPaymentListAdapter.ClickListenerInterface {

    private final int ADD_CARD_CODE = 435;
    Activity activity;
    Context context;
    CustomDialog customDialog;
    TextView addCard;
    ListView payment_list_view;
    ArrayList<JSONObject> listItems;
    NewPaymentListAdapter paymentAdapter;
    TextView empty_text;
    JSONObject cardObject = new JSONObject();

    LinearLayout cashLayout;

    //Internet
    ConnectionHelper helper;
    Boolean isInternet;

    ImageView backArrow;

    private ArrayList<CardDetails> cardArrayList;

    public ManageCardActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_card);
        context = ManageCardActivity.this;
        activity = ManageCardActivity.this;
        findViewByIdAndInitialize();
        getCardList();

        backArrow.setOnClickListener(view -> finish());

        addCard.setOnClickListener(view -> GoToAddCard());

        cashLayout.setOnClickListener(view -> {
            CardInfo cardInfo = new CardInfo();
            cardInfo.setLastFour("CASH");
            Intent intent = new Intent();
            intent.putExtra("card_info", cardInfo);
            setResult(RESULT_OK, intent);
            finish();
        });

        payment_list_view.setOnItemClickListener((parent, view, position, id) -> {



        });



    }

    private void DeleteCardDailog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.are_you_sure))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    deleteCard();
                })
                .setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    public void deleteCard() {
        customDialog = new CustomDialog(context);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("card_id", cardObject.optString("card_id"));
            object.put("_method", "DELETE");

        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.DELETE_CARD_FROM_ACCOUNT_API, object, response -> {
            Utilities.print("SendRequestResponse", response.toString());
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            getCardList();
            cardObject = new JSONObject();
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {

                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));

                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            displayMessage(errorObj.getString("message"));
                        } catch (Exception e) {
                            displayMessage(errorObj.optString("error"));
                        }
                        Utilities.print("MyTest", "" + errorObj.toString());
                    } else if (response.statusCode == 401) {
                        refreshAccessToken("DELETE_CARD");
                    } else if (response.statusCode == 422) {

                        json = DriverApplication.trimMessage(new String(response.data));
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
                    deleteCard();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void getCardList() {

        customDialog = new CustomDialog(context);
        if (customDialog != null)
            customDialog.show();

        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLHelper.CARD_PAYMENT_LIST, response -> {

            Utilities.print("GetPaymentList", response.toString());
            if (response != null && response.length() > 0) {
                listItems = getArrayListFromJSONArray(response);
                if (listItems.isEmpty()) {
                    //empty_text.setVisibility(View.VISIBLE);
                    payment_list_view.setVisibility(View.GONE);
                } else {
                    //empty_text.setVisibility(View.GONE);
                    payment_list_view.setVisibility(View.VISIBLE);
                }

                cardArrayList = new ArrayList<>();
                for (JSONObject jsonObject : listItems) {
                    Gson gson = new Gson();
                    CardDetails card = gson.fromJson(jsonObject.toString(), CardDetails.class);
                    card.setSelected("false");
                    card.setSelected("true");
                    cardArrayList.add(card);
                }

                Log.e("", "onResponse: " + cardArrayList.toString());
                paymentAdapter = new NewPaymentListAdapter(context, cardArrayList, activity,ManageCardActivity.this);
                payment_list_view.setAdapter(paymentAdapter);
            } else {
                //empty_text.setVisibility(View.VISIBLE);
                payment_list_view.setVisibility(View.GONE);
            }
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
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
                        refreshAccessToken("PAYMENT_LIST");
                    } else if (response.statusCode == 422) {

                        json = DriverApplication.trimMessage(new String(response.data));
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
                    getCardList();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    private void refreshAccessToken(final String tag) {


        JSONObject object = new JSONObject();
        try {
            object.put("grant_type", "refresh_token");
            object.put("client_id", URLHelper.client_id);
            object.put("client_secret", URLHelper.client_secret);
            object.put("refresh_token", SharedHelper.getKey(context, "refresh_token"));
            object.put("scope", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, response -> {
            Utilities.print("SignUpResponse", response.toString());
            SharedHelper.putKey(context, "access_token", response.optString("access_token"));
            SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
            SharedHelper.putKey(context, "token_type", response.optString("token_type"));
            if (tag.equalsIgnoreCase("PAYMENT_LIST")) {
                getCardList();
            }
           else if (tag.equalsIgnoreCase("DEFAULT_CARD")) {
                setDefaultCard();
            }
            else {
                deleteCard();
            }
        }, error -> {
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                GoToBeginActivity();
            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    refreshAccessToken(tag);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };
        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }


    private ArrayList<JSONObject> getArrayListFromJSONArray(JSONArray jsonArray) {
        ArrayList<JSONObject> aList = new ArrayList<>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    aList.add(jsonArray.getJSONObject(i));
                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }

        return aList;

    }

    public void findViewByIdAndInitialize() {
        addCard = findViewById(R.id.addCard);
        payment_list_view = findViewById(R.id.payment_list_view);
        empty_text = findViewById(R.id.empty_text);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        cashLayout = findViewById(R.id.cash_layout);
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(view -> onBackPressed());
    }

    public void displayMessage(String toastString) {
        try {
            if (getCurrentFocus() != null)
                Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void GoToAddCard() {
        Intent mainIntent = new Intent(activity, AddCardActivity.class);
        startActivityForResult(mainIntent, ADD_CARD_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CARD_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean result = data.getBooleanExtra("isAdded", false);
                if (result) {
                    getCardList();
                }
            }
        }
    }

    @Override
    public void itemClick(int id,int position) {
        String json = new Gson().toJson(paymentAdapter.getItem(position));

        try {
            cardObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utilities.print("MyTest", "" + paymentAdapter.getItem(position));
        if(R.id.img_del==id){ //delete card
            DeleteCardDailog();
        }
        else { //select default card

                setDefaultCard();
        }
    }

    private void setDefaultCard() {
        customDialog = new CustomDialog(context);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("card_id", cardObject.optString("card_id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CHANGE_DEFAULT_CARD, object, response -> {
            Utilities.print("SendRequestResponse", response.toString());
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            getCardList();
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {

                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));

                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            displayMessage(errorObj.getString("message"));
                        } catch (Exception e) {
                            displayMessage(errorObj.optString("error"));
                        }
                        Utilities.print("MyTest", "" + errorObj.toString());
                    } else if (response.statusCode == 401) {
                        refreshAccessToken("DEFAULT_CARD");
                    } else if (response.statusCode == 422) {

                        json = DriverApplication.trimMessage(new String(response.data));
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
                    setDefaultCard();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }
}
