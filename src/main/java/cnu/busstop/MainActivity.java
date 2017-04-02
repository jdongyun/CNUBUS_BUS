package cnu.busstop;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
    public static boolean THREAD_LOOP = true;
    public static boolean X_LOOP = true;

    Double latitude, longitude;
    float accuracy;
    Double tmp_latitude = 0.0, tmp_longitude = 0.0;

    String LOG_TAG = "CNUBUS_forBus";

    TextView tvBusStyle;
    TextView tvGPSProvider;
    TextView tvGPSLocation;
    TextView tvConnect;

    public LocationManager mLM;

    OkHttpClient client;
    public static Thread connThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences pref = getSharedPreferences("busStop", MODE_PRIVATE);
        if(pref.getString("busStyle", "NULL").equals("NULL")) { //처음 실행시 버스종류 지정이 안됐을 때
            Intent intent = new Intent(MainActivity.this, BusSetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(intent);

        } else { //버스 종류가 지정되어 있을 때
            setContentView(R.layout.activity_main);

            THREAD_LOOP = true;

            tvBusStyle = (TextView) findViewById(R.id.tv_busstyle);
            tvGPSProvider = (TextView) findViewById(R.id.tv_gps_provider);
            tvGPSLocation = (TextView) findViewById(R.id.tv_gps_location);
            tvConnect = (TextView) findViewById(R.id.tv_svr_connect);

            tvConnect.setText("서버 전송 준비중입니다.");

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            }


            Button btnChangeBus = (Button) findViewById(R.id.btn_changebus);
            btnChangeBus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, BusSetActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.putExtra("changeBus", true);
                    if(mLM != null) {
                        Log.i("CNUBUS", "mLM is not null");
                        intent.putExtra("mLM", true);
                        mLM.removeUpdates(mLocationListener);
                    }else {
                        Log.i("CNUBUS", "mLM is null");
                    }
                    startActivity(intent);
                }
            });


            if(pref.getString("busStyle", "NULL").equals("X")) { //버스가 운행중이 아닐 때
                tvBusStyle.setText("중지");

                client = new OkHttpClient();


                new Thread() {
                    @Override
                    public void run() {
                        X_LOOP = true;
                        while(X_LOOP) {

                            RequestBody formBody = new FormBody.Builder()
                                    .add("id", pref.getString("busID", "NULL"))
                                    .add("lat", "0")
                                    .add("lng", "0")
                                    .add("accu", "0")
                                    .add("route", "X")
                                    .add("time", String.valueOf(System.currentTimeMillis()/1000L))
                                    .add("key", "xfjNb4WqiOPVLdR")
                                    .build();

                            Request request = new Request.Builder()
                                    .url("https://cnubus.dyun.kr/location/")
                                    .post(formBody)
                                    .build();

                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    //아예 서버통신이 불가능할 때(인터넷통신이 안된다거나)

                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            tvConnect.setText("서버와의 통신이 불가능합니다.");
                                            tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                                        }
                                    });
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(Call call, final Response response) throws IOException {
                                    try {
                                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                                        runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                tvConnect.setText("서버에 전송되었습니다.");
                                                tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
                                                X_LOOP = false;
                                            }
                                        });
                                        JSONObject json = new JSONObject(response.body().string());
                                        Log.d(LOG_TAG, "JSON is " + json.getInt("time"));

                                    }catch (IOException e) {
                                        runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                if(response.code() == 403) {
                                                    tvConnect.setText("전송 정보에 오류가 있습니다.");
                                                    tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                                                } else if(response.code() == 502 || response.code() == 521) {
                                                    tvConnect.setText("서버에 오류가 있습니다.");
                                                    tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                                                }

                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }


                    }
                }.start();


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
                                if(latitude != null/* &&
                                        tmp_latitude != latitude &&
                                        tmp_longitude != longitude*/) {

                                    RequestBody formBody = new FormBody.Builder()
                                            .add("id", pref.getString("busID", "NULL"))
                                            .add("lat", String.valueOf(latitude))
                                            .add("lng", String.valueOf(longitude))
                                            .add("accu", String.valueOf(accuracy))
                                            .add("route", pref.getString("busStyle", "NULL"))
                                            .add("time", String.valueOf(System.currentTimeMillis()/1000L))
                                            .add("key", "xfjNb4WqiOPVLdR")
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
                                            runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    tvConnect.setText("서버와의 통신이 불가능합니다.");
                                                    tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                                                }
                                            });
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(Call call, final Response response) throws IOException {
                                            Log.i(LOG_TAG, "code is " + response.code());
                                            try {
                                                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        tvConnect.setText("서버와 정상적으로 통신 중입니다.");
                                                        tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));

                                                    }
                                                });
                                                JSONObject json = new JSONObject(response.body().string());
                                                Log.d(LOG_TAG, "JSON is " + json.getInt("time"));

                                            }catch (IOException e) {
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        if(response.code() == 403) {
                                                            tvConnect.setText("전송 정보에 오류가 있습니다.");
                                                            tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                                                        } else if(response.code() == 502 || response.code() == 521) {
                                                            tvConnect.setText("서버에 오류가 있습니다.");
                                                            tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                                                        }

                                                    }
                                                });
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
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

                tvGPSLocation.setText("위도 : " +Double.toString(latitude)+"\n경도 : " + Double.toString(longitude));

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



        Log.d("TestAppActivity", "onStop");
        /*
        if(mLM != null) //운행이 중지되었을 때에는 LocationManager이 할당되지 않았으므로 NPE가 발생함.
            mLM.removeUpdates(mLocationListener);
        if(connThread != null) {
            connThread.interrupt();
            THREAD_LOOP = false; //Thread에서 더 이상 LOOP를 돌리지 않음
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TestAppActivity", "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("TestAppActivity", "onRestart");
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("TestAppActivity", "onPostResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TestAppActivity", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TestAppActivity", "onResume");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onPostCreate", "onDestroy");
    }
}
