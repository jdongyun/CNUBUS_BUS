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
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    public static Timer connTimer;
    public LocationManager mLM;
    int GPS_SYNC_TIME = 1000;
    long TIMER_LOOP_TIME = 3000;
    int GPS_SYNC_METER = 1;
    Double latitude, longitude;
    float accuracy;
    Double tmp_latitude = 0.0, tmp_longitude = 0.0;
    String LOG_TAG = "CNUBUS_forBus";
    TextView tvBusStyle;
    TextView tvGPSProvider;
    TextView tvGPSLocation;


    TextView tvConnect;
    OkHttpClient client;

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

            tvBusStyle = (TextView) findViewById(R.id.tv_busstyle);
            tvGPSProvider = (TextView) findViewById(R.id.tv_gps_provider);
            tvGPSLocation = (TextView) findViewById(R.id.tv_gps_location);
            tvConnect = (TextView) findViewById(R.id.tv_svr_connect);

            tvConnect.setText("서버 전송 준비중입니다.");
            if(pref.getString("busID", "NULL").equals("NULL")) {
                ((TextView) findViewById(R.id.tv_bus_id)).setText("단말기 ID가 설정되지 않았습니다.");
            }





            Button btnChangeBus = (Button) findViewById(R.id.btn_changebus);
            btnChangeBus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, BusSetActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.putExtra("changeBus", true);
                    if(mLM != null)
                        mLM.removeUpdates(mLocationListener);
                    startActivity(intent);
                }
            });


            if(pref.getString("busStyle", "NULL").equals("X")) { //버스가 운행중이 아닐 때

                tvBusStyle.setText("중지");

                findViewById(R.id.tv_gps_provider).setVisibility(View.GONE);
                findViewById(R.id.tv_gps_location).setVisibility(View.GONE);

                client = new OkHttpClient();

                final Timer pushTimer = new Timer();

                pushTimer.schedule(new TimerTask(){
                    public void run() {

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
                                //인터넷 연결 등이 안되어 서버와의 통신이 불가능할 때

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        tvConnect.setText("인터넷 연결이 올바르지 않습니다.");
                                        tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                                    }
                                });
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                //인터넷 연결은 되어 있으나, 단말기 또는 서버에서 올바른 처리를 하지 못할 때
                                Log.i(LOG_TAG, "HTTP code is " + response.code());
                                try {
                                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tvConnect.setText("서버에 전송되었습니다.");
                                            tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
                                            pushTimer.cancel();
                                        }
                                    });
                                    JSONObject json = new JSONObject(response.body().string());
                                    Log.d(LOG_TAG, "SendTime is " + json.getInt("time"));

                                }catch (IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(response.code() == 403) {
                                                tvConnect.setText("전송 정보에 오류가 있습니다.");
                                                tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                                            } else if(response.code() == 400 || response.code() == 500 || response.code() == 502 || response.code() == 521) {
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
                }, 0, TIMER_LOOP_TIME);




            } else { //버스가 운행중일 때
                tvBusStyle.setText(pref.getString("busStyle", ""));

                mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        GPS_SYNC_TIME, GPS_SYNC_METER, mLocationListener);
                mLM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        GPS_SYNC_TIME, GPS_SYNC_METER, mLocationListener);

                client = new OkHttpClient();

                connTimer = new Timer();

                connTimer.schedule(new TimerTask(){
                    public void run() {
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
                                                tvConnect.setText("인터넷 연결이 올바르지 않습니다.");
                                                tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                                            }
                                        });
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, final Response response) throws IOException {
                                        Log.i(LOG_TAG, "HTTP code is " + response.code());
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
                                            Log.d(LOG_TAG, "sendTime is " + json.getInt("time"));

                                        }catch (IOException e) {
                                            runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    if(response.code() == 403) {
                                                        tvConnect.setText("전송 정보에 오류가 있습니다.");
                                                        tvConnect.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                                                    } else if(response.code() == 400 || response.code() == 500 || response.code() == 502 || response.code() == 521) {
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
                    }
                }, 0, TIMER_LOOP_TIME);
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
    public void onDestroy() {
        super.onDestroy();
        if(mLM != null)
            mLM.removeUpdates(mLocationListener);
        if(connTimer != null)
            connTimer.cancel();
    }
}
