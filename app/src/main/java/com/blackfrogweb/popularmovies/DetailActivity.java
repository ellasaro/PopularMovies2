package com.blackfrogweb.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackfrogweb.popularmovies.databinding.ActivityDetailBinding;
import com.blackfrogweb.popularmovies.utilities.NetworkUtils;
import com.blackfrogweb.popularmovies.utilities.OpenMoviesJsonUtils;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends FragmentActivity implements TrailerAdapter.TrailerAdapterOnClickHandler, LoaderManager.LoaderCallbacks<ArrayList<String>>{

    private RecyclerView mRecyclerView;
    private TrailerAdapter mTrailerAdapter;
    private static final int TRAILERS_LOADER_ID = 1;
    private static final int REVIEWS_LOADER_ID = 2;
    int loaderId;
    ArrayList<String> trailerList = new ArrayList<String>();
    ArrayList<String> reviewList = new ArrayList<String>();
    private MovieParcel passedMovie;

    //private ProgressBar mLoadingIndicator;
    private TextView mMovieTitle;
    private TextView mReleaseDate;
    private TextView mScore;
    private TextView mPlot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //TODO remove this
        //mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        ImageView moviePoster = (ImageView) findViewById(R.id.movie_poster);
        TextView mMovieTitle = (TextView) findViewById(R.id.movie_title);
        TextView mReleaseDate = (TextView) findViewById(R.id.release_date);
        TextView mScore = (TextView) findViewById(R.id.score);
        TextView mPlot = (TextView) findViewById(R.id.plot);

        //get intent and movie object
        Intent intent = getIntent();
        passedMovie = intent.getExtras().getParcelable("movie");

        //get id of movie to make trailer and reviews request
        String id = passedMovie.getmId();

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

        //Trailers Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mTrailerAdapter = new TrailerAdapter(this);
        mRecyclerView.setAdapter(mTrailerAdapter);

        //if we never loaded the trailer list for this movie, do it now
        if(true) {
            Log.d("Auxiliar: ", "no trailers available");
            //The second parameter of the initLoader method below is a Bundle
            Bundle bundleForLoader = new Bundle();
            bundleForLoader.putString("preferences", id);
            //Load Trailers
            loaderId = TRAILERS_LOADER_ID;
            //From MainActivity, we have implemented the LoaderCallbacks interface with the type of String array
            LoaderManager.LoaderCallbacks<ArrayList<String>> callback = DetailActivity.this;
            //Ensures a loader is initialized and active
            getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);
        } else{
            //fill the recycler view with the existing data
            Log.d("Auxiliar: ", "we already have these trailers.");
            mTrailerAdapter.setTrailerData(trailerList);
        }
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

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<String>>(this) {

            //get the sorting preferences
            String sortPreferences = args.getString("preferences");

            //Build URL requesting movie list
            URL trailersRequestUrl = NetworkUtils.buildUrl(sortPreferences, "trailers");

            URL reviewsRequestUrl = NetworkUtils.buildUrl(sortPreferences, "reviews");

            ArrayList<String> loaderList = new ArrayList<>();

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public ArrayList<String> loadInBackground() {

                Log.d("Auxiliar: ", "loadInBackground");
                if(isOnline()) {
                    try {
                        Log.d("Auxiliar: ", "We are online.");

                        String jsonMovieResponse = NetworkUtils
                                .getResponseFromHttpUrl(trailersRequestUrl);

                        //Array with movie objects
                        loaderList = OpenMoviesJsonUtils
                                .getTrailersFromJson(DetailActivity.this, jsonMovieResponse);

                        return loaderList;

                    } catch (Exception e) {
                        Log.d("POPMOVIES", "Oops!");
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    return null;
                }
            }

            public void deliverResult(ArrayList<String> data) {
                loaderList = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> loaderList) {
        Log.d("Auxiliar: ", "Finished loading trailers.");

        int finishedLoaderId = loader.getId();

        passedMovie.mTrailers = loaderList;

        if (finishedLoaderId == 1) {
            //mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (loaderList != null) {
                //showDataView();
                Log.d("Auxiliar: ", "Populate view.");
                mTrailerAdapter.setTrailerData(loaderList);
            } else {
                //showErrorMessage();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {
        //required to Override it to implement the LoaderCallbacks<String> interface
    }

    @Override
    public void onClick(String trailerClicked) {
        Context context = this;
        //Toast.makeText(context, "Play " + trailerClicked, Toast.LENGTH_SHORT).show();
        playTrailer(trailerClicked);
    }

    //Check for internet connection
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void playTrailer(String url){
        Uri trailer = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, trailer);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }
}
