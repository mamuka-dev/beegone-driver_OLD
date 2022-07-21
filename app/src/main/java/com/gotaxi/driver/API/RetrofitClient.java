package com.gotaxi.driver.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gotaxi.driver.Helper.URLHelper;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static String BASE_URL = URLHelper.base + "public/cabuser/public/";

    private static RetrofitClient mInstance;
    private static RetrofitClient mInstanceSecond;
    private Retrofit retrofit;
    OkHttpClient client = new OkHttpClient();
    Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    private RetrofitClient( String baseUrl) {

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }
    public static synchronized RetrofitClient getInstance(String baseUrl) {
        if (mInstanceSecond == null) {
            mInstanceSecond = new RetrofitClient(baseUrl);
        }
        return mInstanceSecond;
    }
    public Api2 getApi() {
        return retrofit.create(Api2.class);
    }
}
