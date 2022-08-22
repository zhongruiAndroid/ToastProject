package com.test.toastproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.toast.ToastUtils;

import androidx.appcompat.app.AppCompatActivity;

public class Test2Activity extends AppCompatActivity {

    private Button btShow1;
    private Button btShow2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test2_act);
        initView();
    }

    private void initView() {
        btShow1 = findViewById(R.id.btShow1);
        btShow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                ToastUtils.showToastFinish("短toast_finish");
            }
        });
        btShow2 = findViewById(R.id.btShow2);
        btShow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                ToastUtils.showToastFinish("长toast_finish");
            }
        });
    }
}