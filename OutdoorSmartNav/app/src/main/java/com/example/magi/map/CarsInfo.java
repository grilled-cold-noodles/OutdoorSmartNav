package com.example.magi.map;

public class CarsInfo {
    private int id;
    private String number;  //车牌
    private double latitude;  //纬度
    private double longitude;  //经度
    private int speed;  //车速

    public CarsInfo(int id, String number, double latitude, double longitude, int speed) {
        this.id = id;
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
