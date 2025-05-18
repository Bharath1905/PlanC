package com.pemchip.blablacar.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pemchip.blablacar.R;


public class ProcessLoaderDialogue extends Dialog {

    private Context activity;

    public ProcessLoaderDialogue(@NonNull Context context) {
        super(context);
        activity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dialog);
    }
}
