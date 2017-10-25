package com.example.senso.budgetracker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

public class reportActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        DBHelper dbh = new DBHelper(this);

        List<expense> normal = dbh.retrieveNormal();
        List<expense> periodic = dbh.retrievePeriodic();
        List<expense> planned = dbh.retrievePlanned();

        TextView nexpenseTw = (TextView) findViewById(R.id.content_nexpense);
        TextView plexpenseTw = (TextView) findViewById(R.id.content_plexpense);
        TextView peexpenseTw = (TextView) findViewById(R.id.content_pexpense);
        TextView date = (TextView) findViewById(R.id.date);

        //populate layout

        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

        date.setText(fDate);

        for ( int i = 0; i < normal.size(); i++) {

            String[] expString = normal.get(i).toString().split(",");

            String tmpString = "- Nome:Costo "+expString[0].toUpperCase()+", Categoria: "+expString[1]+", Descrizione: "+expString[2]+", Data: "+expString[3]+". \n";
            nexpenseTw.append(tmpString);
        }

        for ( int i = 0; i < planned.size(); i++) {

            String[] expString = planned.get(i).toString().split(",");

            String tmpString = "- Nome:Costo "+expString[0].toUpperCase()+", Categoria: "+expString[1]+", Descrizione: "+expString[2]+", Data: "+expString[3]+". \n";
            plexpenseTw.append(tmpString);
        }

        for ( int i = 0; i < periodic.size(); i++) {

            String[] expString = periodic.get(i).toString().split(",");

            String tmpString = "- Nome:Costo "+expString[0].toUpperCase()+", Categoria: "+expString[1]+", Descrizione: "+expString[2]+", Data: "+expString[3]+". \n";
            peexpenseTw.append(tmpString);
        }





        // write the document, ask for permissions first

        boolean hasPermission = (ContextCompat.checkSelfPermission(reportActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {

            // create a new document

            if (isExternalStorageWritable()) {

                File myExternalFile = new File(getStorageDir("BudgeTracker"), "snapshot.pdf");

                try {
                    myExternalFile.createNewFile();
                } catch (IOException e) {
                    Log.e("TAG", "Explanation of what was being attempted when the exception was thrown", e);
                }



                PdfDocument document = new PdfDocument();

                LayoutInflater inflater = LayoutInflater.from(this);
                View content = (RelativeLayout) findViewById(R.id.activity_reportt);


                // crate a page description
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1000,1000 , 1).create();

                // start a page
                PdfDocument.Page page = document.startPage(pageInfo);
                int measureWidth = View.MeasureSpec.makeMeasureSpec(page.getCanvas().getWidth(), View.MeasureSpec.EXACTLY);
                int measuredHeight = View.MeasureSpec.makeMeasureSpec(page.getCanvas().getHeight(), View.MeasureSpec.EXACTLY);

                content.measure(measureWidth, measuredHeight);
                content.layout(0, 0, page.getCanvas().getWidth(), page.getCanvas().getHeight());

                // draw something on the page
                content.draw(page.getCanvas());

                // finish the page
                document.finishPage(page);

                // write the document content
                try {
                    document.writeTo(new FileOutputStream(myExternalFile));
                    Toast.makeText(this, "File snapshot.pdf creato e salvato!", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e("TAG", "Explanation of what was being attempted when the exception was thrown", e);
                }

                // close the document
                document.close();

            }

        }

    }



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public File getStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) , albumName);
        if (!file.mkdirs()) {
            Log.e("error: ", "Directory not created");
        }
        return file;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Intent i = new Intent(this, reportActivity.class);
                    startActivity(i);
                } else
                {
                    Toast.makeText(this, "All'applicazione non è stato permesso di scrivere sulla memoria esterna, quindi può non funzionare correttamente. Perfavore considera di attivare i permessi.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


}
