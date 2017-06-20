package com.blackfrogweb.popularmovies.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.blackfrogweb.popularmovies.MovieParcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

public final class OpenMoviesJsonUtils {

    public static ArrayList<MovieParcel> getMoviesFromJson(Context context, String moviesJsonStr)
            throws JSONException {

        final String M_RESULTS = "results";

        final String M_ID = "id";
        final String M_TITLE = "title";
        final String M_PLOT = "overview";
        final String M_RATING = "vote_average";
        final String M_POSTER = "poster_path";
        final String M_RELEASE = "release_date";

        final String M_MESSAGE_CODE = "cod";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        if (moviesJson.has(M_MESSAGE_CODE)) {
            int errorCode = moviesJson.getInt(M_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        String id, title, poster_path, plot, release_date, vote_average;

        JSONArray moviesArray = moviesJson.getJSONArray(M_RESULTS);
        int length = moviesArray.length();

        ArrayList<MovieParcel> movieList = new ArrayList<>();

        for (int i = 0; i < length; i++) {

            //get json movie object
            JSONObject movieObject = moviesArray.getJSONObject(i);

            id = movieObject.getString(M_ID);
            title = movieObject.getString(M_TITLE);
            plot = movieObject.getString(M_PLOT);
            release_date = movieObject.getString(M_RELEASE);
            vote_average = movieObject.getString(M_RATING);
            poster_path = movieObject.getString(M_POSTER);

            //create movie object with the parsed json data
            //in this first loading instance we don't load trailers or reviews saving some time
            MovieParcel mMovie = new MovieParcel(id, title, release_date, poster_path, vote_average, plot, null, null);

            movieList.add(mMovie);
        }

        return movieList;
    }

    public static ArrayList<String> getTrailersFromJson(Context context, String moviesJsonStr)
            throws JSONException {

        final String M_RESULTS = "results";

        final String M_ID = "key";

        final String M_MESSAGE_CODE = "cod";

        final String BASE_YOUTUBE_URL = "https://www.youtube.com/watch?v=";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        if (moviesJson.has(M_MESSAGE_CODE)) {
            int errorCode = moviesJson.getInt(M_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        String videoId;

        JSONArray trailersArray = moviesJson.getJSONArray(M_RESULTS);
        int length = trailersArray.length();

        //some trailers have over 10 trailers. Let's limit them to three.
        if(length > 3)
            length = 3;

        Log.d("Auxiliar:", "Retrieved " + length + " trailers");

        ArrayList<String> trailerList = new ArrayList<>();

        for (int i = 0; i < length; i++) {

            //get json movie object
            JSONObject trailerObject = trailersArray.getJSONObject(i);

            videoId = trailerObject.getString(M_ID);

            String FINAL_URL = BASE_YOUTUBE_URL + videoId;

            trailerList.add(FINAL_URL);
            Log.d("Auxiliar", FINAL_URL);
        }

        return trailerList;
    }
}