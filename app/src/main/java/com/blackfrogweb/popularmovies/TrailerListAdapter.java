/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.blackfrogweb.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


// Custom adapter class that displays a list of Android-Me images in a GridView
public class TrailerListAdapter {

    // Keeps track of the context and list of images to display
    private Context mContext;
    private TextView mTrailerNumber;
    private ArrayList<String> mTrailerList;

    public TrailerListAdapter(Context context, ArrayList<String> trailerList) {
        mContext = context;
        mTrailerList = trailerList;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            LayoutInflater lInflater = (LayoutInflater) mContext.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            convertView = lInflater.inflate(R.layout.trailer_list_item, null);
        }

        View vi=convertView;
        mTrailerNumber = (TextView) vi.findViewById(R.id.tv_trailer_number);
        mTrailerNumber.setText(Integer.toString(position+1));

        vi.setTag(mTrailerList.get(position));
        return vi;
    }
}
