package com.example.magi.map;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

//停车场列表存储实体类，实现Pacelable接口
public class PoiList implements Parcelable {
    private int totalNum;
    private int currSize;
    private int totalPage;
    private List<Storage> mStorageList;

    PoiList(){}

    PoiList(Parcel dest){
        this.totalNum = dest.readInt();
        this.currSize = dest.readInt();
        this.totalPage = dest.readInt();
        this.mStorageList = dest.readArrayList(Storage.class.getClassLoader());
    }

    public static final Creator<PoiList> CREATOR = new Creator<PoiList>() {
        @Override
        public PoiList createFromParcel(Parcel in) {
            return new PoiList(in);
        }

        @Override
        public PoiList[] newArray(int size) {
            return new PoiList[size];
        }
    };

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getCurrSize() {
        return currSize;
    }

    public void setCurrSize(int currSize) {
        this.currSize = currSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<Storage> getStorageList() {
        return mStorageList;
    }

    public void setStorageList(List<Storage> storageList) {
        mStorageList = storageList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.totalNum);
        dest.writeInt(this.currSize);
        dest.writeInt(this.totalPage);
        dest.writeList(this.mStorageList);
    }
}
