package ru.android_2019.citycam.dataBase;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ru.android_2019.citycam.model.City;
import ru.android_2019.citycam.model.Webcam;

@Dao
public interface WebcamDAO {

    @Insert
    void insertWebcam(Webcam webcam);

    @Query("SELECT * FROM webcam")
    List<Webcam> selectAll();

    @Query("SELECT * FROM webcam WHERE cityName IS :name")
    List<Webcam> selectByName(String name);

    @Query("DELETE FROM webcam WHERE cityName = :name")
    void deleteByCityName(String name);

}
