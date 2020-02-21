package com.example.linememo;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable, Comparable
{

    Uri image = null;
    private String title = null;
    private String text = null;
    private String date = null;



    public Item(){}

    public Item(Uri image,String title, String text, String date)
    {
        this.title = title;
        this.image = image;
        this.text =text;
        this.date = date;
    }

    public Item(String title, String text, String date)
    {
        this.title = title;
        this.text =text;
        this.date = date;
    }

    protected Item(Parcel in) {
        image = in.readParcelable(Uri.class.getClassLoader());
        title = in.readString();
        text = in.readString();
        date = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    //getter와 setter들
    public void setImage(Uri image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Uri getImage() {
        return image;
    }

    public String getTitle() {
        if(title ==null) return "";
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        if(text ==null) return "";
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(image, flags);
        dest.writeString(title);
        dest.writeString(text);
        dest.writeString(date);
    }


    @Override
    public int compareTo(Object compareItem) {
        Item item = (Item)compareItem;
        return this.getDate().compareTo(item.getDate());
    }
}
