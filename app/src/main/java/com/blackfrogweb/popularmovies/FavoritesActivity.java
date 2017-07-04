package com.blackfrogweb.popularmovies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;
import com.blackfrogweb.popularmovies.data.FavoritesContract;
import com.blackfrogweb.popularmovies.data.FavoritesDbHelper;

import java.util.ArrayList;

public class FavoritesActivity extends FragmentActivity implements FavoritesAdapter.FavoritesAdapterOnClickHandler,
                                                                    LoaderManager.LoaderCallbacks<Cursor>{

    private RecyclerView mRecyclerView;
    private FavoritesAdapter mFavoritesAdapter;

    private SQLiteDatabase mDb;
    private Cursor cursor;

    private TextView mNoFavoritesMessage;
    private ArrayList<MovieParcel> movieList;

    private static final int FAVORITES_LOADER_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        mNoFavoritesMessage = (TextView) findViewById(R.id.no_favorites);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_favorites);

        //Database
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        //RECYCLER VIEW PARAMETERS
        //spanCount determines the number of columns
        int spanCount = 2;
        int screenOrientation = getWindowManager().getDefaultDisplay().getRotation();
        if (screenOrientation == Surface.ROTATION_90
                || screenOrientation == Surface.ROTATION_270) {
            //if the screen is rotated I want four, not 2
            spanCount = 4;
        }

        GridLayoutManager layoutManager = new GridLayoutManager (this, spanCount);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mFavoritesAdapter = new FavoritesAdapter(this, cursor);
        mRecyclerView.setAdapter(mFavoritesAdapter);

        LoaderManager.LoaderCallbacks<Cursor> callback = FavoritesActivity.this;
        getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, callback);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mFavoritesData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mFavoritesData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mFavoritesData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mFavoritesData = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.getCount() != 0) mNoFavoritesMessage.setVisibility(View.GONE);
        else mNoFavoritesMessage.setVisibility(View.VISIBLE);

        mFavoritesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoritesAdapter.swapCursor(null);
    }

    private void loadFavorites(Bundle bundle){

        if (cursor != null) {
            if (cursor.getCount() == 0) mNoFavoritesMessage.setVisibility(View.VISIBLE);
            else mNoFavoritesMessage.setVisibility(View.GONE);
        }

        mFavoritesAdapter = new FavoritesAdapter(this, cursor);
        mRecyclerView.setAdapter(mFavoritesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_favorites, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.all_movies) {
            finish();
        } else if(id == android.R.id.home){
            finish();
        } else if(id == R.id.clear_data){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            // ...Irrelevant code for customizing the buttons and title
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_label_editor, null);
            dialogBuilder.setView(dialogView);

            TextView textView = (TextView) dialogView.findViewById(R.id.label_field);
            textView.setText(R.string.dialog_clear_data);

            dialogBuilder.setView(dialogView).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dropFavorites();

                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
        return true;
    }

    private void dropFavorites(){
        if(getContentResolver().delete(FavoritesContract.FavoritesEntry.CONTENT_URI, null, null) > 0)
            getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    @Override
    public void onClick(MovieParcel movieClicked) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movie", movieClicked);
        startActivity(intentToStartDetailActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //re-query
        getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
