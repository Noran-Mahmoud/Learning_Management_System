package com.learning_managment_system.model;

public class Response {
    private String status;
    private String message;
    private Object data;

    public Response() {}

    public Response(Boolean status, String message) {
        this.status = status? "success" : "error";
        this.message = message;
    }

    public Response(Boolean status, String message, Object data) {
        this.status = status? "success" : "error";
        this.message = message;
        this.data = data;
    }

    public void setStatus(Boolean status) {
        this.status = status? "success" : "error";
    }
    public String getStatus() {
        return status;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public Object getData() {
        return data;
    }
}
