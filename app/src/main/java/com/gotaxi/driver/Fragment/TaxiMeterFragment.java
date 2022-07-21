package com.gotaxi.driver.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.gotaxi.driver.API.RetrofitClient;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.Models.Errorresponse;
import com.gotaxi.driver.R;
import com.gotaxi.driver.Utilities.Utilities;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;

import static com.gotaxi.driver.DriverApplication.trimMessage;


public class TaxiMeterFragment extends Fragment implements com.google.android.gms.location.LocationListener {
    private static DecimalFormat df = new DecimalFormat("0.000");
    private double lastlat = 0;
    private double lastlong = 0;
    private double lastdistance = 0;
    private Button startnow, ride_completed;
    private ImageView backArrow;
    private TextView distancetext, meter_base, meter_distance, meter_total;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;
    private double latitude;
    private double longitude;
    private GifImageView mainimg;
    private String getdist;
    private Handler ha;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean stop = false;

    public TaxiMeterFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taximeter, container, false);
        ha = new Handler();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        startnow = view.findViewById(R.id.start_now);
        backArrow = view.findViewById(R.id.backArrow);
        mainimg = view.findViewById(R.id.mainimg);
        meter_base = view.findViewById(R.id.meter_base);
        meter_distance = view.findViewById(R.id.meter_price);
        meter_total = view.findViewById(R.id.meter_total);
        distancetext = view.findViewById(R.id.distancetext);
        chronometer = view.findViewById(R.id.chronometer);
        ride_completed = view.findViewById(R.id.ride_completed);
        chronometer.setFormat("%s");
        chronometer.setBase(SystemClock.elapsedRealtime());

        meter_base.setText(SharedHelper.getKey(getActivity(), "currency") + "0.00");
        meter_distance.setText(SharedHelper.getKey(getActivity(), "currency") + "0.00");
        meter_total.setText(SharedHelper.getKey(getActivity(), "currency") + "0.00");

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.hideKeyboard(getActivity());
                Fragment fragment = new SettingFragment();

                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
            }
        });
        ride_completed.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                CustomDialog customDialog = new CustomDialog(getActivity());
                customDialog.show();
                Call<Errorresponse> call = RetrofitClient.getInstance().getApi().addtaximeterride(
                        SharedHelper.getKey(getActivity(), "id"),
                        getdist,
                        df.format((Double.valueOf(SharedHelper.getKey(getActivity(), "price")) * lastdistance + Double.valueOf(SharedHelper.getKey(getActivity(), "fixed")))));
                call.enqueue(new Callback<Errorresponse>() {
                    @Override
                    public void onResponse(Call<Errorresponse> call, retrofit2.Response<Errorresponse> response) {
                        if (response.isSuccessful()) {
                            if (!response.body().isError()) {
                                customDialog.dismiss();
                                Toast.makeText(getActivity(), getResources().getString(R.string.ride_completed_successfully), Toast.LENGTH_SHORT).show();
                                meter_base.setText(SharedHelper.getKey(getActivity(), "currency") + "0.00");
                                meter_distance.setText(SharedHelper.getKey(getActivity(), "currency") + "0.00");
                                meter_total.setText(SharedHelper.getKey(getActivity(), "currency") + "0.00");
                            }

                            customDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<Errorresponse> call, Throwable t) {
                        Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_SHORT).show();
                        customDialog.dismiss();
                    }
                });

            }
        });
        startnow.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (!running) {
                    stop = false;
                    mainimg.setImageResource(R.drawable.taximeter_started);
                    startnow.setText("Stop Now");
                    getdistance();
                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    chronometer.start();
                    running = true;
                } else {
                    running = false;
                    stop = true;
                    mainimg.setImageResource(R.drawable.taximeter_startnow);
                    startnow.setText("Start Now");
                    distancetext.setText("0.00 KM");
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.stop();
                    pauseOffset = 0;
                }
            }
        });
        getProfile();
        return view;
    }

    private void tryToGetlastKnowLocation() {

        if (getActivity() == null)
            return;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.

                        if (location != null) {
                            // Logic to handle location object

                            onLocationChanged(location);


                        }
                    }
                });
    }


    @SuppressLint("MissingPermission")
    private void getdistance() {
        getdist = "0.00";
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                //call function
                tryToGetlastKnowLocation();
                ha.postDelayed(this, 1000);
            }
        }, 1000);

    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    public void getProfile() {
        JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API, object, new Response.Listener<JSONObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject response) {
                if (response.optJSONObject("service") != null) {
                    try {
                        JSONObject service = response.optJSONObject("service");
                        if (service.optJSONObject("service_type") != null) {
                            JSONObject serviceType = service.optJSONObject("service_type");
                            SharedHelper.putKey(getActivity(), "fixed", serviceType.optString("fixed"));
                            SharedHelper.putKey(getActivity(), "price", serviceType.optString("price"));
                            meter_base.setText(SharedHelper.getKey(getActivity(), "currency") + SharedHelper.getKey(getActivity(), "fixed"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


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
                        getProfile();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + "Bearer" + " " + SharedHelper.getKey(getActivity(), "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);


    }

    public void displayMessage(String toastString) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (!stop) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            if (lastlat != 0) {
                getdist = df.format(lastdistance + distance(latitude, longitude, lastlat, lastlong) * 1.609344);
            }

            lastlat = latitude;
            lastlong = longitude;
            lastdistance = Double.parseDouble(getdist.replace(",",""));
            long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            Log.v("Lat: " + latitude, "Long: " + longitude);
            Log.v("GetDistance", "Dis: " + getdist);
            distancetext.setText(getdist + " KM");
            meter_distance.setText(SharedHelper.getKey(getActivity(), "currency") + df.format(Double.valueOf(SharedHelper.getKey(getActivity(), "price")) * lastdistance));
            meter_total.setText(SharedHelper.getKey(getActivity(), "currency") + df.format((Double.valueOf(SharedHelper.getKey(getActivity(), "price")) * lastdistance + Double.valueOf(SharedHelper.getKey(getActivity(), "fixed")))));
        } else {
            distancetext.setText("0.00 KM");
        }
    }
}
