package com.gotaxi.driver.API;

import static com.gotaxi.driver.Helper.URLHelper.ADD_MONEY_DRIVER_WALLET;
import static com.gotaxi.driver.Helper.URLHelper.CANCEL_SUBSC;
import static com.gotaxi.driver.Helper.URLHelper.SET_SUBSC;
import static com.gotaxi.driver.Helper.URLHelper.SUBSC_LIST;

import com.cheetah.driver.Model.subsc_model.SubscriptionResponse;
import com.gotaxi.driver.Model.BankAccountResponse;
import com.gotaxi.driver.Model.CityResponse;
import com.gotaxi.driver.Models.ChatResponse;
import com.gotaxi.driver.Models.ConstDataResponse;
import com.gotaxi.driver.Models.DocumentResponse;
import com.gotaxi.driver.Models.Errorresponse;
import com.gotaxi.driver.Models.Servicelistresponse;
import com.gotaxi.driver.Models.UserResponse;
import com.gotaxi.driver.Models.docResposne;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface Api2 {

    @FormUrlEncoded
    @POST("driverloginbyphone")
    Call<UserResponse> userlogin(
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST(ADD_MONEY_DRIVER_WALLET)
    Call<Errorresponse> adddriverwallet(
            @Field("id") String id,
            @Field("amount") String amount
    );

    @FormUrlEncoded
    @POST("startorder")
    Call<Errorresponse> startorder(
            @Field("id") String id
    );

    @GET("getallservices")
    Call<Servicelistresponse> getallservices();

    @GET("getalldoc/{ids}")
    Call<DocumentResponse> getalldocs(
            @Path("ids") String getPID
    );

    @GET("getbankaccount/{id}")
    Call<BankAccountResponse> getbankaccount(
            @Path("id") String id
    );

    @FormUrlEncoded
    @POST("addtaximeterride")
    Call<Errorresponse> addtaximeterride(
            @Field("id") String id,
            @Field("distance") String distance,
            @Field("amount") String amount);

    @FormUrlEncoded
    @POST("addwithdraw")
    Call<Errorresponse> addwithdraw(
            @Field("id") String id,
            @Field("bid") String bid,
            @Field("amount") String amount);

    @FormUrlEncoded
    @POST("addbankaccount")
    Call<Errorresponse> addbankaccount(
            @Field("name") String name,
            @Field("type") String type,
            @Field("bankname") String bankname,
            @Field("accountnumber") String accountnumber,
            @Field("ifsc") String ifsc,
            @Field("micr") String micr,
            @Field("id") String id,
            @Field("country") String country,
            @Field("currency") String currency
    );

    @GET("getconstdata")
    Call<ConstDataResponse> getconstdata();

    @GET("getcity")
    Call<CityResponse> getcity();

    @Multipart
    @POST("uploaddoc")
    Call<docResposne> uploaddoc(
            @Part MultipartBody.Part image,
            @Part("pid") RequestBody pid,
            @Part("did") RequestBody did
    );

    @FormUrlEncoded
    @POST("addchat")
    Call<Errorresponse> addchat(
            @Field("booking_id") String booking_id,
            @Field("uid") String uid,
            @Field("pid") String pid,
            @Field("message") String message,
            @Field("type") String type
    );

    @GET("getchat/{booking_id}")
    Call<ChatResponse> getchat(
            @Path("booking_id") String booking_id
    );
    @GET(SUBSC_LIST)
    Call<SubscriptionResponse> getSubList(
            @Header("Authorization") String auth
    );
    @FormUrlEncoded
    @POST(SET_SUBSC)
    Call<SubscriptionResponse> setSubsc(
            @Header("Authorization") String auth,
            @Field("subscription_id") String pkg_id
    );
    @POST(CANCEL_SUBSC)
    Call<SubscriptionResponse> cancelSubsc(
            @Header("Authorization") String auth
    );

}
