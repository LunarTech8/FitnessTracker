package com.romanbrunner.apps.fitnesstracker.ui;

import android.view.View;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BindingAdapters
{
    // --------------------
    // Functional code
    // --------------------

    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show)
    {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("android:text")
    public static void setFloat(TextView view, float value)
    {
        if (Float.isNaN(value))
        {
            return;
        }
        view.setText(String.valueOf(value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static float getFloat(TextView view)
    {
        String num = view.getText().toString();
        if(num.isEmpty())
        {
            return 0.0F;
        }
        try
        {
            return Float.parseFloat(num);
        }
        catch (NumberFormatException e)
        {
            return 0.0F;
        }
    }

    @BindingAdapter("android:text")
    public static void setInt(TextView view, int value)
    {
        view.setText(String.valueOf(value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static int getInt(TextView view)
    {
        String num = view.getText().toString();
        if(num.isEmpty())
        {
            return 0;
        }
        try
        {
            return Integer.parseInt(num);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    @BindingAdapter("android:text")
    public static void setDate(TextView view, Date value)
    {
        if (value == null)
        {
            return;
        }
        view.setText(SimpleDateFormat.getDateInstance().format(value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static Date getDate(TextView view)
    {
        String dateString = view.getText().toString();
        if(dateString.isEmpty())
        {
            return null;
        }
        try
        {
            return SimpleDateFormat.getDateInstance().parse(dateString);
        }
        catch (ParseException e)
        {
            return null;
        }
    }
}