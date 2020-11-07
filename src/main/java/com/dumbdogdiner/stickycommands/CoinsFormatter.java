package com.dumbdogdiner.stickycommands;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.spi.NumberFormatProvider;
import java.util.Locale;

/** Class to hold and format coins
 * Could use or be in StickyAPI, but this would be a future move.
 * Contains formatting and some worth calculations
 */
public class CoinsFormatter extends NumberFormatProvider {
    
    @Override
    public NumberFormat getCurrencyInstance(Locale locale) {
        return null;
    }

    @Override
    public NumberFormat getIntegerInstance(Locale locale) {
        return null;
    }

    @Override
    public NumberFormat getNumberInstance(Locale locale) {
        return null;
    }

    @Override
    public NumberFormat getPercentInstance(Locale locale) {
        return null;
    }

    @Override
    public Locale[] getAvailableLocales() {
        return new Locale[0];
    }
}
