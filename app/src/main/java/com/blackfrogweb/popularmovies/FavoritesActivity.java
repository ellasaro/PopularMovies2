package com.blackfrogweb.popularmovies;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackfrogweb.popularmovies.R;
import com.blackfrogweb.popularmovies.data.FavoritesContract;
import com.blackfrogweb.popularmovies.data.FavoritesDbHelper;

import java.util.ArrayList;

public class FavoritesActivity extends FragmentActivity implements FavoritesAdapter.FavoritesAdapterOnClickHandler  {

    private RecyclerView mRecyclerView;
    private FavoritesAdapter mFavoritesAdapter;

    private SQLiteDatabase mDb;

    private TextView mNoFavoritesMessage;
    private ArrayList<MovieParcel> movieList;

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

        loadFavorites(savedInstanceState);
    }

    private Cursor getFavorites(){
        return mDb.query(
                FavoritesContract.FavoritesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private void loadFavorites(Bundle bundle){

        //get favorites from DB
        Cursor cursor = getFavorites();

        if(cursor.getCount() == 0) mNoFavoritesMessage.setVisibility(View.VISIBLE);
        else mNoFavoritesMessage.setVisibility(View.GONE);

        mFavoritesAdapter = new FavoritesAdapter(this, cursor);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mFavoritesAdapter);
        cursor.close();
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
                    loadFavorites(new Bundle());
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
        mDb.delete(FavoritesContract.FavoritesEntry.TABLE_NAME, null, null);
    }

    @Override
    public void onClick(MovieParcel movieClicked) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movie", movieClicked);
        startActivity(intentToStartDetailActivity);
    }
}
