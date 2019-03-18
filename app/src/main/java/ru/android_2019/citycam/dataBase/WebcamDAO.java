package ru.android_2019.citycam.Interface;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.android_2019.citycam.model.Webcam;

@Dao
public interface WebcamDAO {

    @Insert
    void insertOne(Webcam webcam);

    @Query("SELECT * FROM webcam")
    List<Webcam> selectAll();

    @Query("SELECT * FROM webcam WHERE id IS :id")
    Webcam selectById(int id);
    
}
