package ru.android_2019.citycam;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import ru.android_2019.citycam.model.City;
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

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;

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

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(city.name);
        }

        progressView.setVisibility(View.VISIBLE);

        new  DownloadImgTask().execute(city);
        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.


    }

    private class DownloadImgTask extends AsyncTask<City, Void, Void> {

        @Override
        protected Void doInBackground(City... cities) {
            City city = cities[0];
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) Webcams.createNearbyUrl(city.getLatitude(), city.getLongitude()).openConnection();
                httpURLConnection.connect();

                JsonReader jsonReader = new JsonReader(new InputStreamReader(httpURLConnection.getInputStream()));
                jsonReader.beginArray();
                while (jsonReader.hasNext()){
                    String key
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static final String TAG = "CityCam";
}
