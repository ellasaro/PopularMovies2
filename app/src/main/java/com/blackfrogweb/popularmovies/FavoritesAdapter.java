package com.blackfrogweb.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blackfrogweb.popularmovies.data.FavoritesContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesAdapterViewHolder> {

    private ArrayList<MovieParcel> mMoviesData;
    private final FavoritesAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;

    public interface FavoritesAdapterOnClickHandler {
        void onClick(MovieParcel favClicked);
    }

    public FavoritesAdapter(FavoritesAdapterOnClickHandler clickHandler, Cursor cursor) {
        mClickHandler = clickHandler;
        this.mCursor = cursor;
    }

    public class FavoritesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final ImageView mMoviesImageView;

        public FavoritesAdapterViewHolder(View view) {
            super(view);
            mMoviesImageView = (ImageView) view.findViewById(R.id.movie_thumbnail);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();
            if(!mCursor.moveToPosition(adapterPosition))
                return;

            String movieId = mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_API_ID));
            String moviePoster = mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH));
            String movieName = mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_TITLE));
            String movieDate = mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));
            String moviePlot = mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_PLOT));
            String movieRating = mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_SCORE));

            //create parcelable and send it in the intent
            MovieParcel movieClicked = new MovieParcel(movieId, movieName, movieDate, moviePoster, movieRating, moviePlot);
            mClickHandler.onClick(movieClicked);
        }
    }

    @Override
    public FavoritesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new FavoritesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoritesAdapterViewHolder favoritesAdapterViewHolder, int position) {
        if(!mCursor.moveToPosition(position))
            return;

        String posterPath = mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH));

        //MovieParcel movie = mMoviesData.get(position);
        Context context = favoritesAdapterViewHolder.mMoviesImageView.getContext();
        Picasso.with(context).load("http://image.tmdb.org/t/p/w500/" + posterPath).into(favoritesAdapterViewHolder.mMoviesImageView);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void setFavoritesData(ArrayList<MovieParcel> favList) {
        mMoviesData = favList;
        notifyDataSetChanged();
    }
}