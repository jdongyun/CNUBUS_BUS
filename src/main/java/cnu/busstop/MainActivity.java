package cnu.busstop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int GPS_SYNC_TIME = 1000;
    int GPS_SYNC_METER = 1;
    String LOG_TAG = "CNUBUS_forBus";

    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvAccuracy;
    private EditText etLatLong;

    LocationManager mLM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvLatitude = (TextView) findViewById(R.id.tv_latitude);
        tvLongitude = (TextView) findViewById(R.id.tv_longitude);
        tvAccuracy = (TextView) findViewById(R.id.tv_accuracy);
        etLatLong = (EditText) findViewById(R.id.et_latlong);

        SharedPreferences pref = getSharedPreferences("busStop", MODE_PRIVATE);
        if(pref.getString("busStyle", "NULL").equals("NULL")) { //처음 실행시 버스종류 지정이 안됐을 때
            startActivity(new Intent(MainActivity.this, BusSetActivity.class));

        } else { //버스 종류가 지정되어 있을 때

            setContentView(R.layout.activity_main);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            }


            mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    GPS_SYNC_TIME, GPS_SYNC_METER, mLocationListener);
            mLM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    GPS_SYNC_TIME, GPS_SYNC_METER, mLocationListener);

/*
            final Handler handler = new Handler();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        //final double latitude = location.getLatitude();

                        //final double longitude = location.getLongitude();



                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //tvLatitude.setText(Long.toString(System.currentTimeMillis() / 1000L) + "\n");
                                //tvLongitude.setText(Double.toString(longitude));
                            }
                        });

                        //Log.i("CNUBUS", "long : " + longitude + ", lati : " + latitude);

                        try {
                            Thread.sleep(GPS_SYNC_TIME);
                        } catch (InterruptedException e) {
                            Log.e("CNUBUS", "Thread.sleep ERROR");
                        }

                    }
                }
            }).start();*/


        }

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {

                    Toast.makeText(this, "권한 오류!", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();    //경도
            double latitude = location.getLatitude();         //위도
            float accuracy = location.getAccuracy();        //정확도
            Log.i(LOG_TAG, latitude + "," + longitude + " accuracy : " + accuracy);

            tvLatitude.setText(Double.toString(latitude));
            tvLongitude.setText(Double.toString(longitude));
            tvAccuracy.setText(Float.toString(accuracy));
            etLatLong.setText(latitude + "," + longitude);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
}
