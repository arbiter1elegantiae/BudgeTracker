package com.example.senso.budgetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Statistics extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        DBHelper dbh = new DBHelper(Statistics.this);
        SharedPreferences sharedPref = getSharedPreferences("FirstTime", Context.MODE_PRIVATE);
        final HashMap<Integer, String> numMap = new HashMap<>();
        List<Pair<String, Double>> dataObjects = new ArrayList<Pair<String, Double>>();

        if (sharedPref.getString("period",null).equals("Settimanale")) {
            //display weekly statistics


            dataObjects = dbh.retrieveWeekly();
            if (dataObjects.size() != 0) {

                //procered building chart

                for (int i = 0; i < dataObjects.size(); i++) {

                    numMap.put(i, dataObjects.get(i).getLeft());
                }

                LineChart chart = (LineChart) findViewById(R.id.chart);

                List<Entry> entries = new ArrayList<Entry>();

                for (int i = 0; i < dataObjects.size(); i++) {

                    // turn your data into Entry objects
                    entries.add(new Entry(i, dataObjects.get(i).getRight().floatValue()));
                }


                LineDataSet dataSet = new LineDataSet(entries, "Andamento spese totali Settimanali"); // addEntries to dataset
                LineData lineData = new LineData(dataSet);

                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new IAxisValueFormatter() {

                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {

                        return numMap.get((int) value);
                    }

                });

                chart.setData(lineData);
                chart.getDescription().setText("Asse X: Data  Asse Y:Euro spesi in quella settimana");
                chart.invalidate(); // refresh
            }

        } else {

            //display monthly statistics

            dataObjects = dbh.retrieveMonthly();

            if (dataObjects.size() != 0) {

                for (int i = 0; i < dataObjects.size(); i++) {

                    numMap.put(i, dataObjects.get(i).getLeft());
                }

                LineChart chart = (LineChart) findViewById(R.id.chart);

                List<Entry> entries = new ArrayList<Entry>();

                for (int i = 0; i < dataObjects.size(); i++) {

                    // turn your data into Entry objects
                    entries.add(new Entry(i, dataObjects.get(i).getRight().floatValue()));
                }


                LineDataSet dataSet = new LineDataSet(entries, "Andamento spese totali Mensili"); // addEntries to dataset
                LineData lineData = new LineData(dataSet);

                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new IAxisValueFormatter() {

                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {

                        return numMap.get((int) value);
                    }

                });

                chart.setData(lineData);
                chart.getDescription().setText("Asse X: Data  Asse Y:Euro spesi in quel mese");
                chart.invalidate(); // refresh
            }
        }

        //display categories chart

        BarChart barChart = (BarChart) findViewById(R.id.barchart);
        ArrayList<String> BarEntryLabels = new ArrayList<String>();
        List<BarEntry> Barentries = new ArrayList<BarEntry>();


        List<Pair<String, Double>> categoriesObjects = new ArrayList<Pair<String, Double>>();
        categoriesObjects = dbh.retrieveCategories();


        for (int i = 0; i < categoriesObjects.size(); i++) {

            // build x-axsis
            BarEntryLabels.add(categoriesObjects.get(i).getLeft());
        }


        for (int i = 0; i < categoriesObjects.size(); i++) {

            // build values
            Barentries.add(new BarEntry(i, categoriesObjects.get(i).getRight().floatValue()));
        }


        BarDataSet dataSet = new BarDataSet(Barentries, "Categorie");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(dataSet);

        BarData data = new BarData(dataSets);


        barChart.setTouchEnabled(false);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(BarEntryLabels));
        barChart.getXAxis().setGranularity(1);
        barChart.getXAxis().setGranularityEnabled(true);

        Description description = new Description();
        description.setText("Spesa complessiva per categoria");
        barChart.setDescription(description);
        barChart.invalidate();

    }
}
