package com.example.senso.budgetracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import static com.example.senso.budgetracker.R.id.circularTextView;

public class ExpenseActivity extends AppCompatActivity  {

    int idExpense;
    double costExpense;
    int type;
    expense exp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        Intent i = getIntent();
        type = i.getIntExtra("type",0);

        DBHelper dbh = new DBHelper(this);
        exp = dbh.fetchExpById(i.getIntExtra("id",0), type  ).getLeft();
        idExpense = dbh.fetchExpById(i.getIntExtra("id",0), type).getRight();


        CircularTextView ctv = (CircularTextView) findViewById(R.id.circularTextView);
        TextView tw1 = (TextView) findViewById(R.id.textview1);
        TextView tw2 = (TextView) findViewById(R.id.textview2);
        TextView tw3 = (TextView) findViewById(R.id.textview3);
        TextView tw4 = (TextView) findViewById(R.id.textview4);
        ImageButton iblocation = (ImageButton) findViewById(R.id.location);
        ImageButton ibremove = (ImageButton) findViewById(R.id.delete);
        String description;
        String cost = ""+exp.getCost()+" €";
        costExpense = exp.getCost();


        //Build the right layout

        if (type==1) {

            TextView point = (TextView) findViewById(R.id.point);

            point.setVisibility(View.GONE);
            iblocation.setVisibility(View.GONE);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)ibremove.getLayoutParams();

            params.removeRule(RelativeLayout.ABOVE);
            params.removeRule(RelativeLayout.END_OF);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            ibremove.setLayoutParams(params);

        }

        if (cost.length() > 6) {

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)ctv.getLayoutParams();
            params.setMargins(0, 0, 0, 30 - 300); //substitute parameters for left, top, right, bottom
            ctv.setLayoutParams(params);
            ctv.setText(cost);
        }
        else { ctv.setText(cost); }
        ctv.setStrokeWidth(100);
        ctv.setStrokeColor("#ffffff");
        ctv.setSolidColor("#000000");

        tw1.setText(exp.getName().substring(0, 1).toUpperCase() + exp.getName().substring(1));
        if (exp.getDescription().equals("")) { description = ""; } else { description = "'' "+exp.getDescription().substring(0, 1).toUpperCase() + exp.getDescription().substring(1)+" ''"; }
        tw2.setText(description);

        if (type == 0) {

            tw4.setText("Spesa Pianificata: "+exp.getCategory());
            tw3.setText("Svolta in data: "+exp.getDate());

        } else if(type == 1) {

            tw4.setText("Spesa Periodica: "+exp.getCategory());
            if (exp.getDate().matches("[0-9]+")) {
                tw3.setText("Da pagare ogni "+exp.getDate()+" del mese.");
            } else {
                tw3.setText("Da pagare ogni "+exp.getDate()+" della settimana.");
            }

        } else {
            tw4.setText("Spesa Normale: "+exp.getCategory());
            tw3.setText("Svolta in data: "+exp.getDate());
        }

        OnLocationButtonClick();
        OnDeleteButtonClick();

    }



    public void OnLocationButtonClick () {

        ImageButton iblocation = (ImageButton) findViewById(R.id.location);

        iblocation.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(ExpenseActivity.this, Locations.class);
                        Bundle arg = new Bundle();
                        arg.putParcelable("position", new LatLng(exp.getLat(),exp.getLng()));
                        i.putExtra("bundle", arg);
                        i.putExtra("from", "expense_activity");
                        startActivity(i);
                    }

                });

    }



    public void OnDeleteButtonClick() {

        ImageButton ibdelete = (ImageButton) findViewById(R.id.delete);

        ibdelete.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(ExpenseActivity.this);
                        builder.setTitle("Cancellazione Spesa");
                        builder.setMessage("Sei sicuro di voler cancellare la spesa?")
                                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        DBHelper dbh = new DBHelper(ExpenseActivity.this);
                                        Intent i = getIntent();
                                        dbh.removeFromId(type, idExpense);

                                        if (type != 1) {
                                            BudgetHelper bh = new BudgetHelper(ExpenseActivity.this);
                                            bh.setBackBudget(costExpense);
                                        }
                                        Intent intent = new Intent(ExpenseActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                })
                                .show();
                    }

                });

    }
}
