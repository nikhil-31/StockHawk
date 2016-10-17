package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;

/**
 * Created by Prateek Phoenix on 7/11/2016.
 */
public class WidgetService extends RemoteViewsService {
    private static final String TAG = "WIDGET";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        ArrayList list = new ArrayList();
        Cursor cursor = this.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, new String[]{
                QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.PERCENT_CHANGE,
                QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.NAME
        }, QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
        return new WidgetDataProvider(this, intent, cursor);
    }
}
