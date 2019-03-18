package ru.android_2019.citycam.dataBase;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import ru.android_2019.citycam.model.Webcam;

@Database(entities = {Webcam.class}, version = 1)
public abstract class WebCamDataBase extends RoomDatabase {

    public abstract WebcamDAO getWebcamDao();

}
