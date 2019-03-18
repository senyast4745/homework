package ru.android_2019.citycam.dataBase;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import ru.android_2019.citycam.model.City;
import ru.android_2019.citycam.model.Webcam;

@Dao
public interface WebcamDAO {

    @Insert
    void insertCity(City city);

    @Query("SELECT * FROM city")
    List<Webcam> selectAll();

    @Query("SELECT * FROM city WHERE name IS :name")
    Webcam selectByName(String name);

}
