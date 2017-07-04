package com.blackfrogweb.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by el-la on 6/23/2017.
 */

public class FavoritesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    public FavoritesDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesContract.FavoritesEntry.TABLE_NAME + " (" +
                FavoritesContract.FavoritesEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                FavoritesContract.FavoritesEntry.COLUMN_API_ID       + " STRING NOT NULL UNIQUE, " +

                FavoritesContract.FavoritesEntry.COLUMN_TITLE + " STRING NOT NULL, " +

                FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE   + " STRING NOT NULL, " +
                FavoritesContract.FavoritesEntry.COLUMN_SCORE   + " STRING NOT NULL, " +

                FavoritesContract.FavoritesEntry.COLUMN_PLOT   + " STRING NOT NULL, " +
                FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH    + " STRING NOT NULL" + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
