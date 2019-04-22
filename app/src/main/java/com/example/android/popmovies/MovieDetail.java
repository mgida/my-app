package com.example.android.popmovies;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popmovies.Adapters.YouTubeAdapter;
import com.example.android.popmovies.DataBase.Executors;
import com.example.android.popmovies.DataBase.Holder;
import com.example.android.popmovies.Utils.JsonParsing;
import com.example.android.popmovies.model.Movie;
import com.example.android.popmovies.model.Trailer;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;

public class MovieDetail extends AppCompatActivity {

    public static Movie movie;
    public static YouTubeAdapter youtubeAdapter;
    public static ArrayList<Trailer> trailerList;
    @BindView(R.id.tv_overview)
    TextView tv_overview;

    @BindView(R.id.tv_original_title)
    TextView tv_title;

    @BindView(R.id.tv_date)
    TextView tv_date;

    @BindView(R.id.iv_poster_detail)
    ImageView iv_image;

    @BindView(R.id.tv_rate)
    TextView tv_rate;
    @BindView(R.id.fav)
    ImageButton imageButton;

    Holder mDb;


    boolean isFavourite = false;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        ActionBar actionBar = getSupportActionBar();


        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ButterKnife.bind(this);
        Intent i = getIntent();
        movie = (Movie) i.getParcelableExtra("TRAILER_PATH");
        Picasso.with(this).load(Constants.MOVIE_URL + movie.getPoster()).into(iv_image);
        tv_date.setText(movie.getDate());
        tv_rate.setText(movie.getRate());
        tv_title.setText(movie.getTitle());
        tv_overview.setText(movie.getOverview());
        mDb = Holder.getInstance(getApplicationContext());
        Paper.init(getApplicationContext());


        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Paper.book().write("title", movie.getTitle());
                Paper.book().write("image", movie.getPoster());
                updateAllWidgets();
            }
        });


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Movie movieInFav = mDb.movieDao().selectMovieByID(movie.getId());
                if (movieInFav != null) {
                    isFavourite = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageButton.setBackgroundColor(Color.RED);
                            Log.d("gida", "fav");
                        }
                    });
                }

            }
        });
        // ************************ Trailer *********************

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rec_Youtube);
        FetchMovieTrailer fetchMovieTrailer = new FetchMovieTrailer();

        trailerList = new ArrayList<Trailer>();
        youtubeAdapter = new YouTubeAdapter(this, trailerList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(youtubeAdapter);

        fetchMovieTrailer.execute(Constants.Movie_Trailer
                + movie.getId()
                + Constants.TRAILER_PATH);

        // ************************ Review *********************

        MovieReviewFragment reviewFragment = new MovieReviewFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().add(R.id.reviews_container, reviewFragment).commit();

        FetchMovieReview fetchMovieReview = new FetchMovieReview();
        fetchMovieReview.execute(Constants.Movie_Review
                + Integer.toString(movie.getId())
                + Constants.Review_PATH);


        InitEvents();

    }

    private void InitEvents() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(movie.getId()));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, movie.getTitle());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, movie.getPoster());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void updateAllWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplication(), MovieWidget.class));
        if (appWidgetIds.length > 0) {
            new MovieWidget().onUpdate(getApplicationContext(), appWidgetManager, appWidgetIds);
        }
    }


    public void favMovies(View view) {

        isFavourite = !isFavourite;


        if (isFavourite) {

            Executors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().insertMovie(movie);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageButton.setBackgroundColor(Color.RED);

                            Log.d("gida", "fav");

                        }
                    });

                }
            });
            Toasty.success(getApplicationContext(), getResources().getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();

        } else {
            Executors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().deleteMovie(movie);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageButton.setBackgroundColor(Color.WHITE);
                            Log.d("gida", "deletedFromFav");

                        }
                    });
                }
            });
            Toasty.error(getApplicationContext(), getResources().getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();

        }


    }


    public class FetchMovieReview extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder stringBuilder = new StringBuilder();

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String Json) {
            super.onPostExecute(Json);
            JsonParsing.ParseMovieReviews(Json);


        }


    }


    public class FetchMovieTrailer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder stringBuilder = new StringBuilder();

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            JsonParsing.ParseMovieTrailers(json);


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

