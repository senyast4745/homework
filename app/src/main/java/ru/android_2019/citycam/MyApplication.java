package ru.android_2019.citycam;

import android.app.Application;
import android.arch.persistence.room.Room;

import ru.android_2019.citycam.dataBase.WebCamDataBase;

public class MyApplication extends Application {

    private static MyApplication instance;
    private WebCamDataBase dataBase;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        dataBase = Room.databaseBuilder(this, WebCamDataBase.class, "database")
                .fallbackToDestructiveMigration()
                .build();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public WebCamDataBase getDataBase() {
        return dataBase;
    }
}
