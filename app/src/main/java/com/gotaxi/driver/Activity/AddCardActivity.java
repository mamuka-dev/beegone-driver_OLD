package com.gotaxi.driver.Activity;

import static com.google.android.material.snackbar.Snackbar.LENGTH_SHORT;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.braintreepayments.cardform.view.CardForm;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.MyButton;
import com.gotaxi.driver.Utilities.Utilities;
import com.koushikdutta.ion.Ion;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class AddCardActivity extends AppCompatActivity {

    Activity activity;
    Context context;
    ImageView backArrow;
    MyButton addCard;
    CardForm cardForm;
    String Card_Token = "";
    CustomDialog customDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Mytheme);
        setContentView(R.layout.activity_add_card);
        findViewByIdAndInitialize();


        backArrow.setOnClickListener(view -> onBackPressed());

        addCard.setOnClickListener(view -> {
            customDialog = new CustomDialog(AddCardActivity.this);
            customDialog.show();
            if (cardForm.getCardNumber() == null || cardForm.getExpirationMonth() == null || cardForm.getExpirationYear() == null || cardForm.getCvv() == null) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                displayMessage(context.getResources().getString(R.string.enter_card_details));
            } else {
                if (cardForm.getCardNumber().equals("") || cardForm.getExpirationMonth().equals("") || cardForm.getExpirationYear().equals("") || cardForm.getCvv().equals("")) {
                    if ((customDialog != null) && (customDialog.isShowing()))
                        customDialog.dismiss();
                    displayMessage(context.getResources().getString(R.string.enter_card_details));
                } else {
                    String cardNumber = cardForm.getCardNumber();
                    int month = Integer.parseInt(cardForm.getExpirationMonth());
                    int year = Integer.parseInt(cardForm.getExpirationYear());
                    String cvv = cardForm.getCvv();
                    Utilities.print("MyTest", "CardDetails Number: " + cardNumber + "Month: " + month + " Year: " + year);

                    Card card = new Card(cardNumber, month, year, cvv);
                    Stripe stripe = new Stripe(context, SharedHelper.getKey(context, "stripe_publishable_key"));
                    stripe.createToken(
                            card,
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    // Send token to your server
                                    Utilities.print("CardToken:", " " + token.getId());
                                    Utilities.print("CardToken:", " " + token.getCard().getLast4());
                                    Card_Token = token.getId();
                                    addCardToAccount(Card_Token);
                                }

                                public void onError(Exception error) {
                                    // Show localized error message
                                    displayMessage(context.getResources().getString(R.string.enter_card_details));
                                    if ((customDialog != null) && (customDialog.isShowing()))
                                        customDialog.dismiss();
                                }
                            }
                    );
                }

            }
        });

    }


    public void findViewByIdAndInitialize() {
        backArrow = findViewById(R.id.backArrow);
//        help_month_and_year = (ImageView)findViewById(R.id.help_month_and_year);
//        help_cvv = (ImageView)findViewById(R.id.help_cvv);
        addCard = findViewById(R.id.addCard);
//        cardNumber = (EditText) findViewById(R.id.cardNumber);
//        cvv = (EditText) findViewById(R.id.cvv);
//        month_and_year = (EditText) findViewById(R.id.monthAndyear);
        context = AddCardActivity.this;
        activity = AddCardActivity.this;
        cardForm = findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(false)
                .mobileNumberRequired(false)
                .actionLabel("Add CardDetails")
                .setup(activity);
    }

    public void addCardToAccount(final String cardToken) {

        JsonObject json = new JsonObject();
        json.addProperty("stripe_token", cardToken);

        Ion.with(this)
                .load(URLHelper.ADD_CARD_TO_ACCOUNT_API)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Authorization", SharedHelper.getKey(AddCardActivity.this, "token_type") + " " + SharedHelper.getKey(context, "access_token"))
                .setJsonObjectBody(json)
                .asString()
                .withResponse()
                .setCallback((e, response) -> {

                    Utilities.print("Card  token response", response.toString());
                    // response contains both the headers and the string result
                    if ((customDialog != null) && (customDialog.isShowing()))
                        customDialog.dismiss();

                    if (e != null) {
                        displayMessage( e.toString());
                        Utilities.print("Card token error", e.toString());

                        if (e instanceof NetworkErrorException) {
                            displayMessage(context.getResources().getString(R.string.please_try_again));
                        }
                        if (e instanceof TimeoutException) {
                            addCardToAccount(cardToken);
                        }
                        return;
                    }

                    if (response.getHeaders().code() == 200) {
                        try {
                            Utilities.print("SendRequestResponse", response.toString());

                            JSONObject jsonObject = new JSONObject(response.getResult());
                            Toast.makeText(AddCardActivity.this, jsonObject.optString("message").isEmpty()?jsonObject.optString("status"):jsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                            // onBackPressed();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("isAdded", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        customDialog.dismiss();
                    } else if (response.getHeaders().code() == 401) {
                        customDialog.dismiss();
                        refreshAccessToken();
                    }
                });


    }


    @SuppressLint("WrongConstant")
    public void displayMessage(String toastString) {
        Snackbar.make(Objects.requireNonNull(getCurrentFocus()), toastString, LENGTH_SHORT)
                .setAction("Action", null).show();
        // Toast.makeText(context, ""+toastString, Toast.LENGTH_SHORT).show();
    }

    private void refreshAccessToken() {


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

            Log.v("SignUpResponse", response.toString());
            SharedHelper.putKey(context, "access_token", response.optString("access_token"));
            SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
            SharedHelper.putKey(context, "token_type", response.optString("token_type"));
            addCardToAccount(Card_Token);
        }, error -> {
            NetworkResponse response = error.networkResponse;

            if (response != null && response.data != null) {
                SharedHelper.putKey(context, "loggedIn", context.getResources().getString(R.string.False));
                GoToBeginActivity();
            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    refreshAccessToken();
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


    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }


}
