package com.group16.eventplaza.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeFragment extends DialogFragment  implements TimePicker.OnTimeChangedListener {

    private TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private Date startTime;

    public TimeFragment(){

    }

    public TimeFragment(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener  = onTimeSetListener;
    }

    public TimeFragment(Date startTime,TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener  = onTimeSetListener;
        this.startTime = startTime;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), onTimeSetListener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }


    @Override
    public void onTimeChanged(TimePicker timePicker, int hour, int min) {



    }

}
