package com.social.messapp34.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.social.messapp34.R;
import com.social.messapp34.utils.CircleTransform;
import com.social.messapp34.utils.Constants;
import com.social.messapp34.utils.Utility;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ousmane on 1/19/17.
 */

public class LastCommentAdapter extends RecyclerView.Adapter<LastCommentAdapter.CommentViewHolder> {

    private static final String TAG = LastCommentAdapter.class.getSimpleName();

    private Context mContext;
    private List<ParseObject> conversationList;

    public LastCommentAdapter(Context context, List<ParseObject> conversationList) {
        this.mContext = context;
        this.conversationList = conversationList;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.conversation_list, viewGroup, false);
        CommentViewHolder pvh = new CommentViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        if(!conversationList.isEmpty()){
            ParseObject commentObject = conversationList.get(position);
            String[] nameChunks = ParseUser.getCurrentUser().getUsername().split("_");
            String currentUsername = nameChunks[0];
            String[] friendNameChunks = commentObject.getString(Constants.FRIEND_NAME).split(",");
            String friendName = "";
            for(String name : friendNameChunks){
                if(!name.equals(currentUsername)){
                    friendName = name;
                }
            }
            holder.friendName.setText(friendName);
            String friendPicUrl = commentObject.getString(Constants.PROFILE_PICTURE);
            String[] chunks = friendPicUrl.split(",");
            String picUrl = mContext.getString(R.string.default_profile_url);
            String currentProfileUrl = ParseUser.getCurrentUser().getString(Constants.PROFILE_PICTURE);
            Log.d(TAG, "Current profile: " + currentProfileUrl);
            Log.d(TAG, "" + currentProfileUrl);
            for(String chunk : chunks){
                if(!chunk.equals(currentProfileUrl)){
                    picUrl = chunk;
                }
            }
            Log.d(TAG, "final : " + picUrl);
            Picasso.with(mContext).load(picUrl).transform(new CircleTransform()).into(holder.friendPhoto);
            String date = Utility.getFriendlyDayAndTimeString(mContext, commentObject.getCreatedAt().getTime());
            holder.date.setText(date);
            holder.lastConversation.setText(commentObject.getString(Constants.MESSAGE));
            Log.d(TAG, commentObject.getString(Constants.MESSAGE));
        }
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public ParseObject getFriend(int position){
        return conversationList.get(position);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView friendName;
        public ImageView friendPhoto;
        public TextView lastConversation;
        public TextView date;

        CommentViewHolder(View itemView) {
            super(itemView);
            friendPhoto = (ImageView) itemView.findViewById(R.id.profile_thumbnail);
            friendName = (TextView)itemView.findViewById(R.id.username);
            lastConversation = (TextView) itemView.findViewById(R.id.conversation);
            date = (TextView) itemView.findViewById(R.id.dateField);
        }
    }
}
