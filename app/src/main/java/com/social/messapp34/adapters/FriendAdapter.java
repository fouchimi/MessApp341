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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.messapp34.R;
import com.social.messapp34.utils.Constants;
import com.social.messapp34.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ousmane on 1/12/17.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<ParseUser> friends;
    private Context mContext;
    private static final String TAG = MemberAdapter.class.getSimpleName();

    public FriendAdapter(Context context, List<ParseUser> members) {
        this.mContext = context;
        this.friends = members;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_layout, viewGroup, false);
        FriendViewHolder pvh = new FriendViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder friendViewHolder, final int position) {
        final ParseUser person = friends.get(position);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.setLimit(1);
        query.getInBackground(person.getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e == null){
                    Log.d(TAG, user.getUsername());
                    final String username = user.getUsername();
                    final String[] cleanedUserName = username.split("_");
                    friendViewHolder.personName.setText(cleanedUserName[0]);
                    friendViewHolder.personLocation.setText(mContext.getString(R.string.lives_at) + " " + user.getString(Constants.LOCATION));
                    String picture_thumbnail = user.getString(Constants.PROFILE_PICTURE);
                    if(picture_thumbnail == null || picture_thumbnail.isEmpty()){
                        Picasso.with(mContext).load(mContext.getString(R.string.default_profile_url)).transform(new CircleTransform()).
                                into(friendViewHolder.personPhoto);
                    }else Picasso.with(mContext).load(user.getString(Constants.PROFILE_PICTURE)).
                            transform(new CircleTransform()).into(friendViewHolder.personPhoto);
                    friendViewHolder.personChecked.setChecked(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public ParseUser getFriend(int position){
        return friends.get(position);
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        public TextView personName;
        public ImageView personPhoto;
        public TextView personLocation;
        public CheckBox personChecked;

        FriendViewHolder(View itemView) {
            super(itemView);
            personPhoto = (ImageView) itemView.findViewById(R.id.profile_thumbnail);
            personName = (TextView)itemView.findViewById(R.id.username);
            personLocation = (TextView) itemView.findViewById(R.id.location);
            personChecked = (CheckBox) itemView.findViewById(R.id.member_checkbox);
        }
    }
}
