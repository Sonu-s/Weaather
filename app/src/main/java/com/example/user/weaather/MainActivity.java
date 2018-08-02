package com.example.user.weaather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.ContentHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText cityName;
    TextView weatherDetails;
    TextView tempDetail;
    TextView addressDetails;
    LocationManager locationManager;
    LocationListener locationListener;

   public void whatsTheWeather(View view) {

        InputMethodManager img = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        img.hideSoftInputFromWindow(cityName.getWindowToken(), 0);

        try {

            String encodedCityName = URLEncoder.encode(cityName.getText().toString(), "UTF-8");
            DownloadTask task = new DownloadTask();
            addressDetails.setVisibility(View.INVISIBLE);

            task.execute("https://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&APPID=00c4e7658b536dce083967aaf393c19f");

        } catch (Exception e) {

            //e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not find weather!", Toast.LENGTH_SHORT).show();
        }
    }


    public void updateLocationInfo(Location location){



        Log.i("Location",location.toString());

        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());

        DownloadTask task = new DownloadTask();
        task.execute("https://api.openweathermap.org/data/2.5/weather?lat="+ latitude +"&lon="+longitude+"&APPID=00c4e7658b536dce083967aaf393c19f");


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";

        try{

            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

            if (addressList != null && addressList.size()> 0){


                if (addressList.get(0).getLocality() != null){

                    address +=addressList.get(0).getLocality()+", ";
                }
                if (addressList.get(0).getCountryName() !=null){

                    address +=addressList.get(0).getCountryName();
                }
            }

                addressDetails.setText(address);
                addressDetails.setVisibility(View.VISIBLE);

        }catch (Exception e){

            e.printStackTrace();

        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 18, locationListener);

                }
            }
        }

    }

    public void currentLocation(View view){

        InputMethodManager img = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        img.hideSoftInputFromWindow(cityName.getWindowToken(), 0);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateLocationInfo(location);


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 18, locationListener);

            Location lastKnownLocation =locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null){

                updateLocationInfo(lastKnownLocation);
            }

            }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        weatherDetails = findViewById(R.id.weatherDetails);
        tempDetail = findViewById(R.id.tempDetails);
        addressDetails = findViewById(R.id.addressDetails);
        //locationUpdate();

    }

   public class DownloadTask extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection connection = null;

            try{

                url=new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();

                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){

                    char ch = (char) data;
                    result += ch;
                    data = reader.read();

                }

                return result;

            }catch (Exception e){

                e.printStackTrace();
                //Toast.makeText(getApplicationContext(),"Could not find weather!",Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String weatherInfo = jsonObject.getString("weather");
                String tempInfo = jsonObject.getString("main");
                JSONArray jsonArray = new JSONArray(weatherInfo );

                JSONObject tempObject = new JSONObject(tempInfo);

                double humidity ,temp;
                 temp = tempObject.getDouble("temp");
                 humidity = tempObject.getDouble("humidity");

                String message ="";
                for(int i=0 ;i< jsonArray.length();i++){

                    JSONObject jsonPoint = jsonArray.getJSONObject(i);

                    String main= "";
                    String description= "";


                    main = jsonPoint.getString("main");
                    description = jsonPoint.getString("description");

                    if(main != null && description != null){

                        message += main +": " +description+ "\r\n";
                    }

                }

                if (message != null){

                    weatherDetails.setText(message);
                }

                tempDetail.setText(String.valueOf((Math.floor(temp-273.15)))+" "+ "\u2103" +
                                      "\r\n" + String.valueOf(humidity) + " %");


            } catch (JSONException e) {

                Toast.makeText(getApplicationContext(),"Could not find weather!",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
