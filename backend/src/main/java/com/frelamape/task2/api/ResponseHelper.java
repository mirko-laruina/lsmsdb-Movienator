package com.frelamape.task2.api;

import com.google.gson.Gson;

import static com.frelamape.task2.api.BaseResponse.*;

public class ResponseHelper {
    public static String response2Json(BaseResponse response){
        return new Gson().toJson(response);
    }

    public static String success(Object payload){
        return response2Json(new BaseResponse(CODE_OK, null, payload));
    }

    public static String error(int code, String msg){
        return response2Json(new BaseResponse(code, msg, null));
    }

    public static String invalidSession(){
        return error(CODE_INVALID_SESSION, "Invalid session");
    }

    public static String userBanned(){
        return error(CODE_USER_BANNED, "User is banned");
    }

    public static String wrongCredentials(){
        return error(CODE_WRONG_CREDENTIALS, "Wrong username or password");
    }

    public static String notFound(){
        return error(CODE_NOT_FOUND, "Not found");
    }

    public static String unauthorized(){
        return error(CODE_UNAUTHORIZED, "Unauthorized");
    }

    public static String mongoError(String msg){
        return error(CODE_MONGO_ERROR, msg);
    }

    public static String genericError(String msg){
        return error(CODE_GENERIC_ERROR, msg);
    }
}
