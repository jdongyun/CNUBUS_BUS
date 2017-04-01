package cnu.busstop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class BusSetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getSharedPreferences("busStop", MODE_PRIVATE);

        if(!pref.getString("busStyle", "NULL").equals("NULL") && !getIntent().getBooleanExtra("changeBus", false)) {
            //전자는 처음 시작일 때 데이터가 있는지 없는지 체크, 후자는 MainActivity에서 노선 변경 버튼을 눌렀을 때 Intent에서 전송되는 값
            startActivity(new Intent(BusSetActivity.this, MainActivity.class));
        }

        setContentView(R.layout.activity_setbus);

        final SharedPreferences.Editor editor = pref.edit();

        Button buttonBusA = (Button) findViewById(R.id.button_bus_a);
        Button buttonBusB = (Button) findViewById(R.id.button_bus_b);
        Button buttonBusPause = (Button) findViewById(R.id.button_bus_pause);

        buttonBusA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               editor.putString("busStyle", "A")
                       .apply();
                startActivity(new Intent(BusSetActivity.this, MainActivity.class));
            }
        });

        buttonBusB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("busStyle", "B")
                        .apply();
                startActivity(new Intent(BusSetActivity.this, MainActivity.class));
            }
        });
        buttonBusPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("busStyle", "X")
                        .apply();
                startActivity(new Intent(BusSetActivity.this, MainActivity.class));
            }
        });


    }

    @Override
    public void onBackPressed() {
        //뒤로가기를 방지한다.
        return;
    }
}
