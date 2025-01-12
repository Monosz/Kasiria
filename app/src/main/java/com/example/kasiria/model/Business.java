package com.example.kasiria.model;

// businesses/bId/

import android.os.Parcel;
import android.os.Parcelable;

public class Business implements Parcelable {
    private String id;
    private String name;
    private String address;
    private String ownerId;

    public Business() { }

    public Business(String name, String address, String ownerId) {
        this.name = name;
        this.address = address;
        this.ownerId = ownerId;
    }

    public Business(String id, String name, String address, String ownerId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.ownerId = ownerId;
    }

    protected Business(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        ownerId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(ownerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Business> CREATOR = new Creator<Business>() {
        @Override
        public Business createFromParcel(Parcel in) {
            return new Business(in);
        }

        @Override
        public Business[] newArray(int size) {
            return new Business[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
