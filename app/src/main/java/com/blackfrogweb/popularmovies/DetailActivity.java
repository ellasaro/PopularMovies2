package com.blackfrogweb.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackfrogweb.popularmovies.data.FavoritesContract;
import com.blackfrogweb.popularmovies.data.FavoritesDbHelper;
import com.blackfrogweb.popularmovies.utilities.NetworkUtils;
import com.blackfrogweb.popularmovies.utilities.OpenMoviesJsonUtils;
import com.squareup.picasso.Picasso;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class DetailActivity extends FragmentActivity implements TrailerAdapter.TrailerAdapterOnClickHandler,
                                                                LoaderManager.LoaderCallbacks<Bundle> {

    //Recycler views
    private RecyclerView mRecyclerView;
    private TrailerAdapter mTrailerAdapter;
    private RecyclerView mrRecyclerView;
    private ReviewAdapter mReviewAdapter;
    //loader variables
    private static final int TRAILERS_LOADER_ID = 1;
    private static final int REVIEWS_LOADER_ID = 2;
    private Bundle bundleForLoader;
    int loaderId;
    ArrayList<String> trailerList = new ArrayList<String>();
    private MovieParcel passedMovie;

    //progress bars
    private ProgressBar mTrailerLoadingIndicator;
    private ProgressBar mReviewLoadingIndicator;

    //movie information
    @BindView(R.id.movie_title) TextView mMovieTitle;
    @BindView(R.id.release_date) TextView mReleaseDate;
    @BindView(R.id.score) TextView mScore;
    @BindView(R.id.tv_no_trailers) TextView mNoTrailers;
    @BindView(R.id.tv_no_reviews) TextView mNoReviews;
    @BindView(R.id.tv_no_internet_trailers) TextView mNoInternetTrailers;
    @BindView(R.id.tv_no_internet_reviews) TextView mNoInternetReviews;
    @BindView(R.id.bttnSaveFavorite) TextView mAddRemoveBttn;
    @BindView(R.id.plot) TextView mPlot;
    @BindView(R.id.movie_poster) ImageView moviePoster;
    @BindView(R.id.trailer_loading_indicator) ProgressBar getmTrailerLoadingIndicator;
    @BindView(R.id.review_loading_indicator) ProgressBar getmReviewLoadingIndicator;

    //DB
    private SQLiteDatabase mDb;
    private boolean mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mTrailerLoadingIndicator = (ProgressBar) findViewById(R.id.trailer_loading_indicator);
        mReviewLoadingIndicator = (ProgressBar) findViewById(R.id.review_loading_indicator);

        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        //get intent and movie object
        Intent intent = getIntent();
        if(intent.hasExtra("movie")) passedMovie = intent.getExtras().getParcelable("movie");
        else {
            Toast.makeText(this, "This movie title can't be loaded right now.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //get id of movie to make trailer and reviews request
        String id = passedMovie.getmId();

        //fetch whether the movie is a favorite or not
        mStatus = isFavorite(id);
        if (mStatus) mAddRemoveBttn.setText(R.string.remove);

        //set the button on click listener
        mAddRemoveBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mStatus) {
                    if (addFavorite(passedMovie) == null) {
                        Toast.makeText(DetailActivity.this, "Something went wrong. Try again later.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DetailActivity.this, "Movie added to Favorites.", Toast.LENGTH_SHORT).show();
                        mAddRemoveBttn.setText(R.string.remove);
                        mStatus = true;
                    }
                } else {
                    if (!removeFavorite(passedMovie)) {
                        //unsuccessful deletion
                        Toast.makeText(DetailActivity.this, "Something went wrong. Try again later.", Toast.LENGTH_SHORT).show();
                    } else {
                        //successful deletion
                        Toast.makeText(DetailActivity.this, "Movie removed from Favorites.", Toast.LENGTH_SHORT).show();
                        mAddRemoveBttn.setText(R.string.add_to_favorites);
                        mStatus = false;
                    }
                }
            }
        });

        //parse the date so it looks better
        String rawDate = passedMovie.getmReleaseDate();
        String parsedDate = parseDate(rawDate);

        //Display information in the view
        mMovieTitle.setText(passedMovie.getmTitle());
        mReleaseDate.setText(parsedDate);
        mScore.setText(passedMovie.getmRating());
        mPlot.setText(passedMovie.getmPlot());

        //get poster uri
        String moviePath = passedMovie.getmPoster();
        int errorIconId = getResources().getIdentifier("oops", "drawable", "com.blackfrogweb.popularmovies");

        //Load movie poster
        Picasso.with(this).load("http://image.tmdb.org/t/p/w500/" + moviePath)
                .placeholder(R.mipmap.ic_launcher)
                .error(errorIconId)
                .into(moviePoster);

        //Change layout's size;
        LinearLayout movieInfo = (LinearLayout) findViewById(R.id.movieInfo);
        ViewGroup.LayoutParams params = movieInfo.getLayoutParams();
        movieInfo.getLayoutParams().height = params.MATCH_PARENT;
        movieInfo.setLayoutParams(params);

        //--- Trailers RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);

        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(trailerLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mTrailerAdapter = new TrailerAdapter(this);
        mRecyclerView.setAdapter(mTrailerAdapter);
        //--- End Trailers RecyclerView

        //--- Reviews Recyclerview
        mrRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);

        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mrRecyclerView.setLayoutManager(reviewLayoutManager);
        mrRecyclerView.setHasFixedSize(true);

        mReviewAdapter = new ReviewAdapter();
        mrRecyclerView.setAdapter(mReviewAdapter);
        //--- End Reviews Recyclerview

        //The second parameter of the initLoader method below is a Bundle
        bundleForLoader = new Bundle();
        bundleForLoader.putString("preferences", id);

        //Load data
        loadTrailers(bundleForLoader, false);
        loadReviews(bundleForLoader, false);
    }

    private void loadTrailers(Bundle bundleForLoader, boolean restart){

        loaderId = TRAILERS_LOADER_ID;
        //we've implemented the LoaderCallbacks interface with the type of String array
        LoaderManager.LoaderCallbacks<Bundle> callbackTrailers = DetailActivity.this;
        //Ensures a loader is initialized and active
        if(!restart) getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callbackTrailers);
        else getSupportLoaderManager().restartLoader(loaderId, bundleForLoader, callbackTrailers);
    }

    private void loadReviews(Bundle bundleForLoader, boolean restart){
        loaderId = REVIEWS_LOADER_ID;
        //we've implemented the LoaderCallbacks interface with the type of String array
        LoaderManager.LoaderCallbacks<Bundle> callbackReviews = DetailActivity.this;
        if(!restart) getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callbackReviews);
        else getSupportLoaderManager().restartLoader(loaderId, bundleForLoader, callbackReviews);
    }

    private boolean isFavorite(String movieId) {

        boolean status = false;

        Cursor queryCursor = mDb.query(
                FavoritesContract.FavoritesEntry.TABLE_NAME,
                null,
                "api_id=?",
                new String[]{movieId},
                null,
                null,
                null);

        if (queryCursor.getCount() > 0) status = true;

        queryCursor.close();
        return status;
    }

    private Uri addFavorite(MovieParcel currentMovie) {

        ContentValues cv = new ContentValues();
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_API_ID, currentMovie.getmId());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_TITLE, currentMovie.getmTitle());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_SCORE, currentMovie.getmRating());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, currentMovie.getmReleaseDate());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_PLOT, currentMovie.getmPlot());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, currentMovie.getmPoster());

        Uri uri = getContentResolver().insert(FavoritesContract.FavoritesEntry.CONTENT_URI, cv);
        return uri;
    }

    private boolean removeFavorite(MovieParcel currentMovie) {
        return getContentResolver().delete(FavoritesContract.FavoritesEntry.CONTENT_URI,
                FavoritesContract.FavoritesEntry.COLUMN_API_ID + "=" + currentMovie.getmId(), null) > 0;
    }

    @Override
    public Loader<Bundle> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Bundle>(this) {

            //get the sorting preferences
            String sortPreferences = args.getString("preferences");

            //Bundle and array lists we'll use for operating
            Bundle result = new Bundle();
            ArrayList<String> trailerList = new ArrayList<>();
            ArrayList<String[]> reviewList = new ArrayList<>();
            ArrayList<String> reviewAuthorList = new ArrayList<>();
            ArrayList<String> reviewContentList = new ArrayList<>();

            @Override
            protected void onStartLoading() {
                //if there is no connection, don't even start loading
                if (isOnline()) {

                    //check if there is any previously stored data
                    if ((trailerList.isEmpty()) && (getId()==1)){
                        mTrailerLoadingIndicator.setVisibility(View.VISIBLE);
                        forceLoad();
                    } else if((reviewList.isEmpty()) && (getId()==2)){
                        mReviewLoadingIndicator.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                } else {
                    showNoConnection();
                }
            }

            @Override
            public Bundle loadInBackground() {
                if (getId() == TRAILERS_LOADER_ID) {
                    try {
                        //Build URL
                        URL trailersRequestUrl = NetworkUtils.buildUrl(sortPreferences, "trailers");
                        //Get trailer info
                        String jsonMovieResponse = NetworkUtils
                                .getResponseFromHttpUrl(trailersRequestUrl);

                        //Array with parsed movie objects
                        trailerList = OpenMoviesJsonUtils
                                .getTrailersFromJson(DetailActivity.this, jsonMovieResponse);

                        if (trailerList.size() != 0) {
                            showTrailers();
                            //put the trailer String Array List into a bundle
                            result.putStringArrayList("Trailer List", trailerList);
                        } else {
                            //put dummy data in because even though we loaded data successfully
                            //there is no trailers available
                            //however we need to set the data so we don't try load data again
                            trailerList.add(0, "Dummy Data");
                        }
                        return result;

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else if (getId() == REVIEWS_LOADER_ID) {
                    try {
                        //Build URL
                        URL reviewsRequestUrl = NetworkUtils.buildUrl(sortPreferences, "reviews");

                        String jsonMovieResponse = NetworkUtils
                                .getResponseFromHttpUrl(reviewsRequestUrl);

                        //Array with movie objects
                        reviewList = OpenMoviesJsonUtils
                                .getReviewsFromJson(DetailActivity.this, jsonMovieResponse);

                        //Separate authors and reviews from the ArrayList of String arrays
                        if (reviewList.size() != 0) {
                            showReviews();
                            for (int i = 0; i < reviewList.size(); i++) {
                                String[] auxiliarList = reviewList.get(i);
                                reviewAuthorList.add(auxiliarList[0]);
                                reviewContentList.add(auxiliarList[1]);
                            }
                        } else {
                            //put dummy data in because even though we loaded data successfully
                            //there is no reviews available
                            //however we need to set the data so we don't try load data again
                            String[] dummyData = {"Dummy Data", "Dummy Data"};
                            reviewList.add(0, dummyData);
                        }
                        //put those arrays into a bundle and return it
                        result.putStringArrayList("Review Authors", reviewAuthorList);
                        result.putStringArrayList("Review Contents", reviewContentList);
                        return result;

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    //the loader id is neither 1 or 2
                    return null;
                }
            }

            public void deliverResult(Bundle loaderResult) {
                if(loaderResult != null) {
                    result = loaderResult;
                    super.deliverResult(result);
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Bundle> loader, Bundle loaderResult) {
        //get the id of the loader that finished
        int finishedLoaderId = loader.getId();

        if (finishedLoaderId == 1) {
            mTrailerLoadingIndicator.setVisibility(GONE);
            ArrayList<String> trailerList = loaderResult.getStringArrayList("Trailer List");
            if (trailerList != null) {

                LinearLayout trailerListView = (LinearLayout) findViewById(R.id.trailerListView);
                TrailerListAdapter mTrailerListAdapter = new TrailerListAdapter(getBaseContext(), trailerList);

                //populate linear layout
                for(int trailerPosition = 0; trailerPosition < trailerList.size(); trailerPosition++){
                    View item = mTrailerListAdapter.getView(trailerPosition, null, null);

                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playTrailer(v.getTag().toString());
                        }
                    });

                    trailerListView.addView(item);
                }

            } else {
                showNoTrailers();
            }
        } else if (finishedLoaderId == 2) {
            mReviewLoadingIndicator.setVisibility(GONE);
            ArrayList<String> authorList = loaderResult.getStringArrayList("Review Authors");
            ArrayList<String> contentList = loaderResult.getStringArrayList("Review Contents");
            if (!authorList.isEmpty() && !contentList.isEmpty()) {
                //populate recycler view
                mReviewAdapter.setReviewData(loaderResult);
            } else {
                showNoReviews();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Bundle> loader) {
        //required to Override it to implement the LoaderCallbacks<String> interface
    }

    @Override
    public void onClick(String trailerClicked) {
        playTrailer(trailerClicked);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else {
            super.onOptionsItemSelected(item);
        }
        return true;
    }

    //Check for internet connection
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private String parseDate(String rawDate) {

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String returnDate = null;

        try {
            Date date = inputFormat.parse(rawDate);
            returnDate = outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnDate;

    }

    private void playTrailer(String url){
        Uri trailer = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, trailer);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }

    private void showTrailers(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mNoTrailers.setVisibility(GONE);
        mNoInternetTrailers.setVisibility(GONE);
    }

    private void showReviews(){
        mrRecyclerView.setVisibility(View.VISIBLE);
        mNoReviews.setVisibility(GONE);
        mNoInternetReviews.setVisibility(GONE);
    }

    private void showNoTrailers(){
        mRecyclerView.setVisibility(GONE);
        mNoTrailers.setVisibility(View.VISIBLE);
        mNoInternetTrailers.setVisibility(GONE);
    }

    private void showNoReviews(){
        mrRecyclerView.setVisibility(GONE);
        mNoReviews.setVisibility(View.VISIBLE);
        mNoInternetReviews.setVisibility(GONE);
    }

    private void showNoConnection(){
        mRecyclerView.setVisibility(GONE);
        mrRecyclerView.setVisibility(GONE);
        mNoTrailers.setVisibility(GONE);
        mNoReviews.setVisibility(GONE);
        mNoInternetReviews.setVisibility(View.VISIBLE);
        mNoInternetTrailers.setVisibility(View.VISIBLE);
    }
}
