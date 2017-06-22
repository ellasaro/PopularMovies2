package com.blackfrogweb.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private ArrayList<MovieParcel> mMoviesData;
    private final MoviesAdapterOnClickHandler mClickHandler;

    public interface MoviesAdapterOnClickHandler {
        void onClick(MovieParcel movieClicked);
    }

    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final ImageView mMoviesImageView;

        public MoviesAdapterViewHolder(View view) {
            super(view);
            mMoviesImageView = (ImageView) view.findViewById(R.id.movie_thumbnail);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();

            String movieId = mMoviesData.get(adapterPosition).getmId();
            String moviePoster = mMoviesData.get(adapterPosition).getmPoster();
            String movieName = mMoviesData.get(adapterPosition).mTitle;
            String movieDate = mMoviesData.get(adapterPosition).mReleaseDate;
            String moviePlot = mMoviesData.get(adapterPosition).mPlot;
            String movieRating = mMoviesData.get(adapterPosition).mRating;
            ArrayList<String> movieTrailers = mMoviesData.get(adapterPosition).mTrailers;
            ArrayList<String> movieReviews = mMoviesData.get(adapterPosition).mReviews;

            //create parcelable and send it in the intent
            MovieParcel movieClicked = new MovieParcel(movieId, movieName, movieDate, moviePoster, movieRating, moviePlot);
            mClickHandler.onClick(movieClicked);
        }
    }

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder moviesAdapterViewHolder, int position) {
        MovieParcel movie = mMoviesData.get(position);
        Context context = moviesAdapterViewHolder.mMoviesImageView.getContext();
        String posterPath = movie.getmPoster();
        Picasso.with(context).load("http://image.tmdb.org/t/p/w500/" + posterPath).into(moviesAdapterViewHolder.mMoviesImageView);
    }

    @Override
    public int getItemCount() {
        if (null == mMoviesData) return 0;
        return mMoviesData.size();
    }

    public void setMoviesData(ArrayList<MovieParcel> movieList) {
        mMoviesData = movieList;
        notifyDataSetChanged();
    }
}