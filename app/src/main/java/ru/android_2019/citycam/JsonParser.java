package ru.android_2019.citycam;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.JsonReader;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.android_2019.citycam.dataBase.WebcamDAO;
import ru.android_2019.citycam.model.City;
import ru.android_2019.citycam.model.Webcam;

class JsonParser {


    private City city;
    private WebcamDAO webcamDAO;
    private JsonReader reader;
    private String LOG_TAG = "JsonParser";
    private List<Webcam> webcams;


    JsonParser(JsonReader reader, City city, WebcamDAO webcamDAO) {
        this.reader = reader;
        webcams = new ArrayList<>();
        this.city = city;
        this.webcamDAO = webcamDAO;
        if(webcamDAO!= null && webcamDAO.selectByName(city.getName()) != null){
            webcamDAO.deleteByCityName(city.getName());
        }

    }

    void mainJsonParser() throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            //TODO
            switch (name) {
                case "status":
                    String resultCode = reader.nextString();
                    Log.d(LOG_TAG, name + " " + resultCode);
                    if (!resultCode.equals("OK")) {
                        throw new IOException();
                    }
                    break;
                case "result":
                    Log.d(LOG_TAG, "result reader");
                    readResult(reader);
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

    }


    private void readResult(JsonReader reader) throws IOException {
        reader.beginObject();
        Log.d(LOG_TAG, "read result ");
        while (reader.hasNext()) {

            String jsonName = reader.nextName();
            switch (jsonName) {
                case "webcams":
                    readWebCamArray(reader);
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
    }

    private void readWebCamArray(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            webcams.add(readWebCamObject(reader));


        }
        reader.endArray();
    }

    private Webcam readWebCamObject(JsonReader reader) throws IOException {
        Webcam webcam = new Webcam();
        Log.d(LOG_TAG, "next object ");
        reader.beginObject();
        while (reader.hasNext()) {

            switch (reader.nextName()) {
                case "image":
                    readImage(reader, webcam);
                    break;
                case "title":
                    String tmp = reader.nextString();
                    Log.d(LOG_TAG, "title of webcam " + tmp);
                    webcam.setTitle(tmp);
                    break;
                default:
                    reader.skipValue();
            }

        }
        reader.endObject();
        webcam.setCityName(city.getName());
        InputStream in = new URL(webcam.getImgUrl()).openStream();
        webcam.setBitmap(BitmapFactory.decodeStream(in));
        /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
        webcam.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, bos);
        webcam.setImage(bos.toByteArray());*/
        //Log.d(LOG_TAG, "byte in webcam " + String.valueOf(webcam.getImage()) );
        if (webcamDAO != null ) {

            webcamDAO.insertWebcam(webcam);
        }
        return webcam;
    }

    private void readImage(JsonReader reader, Webcam webcam) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "current":
                    readCurrent(reader, webcam);
                    break;
                case "update":
                    webcam.setTime(parseTime(reader.nextLong()));
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
    }

    private void readCurrent(JsonReader reader, Webcam webcam) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "preview":
                    webcam.setImgUrl(reader.nextString());
                    Log.d(LOG_TAG, "url of img: " + webcam.getImgUrl());
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
    }

    List<Webcam> getWebcams() {
        return webcams;
    }


    private String parseTime(long epoch) {

        @SuppressLint("SimpleDateFormat")
        String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(epoch * 1000));
        return date;
    }

}
