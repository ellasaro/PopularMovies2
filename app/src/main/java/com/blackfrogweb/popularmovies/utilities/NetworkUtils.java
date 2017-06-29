package com.blackfrogweb.popularmovies.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String MOVIES_URL_PART1 =
            "http://api.themoviedb.org/3/movie/";

    private static final String MOVIES_URL_PART2 = "?api_key=";

    private static final String MOVIES_URL_VIDEOS = "/videos";

    private static final String MOVIES_URL_REVIEWS = "/reviews";

    //TODO your key HERE
    private static final String KEY =" ";


    private static final String URL_LIST = "list";
    private static final String URL_TRAILERS = "trailers";
    private static final String URL_REVIEWS = "reviews";

    public static URL buildUrl(String sortPreferences, String urlType) {

        String finalUri;

        if(urlType.equals(URL_LIST)){
            finalUri = MOVIES_URL_PART1 + sortPreferences + MOVIES_URL_PART2 + KEY;
        } else if(urlType.equals(URL_TRAILERS)){
            finalUri = MOVIES_URL_PART1 + sortPreferences + MOVIES_URL_VIDEOS + MOVIES_URL_PART2 + KEY;
        } else if(urlType.equals(URL_REVIEWS)){
            finalUri = MOVIES_URL_PART1 + sortPreferences + MOVIES_URL_REVIEWS + MOVIES_URL_PART2 + KEY;
        } else
            finalUri = "http://api.themoviedb.org";

        Log.d("Auxiliar: ", finalUri);
        Uri builtUri = Uri.parse(finalUri);

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    //get Response
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}