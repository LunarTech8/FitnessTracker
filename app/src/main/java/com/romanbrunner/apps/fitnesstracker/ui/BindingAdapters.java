package com.romanbrunner.apps.fitnesstracker.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class BindingAdapters
{
    // --------------------
    // Data code
    // --------------------

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);


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
        view.setText(DATE_FORMAT.format(value));
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
            return DATE_FORMAT.parse(dateString);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    @BindingAdapter("android:layout_marginStart")
    public static void setLayoutMarginStart(View view, float marginStart)
    {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
        layoutParams.setMarginStart((int)marginStart);
        view.setLayoutParams(layoutParams);
    }
}