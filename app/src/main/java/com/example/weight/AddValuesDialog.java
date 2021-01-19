package com.example.weight;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddValuesDialog extends AppCompatDialogFragment {
    private DialogListener listener;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_value_dialog,null);

        final Button datePickerButton = view.findViewById(R.id.datePicker);
        final EditText weightPicker = view.findViewById(R.id.weightPicker);
        final EditText fatPicker = view.findViewById(R.id.fatPicker);
        final EditText musclePicker = view.findViewById(R.id.musclePicker);

        datePickerButton.setText(df.format(new Date()));

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        getContext(),
                        dateSetListener,
                        year,month,day);
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                year-=1900;
                datePickerButton.setText(df.format(new Date(year,month,day)));
            }
        };

        builder.setView(view).setTitle("New Entry")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.sendData(datePickerButton.getText().toString(),editableToDouble(weightPicker.getText()),editableToDouble(fatPicker.getText()),editableToDouble(musclePicker.getText()));
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (DialogListener) context;
    }


    public interface DialogListener{
        public void sendData(String date, double weight, double fat, double muscle);
    }

    private double editableToDouble(Editable editable){
        double d;
        try{
            d = Double.valueOf(editable.toString());
        }catch(NumberFormatException e){
            d = 0;
        }
        return d;
    }
}
