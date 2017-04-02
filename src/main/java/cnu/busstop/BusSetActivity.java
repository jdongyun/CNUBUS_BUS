package cnu.busstop;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



public class BusSetActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }


        final SharedPreferences pref = getSharedPreferences("busStop", MODE_PRIVATE);


        if(!pref.getString("busStyle", "NULL").equals("NULL") && !getIntent().getBooleanExtra("changeBus", false)) {
            //전자는 처음 시작일 때 데이터가 있는지 없는지 체크, 후자는 MainActivity에서 노선 변경 버튼을 눌렀을 때 Intent에서 전송되는 값
            Intent intent = new Intent(BusSetActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }

        if(MainActivity.connTimer != null) {
            MainActivity.connTimer.cancel();
        }

        setContentView(R.layout.activity_setbus);

        final SharedPreferences.Editor editor = pref.edit();

        Button buttonBusA = (Button) findViewById(R.id.button_bus_a);
        Button buttonBusB = (Button) findViewById(R.id.button_bus_b);
        Button buttonBusPause = (Button) findViewById(R.id.button_bus_pause);
        TextView tvBusSet = (TextView) findViewById(R.id.tv_bus_set);

        buttonBusA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("busStyle", "A")
                       .apply();
                Intent intent = new Intent(BusSetActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        buttonBusB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("busStyle", "B")
                        .apply();
                Intent intent = new Intent(BusSetActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });
        buttonBusPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("busStyle", "X")
                        .apply();
                Intent intent = new Intent(BusSetActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });
        tvBusSet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final EditText input = new EditText(BusSetActivity.this);
                if (!pref.getString("busID", "NULL").equals("NULL")) {
                    input.setText(pref.getString("busID", "NULL"));
                }

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(BusSetActivity.this);
                builder.setTitle("버스 ID를 입력해 주세요.")
                        .setView(input)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               editor.putString("busID", input.getText().toString()).apply();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

                return true;
            }
        });


    }

    @Override
    public void onBackPressed() {
        //뒤로가기를 방지한다.
        return;
    }


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
}
