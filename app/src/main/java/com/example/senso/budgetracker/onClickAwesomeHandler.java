package com.example.senso.budgetracker;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.senso.budgetracker.AddExpense;
import com.example.senso.budgetracker.R;

/**
 * Total onclicks app handler class (maybe not so stable solution, but at least elegant)
 */

public class onClickAwesomeHandler implements View.OnClickListener {

    private Context currContext;


    public onClickAwesomeHandler(Context currContext) {
        this.currContext = currContext;
    }


    @Override
    public void onClick(View v) {

        Intent intent = new Intent(currContext, AddExpense.class);
        switch (v.getId()) {
            case R.id.material_design_floating_action_menu_item1:

                intent.putExtra("Expense Type", "normal");
                break;
            case R.id.material_design_floating_action_menu_item2:

                intent.putExtra("Expense Type", "planned");
                break;
            case R.id.material_design_floating_action_menu_item3:

                intent.putExtra("Expense Type", "periodic");
                break;
        }
        currContext.startActivity(intent);
    }
}
