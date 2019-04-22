package com.example.android.popmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.example.android.popmovies.Adapters.MovieAdapter;
import com.example.android.popmovies.DataBase.Holder;
import com.example.android.popmovies.DataBase.MainViewModel;
import com.example.android.popmovies.Utils.CheckConnection;
import com.example.android.popmovies.Utils.JsonParsing;
import com.example.android.popmovies.Utils.Networking;
import com.example.android.popmovies.model.Movie;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieClickHandler {
    GridLayoutManager gridLayoutManager;
    static MovieAdapter adapter;
    static RecyclerView recyclerView;

    static Movie[] movies;

    static String type;
    SharedPreferences sharedPreferences;
    public static final String POPULAR = "popular";
    public static final String TOP_RATED = "top_rated";
    public static TextView tv_error;
    Holder mDb;
    int state;
    String keystate = "SCROLL_POSITION";
    public static int prevstate = -1;
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.adView)
    AdView mAdView;
    android.support.v7.widget.Toolbar toolbar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.tool_Bar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        tv_error = (TextView) findViewById(R.id.error);
        recyclerView = (RecyclerView) findViewById(R.id.rv);

        recyclerView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(this, 2);

        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new MovieAdapter(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        mDb = Holder.getInstance(getApplicationContext());


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        type = sharedPreferences.getString("popular", POPULAR);

        if (savedInstanceState != null) {


            if (savedInstanceState.containsKey(keystate)) {
                prevstate = savedInstanceState.getInt(keystate);
                Log.e("state", prevstate + "");

            }
            if (savedInstanceState.containsKey("cat")) {
                type = savedInstanceState.getString("cat");
            }
        }


        if (CheckConnection.isOnline(getApplicationContext())) {
            loadMovieData();

        } else {
            tv_error.setVisibility(View.VISIBLE);
        }
        initAdMob();


    }

    private void initAdMob() {
        MobileAds.initialize(this, Constants.ADMOB_APP_ID);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                if (errorCode == AdRequest.ERROR_CODE_NETWORK_ERROR) {
                    Log.e(TAG, "Check NetworkConntion");
                } else if (errorCode == AdRequest.ERROR_CODE_INVALID_REQUEST) {
                    Log.e(TAG, "ad request was invalid");

                } else if (errorCode == AdRequest.ERROR_CODE_INTERNAL_ERROR) {
                    Log.e(TAG, "invalid response");

                } else if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                    Log.e(TAG, "lack of ad inventory");

                } else if (errorCode == AdRequest.GENDER_UNKNOWN) {
                    Log.e(TAG, "Unknown Error");
                }
                Log.e(TAG, "errorCode: " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.

            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.


            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.


            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {


        state = gridLayoutManager.findFirstCompletelyVisibleItemPosition();

        if (state == -1) {
            state = gridLayoutManager.findLastVisibleItemPosition();
        }

        Log.e("state", state + "");

        outState.putInt(keystate, state);
        outState.putString("cat", getType());


        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        movies = (Movie[]) savedInstanceState.getParcelableArray("DataSaved");

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("DataSaved")) {
                movies = (Movie[]) savedInstanceState.getParcelableArray("DataSaved");
            }
        }

        super.onRestoreInstanceState(savedInstanceState);


    }

    public String getType() {
        return this.type;
    }

    public static void setType(String type) {
        MainActivity.type = type;
    }

    public static void loadMovieData() {


        tv_error.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        process p = new process();
        p.execute(type);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menu_id = item.getItemId();
        switch (menu_id) {
            case R.id.m_mostPopular:
                if (!CheckConnection.isOnline(getApplicationContext())) {
                    tv_error.setVisibility(View.VISIBLE);
                } else {

                    Toast.makeText(this, "popular", Toast.LENGTH_SHORT).show();


                    if (type.equals(POPULAR) != true) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("popular", POPULAR);
                        editor.commit();
                        type = POPULAR;

                        loadMovieData();

                    }
                    type = POPULAR;
                    loadMovieData();
                }
                return true;

            case R.id.m_topRated:
                if (!CheckConnection.isOnline(getApplicationContext())) {
                    tv_error.setVisibility(View.VISIBLE);
                } else {


                    Toast.makeText(this, "top Rated", Toast.LENGTH_SHORT).show();


                    if (type.equals(TOP_RATED) != true) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("top Rated", TOP_RATED);
                        editor.commit();
                        type = TOP_RATED;
                        loadMovieData();
                    }
                    type = TOP_RATED;
                    loadMovieData();
                }
                return true;
            case R.id.mFavourite:
                if (!CheckConnection.isOnline(getApplicationContext())) {
                    tv_error.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "favourites", Toast.LENGTH_SHORT).show();


                    loadMovieFavourite();
                }

        }
        return super.onOptionsItemSelected(item);


    }

    @Override
    public void OnClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, MovieDetail.class);
        intent.putExtra("TRAILER_PATH", movie);
        startActivity(intent);

    }

    public void loadMovieFavourite() {
        MainViewModel model = ViewModelProviders.of(this).get(MainViewModel.class);

        model.getMovies().observe(this, new Observer<Movie[]>() {
            @Override
            public void onChanged(@Nullable Movie[] movies) {
                Log.d("gida", "Updating list of tasks from LiveData in ViewModel");
                adapter.setMovieData(movies);


            }
        });


    }

    public static class process extends AsyncTask<String, Void, Movie[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tv_error.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            String mType = params[0];

            URL url = Networking.buildUri(mType);
            try {
                String response = Networking.ReadData(url);
                movies = JsonParsing.DataParsed(response);
                return movies;


            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


        }

        @Override
        protected void onPostExecute(Movie[] movieData) {
            if (movieData != null) {
                movies = movieData;
                adapter.setMovieData(movies);
            } else {
                recyclerView.setVisibility(View.INVISIBLE);
                tv_error.setVisibility(View.VISIBLE);
            }

        }
    }

}
