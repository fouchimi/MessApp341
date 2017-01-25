package com.social.messapp34;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.messapp34.adapters.LastCommentAdapter;
import com.social.messapp34.model.ChatUser;
import com.social.messapp34.utils.Constants;
import com.social.messapp34.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private LastCommentAdapter commentAdapter;
    final List<ParseObject> comments = new ArrayList<>();

    private static final String TAG = ChatFragment.class.getSimpleName();

    public ChatFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        emptyTextView = (TextView) view.findViewById(R.id.empty_view);
        emptyTextView.setTypeface(typeFace);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.chat_rv);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(itemDecoration);

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = mRecyclerView.findChildViewUnder(e.getX(),e.getY());

                if(child!=null ){
                    int position = mRecyclerView.getChildAdapterPosition(child);
                    commentAdapter = (LastCommentAdapter) mRecyclerView.getAdapter();
                    final ParseObject friend = commentAdapter.getFriend(position);
                    String friendId = "";
                    if(!ParseUser.getCurrentUser().getObjectId().equals(friend.getString(Constants.USER_ID))){
                        friendId = friend.getString(Constants.USER_ID);
                    }
                    if(!ParseUser.getCurrentUser().getObjectId().equals(friend.getString(Constants.FRIEND_ID))){
                        friendId = friend.getString(Constants.FRIEND_ID);
                    }
                    final String id = friendId;
                    Log.d(TAG, "Friend id: " + friendId);
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.setLimit(1);
                    query.getInBackground(friendId, new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(e == null){
                                ChatUser buddy = new ChatUser();
                                Log.d(TAG, user.getUsername());
                                final String username = user.getUsername();
                                final String[] cleanedUserName = username.split("_");
                                buddy.setId(id);
                                buddy.setUsername(cleanedUserName[0]);
                                buddy.setLocation(user.getString(Constants.LOCATION));
                                String picture_thumbnail = user.getString(Constants.PROFILE_PICTURE);
                                if(picture_thumbnail == null || picture_thumbnail.isEmpty()){
                                    buddy.setThumbnail(getActivity().getString(R.string.default_profile_url));
                                }else buddy.setThumbnail(picture_thumbnail);
                                Intent intent =  new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra(Constants.FRIEND, buddy);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                getActivity().startActivity(intent);
                            }
                        }
                    });
                    return true;

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchRecentMessages();
    }

    public void fetchRecentMessages(){
        comments.clear();
        ParseQuery<ParseObject> firstQuery = ParseQuery.getQuery(Constants.LAST_CHAT_TABLE);
        firstQuery.whereEqualTo(Constants.USER_ID, ParseUser.getCurrentUser().getObjectId());

        ParseQuery<ParseObject> secondQuery = ParseQuery.getQuery(Constants.LAST_CHAT_TABLE);
        secondQuery.whereEqualTo(Constants.FRIEND_ID, ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(firstQuery);
        queries.add(secondQuery);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.orderByDescending(Constants.DATE);

        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> chats, ParseException e) {
                if(e == null){
                    if(chats.isEmpty()){
                        Log.d(TAG, "Size: " + chats.size());
                        emptyTextView.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }else {
                        emptyTextView.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        commentAdapter = new LastCommentAdapter(getContext(), comments);
                        for(ParseObject chat : chats){
                            comments.add(chat);
                            Log.d(TAG, chat.getString(Constants.MESSAGE));
                            commentAdapter.notifyDataSetChanged();
                        }
                        Log.d(TAG, "comment size: " + comments.size());
                        mRecyclerView.setAdapter(commentAdapter);
                    }
                }else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }
}
