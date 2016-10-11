package com.sam_chordas.android.stockhawk.rest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nikhil on 11-10-2016.
 */

public class Stock  {

    private String symbol;
    private String date;
    private String open;
    private String high;
    private String low;

    public Stock() {

    }


    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }



}
