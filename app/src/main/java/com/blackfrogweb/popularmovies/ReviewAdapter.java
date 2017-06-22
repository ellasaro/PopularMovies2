package com.blackfrogweb.popularmovies;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by el-la on 4/21/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private Bundle mReviewData;
    private ArrayList<String> authorReviewList;
    private ArrayList<String> contentReviewList;

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {

        private final TextView mReviewAuthor;
        private final TextView mReviewContent;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            mReviewAuthor = (TextView) view.findViewById(R.id.tv_review_author);
            mReviewContent = (TextView) view.findViewById(R.id.tv_review_content);
        }
    }

    @Override
    public ReviewAdapter.ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.review_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewAdapterViewHolder trailerHolder, int position) {
        trailerHolder.mReviewAuthor.setText(authorReviewList.get(position));
        trailerHolder.mReviewContent.setText(contentReviewList.get(position));
    }

    @Override
    public int getItemCount() {
        if (null == mReviewData) return 0;
        authorReviewList = mReviewData.getStringArrayList("Review Authors");
        contentReviewList = mReviewData.getStringArrayList("Review Contents");
        return authorReviewList.size();
    }

    public void setReviewData(Bundle reviewBundle) {
        mReviewData = reviewBundle;
        notifyDataSetChanged();
    }
}
