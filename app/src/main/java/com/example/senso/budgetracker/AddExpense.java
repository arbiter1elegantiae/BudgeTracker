package com.example.senso.budgetracker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static com.example.senso.budgetracker.DBHelper.TABLE2_NAME;

public class AddExpense extends AppCompatActivity {

    public Button categoryDialogButton;
    public Button storeExpenseButton;
    public Button locationExpenseButton;
    public EditText nameText;
    public EditText costText;
    public EditText descriptionText;
    public String category = null;
    public String date = null;
    public LatLng spot = null;
    ArrayAdapter<String> arrayAdapter;
    List<String> removedFromAdapter = new ArrayList<String>();

    int REQUEST_CODE = 1;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //which type of expense do we want to add?
        Intent intent = getIntent();
        type = intent.getStringExtra("Expense Type");
        switch (type) {

            case "normal":
                setContentView(R.layout.activity_add_expense);
                onButtonLocationClickListener();
                break;
            case "planned":
                setContentView(R.layout.activity_add_expense_planned);
                onButtonLocationClickListener();
                break;
            case "periodic":
                setContentView(R.layout.activity_add_expense_periodic);
                break;

        }
        onButtonCategoryClickListener();
        onButtonStoreClickListener();
    }



    public void onButtonCategoryClickListener() {

        //setting adapter for dialog content
        DBHelper dbh = new DBHelper(AddExpense.this);
        List<String> categoriesContent = dbh.readCategories();
        arrayAdapter  = new ArrayAdapter<String>(AddExpense.this, android.R.layout.select_dialog_singlechoice, categoriesContent);

        categoryDialogButton = (Button) findViewById(R.id.category_btn);
        categoryDialogButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {//setting up alert dialog
                        final AlertDialog.Builder aBuilder = new AlertDialog.Builder(AddExpense.this);
                        aBuilder.setTitle("Scegli la categoria")
                                .setIcon(R.mipmap.ic_launcher);


                        aBuilder.setPositiveButton("Aggiungi categoria", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                AlertDialog.Builder builderInner = new AlertDialog.Builder(AddExpense.this);
                                builderInner.setTitle("Inserisci il nome");
                                final EditText input = new EditText(AddExpense.this);
                                builderInner.setView(input);

                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String newCategory = input.getText().toString();

                                        //check if the category already exists
                                        if (arrayAdapter.getPosition(newCategory.trim()) != -1) {

                                            showToast("La categoria esiste già");
                                        } else {

                                            arrayAdapter.add(newCategory);
                                            arrayAdapter.notifyDataSetChanged();
                                        }
                                        dialog.cancel();
                                    }
                                });
                                builderInner.show();
                            }
                        });

                        aBuilder.setNegativeButton("Rimuovi categoria", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                AlertDialog.Builder builderInner = new AlertDialog.Builder(AddExpense.this);
                                builderInner.setTitle("Inserisci il nome");
                                final EditText input = new EditText(AddExpense.this);
                                builderInner.setView(input);

                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String oldCategory = input.getText().toString().trim();
                                        //check if the category do not exists
                                        if (arrayAdapter.getPosition(oldCategory) == -1) {

                                            showToast("La categoria non esiste!");
                                        } else {

                                            arrayAdapter.remove(oldCategory);
                                            arrayAdapter.notifyDataSetChanged();
                                            removedFromAdapter.add(oldCategory);
                                        }
                                        dialog.cancel();
                                    }
                                });
                                builderInner.show();
                            }
                        });

                        aBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final String strName = arrayAdapter.getItem(which);
                                AlertDialog.Builder builderInner = new AlertDialog.Builder(AddExpense.this);
                                builderInner.setMessage(strName);
                                builderInner.setTitle("Hai scelto");
                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //save choice
                                        category = strName;
                                        categoryDialogButton.setError(null);
                                        dialog.dismiss();
                                    }
                                });
                                builderInner.show();
                            }
                        })
                        .show();
                    }
                });
    }

    //store expense in the DB
    public void onButtonStoreClickListener() {

        storeExpenseButton = (Button) findViewById(R.id.store_expense);
        storeExpenseButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        nameText = (EditText) findViewById(R.id.name_expense);
                        descriptionText = (EditText) findViewById(R.id.description_exprense);
                        costText = (EditText) findViewById(R.id.cost_expense);

                        String expenseName = nameText.getText().toString().trim();
                        String description = descriptionText.getText().toString().trim();

                        //mandatory fields checks
                        if (expenseName.equals("")){

                            nameText.setError( "Il nome è obbligatorio!" );
                        } else if (TextUtils.isEmpty(category)) {

                            categoryDialogButton.setError( "La categoria è obbligatoria!" );
                        } else if (costText.getText().toString().equals("")) {

                            costText.setError("Il costo è obbligatorio!");
                        } else if (date == null) {

                            Button dateBtn = (Button) findViewById(R.id.date_button);
                            if (type.equals("normal") || type.equals("planned")) {

                                dateBtn.setError("La data è obbligatoria!");
                            } else {

                                dateBtn.setError("Il periodo è obbligatorio!");
                            }
                        } else {

                            Float cost = Float.valueOf(costText.getText().toString().trim());

                            //creating an DBH instance
                            DBHelper dbh = new DBHelper(getApplicationContext());

                            //creating an expense object
                            expense newExp = new expense(expenseName, category, cost, date);
                            newExp.setDescription(description);
                            newExp.setSpot(spot);

                            String text;
                            if (dbh.addExpense(newExp, type) == -1) {

                                text = "Errore: salvataggio non riuscito!";
                            } else {

                                //update budget value
                                if (type.equals("normal")) {
                                    BudgetHelper bh = new BudgetHelper(AddExpense.this);
                                    bh.setBudget(cost);

                                    //add expense to relative category
                                    dbh.addAmountCategory(category, cost);
                                }
                                text = "Spesa inserita";
                            }
                            showToast(text);
                            dbh.close();
                        }
                    }
                }
        );
    }



    public void onButtonLocationClickListener() {

        locationExpenseButton = (Button) findViewById(R.id.btn_location);
        locationExpenseButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent elactivity = new Intent(AddExpense.this,ExpenseLocation.class);
                        startActivityForResult(elactivity, REQUEST_CODE);

                    }
                }
        );
    }



    public void onDestroy() {

        super.onDestroy();

        // save new categories to the db before the activity gets destroyed
        DBHelper dbh = new DBHelper(AddExpense.this);

        for(int i=0 ; i < arrayAdapter.getCount() ; i++){
            dbh.addCategory(arrayAdapter.getItem(i));
        }
        for(int i=0 ; i < removedFromAdapter.size(); i++){
            dbh.deleteCategory(removedFromAdapter.get(i));
        }

    }



    public void showDatePickerDialog(View v) {

        if (type.equals("normal") || type.equals("planned")) {

            DialogFragment newFragment = new DatePickerFragment();
            //make different date dialogs depending on type
            Bundle args = new Bundle();
            args.putString("type", type);
            newFragment.setArguments(args);
            newFragment.show(getSupportFragmentManager(), "datePicker");
        } else {

            onButtonPeriodClickListener();
        }
    }


    //handle results from expenselocation class
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {

            if(resultCode == RESULT_OK) {

                Bundle spotBundle = data.getParcelableExtra("bundle");
                spot = spotBundle.getParcelable("latlng");
            }
        }
    }



    public void showToast(String text) {
        Toast.makeText(this,  text, Toast.LENGTH_LONG).show();
    }


    //handle periodic date
    public void onButtonPeriodClickListener() {

        String [] frequences= {"Settimanale", "Mensile"};
        final ArrayAdapter<String> frequencesAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, frequences) ;

        final AlertDialog.Builder aBuilder = new AlertDialog.Builder(AddExpense.this);
        aBuilder.setTitle("Con quale frequenza compierai questa spesa?")
                .setAdapter(frequencesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String strName = frequencesAdapter.getItem(which);
                switch (strName) {

                    case "Settimanale":

                        final String [] days= {"Lun", "Mar", "Mer", "Gio", "Ve", "Sa", "Dom"};

                        AlertDialog.Builder aBuilderInner1 = new AlertDialog.Builder(AddExpense.this);
                        aBuilderInner1.setTitle("Scegli il giorno")
                                     .setItems(days, new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialogInterface, int i) {

                                             date = days[i];
                                         }
                                     })
                                    .show();
                        break;

                    case "Mensile":

                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View npView = inflater.inflate(R.layout.dialog, null);
                        final NumberPicker np = (NumberPicker) npView.findViewById(R.id.numberPicker);
                        np.setMinValue(1);
                        np.setMaxValue(31);

                        AlertDialog aBuilderInner2 = new AlertDialog.Builder(AddExpense.this)
                                .setTitle("Scegli il giorno:")
                                .setView(npView)
                                .setPositiveButton("Set",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                date = ""+np.getValue();
                                            }
                                        })
                                .show();

                        break;
                }
            }
        })
        .show();

    }

}
