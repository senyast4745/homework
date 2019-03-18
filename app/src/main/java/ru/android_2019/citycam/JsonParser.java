package ru.android_2019.citycam;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.android_2019.citycam.model.Webcam;

public class JsonParser {

    //Webcam webcam;
    private JsonReader reader;
    private String LOG_TAG = "JsonParser";
    private List<Webcam> webcams;
    private Random random = new Random();

    JsonParser(JsonReader reader) {
        this.reader = reader;
        webcams = new ArrayList<>();
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
        //Log.d(LOG_TAG, "next object ");
        reader.beginObject();
        while (reader.hasNext()) {

            switch (reader.nextName()) {
                case "id": {
                    webcam.setId(reader.nextInt());
                    break;
                }
                case "image":
                    readImage(reader, webcam);
                    break;
                case "title":
                    String tmp = reader.nextString();
                    Log.d(LOG_TAG, "title of webcam" + tmp);
                    webcam.setTitle(tmp);
                    break;
                default:
                    reader.skipValue();
            }

        }
        reader.endObject();
        InputStream in = new URL(webcam.getImgUrl()).openStream();
        webcam.setBitmap(BitmapFactory.decodeStream(in));
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

    private Webcam getWebCam(int index) {
        Log.d(LOG_TAG, webcams.get(index).getTitle());
        return webcams.get(index);
    }

    public Webcam getRandomWebcam() {
        int index = random.nextInt(webcams.size());
        return getWebCam(index);
    }

    private String parseTime(long epoch) {

        @SuppressLint("SimpleDateFormat")
        String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(epoch * 1000));
        return date;
    }

}
