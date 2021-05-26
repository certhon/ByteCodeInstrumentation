package com.example.bytecodeinstrumentation;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.costannotation.MethodCost;

public class JavassitActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    hello();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @MethodCost
    public  void hello() throws Exception{
        new Test().test();

    }
}
