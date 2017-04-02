package cnu.busstop;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BusSetActivity extends AppCompatActivity {
    public static boolean isRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final SharedPreferences pref = getSharedPreferences("busStop", MODE_PRIVATE);


        if(!pref.getString("busStyle", "NULL").equals("NULL") && !getIntent().getBooleanExtra("changeBus", false)) {
            //전자는 처음 시작일 때 데이터가 있는지 없는지 체크, 후자는 MainActivity에서 노선 변경 버튼을 눌렀을 때 Intent에서 전송되는 값
            Intent intent = new Intent(BusSetActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }

        Log.i("CNUBUS", "check is " + pref.getBoolean("mLM", false));

        if(getIntent().getBooleanExtra("mLM", false)) {

            isRun = false;
            Log.i("CNUBUS", "bool is " + isRun);
            MainActivity.THREAD_LOOP = false;

            MainActivity.connThread.interrupt();
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
                MainActivity.THREAD_LOOP = true;
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
}
