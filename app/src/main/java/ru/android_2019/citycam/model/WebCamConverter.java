package ru.android_2019.citycam.model;

import android.arch.persistence.room.TypeConverter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class WebCamConverter {
    @TypeConverter
    public byte[] fromBitmap(Bitmap bitmap){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    @TypeConverter
    public Bitmap toBitmap(byte[] array){
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }
}
