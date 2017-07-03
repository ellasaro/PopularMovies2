package com.blackfrogweb.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by el-la on 6/23/2017.
 */

public class FavoritesContract {

    public static final String CONTENT_AUTHORITY = "com.blackfrogweb.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final class FavoritesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        public static final String TABLE_NAME = "favorites";

        public static final String COLUMN_API_ID = "api_id";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_SCORE = "score";

        public static final String COLUMN_PLOT = "plot";

        public static final String COLUMN_POSTER_PATH = "poster";

        public static Uri buildWeatherUriWithDate(long date) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(date))
                    .build();
        }

    }
}
