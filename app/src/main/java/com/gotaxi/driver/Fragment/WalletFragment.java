package com.gotaxi.driver.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.gotaxi.driver.Activity.WelcomeScreenActivity;
import com.gotaxi.driver.Helper.ConnectionHelper;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.R;


public class WalletFragment extends Fragment {

    Activity activity;
    Context context;
    ConnectionHelper helper;
    Boolean isInternet;
    View rootView;

    public WalletFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
        findViewByIdAndInitialize();
        return rootView;
    }

    public void findViewByIdAndInitialize() {
        helper = new ConnectionHelper(activity);
        isInternet = helper.isConnectingToInternet();
    }

    public void displayMessage(String toastString) {
        Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
               .setAction("Action", null).setTextColor(Color.WHITE).show();
    }

    public void GoToBeginActivity() {
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
}
