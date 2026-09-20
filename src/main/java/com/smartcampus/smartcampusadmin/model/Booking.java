package com.smartcampus.smartcampusadmin.model;

public class Booking {

    private int id;
    private int user_id;
    private int resource_id;
    private String start_time;
    private String end_time;
    private String status;
    private String created_at;

    public int getId() {
        return id;
    }

    public int getUserId() {
        return user_id;
    }

    public int getResourceId() {
        return resource_id;
    }

    public String getStartTime() {
        return start_time;
    }

    public String getEndTime() {
        return end_time;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return created_at;
    }
}