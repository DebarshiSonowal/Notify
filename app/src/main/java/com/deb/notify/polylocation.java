package com.deb.notify;

public class polylocation {
    private String name;
    private  double Latitude;
    private  double Longitude;

    public polylocation(String name, double latitude, double longitude) {
        this.name = name;
        Latitude = latitude;
        Longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }
}
