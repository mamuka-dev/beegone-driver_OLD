package com.gotaxi.driver.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import com.facebook.FacebookSdk;
import com.facebook.accountkit.AccountKit;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gotaxi.driver.Helper.AutoStartHelper;
import com.squareup.picasso.Picasso;
import com.gotaxi.driver.API.RetrofitClient;
import com.gotaxi.driver.Bean.Connect;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Fragment.EarningsFragment;
import com.gotaxi.driver.Fragment.SupportFragment;
import com.gotaxi.driver.Fragment.HomeFragment;
import com.gotaxi.driver.Fragment.OnGoingTrips;
import com.gotaxi.driver.Fragment.PastTrips;
import com.gotaxi.driver.Fragment.SummaryFragment;
import com.gotaxi.driver.Fragment.SettingFragment;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.LanguageData;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.Listeners.ConnectionBooleanChangedListener;
import com.gotaxi.driver.Models.ConstData;
import com.gotaxi.driver.Models.ConstDataResponse;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.Utilities;
import com.gotaxi.driver.adapter.LanguageAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

import static com.gotaxi.driver.DriverApplication.trimMessage;

public class MainActivity extends AppCompatActivity {
    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_YOURTRIPS = "yourtrips";
    private static final String TAG_SUMMARY = "summary";
    private static final String TAG_HELP = "help";
    private static final String TAG_EARNINGS = "earnings";
    private static final String TAG_SHARE = "share";
    private static final String TAG_LOGOUT = "logout";
    public static FragmentManager fragmentManager;
    // index to identify current nav menu item
    public int navItemIndex = 0;
    public String CURRENT_TAG = TAG_HOME;
    Fragment fragment;
    Activity activity;
    Context context;
    Toolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView btnavigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgProfile;
    private TextView txtName, approvaltxt;
    private ImageView status;
    private Dialog alertDialog;
    LanguageData languageData;
    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private static final int REQUEST_LOCATION = 1450;
    Utilities utils = new Utilities();
    boolean push = false;
    Button btnFusedLocation;
    TextView tvLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    String[] key = new String[1];
    final String[][] value = new String[1][1];

    private static final int APP_PERMISSION_REQUEST = 102;
    private NotificationManager notificationManager;
    private boolean mAlreadyStartedService = false;

    HomeFragment lFrag;

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        context = getApplicationContext();
        SharedPreferences sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        String autoStart = sharedpreferences.getString("autoStart", "");
        if (autoStart.equals("")) {
            AutoStartHelper.getInstance().getAutoStartPermission(this);
        }
        FirebaseMessaging.getInstance().subscribeToTopic("driver");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (SharedHelper.getKey(context, "login_by").equals("facebook"))
            FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        findViewById();
        Bundle extras = getIntent().getExtras();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        if (extras != null) {
            push = extras.getBoolean("push");
        }

        map();
        Connect.addMyBooleanListener(new ConnectionBooleanChangedListener() {
            @Override
            public void OnMyBooleanChanged() {
                Toast.makeText(getApplication(), "Changed", Toast.LENGTH_SHORT).show();
            }
        });
        loadNavHeader();
        setUpNavigationView();
        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawers();
                startActivity(new Intent(activity, Profile.class));
            }
        });

        TextView drive_link = navigationView.findViewById(R.id.drive_link);

        drive_link.setOnClickListener(v -> {
            String url = SharedHelper.getKey(context, "f_u_url");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        getconstdata();


    }


    private void findViewById() {
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navHeader = navigationView.getHeaderView(0);
        txtName = navHeader.findViewById(R.id.usernameTxt);
        imgProfile = navHeader.findViewById(R.id.img_profile);
        status = navHeader.findViewById(R.id.status);

        navigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));


    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        drawer.closeDrawers();
                        startActivity(new Intent(context, MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_subscription:
                        startActivity(new Intent(context, SubscriptionActivity.class));
                        break;
                    case R.id.nav_schedule_ride:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_SUMMARY;
                        fragment = new OnGoingTrips();
                        drawer.closeDrawers();
                        FragmentManager manager2 = getSupportFragmentManager();
                        @SuppressLint("CommitTransaction")
                        FragmentTransaction transaction1 = manager2.beginTransaction();
                        transaction1.replace(R.id.content, fragment);
                        transaction1.addToBackStack(null);
                        transaction1.commit();
                        break;
                    case R.id.nav_yourtrips:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_EARNINGS;
                        fragment = new PastTrips();
                        drawer.closeDrawers();
                        FragmentManager manager6 = getSupportFragmentManager();
                        @SuppressLint("CommitTransaction")
                        FragmentTransaction transaction7 = manager6.beginTransaction();
                        transaction7.replace(R.id.content, fragment);
                        transaction7.addToBackStack(null);
                        transaction7.commit();
                        break;
                    case R.id.nav_summary:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_SUMMARY;
                        fragment = new SummaryFragment();
                        drawer.closeDrawers();
                        FragmentManager manager3 = getSupportFragmentManager();
                        @SuppressLint("CommitTransaction")
                        FragmentTransaction transaction2 = manager3.beginTransaction();
                        transaction2.replace(R.id.content, fragment);
                        transaction2.addToBackStack(null);
                        transaction2.commit();
                        //GoToFragment();
                        break;
                    case R.id.nav_help:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_HELP;
                        fragment = new SupportFragment();
                        drawer.closeDrawers();
                        FragmentManager manager4 = getSupportFragmentManager();
                        @SuppressLint("CommitTransaction")
                        FragmentTransaction transaction3 = manager4.beginTransaction();
                        transaction3.replace(R.id.content, fragment);
                        transaction3.addToBackStack(null);
                        transaction3.commit();
                        //GoToFragment();
                        break;
                    case R.id.nav_wallets:
                        Intent intent = new Intent(MainActivity.this, WalletActivity.class);
                        startActivity(intent);
                        //GoToFragment();
                        break;
                    case R.id.nav_cards:
                        startActivity(new Intent(context, ManageCardActivity.class));
                        break;
                    case R.id.nav_earnings:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_EARNINGS;
                        fragment = new EarningsFragment();
                        drawer.closeDrawers();
                        FragmentManager manager1 = getSupportFragmentManager();
                        @SuppressLint("CommitTransaction")
                        FragmentTransaction transaction4 = manager1.beginTransaction();
                        transaction4.replace(R.id.content, fragment);
                        transaction4.addToBackStack(null);
                        transaction4.commit();
                        break;

                    case R.id.nav_settings:
                        navItemIndex = 5;
                        CURRENT_TAG = TAG_EARNINGS;
                        fragment = new SettingFragment();
                        drawer.closeDrawers();
                        FragmentManager manager5 = getSupportFragmentManager();
                        @SuppressLint("CommitTransaction")
                        FragmentTransaction transaction5 = manager5.beginTransaction();
                        transaction5.replace(R.id.content, fragment);
                        transaction5.addToBackStack(null);
                        transaction5.commit();
                        break;

                    default:
                        navItemIndex = 0;
                }


                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    loadNavHeader();
                }
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        Menu m = navigationView.getMenu();
        if(checkIsOnlyCash()){
            m.findItem(R.id.nav_cards).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_subscription).setVisible(false);
        }
    }
    private boolean checkIsOnlyCash() {
        return (SharedHelper.getKey(context, "CARD").equalsIgnoreCase("0") && SharedHelper.getKey(context, "paypal").equalsIgnoreCase("0") &&
                SharedHelper.getKey(context, "rave").equalsIgnoreCase("0") && SharedHelper.getKey(context, "UPI").equalsIgnoreCase("0") &&
                SharedHelper.getKey(context, "razor").equalsIgnoreCase("0")
        ) ;

    }
    public void getconstdata() {
        CustomDialog customDialog = new CustomDialog(this);
        customDialog.show();
        Call<ConstDataResponse> call = RetrofitClient.getInstance().getApi().getconstdata();
        call.enqueue(new Callback<ConstDataResponse>() {
            @Override
            public void onResponse(Call<ConstDataResponse> call, retrofit2.Response<ConstDataResponse> response) {
                if (response.isSuccessful()) {
                    List<ConstData> constData;
                    ConstDataResponse constDataResponse = response.body();
                    constData = constDataResponse.getData();

                    key = new String[constData.size()];
                    value[0] = new String[constData.size()];

                    for (int i = 0; i < constData.size(); i++) {
                        key[i] = constData.get(i).getKey();
                        value[0][i] = constData.get(i).getValue();

                        SharedHelper.putKey(context, "appmaintain", "0");

                        if (key[i].equals("appmaintain"))
                            SharedHelper.putKey(context, "appmaintain", value[0][i]);


                        if (SharedHelper.getKey(context, "appmaintain").equals("1")) {
                            AppMaintainDialogShow("nothing");
                            return;
                        }

                        if (key[i].equals("page_privacy"))
                            SharedHelper.putKey(context, "privacy_page", value[0][i]);

                        if (key[i].equals("f_u_url"))
                            SharedHelper.putKey(context, "f_u_url", value[0][i]);

                        if (key[i].equals("tawk_live"))
                            SharedHelper.putKey(context, "tawk_live", value[0][i]);

                        if (key[i].equals("page_terms"))
                            SharedHelper.putKey(context, "terms_page", value[0][i]);

                        if (key[i].equals("CARD"))
                            SharedHelper.putKey(context, "CARD", value[0][i]);

                        if (key[i].equals("paypal"))
                            SharedHelper.putKey(context, "paypal", value[0][i]);

                        if (key[i].equals("rave"))
                            SharedHelper.putKey(context, "rave", value[0][i]);

                        if (key[i].equals("UPI"))
                            SharedHelper.putKey(context, "UPI", value[0][i]);

                        if (key[i].equals("razor"))
                            SharedHelper.putKey(context, "razor", value[0][i]);

                        if (key[i].equals("stripe_publishable_key"))
                            SharedHelper.putKey(context, "stripe_publishable_key", value[0][i]);

                        if (key[i].equals("paypal_client_id"))
                            SharedHelper.putKey(context, "paypal_client_id", value[0][i]);

                        if (key[i].equals("rave_publicKey"))
                            SharedHelper.putKey(context, "rave_publicKey", value[0][i]);

                        if (key[i].equals("rave_encryptionKey"))
                            SharedHelper.putKey(context, "rave_encryptionKey", value[0][i]);

                        if (key[i].equals("rave_country"))
                            SharedHelper.putKey(context, "rave_country", value[0][i]);

                        if (key[i].equals("rave_currency"))
                            SharedHelper.putKey(context, "rave_currency", value[0][i]);

                        if (key[i].equals("UPI_key"))
                            SharedHelper.putKey(context, "UPI_key", value[0][i]);

                        if (key[i].equals("razor_key"))
                            SharedHelper.putKey(context, "razor_key", value[0][i]);

                        if (key[i].equals("cat_app_ecomony"))
                            SharedHelper.putKey(context, "cat_app_ecomony", value[0][i]);

                        if (key[i].equals("cat_app_lux"))
                            SharedHelper.putKey(context, "cat_app_lux", value[0][i]);

                        if (key[i].equals("cat_app_ext"))
                            SharedHelper.putKey(context, "cat_app_ext", value[0][i]);

                        if (key[i].equals("cat_app_out"))
                            SharedHelper.putKey(context, "cat_app_out", value[0][i]);
                        if (key[i].equals("ride_otp"))
                            SharedHelper.putKey(context, "ride_otp", value[0][i]);
                        if (key[i].equals("subscription_module")){
                            SharedHelper.putKey(context, "subscription_module", value[0][i]);
                            if(value[0][i].equals("1"))
                            navigationView.getMenu().findItem(R.id.nav_subscription).setVisible(true);
                            else
                                navigationView.getMenu().findItem(R.id.nav_subscription).setVisible(false);
                        }



                    }
                    if (customDialog.isShowing())
                        customDialog.dismiss();
                } else {
                    if (customDialog.isShowing())
                        customDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Some Error Occurred ! Please Try Again Later", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ConstDataResponse> call, Throwable t) {
                customDialog.dismiss();
                Toast.makeText(MainActivity.this, "Some Error Occurred ! Please Try Again Later", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void refreshView() {
        startActivity(new Intent(MainActivity.this, MainActivity.class));
        finish();
    }
    public void AppMaintainDialogShow(String txt) {
        Dialog dialog;
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.app_maintain_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.GRAY));
        dialog.closeOptionsMenu();
        dialog.setCancelable(false);
        dialog.show();

        new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            } 
            @Override
            public void onFinish() {
                onBackPressed();
            }
        };
    }

    /*------------------Language Dialog here-------------------------*/

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
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

            LanguageAdapter detailsSizeAdapter = new LanguageAdapter(MainActivity.this, languageDataArrayList);
            langList.setAdapter(detailsSizeAdapter);


            detailsSizeAdapter.setOnItemClickListener(new LanguageAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {

                    alertDialog.dismiss();

                    System.out.println(" Lang_Ccode -- >  " + languageDataArrayList.get(position).getLanguageCode());


                    updateLocale(languageDataArrayList.get(position));
                    /*
                    restart the activity
                     */
                    Intent intent = getIntent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    private void loadNavHeader() {
        // name, website
        txtName.setText(SharedHelper.getKey(context, "first_name") + " " + SharedHelper.getKey(context, "last_name"));
        if (SharedHelper.getKey(context, "approval_status").equals("new") || SharedHelper.getKey(context, "approval_status").equals("onboarding")) {
//            approvaltxt.setTextColor(Color.YELLOW);
//            approvaltxt.setText(getText(R.string.waiting_for_approval));
            status.setImageResource(R.drawable.newuser);
        } else if (SharedHelper.getKey(context, "approval_status").equals("banned")) {
//            approvaltxt.setTextColor(Color.RED);
//            approvaltxt.setText(getText(R.string.banned));
            status.setImageResource(R.drawable.banned);
        } else {
//            approvaltxt.setTextColor(Color.GREEN);
//            approvaltxt.setText(getText(R.string.approved));
            status.setImageResource(R.drawable.approved);
        }
        utils.print("Profile_PIC", "" + SharedHelper.getKey(context, "picture"));

        // Loading profile image
//        Glide.with(this).load(SharedHelper.getKey(context,"picture"))
//                .placeholder(R.drawable.img_profile_placehodler)
//                .error(R.drawable.img_profile_placehodler)
//                .crossFade()
//                .thumbnail(0.5f)
//                .bitmapTransform(new CircleTransform(this))
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(imgProfile);
        //Assign current profile values to the edittext
        //Glide.with(activity).load(SharedHelper.getKey(context,"picture")).placeholder(R.drawable.img_profile_placehodler).error(R.drawable.img_profile_placehodler).into(imgProfile);
        Picasso.get().load(SharedHelper.getKey(context, "picture")).placeholder(R.drawable.img_profile_placehodler).error(R.drawable.img_profile_placehodler).into(imgProfile);

    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
//                if (CURRENT_TAG.equalsIgnoreCase(TAG_SUMMARY) || CURRENT_TAG.equalsIgnoreCase(TAG_EARNINGS)
//                        || CURRENT_TAG.equalsIgnoreCase(TAG_YOURTRIPS)){
//
//                }else{
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                fragment = new HomeFragment();
                GoToFragment();
                return;
//                }
            } else {
                System.exit(0);
            }
        }
        super.onBackPressed();
    }


    private void map() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragment = new HomeFragment();
                FragmentManager manager = getSupportFragmentManager();
                @SuppressLint("CommitTransaction")
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.content, fragment);
                transaction.commit();
                fragmentManager = getSupportFragmentManager();
            }
        });
    }

    public void GoToFragment() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawer.closeDrawers();
                FragmentManager manager = getSupportFragmentManager();
                @SuppressLint("CommitTransaction")
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.content, fragment);
                transaction.commit();
            }
        });
    }

    public void navigateToShareScreen(String shareUrl) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl + " -via " + getString(R.string.app_name));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLoc();
        }
    }

    private void enableLoc() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                        utils.print("Location error", "Location error " + connectionResult.getErrorCode());
                    }
                }).build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });
//	        }

    }


    public void logout() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", SharedHelper.getKey(context, "id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.LOGOUT, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                drawer.closeDrawers();
                if (SharedHelper.getKey(context, "login_by").equals("facebook"))
                    LoginManager.getInstance().logOut();
                if (SharedHelper.getKey(context, "login_by").equals("google"))
                    signOut();
                if (!SharedHelper.getKey(MainActivity.this, "account_kit_token").equalsIgnoreCase("")) {
                    Log.e("MainActivity", "Account kit logout: " + SharedHelper.getKey(MainActivity.this, "account_kit_token"));
                    AccountKit.logOut();
                    SharedHelper.putKey(MainActivity.this, "account_kit_token", "");
                }
                SharedHelper.putKey(context, "current_status", "");
                SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
                SharedHelper.putKey(context, "email", "");
                Intent goToLogin = new Intent(activity, WelcomeScreenActivity.class);
                goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(goToLogin);
                finish();
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
                        logout();
                    }
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                Log.e("getHeaders: Token", SharedHelper.getKey(context, "access_token") + SharedHelper.getKey(context, "token_type"));
                headers.put("Authorization", "" + "Bearer" + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void signOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //taken from google api console (Web api client id)
//                .requestIdToken("795253286119-p5b084skjnl7sll3s24ha310iotin5k4.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {

                FirebaseAuth.getInstance().signOut();
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d("MainAct", "Google User Logged out");
                               /* Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();*/
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d("MAin", "Google API Client Connection Suspended");
            }
        });
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


    @Override
    protected void onStop() {
        super.onStop();
        // startService(new Intent(MainActivity.this, FloatWidgetService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //stopService(new Intent(MainActivity.this, FloatWidgetService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //  startService(new Intent(MainActivity.this, FloatWidgetService.class));
    }


    public void updateLocale(LanguageData languageData) {

        SharedHelper.putKey(context, "language", languageData.getLanguageCode());

        /*
        set language
         */
        setLanguage();

    }


    private void setLanguage() {
        String languageCode = SharedHelper.getKey(MainActivity.this, "language");
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
//        LocaleUtils.setLocale(this, languageCode);
    }


}
