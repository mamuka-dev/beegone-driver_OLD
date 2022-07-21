package com.gotaxi.driver.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.gotaxi.driver.API.RetrofitClient;
import com.gotaxi.driver.Activity.WelcomeScreenActivity;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.Model.BankAccountResponse;
import com.gotaxi.driver.Models.Errorresponse;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.Utilities;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

import static com.gotaxi.driver.DriverApplication.trimMessage;

public class ManageBankAccountFragment extends Fragment {
    private static DecimalFormat df = new DecimalFormat("0.00");
    private Dialog myDialog;
    private ImageView edit_bank, provider_img, backArrow;
    private CardView view_bank;
    private FrameLayout bottom_sheet;
    private BottomSheetBehavior sheetBehavior;
    private TextView acc_pendingwith, acc_revenue, acc_withdrawn, acc_currentbalance, provider_name;
    private TextView b_bankname, b_ifsc, b_micr, b_accountholdername, b_accountnum, b_type;
    private EditText b_banknamei, b_ifsci, b_micri, b_accountholdernamei, b_accountnumi, b_typei;
    private OnFragmentInteractionListener mListener;
    private Button update_bank, withdrawAmountBtn;
    private int canWithdraw = 1;

    public ManageBankAccountFragment() {

    }

    public static ManageBankAccountFragment newInstance(String param1, String param2) {
        ManageBankAccountFragment fragment = new ManageBankAccountFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint({"SetTextI18n", "CheckResult"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_bank_account, container, false);
        myDialog = new Dialog(requireActivity());
        update_bank = view.findViewById(R.id.update_bank);
        provider_img = view.findViewById(R.id.provider_img);
        backArrow = view.findViewById(R.id.backArrow);
        provider_name = view.findViewById(R.id.provider_name);
        b_bankname = view.findViewById(R.id.b_bankname);
        withdrawAmountBtn = view.findViewById(R.id.with_amount);
        b_ifsc = view.findViewById(R.id.b_ifsc);
        b_accountholdername = view.findViewById(R.id.b_accountholdername);
        b_accountnum = view.findViewById(R.id.b_accnumber);
        b_type = view.findViewById(R.id.b_type);
        b_banknamei = view.findViewById(R.id.b_banknamei);
        b_ifsci = view.findViewById(R.id.b_ifsci);
        b_accountholdernamei = view.findViewById(R.id.b_accountholdernamei);
        b_accountnumi = view.findViewById(R.id.b_accountnumi);
        b_typei = view.findViewById(R.id.b_typei);
        edit_bank = view.findViewById(R.id.edit_bank);
        view_bank = view.findViewById(R.id.view_bank_account);
        bottom_sheet = view.findViewById(R.id.bottom_sheet);
        acc_pendingwith = view.findViewById(R.id.acc_pendingwith);
        acc_revenue = view.findViewById(R.id.acc_revenue);
        acc_withdrawn = view.findViewById(R.id.acc_withdrawn);
        acc_currentbalance = view.findViewById(R.id.acc_currentbalance);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        Glide.with(getActivity()).load(SharedHelper.getKey(getActivity(), "picture")).placeholder(getResources().getDrawable(R.drawable.img_profile_placehodler)).into(provider_img);
        provider_name.setText(SharedHelper.getKey(getActivity(), "first_name") + " " + SharedHelper.getKey(getActivity(), "last_name"));
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.hideKeyboard(getActivity());
                Fragment fragment = new SettingFragment();

                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
            }
        });
        withdrawAmountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canWithdraw == 1) {
                    if (SharedHelper.getKey(getActivity(), "currentbalance") != null && !SharedHelper.getKey(getActivity(), "currentbalance").equals("0.00")) {
                        final CustomDialog customDialog = new CustomDialog(getActivity());
                        customDialog.show();
                        Call<Errorresponse> call = RetrofitClient.getInstance().getApi().addwithdraw(SharedHelper.getKey(getActivity(), "id"), SharedHelper.getKey(getActivity(), "bid"), SharedHelper.getKey(getActivity(), "currentbalance"));
                        call.enqueue(new Callback<Errorresponse>() {
                            @Override
                            public void onResponse(Call<Errorresponse> call, retrofit2.Response<Errorresponse> response) {
                                if (response.isSuccessful()) {
                                    if (response.body().isError()) {
                                        Toast.makeText(getActivity(), "Withdraw Failed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        ShowPopuplogout();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Errorresponse> call, Throwable t) {
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.w_am_not_available), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.b_details_not_available), Toast.LENGTH_SHORT).show();
                }

            }
        });
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        view_bank.setVisibility(View.GONE);

                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Utilities.hideKeyboard(getActivity());
                        view_bank.setVisibility(View.VISIBLE);

                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        update_bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatebankaccount();
            }
        });
        edit_bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_bank.setVisibility(View.GONE);
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        getProviderSummary();
        return view;
    }

    private void updatebankaccount() {
        final CustomDialog customDialog = new CustomDialog(getActivity());
        customDialog.show();
        Call<Errorresponse> call = RetrofitClient.getInstance().getApi().addbankaccount(
                b_accountholdernamei.getText().toString(),
                b_typei.getText().toString(),
                b_banknamei.getText().toString(),
                b_accountnumi.getText().toString(),
                b_ifsci.getText().toString(),
                "",
                SharedHelper.getKey(getActivity(), "id"),
                "0", "0");
        call.enqueue(new Callback<Errorresponse>() {
            @Override
            public void onResponse(Call<Errorresponse> call, retrofit2.Response<Errorresponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().isError()) {
                        Toast.makeText(getActivity(), "Bank Account Update Failed", Toast.LENGTH_SHORT).show();
                    } else {
                        canWithdraw = 1;
                        getbankaccount();
                        Toast.makeText(getActivity(), "Bank Account Success", Toast.LENGTH_SHORT).show();
                    }
                }
                customDialog.dismiss();
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                view_bank.setVisibility(View.VISIBLE);
                Utilities.hideKeyboard(getActivity());
            }

            @Override
            public void onFailure(Call<Errorresponse> call, Throwable t) {
                customDialog.dismiss();
            }
        });
    }


    private void getbankaccount() {
        final CustomDialog customDialog = new CustomDialog(getActivity());
        if (!customDialog.isShowing())
            customDialog.show();
        Call<BankAccountResponse> call = RetrofitClient.getInstance().getApi().getbankaccount(SharedHelper.getKey(getActivity(), "id"));
        call.enqueue(new Callback<BankAccountResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<BankAccountResponse> call, retrofit2.Response<BankAccountResponse> response) {
                if (response.isSuccessful()) {
                    BankAccountResponse resp = response.body();
                    if (resp.getBankaccount().getPending() != null) {
                        BigDecimal amount = new BigDecimal(resp.getBankaccount().getPending());
                        BigDecimal roundOffAmount = amount.setScale(2, BigDecimal.ROUND_HALF_EVEN);

                        acc_pendingwith.setText(SharedHelper.getKey(getActivity(), "currency") + roundOffAmount);
                    }
                    if (resp.getBankaccount().getPaid() != null) {
                        BigDecimal amount = new BigDecimal(resp.getBankaccount().getPaid());
                        BigDecimal roundOffAmount = amount.setScale(2, BigDecimal.ROUND_HALF_EVEN);

                        acc_withdrawn.setText(SharedHelper.getKey(getActivity(), "currency") + roundOffAmount);
                    }
                    if(resp.getBankaccount().getTotal() != null){
                        SharedHelper.putKey(getActivity(), "currentbalance",
                                df.format(Double.parseDouble(SharedHelper.getKey(getActivity(), "totalrev")) - Double.parseDouble(resp.getBankaccount().getTotal())));
                        acc_currentbalance.setText(SharedHelper.getKey(getActivity(), "currency") +
                                df.format(Double.parseDouble(SharedHelper.getKey(getActivity(), "totalrev")) - Double.parseDouble(resp.getBankaccount().getTotal())));
                    }
                    if(resp.getBankaccount().getBid() != null){
                        SharedHelper.putKey(getActivity(), "bid", resp.getBankaccount().getBid());
                    }


                    if (resp.getBankaccount().getBank_name() != null)
                        b_bankname.setText(resp.getBankaccount().getBank_name());
                    else {
                        canWithdraw = 0;
                        b_bankname.setHint(getResources().getString(R.string.not_available));
                    }


                    if (resp.getBankaccount().getIFSC_code() != null)
                        b_ifsc.setText(resp.getBankaccount().getIFSC_code());
                    else {
                        canWithdraw = 0;
                        b_ifsc.setHint(getResources().getString(R.string.not_available));
                    }


                    if (resp.getBankaccount().getAccount_number() != null)
                        b_accountnum.setText(resp.getBankaccount().getAccount_number());
                    else {
                        canWithdraw = 0;
                        b_accountnum.setHint(getResources().getString(R.string.not_available));
                    }

                    if (resp.getBankaccount().getAccount_name() != null)
                        b_accountholdername.setText(resp.getBankaccount().getAccount_name());
                    else {
                        canWithdraw = 0;
                        b_accountholdername.setHint(getResources().getString(R.string.not_available));
                    }

                    if (resp.getBankaccount().getType() != null)
                        b_type.setText(resp.getBankaccount().getType());
                    else {
                        canWithdraw = 0;
                        b_type.setHint(getResources().getString(R.string.not_available));
                    }


                    if (resp.getBankaccount().getBank_name() != null)
                        b_banknamei.setText(resp.getBankaccount().getBank_name());
                    else {
                        canWithdraw = 0;
                    }

                    if (resp.getBankaccount().getIFSC_code() != null)
                        b_ifsci.setText(resp.getBankaccount().getIFSC_code());
                    else {
                        canWithdraw = 0;
                    }


                    if (resp.getBankaccount().getAccount_number() != null)
                        b_accountnumi.setText(resp.getBankaccount().getAccount_number());
                    else {
                        canWithdraw = 0;
                    }


                    if (resp.getBankaccount().getAccount_name() != null)
                        b_accountholdernamei.setText(resp.getBankaccount().getAccount_name());
                    else {
                        canWithdraw = 0;
                    }


                    if (resp.getBankaccount().getType() != null)
                        b_typei.setText(resp.getBankaccount().getType());
                    else {
                        canWithdraw = 0;
                    }

                }
                customDialog.dismiss();
            }

            @Override
            public void onFailure(Call<BankAccountResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "" + t, Toast.LENGTH_SHORT).show();
                customDialog.dismiss();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

    @SuppressLint("SetTextI18n")
    public void ShowPopuplogout() {
        myDialog.setContentView(R.layout.dialog_withdraw_success);
        TextView email = myDialog.findViewById(R.id.popemail);
        TextView datetext = myDialog.findViewById(R.id.popdata);
        TextView name = myDialog.findViewById(R.id.popfullname);
        TextView time = myDialog.findViewById(R.id.poptime);
        TextView amount = myDialog.findViewById(R.id.popamount);

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat2 = new SimpleDateFormat("HH:mm");
        Date currentTime = Calendar.getInstance().getTime();

        email.setText(SharedHelper.getKey(getActivity(), "email"));
        datetext.setText(dateFormat.format(currentTime));
        time.setText(dateFormat2.format(currentTime));
        name.setText(SharedHelper.getKey(getActivity(), "first_name") + " " + SharedHelper.getKey(getActivity(), "last_name"));
        amount.setText(SharedHelper.getKey(getActivity(), "currency") + " " + SharedHelper.getKey(getActivity(), "currentbalance"));
        FloatingActionButton popdismiss = myDialog.findViewById(R.id.popdismiss);
        popdismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                getbankaccount();
            }
        });
        Objects.requireNonNull(myDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.closeOptionsMenu();
        myDialog.show();
    }

    public void getProviderSummary() {

            final CustomDialog customDialog = new CustomDialog(getActivity());
            customDialog.show();
            JSONObject object = new JSONObject();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.SUMMARY, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v("summary", response.toString());
                    customDialog.dismiss();
                    SharedHelper.putKey(getActivity(), "totalrev", response.optString("withdraw"));
                    acc_revenue.setText(SharedHelper.getKey(getContext(), "currency") + response.optString("withdraw"));
                    getbankaccount();
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
