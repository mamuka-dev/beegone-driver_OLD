package com.gotaxi.driver.Retrofit;


import okhttp3.ResponseBody;
import retrofit2.Response;


public interface RetrofitCallback {
    void Success(Response<ResponseBody> response);

    void Failure(Throwable errorResponse);
}
