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

        setContentView(R.layout.activity_setbus);

        SharedPreferences pref = getSharedPreferences("busStop", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        pref.getString("busStyle", "NULL");


        Button buttonBusA = (Button) findViewById(R.id.button_bus_a);
        Button buttonBusB = (Button) findViewById(R.id.button_bus_b);

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


    }
}
