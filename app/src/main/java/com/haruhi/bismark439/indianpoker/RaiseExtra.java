package com.haruhi.bismark439.indianpoker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * Created by Bismark439 on 08/07/2017.
 */


public class RaiseExtra extends DialogFragment {
   public static int cRaise = 3;
   public static int REQCODE = 100;
    int maxRaise;
    SeekBar mSeekBar;
    TextView tv;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        return builder.create();
    }

    public void onAllIn(View v){
        cRaise=maxRaise;
        mSeekBar.setProgress(cRaise);
    }

}