package ru.android_2019.citycam;

import android.annotation.SuppressLint;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView timeTextView;
    private TextView titleTextView;
    int countOfWebcam;
    List<Webcam> webcams;

    private PictureDownloadTask pictureDownloadTask;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        pictureDownloadTask.attachActivity(null);
        super.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return pictureDownloadTask;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        City city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = findViewById(R.id.cam_image);
        progressView = findViewById(R.id.progress);
        timeTextView = findViewById(R.id.activity_city_cam_text_last_update);
        titleTextView = findViewById(R.id.activity_city_cam_text_location);

        progressView.setVisibility(View.VISIBLE);
        countOfWebcam = 0;

        if (getSupportActionBar() != null) {
            if (city != null) {
                getSupportActionBar().setTitle(city.getName());
            }
        }


        if (savedInstanceState != null) {
            // Пытаемся получить ранее запущенный таск
            pictureDownloadTask = (PictureDownloadTask) getLastCustomNonConfigurationInstance();
        }
        if (pictureDownloadTask == null) {
            // Создаем новый таск, только если не было ранее запущенного таска
            Log.d(LOG_TAG, "download");
            pictureDownloadTask = new PictureDownloadTask(this, progressView);
            pictureDownloadTask.execute(city);
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            Log.d(LOG_TAG, "download continue");
            pictureDownloadTask.attachActivity(this);
            if (pictureDownloadTask.isDownloaded()) {
                progressView.setVisibility(View.GONE);
            }

        }

        //webcams  =  pictureDownloadTask.getWebcams();
        //Log.d(LOG_TAG, webcams.get(countOfWebcam).getTitle());
        //setupButtons();

    }

    @SuppressLint("SetTextI18n")
    private void setupButtons() {
        countOfWebcam = webcams.size();
        Button nextButton = findViewById(R.id.activity_city_cam_next_button);
        nextButton.setOnClickListener(v -> pictureDownloadTask.nextWebCamImage());

        Button prevButton = findViewById(R.id.activity_city_cam_prev_button);
        prevButton.setOnClickListener(v -> pictureDownloadTask.prevWebCamImage());

    }


    void checkDownLoad(List<Webcam> webcams) {
        if (pictureDownloadTask.isDownloaded()) {
            progressView.setVisibility(View.GONE);
            this.webcams = webcams;
            setupButtons();

        }
    }


    static class PictureDownloadTask extends AsyncTask<City, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        private ProgressBar progressView;
        private List<Webcam> webcams;
        private boolean isDownloaded;
        private JsonParser parser;
        int countOfWebcam;
        Webcam tmpWebcam;

        @SuppressLint("StaticFieldLeak")
        private CityCamActivity cityCamActivity;

        PictureDownloadTask(CityCamActivity cityCamActivity, ProgressBar progressView) {
            countOfWebcam = 0;
            isDownloaded = false;
            this.cityCamActivity = cityCamActivity;
            this.progressView = progressView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            webcams = new ArrayList<>();
            updateView();
        }

        @Override
        @WorkerThread
        protected Void doInBackground(City... cities) {
            City city = cities[0];
            Log.d(LOG_TAG, "execute task");
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) Webcams.createNearbyUrl(city.getLatitude(), city.getLongitude()).openConnection();
                httpURLConnection.setReadTimeout(3000);
                httpURLConnection.setConnectTimeout(3000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("X-RapidAPI-KEY", devId);
                httpURLConnection.connect();
                JsonReader reader = new JsonReader(new InputStreamReader(httpURLConnection.getInputStream()));

                parser = new JsonParser(reader);
                parser.mainJsonParser();

                Log.d(LOG_TAG, "Begin connect");

                webcams = parser.getWebcams();

                reader.close();
                httpURLConnection.disconnect();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        boolean isDownloaded() {
            return isDownloaded;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(LOG_TAG, "task executed");
            super.onPostExecute(aVoid);
            isDownloaded = true;
            progressView.setVisibility(View.GONE);
            updateView();
        }

        void attachActivity(CityCamActivity activity) {
            this.cityCamActivity = activity;
            updateView();
        }


        @SuppressLint("SetTextI18n")
        void updateView() {
            if (cityCamActivity != null && webcams != null) {
                initWebCamImage();
                cityCamActivity.checkDownLoad(webcams);
            }
        }

        @SuppressLint("SetTextI18n")
        void nextWebCamImage() {
            countOfWebcam++;
            if (countOfWebcam > webcams.size() - 1) {
                countOfWebcam = 0;
            }
            Log.d(LOG_TAG, countOfWebcam + " in next click");
            initWebCamImage();
        }

        @SuppressLint("SetTextI18n")
        void prevWebCamImage() {
            countOfWebcam--;
            if (countOfWebcam < 0) {
                countOfWebcam = webcams.size() - 1;
            }
            Log.d(LOG_TAG, countOfWebcam + " in prev click");
            initWebCamImage();
        }

        @SuppressLint("SetTextI18n")
        void initWebCamImage() {
            if (webcams.size() > 0) {
                Log.d(LOG_TAG, countOfWebcam + " in init");
                tmpWebcam = webcams.get(countOfWebcam);
                cityCamActivity.camImageView.setImageBitmap(tmpWebcam.getBitmap());
                String time = tmpWebcam.getTime();
                String title = tmpWebcam.getTitle();
                cityCamActivity.timeTextView.setText(cityCamActivity.getString(R.string.last_update_time) + " " + time);
                cityCamActivity.titleTextView.setText(cityCamActivity.getString(R.string.about_location) + " " + title);
            } else {
                if (isDownloaded) {
                    cityCamActivity.camImageView.setImageResource(R.drawable.page_not_foud);
                }
            }
        }

    }

}