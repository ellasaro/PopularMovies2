package com.blackfrogweb.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Created by el-la on 6/23/2017.
 */

public class FavoritesContract {

    public static final class FavoritesEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorites";

        public static final String COLUMN_API_ID = "api_id";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_SCORE = "score";

        public static final String COLUMN_PLOT = "plot";

        public static final String COLUMN_POSTER_PATH = "poster";

    }
}
