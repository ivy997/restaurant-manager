package com.example.restaurantmanager.enums;

public enum OrderStatus {
    PLACED("Placed"),
    PREPARING("Preparing"),
    SERVED("Served"),
    COMPLETED("Completed"),
    CANCELED("Canceled");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
