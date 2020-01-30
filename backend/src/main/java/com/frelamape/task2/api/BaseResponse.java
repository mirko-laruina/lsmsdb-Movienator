package com.frelamape.task2.api;

public class BaseResponse {
    private boolean success;
    private String message;
    Object response;

    public BaseResponse(boolean success, String message, Object response) {
        this.success = success;
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
}
