package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.List;


public class WidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        ArrayList list = new ArrayList();
        Cursor cursor = this.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{
                        QuoteColumns._ID,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE,
                        QuoteColumns.ISUP,
                        QuoteColumns.NAME
        },
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"}, null);
        cursor.close();
        return new WidgetDataProvider(this, intent, cursor);

    }

    public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
        List<String> collection = new ArrayList<>();
        private Context context;
        private Intent intent;
        private Cursor mCursor;
        private static final String TAG = "WidgetDataProvider";

        public WidgetDataProvider(Context context, Intent intent, Cursor cursor) {
            this.context = context;
            this.intent = intent;
            mCursor = cursor;
        }

        @Override public void onCreate() {
        }

        @Override public void onDataSetChanged() {
            if(mCursor != null){
                mCursor.close();
            }

            final long identityToken = Binder.clearCallingIdentity();

            Uri uri = QuoteProvider.Quotes.CONTENT_URI;
            mCursor = WidgetService.this.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{
                            QuoteColumns._ID,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP,
                            QuoteColumns.NAME
                    },
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"}, null);
            Binder.restoreCallingIdentity(identityToken);

        }

        @Override public void onDestroy() {
            mCursor.close();
        }

        @Override public int getCount() {
            Log.e(TAG, "getCount: " + mCursor.getCount());
            return mCursor.getCount();
        }

        @Override public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_widget);
            mCursor.moveToPosition(position);
            remoteViews.setTextViewText(R.id.symbol,
                    mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)).toUpperCase());
            remoteViews.setTextViewText(R.id.change,
                    mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));

            final Intent intentGraph = new Intent();
            intentGraph.putExtra(getString(R.string.symbol), mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)).toUpperCase());
            remoteViews.setOnClickFillInIntent(R.id.widget_list_item,intentGraph);

            Log.e(TAG, "getViewAt: " + mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
            return remoteViews;
        }

        @Override public RemoteViews getLoadingView() {
            return null;
        }

        @Override public int getViewTypeCount() {
            return 1;
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public boolean hasStableIds() {
            return true;
        }
    }


}
