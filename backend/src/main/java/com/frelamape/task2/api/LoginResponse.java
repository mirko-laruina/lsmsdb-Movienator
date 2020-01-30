package com.frelamape.task2.api;

public class LoginResponse {
    private String sessionId;
    private Boolean is_admin;

    public LoginResponse(String sessionId, Boolean is_admin) {
        this.sessionId = sessionId;
        this.is_admin = is_admin;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isAdmin() {
        return is_admin;
    }

    public void setIs_admin(Boolean is_admin) {
        this.is_admin = is_admin;
    }
}
