package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import barqsoft.footballscores.R;

/**
 * Created by Georgi on 24.7.2015 г..
 */
public class WidgetProvider extends AppWidgetProvider {

    private static final String ACTION_REFRESH = "WidgetProvider.ACTION_REFRESH";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if(ACTION_REFRESH.equals(intent.getAction())){
            handleRefreshAction(context, intent);
        }
    }

    private void handleRefreshAction(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != 1) {
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listItems);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int widgetId : appWidgetIds){
            // Sets up the intent that points to the StackViewService that will
            // provide the views for this collection.
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                //noinspection deprecation
                rv.setRemoteAdapter(widgetId, R.id.listItems, intent);
            } else {
                rv.setRemoteAdapter(R.id.listItems, intent);
            }

            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            rv.setEmptyView(R.id.listItems, R.id.emptyView);

            Intent refreshBroadcastIntent = new Intent(context, WidgetProvider.class)
                    .setAction(ACTION_REFRESH)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            PendingIntent refreshIntent = PendingIntent.getBroadcast(
                    context,
                    widgetId,
                    refreshBroadcastIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            rv.setOnClickPendingIntent(R.id.syncButton, refreshIntent);

            appWidgetManager.updateAppWidget(widgetId, rv);
        }

    }
}
