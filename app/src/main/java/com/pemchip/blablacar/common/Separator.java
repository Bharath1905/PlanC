package com.pemchip.blablacar.common;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Separator {

    private final static Separator instance;

    static {
        instance = new Separator();
    }

    public static Separator getInstance() {
        return instance;
    }

    public String doSeparate(final String passingValue, Locale locale) {

        String formatedValue ="";

        String pattern = "#,###,###,##0.00";

        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(0);

        decimalFormat.applyPattern(pattern);
        try {
            formatedValue = decimalFormat.format(Double.parseDouble(passingValue));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

        return formatedValue;

    }

}
