package com.blackfrogweb.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MovieParcel implements Parcelable {
    String mId;
    String mTitle;
    String mReleaseDate;
    String mPoster;
    String mRating;
    String mPlot;
    ArrayList<String> mTrailers;
    ArrayList<String> mReviews;

    public MovieParcel(String id, String title, String release, String poster, String rating, String plot,
                       ArrayList<String> trailers, ArrayList<String> reviews)
    {
        this.mId = id;
        this.mTitle = title;
        this.mReleaseDate = release;
        this.mPoster = poster;
        this.mRating = rating;
        this.mPlot = plot;
        this.mTrailers = trailers;
        this.mReviews = reviews;
    }

    private MovieParcel(Parcel in){
        mId = in.readString();
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mPoster = in.readString();
        mRating = in.readString();
        mPlot = in.readString();
        mTrailers = in.readArrayList(String.class.getClassLoader());
        mReviews = in.readArrayList(String.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString() { return mId + "--" + mTitle + "--" + mReleaseDate + "--" + mRating + "--" + mPlot + "--" + mPoster; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mTitle);
        parcel.writeString(mReleaseDate);
        parcel.writeString(mPoster);
        parcel.writeString(mRating);
        parcel.writeString(mPlot);
        parcel.writeList(mTrailers);
        parcel.writeList(mReviews);
    }

    public static final Parcelable.Creator<MovieParcel> CREATOR = new Parcelable.Creator<MovieParcel>() {
        @Override
        public MovieParcel createFromParcel(Parcel parcel) {
            return new MovieParcel(parcel);
        }

        @Override
        public MovieParcel[] newArray(int i) {
            return new MovieParcel[i];
        }

    };

    public String getmId() {
        return mId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmReleaseDate() {
        return mReleaseDate;
    }

    public String getmPoster() {
        return mPoster;
    }

    public String getmRating() {
        return mRating;
    }

    public String getmPlot() {
        return mPlot;
    }

    public ArrayList<String> getmTrailers() { return mTrailers; }

    public ArrayList<String> getmReviews() { return mReviews; }
}
