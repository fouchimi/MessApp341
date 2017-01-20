package com.social.messapp34;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.messapp34.adapters.MemberAdapter;
import com.social.messapp34.utils.Constants;
import com.social.messapp34.utils.DividerItemDecoration;

import java.util.List;

public class MemberActivity extends AppCompatActivity {
    private static final String TAG = MemberActivity.class.getSimpleName();

    private ParseUser mCurrentUser;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private TextView emptyTextView;
    private MemberAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        mCurrentUser = ParseUser.getCurrentUser();

        mRecyclerView = (RecyclerView) findViewById(R.id.member_rv);
        emptyTextView = (TextView) findViewById(R.id.empty_view);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        emptyTextView.setTypeface(typeFace);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMembers();
    }

    public void loadMembers(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo(Constants.USERNAME, mCurrentUser.getUsername());
        query.orderByAscending(Constants.USERNAME);
        query.setLimit(500);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> memberList, ParseException e) {
                if (e == null) {
                    if (memberList.isEmpty()) {
                        mRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                    else {
                        mAdapter = new MemberAdapter(MemberActivity.this, memberList);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                    Log.d(TAG, "Retrieved " + memberList.size() + " members");
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }
}
