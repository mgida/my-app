package com.example.android.popmovies;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.popmovies.Adapters.ReviewAdapter;
import com.example.android.popmovies.model.Review;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MovieReviewFragment extends Fragment {
    @BindView(R.id.rec_Reviews)
    RecyclerView reviewRecyclerView;

    public static ArrayList<Review> reviews;
    public static ReviewAdapter reviewAdapter;
    Unbinder unbinder;


    public MovieReviewFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_review, container, false);
        unbinder = ButterKnife.bind(this, rootView);


        reviews = new ArrayList<Review>();
        reviewRecyclerView.setHasFixedSize(true);
        reviewAdapter = new ReviewAdapter(reviews);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(getContext());
        reviewRecyclerView.setLayoutManager(layoutManager2);
        reviewRecyclerView.setAdapter(reviewAdapter);
        if (savedInstanceState != null) {
            Parcelable recyclerViewState = savedInstanceState.getParcelable("s");
            reviewRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }

        return rootView;


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("s",
                reviewRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
