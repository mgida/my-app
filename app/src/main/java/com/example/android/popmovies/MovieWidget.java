package com.example.android.popmovies;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

/**
 * Implementation of App Widget functionality.
 */
public class MovieWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        Paper.init(context);
        String title = Paper.book().read("title");
        final String image = Paper.book().read("image");

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.movie_widget);
        if (!title.isEmpty()) {
            views.setTextViewText(R.id.appwidget_movie_title, title);
        }
        if (!image.isEmpty()) {
            Picasso.with(context).load(Constants.MOVIE_URL + image).into(views, R.id.appwidget_movie_image, new int[]{appWidgetId});
        }


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

