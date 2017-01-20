package com.social.messapp34;

import android.content.Context;
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
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.messapp34.adapters.FriendAdapter;
import com.social.messapp34.model.ChatUser;
import com.social.messapp34.utils.CircleTransform;
import com.social.messapp34.utils.Constants;
import com.social.messapp34.utils.DividerItemDecoration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FriendFragment extends Fragment {
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private static final String TAG = FriendFragment.class.getSimpleName();
    private ParseUser mCurrentUser;
    private FriendAdapter mAdapter;

    public FriendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentUser = ParseUser.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        emptyTextView = (TextView) view.findViewById(R.id.empty_view);
        emptyTextView.setTypeface(typeFace);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.friend_rv);
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
                    //Toast.makeText(getContext(),"The Item Clicked is: "+position,Toast.LENGTH_SHORT).show();
                    mAdapter = (FriendAdapter) mRecyclerView.getAdapter();
                    final ParseUser friend = mAdapter.getFriend(position);
                    Log.d(TAG, friend.getObjectId());
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.setLimit(1);
                    query.getInBackground(friend.getObjectId(), new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(e == null){
                                ChatUser buddy = new ChatUser();
                                Log.d(TAG, user.getUsername());
                                final String username = user.getUsername();
                                final String[] cleanedUserName = username.split("_");
                                buddy.setId(friend.getObjectId());
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
        fetchFriendList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void fetchFriendList(){
        final List<ParseUser> friends = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FRIENDS_TABLE);
        query.whereEqualTo(Constants.SENDER, mCurrentUser);
        query.whereEqualTo(Constants.CHECKED, true);
        query.setLimit(500);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> friendList, ParseException e) {
                if (e == null) {
                    if (friendList.isEmpty()) {
                        mRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                    else {
                        for(ParseObject row : friendList){
                            friends.add((ParseUser) row.get(Constants.RECEIVER));
                        }
                        mAdapter = new FriendAdapter(getContext(), friends);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                    Log.d(TAG, "Retrieved " + friendList.size() + " friends");
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }

}
