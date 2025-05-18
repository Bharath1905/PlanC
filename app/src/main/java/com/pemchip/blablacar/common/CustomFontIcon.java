package com.pemchip.blablacar.common;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class CustomFontIcon extends TextView {

    private TextView tv;

    public CustomFontIcon(Context context) {
        super(context);
        renderIcon(context);
    }

    public CustomFontIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        renderIcon(context);
    }

    public CustomFontIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        renderIcon(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomFontIcon(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        renderIcon(context);
    }

    private void renderIcon(Context context)
    {
        tv = this;
        Typeface face = Typeface.createFromAsset(context.getAssets(),"font/fonticons.ttf");
        this.setTypeface(face);
        this.setSingleLine();

        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        // gets called after layout has been done but before display
                        // so we can get the height then hide the view


                        int width = tv.getWidth();
                        int height = tv.getHeight();
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(width, height) * 1f);
                    }
                });
    }
}
