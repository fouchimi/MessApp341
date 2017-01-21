package com.social.messapp34;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.messapp34.adapters.LastCommentAdapter;
import com.social.messapp34.utils.Constants;
import com.social.messapp34.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private LastCommentAdapter commentAdapter;

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
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchRecentMessages();
    }

    public void fetchRecentMessages(){
        final List<ParseObject> comments = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.LAST_CHAT_TABLE);
        Log.d(TAG, "Current id: " + ParseUser.getCurrentUser().getObjectId());
        query.whereEqualTo(Constants.USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.orderByDescending(Constants.DATE);

        query.findInBackground(new FindCallback<ParseObject>() {
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
