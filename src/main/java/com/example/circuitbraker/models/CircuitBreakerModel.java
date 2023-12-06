package com.example.circuitbraker.models;

import java.util.Date;

public class CircuitBreakerModel {
    private String methodName;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getLastInvoked() {
        return lastInvoked;
    }

    public void setLastInvoked(Date lastInvoked) {
        this.lastInvoked = lastInvoked;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    private int count;
    private Date lastInvoked;
    private int httpStatus;
    private String payload;

    public CircuitBreakerModel(String methodName, int count, Date lastInvoked, int httpStatus, String payload,String errorMessage) {
        this.methodName = methodName;
        this.errorMessage = errorMessage;
        this.count = count;
        this.lastInvoked = lastInvoked;
        this.httpStatus = httpStatus;
        this.payload = payload;
    }

}
