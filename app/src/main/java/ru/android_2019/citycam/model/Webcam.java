package ru.android_2019.citycam.model;

import android.graphics.Bitmap;

public class Webcam {

    private String title;
    private String imgUrl;
    private long time;
    private long id;
    private Bitmap bitmap;

    public Webcam(String title, String imgUrl, long time, long id) {
        this.title = title;
        this.imgUrl = imgUrl;
        this.time = time;
        this.id = id;
    }

    public Webcam() {
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }


    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}