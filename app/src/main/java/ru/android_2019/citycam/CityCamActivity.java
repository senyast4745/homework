package ru.android_2019.citycam;

import android.annotation.SuppressLint;


import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
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

import ru.android_2019.citycam.dataBase.WebCamDataBase;
import ru.android_2019.citycam.dataBase.WebcamDAO;
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
    private List<Webcam> webcams;
    private City city;
    private boolean isDownload;
    WebcamDAO webcamDAO;
    private boolean isOffline;

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    private PictureDownloadTask pictureDownloadTask;

    public void setDownload(boolean download) {
        isDownload = download;
    }

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

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = findViewById(R.id.cam_image);
        progressView = findViewById(R.id.progress);
        timeTextView = findViewById(R.id.activity_city_cam_text_last_update);
        titleTextView = findViewById(R.id.activity_city_cam_text_location);


        webcamDAO = MyApplication.getInstance().getDataBase().getWebcamDao();

        progressView.setVisibility(View.VISIBLE);
        //countOfWebcam = 0;

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
            if (isDownload) {
                progressView.setVisibility(View.GONE);
            }
        }


        //webcams  =  pictureDownloadTask.getWebcams();
        //Log.d(LOG_TAG, webcams.get(countOfWebcam).getTitle());
        //setupButtons();

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
        if (webcams != null && webcams.size() > 0) {
            Log.d(LOG_TAG, countOfWebcam + " in init");
            Webcam tmpWebcam = webcams.get(countOfWebcam);
            Log.d(LOG_TAG, "init title " + tmpWebcam.getTitle());
            /*if (tmpWebcam.getBitmap() == null) {
                byte[] array = tmpWebcam.getImage();
                tmpWebcam.setBitmap(BitmapFactory.decodeByteArray(array, 0, array.length));
            }*/
            camImageView.setImageBitmap(tmpWebcam.getBitmap());
            String time = tmpWebcam.getTime();
            String title = tmpWebcam.getTitle();
            timeTextView.setText(getString(R.string.last_update_time) + " " + time);
            titleTextView.setText(getString(R.string.about_location) + " " + title);
        } else {
            if (isDownload || isOffline ) {
                camImageView.setImageResource(R.drawable.page_not_foud);
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private void setupButtons() {


        countOfWebcam = webcams.size() - 1;
        Log.d(LOG_TAG, "size " + webcams.size());
        Button nextButton = findViewById(R.id.activity_city_cam_next_button);
        nextButton.setOnClickListener(v -> nextWebCamImage());

        Button prevButton = findViewById(R.id.activity_city_cam_prev_button);
        prevButton.setOnClickListener(v -> prevWebCamImage());
        initWebCamImage();

    }


    void checkDownLoad(List<Webcam> webcams) {
        if (isDownload) {


            progressView.setVisibility(View.GONE);
            this.webcams = webcams;
            if (webcams.size() > 0)
                Log.d(LOG_TAG, "downloaded " + webcams.get(0).getTitle());
            city.setWebcams(webcams);

            setupButtons();

        }
    }

    public void setWebcams(List<Webcam> webcams) {
        this.webcams = webcams;
        setupButtons();

    }


    static class PictureDownloadTask extends AsyncTask<City, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        private ProgressBar progressView;
        private List<Webcam> webcams;
        private boolean isDownloaded;
        private JsonParser parser;
        City city;

        @SuppressLint("StaticFieldLeak")
        private CityCamActivity cityCamActivity;

        PictureDownloadTask(CityCamActivity cityCamActivity, ProgressBar progressView) {
            city = null;
            webcams = null;
            isDownloaded = false;
            this.cityCamActivity = cityCamActivity;
            this.progressView = progressView;
        }

        @Override
        protected void onPreExecute() {
            isDownloaded = false;
            super.onPreExecute();
            webcams = new ArrayList<>();
            updateView();
        }


        @Override
        @WorkerThread
        protected Void doInBackground(City... cities) {
            city = cities[0];
            Log.d(LOG_TAG, "execute task");
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) Webcams.createNearbyUrl(city.getLatitude(), city.getLongitude()).openConnection();
                httpURLConnection.setReadTimeout(3000);
                httpURLConnection.setConnectTimeout(3000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("X-RapidAPI-KEY", devId);
                Log.d(LOG_TAG, httpURLConnection.toString());
                httpURLConnection.connect();

                JsonReader reader = new JsonReader(new InputStreamReader(httpURLConnection.getInputStream()));

                parser = new JsonParser(reader, city, cityCamActivity.webcamDAO);
                parser.mainJsonParser();

                Log.d(LOG_TAG, "Begin connect");

                webcams = parser.getWebcams();

                reader.close();
                httpURLConnection.disconnect();


            } catch (IOException e) {
                Log.d(LOG_TAG, "no connection");
                cityCamActivity.setOffline(true);
                webcams = cityCamActivity.webcamDAO.selectByName(city.getName());
                //e.printStackTrace();


            }

            return null;
        }


        //boolean isDownloaded() {
        //  return isDownloaded;
        //}cityCamActivity.setWebcams(cityCamActivity.webcamDAO.selectByName(city.getName()));


        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(LOG_TAG, "task executed");
            super.onPostExecute(aVoid);
            progressView.setVisibility(View.GONE);
            if (!cityCamActivity.isOffline()) {
                cityCamActivity.setDownload(true);
            } else {
                cityCamActivity.setWebcams(webcams);
            }
            updateView();

        }

        void attachActivity(CityCamActivity activity) {
            this.cityCamActivity = activity;
            updateView();
        }


        @SuppressLint("SetTextI18n")
        void updateView() {
            if (cityCamActivity != null && webcams != null) {
                cityCamActivity.checkDownLoad(webcams);
            }
        }


    }

}