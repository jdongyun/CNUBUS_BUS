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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    int GPS_SYNC_TIME = 1000;
    int GPS_SYNC_METER = 1;
    boolean THREAD_LOOP= true;

    Double latitude, longitude;
    float accuracy;
    Double tmp_latitude = 0.0, tmp_longitude = 0.0;

    String LOG_TAG = "CNUBUS_forBus";

    TextView tvBusStyle;
    TextView tvLatitude;
    TextView tvLongitude;
    TextView tvGPSProvider;

    LocationManager mLM;

    OkHttpClient client;
    Thread connThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        SharedPreferences pref = getSharedPreferences("busStop", MODE_PRIVATE);
        if(pref.getString("busStyle", "NULL").equals("NULL")) { //처음 실행시 버스종류 지정이 안됐을 때
            startActivity(new Intent(MainActivity.this, BusSetActivity.class));

        } else { //버스 종류가 지정되어 있을 때
            setContentView(R.layout.activity_main);

            tvBusStyle = (TextView) findViewById(R.id.tv_busstyle);
            tvLatitude = (TextView) findViewById(R.id.tv_latitude);
            tvLongitude = (TextView) findViewById(R.id.tv_longitude);
            tvGPSProvider = (TextView) findViewById(R.id.tv_gps_provider);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            }


            Button btnChangeBus = (Button) findViewById(R.id.btn_changebus);
            btnChangeBus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, BusSetActivity.class);
                    intent.putExtra("changeBus", true);
                    startActivity(intent);
                }
            });


            if(pref.getString("busStyle", "NULL").equals("X")) { //버스가 운행중이 아닐 때
                tvBusStyle.setText("중지");


            } else { //버스가 운행중일 때
                tvBusStyle.setText(pref.getString("busStyle", ""));

                mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        GPS_SYNC_TIME, GPS_SYNC_METER, mLocationListener);
                mLM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        GPS_SYNC_TIME, GPS_SYNC_METER, mLocationListener);

                client = new OkHttpClient();

                connThread = new Thread() {
                    @Override
                    public void run() {

                        while(THREAD_LOOP) {
                            try {
                                if(latitude != null &&
                                        tmp_latitude != latitude &&
                                        tmp_longitude != longitude) {

                                    RequestBody formBody = new FormBody.Builder()
                                            .add("id", "q555")
                                            .add("lat", String.valueOf(latitude))
                                            .add("lng", String.valueOf(longitude))
                                            .add("accu", String.valueOf(accuracy))
                                            .add("route", pref.getString("busStyle", "NULL"))
                                            .add("time", String.valueOf(System.currentTimeMillis()/1000L))
                                            .build();

                                    Request request = new Request.Builder()
                                            .url("https://cnubus.dyun.kr/location/")
                                            .post(formBody)
                                            .build();
                                    tmp_latitude = latitude;
                                    tmp_longitude = longitude;

                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                                            Headers responseHeaders = response.headers();
                                            for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                                                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                                            }

                                            System.out.println(response.body().string());
                                        }
                                    });
                                }

                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }




                        }


                    }
                };
                connThread.start();

            }






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
            longitude = location.getLongitude();    //경도
            latitude = location.getLatitude();         //위도
            accuracy = location.getAccuracy();        //정확도
            Log.i(LOG_TAG, latitude + "," + longitude + " accuracy : " + accuracy);
            if(longitude != null) {


                tvLatitude.setText(Double.toString(latitude));
                tvLongitude.setText(Double.toString(longitude));
                //tvAccuracy.setText(Float.toString(accuracy));

                if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    tvGPSProvider.setText("GPS에 연결되었습니다.\n정확도 : " + accuracy);
                } else {
                    tvGPSProvider.setText("아직 GPS에 연결되지 않았습니다.");
                }
            }


        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    public void onBackPressed() {
        //뒤로가기를 방지한다.
        return;
    }

    @Override
    protected void onStop() {
        //노선 변경으로 Activity가 변경되었을 때 LocationManager의 Update를 해제한다.
        super.onStop();
        if(mLM != null) //운행이 중지되었을 때에는 LocationManager이 할당되지 않았으므로 NPE가 발생함.
            mLM.removeUpdates(mLocationListener);
        if(connThread != null) {
            connThread.interrupt();
            THREAD_LOOP = false; //Thread에서 더 이상 LOOP를 돌리지 않음
        }
    }
}
