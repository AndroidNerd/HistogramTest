package com.example.nerd.histogramtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.nerd.histogramtest.view.PerformanceHistogram;
import com.example.nerd.histogramtest.view.PerformanceMonth;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private PerformanceHistogram chart;
    private ArrayList<PerformanceMonth> arrayList;

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chart = (PerformanceHistogram) findViewById(R.id.chart);
        btn = (Button) findViewById(R.id.btn);
        arrayList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            PerformanceMonth performanceMonth = new PerformanceMonth();
            performanceMonth.setMonth(i + 1);
            performanceMonth.setSalesVolumes(new Random().nextDouble() * 500);
            arrayList.add(performanceMonth);
        }
        chart.setData(arrayList);
        chart.setOnMonthSelectedListener(new PerformanceHistogram.OnMonthSelectedListener() {
            @Override
            public void onMonthSelected(int index) {
                Toast.makeText(MainActivity.this, "--" + arrayList.get(index).getSalesVolumes(), Toast.LENGTH_SHORT).show();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrayList = new ArrayList<>();
                for (int i = 0; i < 12; i++) {
                    PerformanceMonth performanceMonth = new PerformanceMonth();
                    performanceMonth.setMonth(i + 1);
                    performanceMonth.setSalesVolumes(new Random().nextDouble() * 500);
                    arrayList.add(performanceMonth);
                }
                chart.setData(arrayList);
            }
        });
    }
}
