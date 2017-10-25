package com.example.senso.budgetracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Position Saved Dialog fragment, define an interface to signal back to the activity host ( ExpenseLocation ) the user decision
 */

public class SavePositionDialog extends DialogFragment {

    public interface ButtonClickedDialogListener {

        void onFinishDialog(boolean saved);
    }
    private ButtonClickedDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Il marker sarà salvato come luogo in cui è avvenuta la spesa")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        listener.onFinishDialog(true);
                    }
                })
                .setNegativeButton("Riposiziona", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        listener.onFinishDialog(false);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ButtonClickedDialogListener so we can send data to the host
            listener = (ButtonClickedDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement EditNameDialogListener");
        }
    }

}
