package com.example.weight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/*
TODO:Import
TODO:Weekly Change
*/

public class MainActivity extends AppCompatActivity implements AddValuesDialog.DialogListener, DeleteValueDialog.DialogListener {

    private static final int FIRSTROW = 2;

    private ArrayList<double[]> rows = new ArrayList<double[]>();
    private AddValuesDialog addValuesDialog = new AddValuesDialog();
    private DeleteValueDialog deleteValueDialog = new DeleteValueDialog();

    private DecimalFormat f = new DecimalFormat("#.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ToggleButton dailyButton = findViewById(R.id.dailyChangeButton);
        final ToggleButton weeklyButton = findViewById(R.id.weeklyChangeButton);

        dailyButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                dailyButton.setChecked(true);
                weeklyButton.setChecked(false);
            }
        });
        weeklyButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                weeklyButton.setChecked(true);
                dailyButton.setChecked(false);
            }
        });

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addValuesDialog.show(getSupportFragmentManager(), "values picker");
            }
        });

        //rows.add(new double[]{new Date(2020-1900,04-1,7).getTime(),157,15.4,44.7});
        //rows.add(new double[]{new Date(2020-1900,04-1,8).getTime(),156.8,15.2,44.8});

        readFile();
        refresh();
    }

    private void clearTable(){
        TableLayout table = findViewById(R.id.table);
        int tableSize = table.getChildCount();
        for (int i = 0; i < tableSize; i++){
            table.removeView(table.getChildAt(FIRSTROW));
        }
    }

    private void refresh(){
        clearTable();
        sort();
        for (double[] row:rows){
            addRow(row);
        }

        TableLayout table = findViewById(R.id.table);

        TableRow changeRow = (TableRow) table.getChildAt(0);
        TableRow firstRow = (TableRow) table.getChildAt(FIRSTROW);
        TableRow lastRow = (TableRow) table.getChildAt(table.getChildCount()-1);
        if (changeRow != null && firstRow != null && lastRow != null) {
            for (int i = 2; i < changeRow.getChildCount(); i += 2) {
                TextView totalChangeVal = (TextView) changeRow.getChildAt(i);
                TextView firstRowVal = (TextView) firstRow.getChildAt(i - 1);
                TextView lastRowVal = (TextView) lastRow.getChildAt(i - 1);

                double firstRowValDouble = Double.valueOf((String) firstRowVal.getText());
                double lastRowValDouble = Double.valueOf((String) lastRowVal.getText());

                totalChangeVal.setText(f.format(firstRowValDouble - lastRowValDouble));
            }
        }
        System.out.println("refresh");
        saveFile("daily.csv");
    }

    private void readFile(){
        BufferedReader in;
        String line;
        try{
            in = new BufferedReader(new InputStreamReader(this.openFileInput("daily.csv")));
            while ((line = in.readLine()) != null) {
                String[] val = line.split(",");
                rows.add(new double[]{Double.valueOf(val[0]),Double.valueOf(val[1]),Double.valueOf(val[2]),Double.valueOf(val[3])});
            }
            in.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String location){
        PrintWriter o = null;
        try {
            o = new PrintWriter(this.openFileOutput("daily.csv", MODE_PRIVATE));
            for (double[] row:rows){
                for (int i = 0; i < 4; i++){
                    o.print(row[i]);
                    if (i < 3) o.print(',');
                }
                o.println();
            }
            o.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("saveFile");
    }

    public void exportFile(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            String baseFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            System.out.println(baseFolder + File.separator);

            PrintWriter o = null;
            try {
                FileOutputStream output = new FileOutputStream(baseFolder + File.separator + "data.csv");
                o = new PrintWriter(output);
                for (double[] row:rows){
                    for (int i = 0; i < 4; i++){
                        o.print(row[i]);
                        if (i < 3) o.print(',');
                    }
                    o.println();
                }
                o.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("exportFile");
        }
    }

    private void addRow(double[] data) {
        TableLayout table = findViewById(R.id.table);

        TableRow tr = new TableRow(this);
        tr.setBackgroundColor(Color.LTGRAY);
        tr.setPadding(0,15,0,15);
        tr.setId(table.getChildCount()-FIRSTROW);
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteValueDialog.setView(view);
                deleteValueDialog.show(getSupportFragmentManager(), "value Deleter");
            }
        });

        TextView dateText = new TextView(this);
        Date date = new Date((long)data[0]);
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yy");
        dateText.setText(df.format(date));
        dateText.setGravity(Gravity.CENTER);
        tr.addView(dateText);

        TextView LBM = null;
        TextView MM = null;

        for (int i = 1; i < data.length+2; i++) {
            TextView val = new TextView(this);
            val.setGravity(Gravity.CENTER);
            if (i < data.length) {
                val.setText(f.format(data[i]));
            }else if (i == data.length){
                TextView weight = (TextView) tr.getChildAt(1);
                TextView fat = (TextView) tr.getChildAt(3);
                LBM = val;
                val.setText(f.format(Double.valueOf((String) weight.getText()) * (1-Double.valueOf((String) fat.getText())/100)));
            }else if (i == data.length + 1) {
                TextView weight = (TextView) tr.getChildAt(1);
                TextView muscle = (TextView) tr.getChildAt(5);
                MM = val;
                val.setText(f.format(Double.valueOf((String) weight.getText()) * (Double.valueOf((String) muscle.getText())/100)));
            }

            TextView valChange = new TextView(this);
            try {
                TableRow lastRow = (TableRow) table.getChildAt(FIRSTROW);
                System.out.println("KHJSFADKJSLKFADKJFDKJSFDAHKSDFKLDFKLHKSFDKASADKHJSFADHKJFSHJKSADA "  + lastRow);
                TextView lastValText = (TextView) lastRow.getChildAt(2*i-1);
                double lastVal = Double.valueOf((String) lastValText.getText());
                if (lastVal > 0){
                    double valChangeDouble = 0;
                    if (i < data.length) {
                        valChangeDouble = data[i] - lastVal;
                    }else if (i == data.length) {
                        valChangeDouble = Double.valueOf((String) LBM.getText()) - lastVal;
                    }else if (i == data.length + 1){
                        valChangeDouble = Double.valueOf((String) MM.getText()) - lastVal;
                    }
                    if (valChangeDouble >= 0){
                        valChange.setTextColor(Color.GREEN);
                        valChange.setText("+"+f.format(valChangeDouble));
                    }else {
                        valChange.setTextColor(Color.RED);
                        valChange.setText(f.format(valChangeDouble));
                    }
                }else{
                    valChange.setText("0");
                }
            }catch(Exception e){
                valChange.setText("0");
                e.printStackTrace();
            }
            valChange.setGravity(Gravity.CENTER);

            tr.addView(val);
            tr.addView(valChange);
        }

        table.addView(tr, FIRSTROW);
    }

    private void sort(){
        rows.sort(new Comparator<double[]>() {
            public int compare(double[] d1, double[] d2) {
                if (d1[0] > d2[0]) return 1;
                else if (d1[0] < d2[0]) return -1;
                return 0;
            }});
    }

    @Override
    public void sendData(String dateString, double weight, double fat, double muscle) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        rows.add(new double[]{Double.valueOf(date.getTime()),weight,fat,muscle});
        refresh();
    }

    @Override
    public void deleteValue(View view) {
        rows.remove(view.getId());
        refresh();
    }

}
