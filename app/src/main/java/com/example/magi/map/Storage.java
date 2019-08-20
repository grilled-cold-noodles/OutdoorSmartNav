package com.example.magi.map;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;

//停车场信息实体类
public class Storage implements Parcelable {
    private String name;     //存储名为title
    private String address;
    private String uid;      //存储名为number
    private LatLng location;
    private String style;
    private int total;
    private int empty;
    private int time1;
    private int time2;
    private int time3;
    private float distance;

    public Storage(){}

    protected Storage(Parcel in) {
        name = in.readString();
        address = in.readString();
        uid = in.readString();
        location = in.readParcelable(LatLng.class.getClassLoader());
        style = in.readString();
        total = in.readInt();
        empty = in.readInt();
        time1 = in.readInt();
        time2 = in.readInt();
        time3 = in.readInt();
        distance = in.readInt();
    }

    public static final Creator<Storage> CREATOR = new Creator<Storage>() {
        @Override
        public Storage createFromParcel(Parcel in) {
            return new Storage(in);
        }

        @Override
        public Storage[] newArray(int size) {
            return new Storage[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getEmpty() {
        return empty;
    }

    public void setEmpty(int empty) {
        this.empty = empty;
    }

    public int getTime1() {
        return time1;
    }

    public void setTime1(int time1) {
        this.time1 = time1;
    }

    public int getTime2() {
        return time2;
    }

    public void setTime2(int time2) {
        this.time2 = time2;
    }

    public int getTime3() {
        return time3;
    }

    public void setTime3(int time3) {
        this.time3 = time3;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeString(this.uid);
        dest.writeParcelable(this.location, 0);
        dest.writeString(this.style);
        dest.writeInt(this.total);
        dest.writeInt(this.empty);
        dest.writeInt(this.time1);
        dest.writeInt(this.time2);
        dest.writeInt(this.time3);
        dest.writeFloat(this.distance);
    }
}
