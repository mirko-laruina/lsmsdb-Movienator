package com.frelamape.task2.api;

public class BaseResponse {
    private int code;
    private boolean success;
    private String message;
    Object response;

    public static final int CODE_OK = 0;
    public static final int CODE_INVALID_SESSION = 1;
    public static final int CODE_MONGO_ERROR = 2;
    public static final int CODE_USER_BANNED = 3;
    public static final int CODE_WRONG_CREDENTIALS = 4;
    public static final int CODE_NOT_FOUND = 5;
    public static final int CODE_UNAUTHORIZED = 6;
    public static final int CODE_GENERIC_ERROR = 7;

    public BaseResponse(boolean success, String message, Object response) {
        this.success = success;
        this.message = message;
        this.response = response;
    }
    public BaseResponse(int code, String message, Object response) {
        this.code = code;
        this.success = code == CODE_OK;
        this.message = message;
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
