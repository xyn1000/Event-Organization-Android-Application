package com.group16.eventplaza.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.group16.eventplaza.PostActivity;

import java.util.Calendar;
import java.util.Date;

public class DateFragment extends DialogFragment  {

    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private Date StartDate;

    public DateFragment(Date startDate,DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.StartDate = startDate;
        this.onDateSetListener = onDateSetListener;
    }

    public DateFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), onDateSetListener, year, month, day);
        if (StartDate == null){
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
        }else{
            datePickerDialog.getDatePicker().setMinDate(StartDate.getTime());
        }
        // Create a new instance of DatePickerDialog and return it
        return datePickerDialog;
    }


}
