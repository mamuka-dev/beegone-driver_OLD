package com.gotaxi.driver.Helper;


public class URLHelper {
    public static final String base = "https://meemcolart.com/";
    //  public static final String base = "https://gaariapp.com/";
    public static final String HELP_URL = base + "";
    public static final String CALL_PHONE = "1";
    public static final String APP_URL = "https://play.google.com/store/apps/details?id=";
    public static final String login = base + "api/provider/oauth/token";
    public static final String register = base + "api/provider/register";
    public static final String CHECK_MAIL_ALREADY_REGISTERED = base + "api/provider/verify";
    public static final String USER_PROFILE_API = base + "api/provider/profile";
    public static final String UPDATE_AVAILABILITY_API = base + "api/provider/profile/available";
    public static final String GET_HISTORY_API = base + "api/provider/requests/history";
    public static final String GET_HISTORY_DETAILS_API = base + "api/provider/requests/history/details";
    public static final String CHANGE_PASSWORD_API = base + "api/provider/profile/password";
    public static final String UPCOMING_TRIP_DETAILS = base + "api/provider/requests/upcoming/details";
    public static final String UPCOMING_TRIPS = base + "api/provider/requests/upcoming";
    public static final String CANCEL_REQUEST_API = base + "api/provider/cancel";
    public static final String REJECT_REQUEST_API = base + "api/provider/nextpro";
    public static final String TARGET_API = base + "api/provider/target";
    public static final String TARGET_API_7DAYS = base + "api/provider/target2";
    public static final String RESET_PASSWORD = base + "api/provider/reset/password";
    public static final String FORGET_PASSWORD = base + "api/provider/forgot/password";
    public static final String FACEBOOK_LOGIN = base + "api/provider/auth/facebook";
    public static final String GOOGLE_LOGIN = base + "api/provider/auth/google";
    public static final String LOGOUT = base + "api/provider/logout";
    public static final String SUMMARY = base + "api/provider/summary";
    public static final String HELP = base + "api/provider/help";
    public static final String GET_DRIVER_DOCUMENTS = "/api/provider/profile/services-documents";
    public static final String UPLOAD_DRIVER_DOCUMENTS = "/api/provider/profile/document";
    public static final String UPDATE_DRIVER_DOCUMENTS = "/api/provider/profile/service";

    //Cards & Payment ApisDELETE_CARD_FROM_ACCOUNT_API
    public static final String ADD_CARD_TO_ACCOUNT_API = base + "api/provider/card";
    public static final String CARD_PAYMENT_LIST = base + "api/provider/card";
    public static final String CHARGE_CARD_FOR_WALLET = base + "api/provider/add/money";
    public static final String DELETE_CARD_FROM_ACCOUNT_API = base + "api/provider/card/destory";
    public static final String CHANGE_DEFAULT_CARD = base + "api/provider/card/set-default";

    public static final int client_id = 12;
    public static final String client_secret = "Vlpw7zY8wRTCxBBZtf0jEvSMaNa2WKYTpQBLub3f";
    public static final String ADD_MONEY_DRIVER_WALLET = "adddriverwallet"; //old api were using to charge wallet

    public static final String SUBSC_LIST = "api/provider/subscriptions";
    public static final String SET_SUBSC = "api/provider/subscription";
    public static final String CANCEL_SUBSC = "api/provider/cancel-subscription";
}
