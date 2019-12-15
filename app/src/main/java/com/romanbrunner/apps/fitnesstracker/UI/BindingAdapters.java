package com.romanbrunner.apps.fitnesstracker.UI;

import android.view.View;

import androidx.databinding.BindingAdapter;


public class BindingAdapters
{
    // --------------------
    // Functional code
    // --------------------

    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show)
    {
        view.setVisibility(show ? View.VISIBLE : View.GONE);  // FIXME: loading text view is still visible after it is set to View.GONE
    }
}