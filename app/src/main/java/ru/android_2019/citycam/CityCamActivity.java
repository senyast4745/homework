package ru.android_2019.citycam;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.android_2019.citycam.model.City;
import ru.android_2019.citycam.model.Webcam;
import ru.android_2019.citycam.webcams.Webcams;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";
    private static final String TAG = "CityCam";
    private static final String LOG_TAG = "CityCamActivity";
    private static final String devId = "5afc8f0d6amshc677913948c374bp1dfe7ajsn3c562e877ada";

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;

    private PictureDownloadTask pictureDownloadTask;


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        pictureDownloadTask.attachActivity(null);
        return pictureDownloadTask;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = findViewById(R.id.cam_image);
        progressView = findViewById(R.id.progress);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(city.getName());
        }

        if (savedInstanceState != null) {
            // Пытаемся получить ранее запущенный таск
            pictureDownloadTask = (PictureDownloadTask) getLastNonConfigurationInstance();
        }
        if (pictureDownloadTask == null) {
            // Создаем новый таск, только если не было ранее запущенного таска
            pictureDownloadTask = new PictureDownloadTask(camImageView, progressView);
            pictureDownloadTask.execute();
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            pictureDownloadTask.attachActivity(this);
        }



        progressView.setVisibility(View.VISIBLE);

         = new PictureDownloadTask();

        pictureDownloadTask.execute(city);
        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
    }


    static class PictureDownloadTask extends AsyncTask<City, Void, Void> {

        //String title, stringUrl;
        //long time;
        //private Bitmap bitmap;
        @SuppressLint("StaticFieldLeak")
        private ImageView camImageView;
        @SuppressLint("StaticFieldLeak")
        private ProgressBar progressView;
        private Webcam webcam;
        private List<Webcam> webcams;

        private CityCamActivity cityCamActivity;

        public PictureDownloadTask(ImageView camImageView, ProgressBar progressView) {
            this.camImageView = camImageView;
            this.progressView = progressView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            webcam = new Webcam();
            webcams = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(City... cities) {
            City city = cities[0];
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) Webcams.createNearbyUrl(city.getLatitude(), city.getLongitude()).openConnection();
                httpURLConnection.setReadTimeout(3000);
                httpURLConnection.setConnectTimeout(3000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("X-RapidAPI-KEY", devId);
                httpURLConnection.connect();
                JsonReader reader = new JsonReader(new InputStreamReader(httpURLConnection.getInputStream()));


                reader.beginObject();
                //Log.d(LOG_TAG, "Begin connect " + reader.nextName() + " " + reader.nextName());
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    //TODO
                    Log.d(LOG_TAG, name);
                    switch (name) {
                        case "status":
                            String resultCode = reader.nextString();
                            Log.d(LOG_TAG, "---" + resultCode + " " + httpURLConnection.getResponseCode());
                            if (!resultCode.equals("OK")) {
                                return null;
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
                Log.d(LOG_TAG, "Begin connect");

                InputStream in = new URL(webcam.getImgUrl()).openStream();
                webcam.setBitmap(BitmapFactory.decodeStream(in));

                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
            return null;
        }

        private void readResult(JsonReader reader) throws IOException {
            reader.beginObject();

            while (reader.hasNext()) {

                String jsonName = reader.nextName();
                Log.d(LOG_TAG, "key of json in readResult " + jsonName);
                switch (jsonName) {
                    case "webcams":
                        //reader.beginArray();
                        Log.d(LOG_TAG, "read webcam");
                        readWebCamObject(reader);
                        //reader.endArray();
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }


        private void readWebCamObject(JsonReader reader) throws IOException {
            reader.beginArray();
            while (reader.hasNext()) {
                Log.d(LOG_TAG, "next object ");
                reader.beginObject();
                while (reader.hasNext()) {

                    switch (reader.nextName()) {
                        case "image":
                            readImage(reader);
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

            }
            reader.endArray();
        }

        private void readImage(JsonReader reader) throws IOException {
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "current":
                        readCurrent(reader);
                        break;
                    case "update":
                        webcam.setTime(reader.nextLong());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }

        private void readCurrent(JsonReader reader) throws IOException {
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressView.setVisibility(View.GONE);
            camImageView.setImageBitmap(webcam.getBitmap());
        }

        void attachActivity(CityCamActivity activity) {
            this.cityCamActivity = activity;
            updateView();
        }

    }

}