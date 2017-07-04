/*
 isOnline method was based on the following StackOverflow post:
    http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
*/

package com.blackfrogweb.popularmovies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blackfrogweb.popularmovies.MoviesAdapter.MoviesAdapterOnClickHandler;
import com.blackfrogweb.popularmovies.utilities.NetworkUtils;
import com.blackfrogweb.popularmovies.utilities.OpenMoviesJsonUtils;

import java.net.URL;
import java.util.ArrayList;

//Changed AppCompatActivity for FragmentActivity
public class MainActivity extends FragmentActivity implements MoviesAdapterOnClickHandler, LoaderManager.LoaderCallbacks<ArrayList<MovieParcel>> {

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    GridLayoutManager mLayoutManager;
    private TextView mErrorMessage;
    private String sortPreference;
    private int mScrollPosition;
    private ArrayList<MovieParcel> movieList;

    private ProgressBar mLoadingIndicator;

    //Loader ID
    private static final int LIST_LOADER_ID = 0;

    private static final String LIST_POSITION_KEY = "savedPosition";
    private static final String MOVIE_LIST_KEY = "movies";
    private static final String PREFERENCES_KEY = "preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorMessage = (TextView) findViewById(R.id.no_connection_error);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);

        //RECYCLER VIEW PARAMETERS
        //spanCount determines the number of columns
        int spanCount = 2;
        int screenOrientation = getWindowManager().getDefaultDisplay().getRotation();
        if (screenOrientation == Surface.ROTATION_90
                || screenOrientation == Surface.ROTATION_270) {
            //if the screen is rotated I want four, not 2
            spanCount = 4;
        }

        mLayoutManager = new GridLayoutManager (this, spanCount);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMoviesAdapter = new MoviesAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mMoviesAdapter);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        loadMovies(savedInstanceState);
    }

    @Override
    public Loader<ArrayList<MovieParcel>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<MovieParcel>>(this) {

            //get the sorting preferences
            String sortPreferences = args.getString(PREFERENCES_KEY);

            //Build URL requesting movie list
            URL movieRequestUrl = NetworkUtils.buildUrl(sortPreferences, "list");

            @Override
            protected void onStartLoading() {
                if (movieList != null) {
                    deliverResult(movieList);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public ArrayList<MovieParcel> loadInBackground() {

                if(isOnline()) {
                    try {
                        String jsonMovieResponse = NetworkUtils
                                .getResponseFromHttpUrl(movieRequestUrl);

                        //Array with movie objects
                        movieList = OpenMoviesJsonUtils
                                .getMoviesFromJson(MainActivity.this, jsonMovieResponse);

                        return movieList;

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else return null;
            }

            public void deliverResult(ArrayList<MovieParcel> data) {
                movieList = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MovieParcel>> loader, ArrayList<MovieParcel> movieList) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (movieList != null) {
            showDataView();
            mMoviesAdapter.setMoviesData(movieList);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MovieParcel>> loader) {
        //required to Override it to implement the LoaderCallbacks<String> interface
    }

    @Override
    public void onClick(MovieParcel movieClicked) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movie", movieClicked);
        startActivity(intentToStartDetailActivity);
    }

    private void showDataView() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.sortBy) {
            //Open dialog asking for sorting criteria
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_label, null);
            dialogBuilder.setView(dialogView);

            //Views in dialog
            final RadioButton sortByPopular = (RadioButton) dialogView.findViewById(R.id.rb_most_popular);
            final RadioButton sortByRating = (RadioButton) dialogView.findViewById(R.id.rb_rating);

            if(sortPreference.equals("popular")){
                sortByPopular.setChecked(true);
            } else {
                sortByRating.setChecked(true);
            }

            dialogBuilder.setView(dialogView).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                //Actions
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(sortByPopular.isChecked()){
                        //Run query by popularity
                        sortPreference = "popular";
                    } else if (sortByRating.isChecked()) {
                        //Run query by ratings
                        sortPreference = "top_rated";
                    } else {
                        Toast.makeText(MainActivity.this, "Oops! Something went wrong", Toast.LENGTH_SHORT).show();
                        sortPreference = "";
                    }

                    movieList = null;

                    Bundle preferenceBundle = new Bundle();
                    preferenceBundle.putString(PREFERENCES_KEY, sortPreference);

                    loadMovies(preferenceBundle);
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
        } else if(id == R.id.refresh){
            Bundle refreshBundle = new Bundle();
            refreshBundle.putString(PREFERENCES_KEY, sortPreference);
            //clear the movie list
            movieList = null;
            loadMovies(refreshBundle);
        } else if(id == R.id.favorites){
            Context context = this;
            Class destinationClass = FavoritesActivity.class;
            Intent intentFavoritesActivity = new Intent(context, destinationClass);
            startActivity(intentFavoritesActivity);
        }
        return true;
    }

    private void loadMovies(Bundle bundle){
        //LOADER MANAGER
        // This ID will uniquely identify the Loader.
        final int loaderId = LIST_LOADER_ID;
        //The second parameter of the initLoader method below is a Bundle
        Bundle bundleForLoader = new Bundle();
        //From MainActivity, we have implemented the LoaderCallbacks interface with the type of String array
        LoaderManager.LoaderCallbacks<ArrayList<MovieParcel>> callback = MainActivity.this;

        //SAVED INSTANCE STATE
        if(bundle == null) {
            //If there is no list saved, then this is an initial state.
            //Initialize the sort preference to most popular
            sortPreference = "popular";
            showDataView();

            //Put the sorting preference into the bundle
            bundleForLoader.putString(PREFERENCES_KEY, sortPreference);
            //Load Movies
            //Ensures a loader is initialized and active
            getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);
        } else {
            sortPreference = bundle.getString(PREFERENCES_KEY);
            mScrollPosition = bundle.getInt(LIST_POSITION_KEY);
            movieList = bundle.getParcelableArrayList(MOVIE_LIST_KEY);

            if(movieList == null){
                showDataView();
                bundleForLoader.putString("preferences", sortPreference);
                getSupportLoaderManager().restartLoader(loaderId, bundleForLoader, callback);
            } else {
                movieList = bundle.getParcelableArrayList(MOVIE_LIST_KEY);
                mMoviesAdapter.setMoviesData(movieList);
                mRecyclerView.getLayoutManager().scrollToPosition(mScrollPosition);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //Save state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(MOVIE_LIST_KEY, movieList);
        outState.putString(PREFERENCES_KEY, sortPreference);

        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if(layoutManager != null && layoutManager instanceof GridLayoutManager){
            mScrollPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }

        outState.putInt(LIST_POSITION_KEY, mScrollPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }

    //Check for internet connection
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
