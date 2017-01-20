package com.social.messapp34.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.messapp34.R;
import com.social.messapp34.utils.Constants;
import com.social.messapp34.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ousmane on 1/10/17.
 */

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<ParseUser> members;
    private Context mContext;
    private static final String TAG = MemberAdapter.class.getSimpleName();
    public MemberAdapter(Context context, List<ParseUser> members) {
        this.mContext = context;
        this.members = members;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        public TextView personName;
        public ImageView personPhoto;
        public TextView personLocation;
        public CheckBox personChecked;

        MemberViewHolder(View itemView) {
            super(itemView);
            personPhoto = (ImageView) itemView.findViewById(R.id.profile_thumbnail);
            personName = (TextView)itemView.findViewById(R.id.username);
            personLocation = (TextView) itemView.findViewById(R.id.location);
            personChecked = (CheckBox) itemView.findViewById(R.id.member_checkbox);
        }
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_layout, viewGroup, false);
        MemberViewHolder pvh = new MemberViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final MemberViewHolder memberViewHolder, int position) {
        final ParseUser person = members.get(position);
        final String username = members.get(position).getUsername();
        final String[] cleanedUserName = username.split("_");
        memberViewHolder.personName.setText(cleanedUserName[0]);
        memberViewHolder.personLocation.setText(mContext.getString(R.string.lives_at) + " " + person.getString(Constants.LOCATION));
        String picture_thumbnail = members.get(position).getString(Constants.PROFILE_PICTURE);
        if(picture_thumbnail == null || picture_thumbnail.isEmpty()){
            Picasso.with(mContext).load(mContext.getString(R.string.default_profile_url)).transform(new CircleTransform()).
                    into(memberViewHolder.personPhoto);
        }else Picasso.with(mContext).load(person .getString(Constants.PROFILE_PICTURE)).
                transform(new CircleTransform()).into(memberViewHolder.personPhoto);

        final ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FRIENDS_TABLE);
        query.whereEqualTo(Constants.SENDER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> receivers, ParseException e) {
                if(e == null){
                    for(ParseObject receiver : receivers){
                        ParseUser rec = (ParseUser) receiver.get(Constants.RECEIVER);
                       Log.d(TAG, "Receiver Id: " + rec.getObjectId());
                        if(rec.getObjectId().equals(person.getObjectId()) && receiver.getBoolean(Constants.CHECKED)){
                            memberViewHolder.personChecked.setChecked(true);
                        }
                    }
                }else {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        memberViewHolder.personChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext, username, Toast.LENGTH_LONG).show();
                //Log.d(TAG, username + " checked");
                if(memberViewHolder.personChecked.isChecked()){
                    //Add to friend table
                    final ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FRIENDS_TABLE);
                    query.whereEqualTo(Constants.SENDER, ParseUser.getCurrentUser());
                    query.whereEqualTo(Constants.RECEIVER, person);
                    query.whereEqualTo(Constants.SENDER, person);
                    query.whereEqualTo(Constants.RECEIVER, ParseUser.getCurrentUser());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> rows, ParseException e) {
                            if(e == null){
                                Log.d(TAG, "row: " + rows.size());
                                if(rows.isEmpty()){
                                    //Go ahead and add data to table
                                    List<ParseObject> newRows = new ArrayList<>();
                                    ParseObject firstRow = new ParseObject(Constants.FRIENDS_TABLE);
                                    firstRow.put(Constants.SENDER, ParseUser.getCurrentUser());
                                    firstRow.put(Constants.RECEIVER, person);
                                    firstRow.put(Constants.CHECKED, true);

                                    ParseObject secondRow = new ParseObject(Constants.FRIENDS_TABLE);
                                    secondRow.put(Constants.SENDER, person);
                                    secondRow.put(Constants.RECEIVER, ParseUser.getCurrentUser());
                                    secondRow.put(Constants.CHECKED, true);

                                    newRows.add(firstRow);
                                    newRows.add(secondRow);
                                    ParseObject.saveAllInBackground(newRows, new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e == null){
                                                Toast.makeText(mContext, cleanedUserName[0] + " has been added in your friend list", Toast.LENGTH_LONG).show();
                                            }else {
                                                Log.d(TAG, e.getMessage());
                                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }else{
                                    Log.d(TAG, "Friendship was already established");
                                    final ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FRIENDS_TABLE);
                                    query.whereEqualTo(Constants.SENDER, ParseUser.getCurrentUser());
                                    query.whereEqualTo(Constants.RECEIVER, person);
                                    query.setLimit(1);
                                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject row, ParseException e) {
                                            if(e == null){
                                                // launch second query;
                                                Log.d(TAG, row.getObjectId());
                                                row.put(Constants.CHECKED, true);
                                                row.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if(e == null){
                                                            final ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FRIENDS_TABLE);
                                                            query.whereEqualTo(Constants.SENDER, person);
                                                            query.whereEqualTo(Constants.RECEIVER, ParseUser.getCurrentUser());
                                                            query.setLimit(1);
                                                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                                                @Override
                                                                public void done(ParseObject row, ParseException e) {
                                                                    if(e == null){
                                                                        Log.d(TAG, row.getObjectId());
                                                                        row.put(Constants.CHECKED, true);
                                                                        row.saveInBackground(new SaveCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {
                                                                                if(e == null){
                                                                                    Log.d(TAG, "Friendship removed");
                                                                                    Toast.makeText(mContext, cleanedUserName[0] + " has been added to your friend list", Toast.LENGTH_LONG).show();
                                                                                }else {
                                                                                    Log.d(TAG, e.getMessage());
                                                                                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }else{
                                                                        Log.d(TAG, e.getMessage());
                                                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });
                                                        }else {
                                                            Log.d(TAG, e.getMessage());
                                                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }else {
                                                Log.d(TAG, e.getMessage());
                                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }else {
                                Log.d(TAG, e.getMessage());
                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else {
                    //Set value of checked to false
                    final ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FRIENDS_TABLE);
                    query.whereEqualTo(Constants.SENDER, ParseUser.getCurrentUser());
                    query.whereEqualTo(Constants.RECEIVER, person);
                    query.setLimit(1);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject row, ParseException e) {
                            if(e == null){
                                // launch second query;
                                Log.d(TAG, row.getObjectId());
                                row.put(Constants.CHECKED, false);
                                row.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            final ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FRIENDS_TABLE);
                                            query.whereEqualTo(Constants.SENDER, person);
                                            query.whereEqualTo(Constants.RECEIVER, ParseUser.getCurrentUser());
                                            query.setLimit(1);
                                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                                @Override
                                                public void done(ParseObject row, ParseException e) {
                                                    if(e == null){
                                                      Log.d(TAG, row.getObjectId());
                                                        row.put(Constants.CHECKED, false);
                                                        row.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if(e == null){
                                                                    Log.d(TAG, "Friendship removed");
                                                                    Toast.makeText(mContext, cleanedUserName[0] + " has been removed from your friends", Toast.LENGTH_LONG).show();
                                                                }else {
                                                                    Log.d(TAG, e.getMessage());
                                                                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    }else{
                                                        Log.d(TAG, e.getMessage());
                                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }else {
                                            Log.d(TAG, e.getMessage());
                                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else {
                                Log.d(TAG, e.getMessage());
                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return members.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
