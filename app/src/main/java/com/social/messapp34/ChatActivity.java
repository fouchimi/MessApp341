package com.social.messapp34;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.messapp34.adapters.LastCommentAdapter;
import com.social.messapp34.model.ChatUser;
import com.social.messapp34.model.Conversation;
import com.social.messapp34.utils.CircleTransform;
import com.social.messapp34.utils.Constants;
import com.social.messapp34.utils.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private List<Conversation> convList;
    private ChatAdapter chatAdapter;

    private EditText txt;
    private ImageButton mSendButton;
    private ChatUser buddy;
    private ListView list;
    private ParseUser mCurrentUser;
    public boolean isRunning = false;
    private Date lastMsgDate;

    private static Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mCurrentUser = ParseUser.getCurrentUser();

        convList = new ArrayList<>();
        list = (ListView) findViewById(R.id.list);
        chatAdapter = new ChatAdapter();
        list.setAdapter(chatAdapter);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EditText) findViewById(R.id.txt);
        mSendButton = (ImageButton) findViewById(R.id.btnSend);
        txt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        buddy = (ChatUser) getIntent().getSerializableExtra(Constants.FRIEND);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(buddy.getUsername());
            actionBar.setDisplayOptions(actionBar.getDisplayOptions()
                    | ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView imageView = new ImageView(actionBar.getThemedContext());
            imageView.setScaleType(ScaleType.FIT_XY);
            Picasso.with(this).load(buddy.getThumbnail()).transform(new CircleTransform()).into(imageView);
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    210,
                    ActionBar.LayoutParams.MATCH_PARENT, Gravity.RIGHT
                    | Gravity.FILL_VERTICAL);
            layoutParams.rightMargin = 10;
            imageView.setLayoutParams(layoutParams);
            actionBar.setCustomView(imageView);
        }

        handler = new Handler();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        loadConversationList();
    }

    @Override
    public void onStop() {
        super.onStop();
        isRunning = false;
    }

    public void loadConversationList(){
        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        final ParseQuery firstQuery = new ParseQuery(Constants.CHATS_TABLE);
        firstQuery.whereEqualTo(Constants.SENDER, mCurrentUser.getObjectId());
        firstQuery.whereEqualTo(Constants.RECEIVER, buddy.getId());

        final ParseQuery secondQuery = new ParseQuery(Constants.CHATS_TABLE);
        secondQuery.whereEqualTo(Constants.SENDER, buddy.getId());
        secondQuery.whereEqualTo(Constants.RECEIVER, mCurrentUser.getObjectId());

        ParseQuery<ParseObject> mainQuery = null;
        if(convList.size() == 0){
            //Fetch all conversations
            queries.add(firstQuery);
            queries.add(secondQuery);

            mainQuery = ParseQuery.or(queries);
            mainQuery.orderByDescending(Constants.CREATED_AT);
            Log.d(TAG, "Fetching all conversations ...");
        }else {
            // Fetch receiving messages
            queries.clear();
            queries.add(secondQuery);
            mainQuery = ParseQuery.or(queries);
            if(lastMsgDate != null){
                mainQuery.orderByDescending(Constants.CREATED_AT);
                mainQuery.whereGreaterThan(Constants.CREATED_AT, lastMsgDate);
            }

        }
        mainQuery.setLimit(30);
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(list != null && list.size() > 0){
                    for(int i= list.size()-1; i >= 0; i--){
                        ParseObject po = list.get(i);
                        Conversation c = new Conversation();
                        c.setMsg(po.getString(Constants.MESSAGE));
                        c.setDate(po.getCreatedAt());
                        Log.d(TAG, "current date: " + c.getDate());
                        c.setSenderId(po.getString(Constants.SENDER));
                        convList.add(c);
                        if(lastMsgDate == null || lastMsgDate.before(c.getDate()))
                            lastMsgDate = c.getDate();
                        chatAdapter.notifyDataSetChanged();
                    }

                    Log.d(TAG, "After loading all data: " + lastMsgDate.toString());

                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isRunning){
                            Log.d(TAG, "Running after new message");
                            loadConversationList();
                        }
                    }
                }, 1000);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);
    }

    public void sendMessage(){
        final String messageText = txt.getText().toString().trim();
        if(messageText.length() == 0 || messageText.isEmpty()) return;
        else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(txt, InputMethodManager.SHOW_FORCED);
            final Date currentDate = new Date();
            final Conversation c = new Conversation(messageText, mCurrentUser.getObjectId());
            c.setStatus(Conversation.STATUS_SENDING);
            if(lastMsgDate != null && lastMsgDate.before(currentDate)) lastMsgDate = currentDate;
            Log.d(TAG, "Last message date: " + lastMsgDate.toString());
            convList.add(c);
            chatAdapter.notifyDataSetChanged();
            txt.setText("");

            String[] chunks = mCurrentUser.getUsername().split("_");
            String currentUserName = chunks[0];

            ParseObject newRecord = new ParseObject(Constants.CHATS_TABLE);
            final ParseObject lastComment = new ParseObject(Constants.LAST_CHAT_TABLE);

            String profile_Url = mCurrentUser.getString(Constants.PROFILE_PICTURE);
            if(profile_Url == null) profile_Url = getString(R.string.default_profile_url);

            newRecord.put(Constants.SENDER, mCurrentUser.getObjectId());
            newRecord.put(Constants.RECEIVER, buddy.getId());
            newRecord.put(Constants.MESSAGE, messageText);

            newRecord.saveEventually(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        c.setStatus(Conversation.STATUS_SENT);
                        chatAdapter.notifyDataSetChanged();
                    }else {
                        Log.d(TAG, e.getMessage());
                        c.setStatus(Conversation.STATUS_FAILED);
                        chatAdapter.notifyDataSetChanged();
                    }
                }
            });

            lastComment.put(Constants.FRIEND_ID, buddy.getId());
            lastComment.put(Constants.MESSAGE, messageText);
            lastComment.put(Constants.CREATED_AT, currentDate);
            lastComment.put(Constants.FRIEND_NAME, buddy.getUsername() + "," + currentUserName);
            lastComment.put(Constants.PROFILE_PICTURE, buddy.getThumbnail()+ "," + profile_Url);
            lastComment.put(Constants.USER_ID, mCurrentUser.getObjectId());

            ParseQuery<ParseObject> firstQuery = ParseQuery.getQuery(Constants.LAST_CHAT_TABLE);
            firstQuery.whereEqualTo(Constants.FRIEND_ID, buddy.getId());
            firstQuery.whereEqualTo(Constants.USER_ID, mCurrentUser.getObjectId());

            ParseQuery<ParseObject> secondQuery = ParseQuery.getQuery(Constants.LAST_CHAT_TABLE);
            secondQuery.whereEqualTo(Constants.FRIEND_ID, mCurrentUser.getObjectId());
            secondQuery.whereEqualTo(Constants.USER_ID, buddy.getId());

            List<ParseQuery<ParseObject>> queries = new ArrayList<>();
            queries.add(firstQuery);
            queries.add(secondQuery);

            ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);

            mainQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject lastConversation, ParseException e) {
                    if(e == null){
                        if(lastConversation != null){
                            lastConversation.put(Constants.MESSAGE, messageText);
                            lastConversation.put(Constants.CREATED_AT, currentDate);
                            lastConversation.saveInBackground();
                        }
                    }else {
                        Log.d(TAG, e.getMessage());
                        lastComment.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e != null){
                                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private  class ChatAdapter extends BaseAdapter {

        public ChatAdapter(){}

        @Override
        public int getCount() {
            return convList.size();
        }

        @Override
        public Conversation getItem(int position) {
            return convList.get(position);
        }

        @Override
        public long getItemId(int id) {
            return id;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            Conversation c = getItem(pos);
            if(c.isSent()){
                convertView = LayoutInflater.from(ChatActivity.this).inflate(R.layout.chat_item_sent, parent, false);
            }
            else{
                convertView = LayoutInflater.from(ChatActivity.this).inflate(R.layout.chat_item_rcv, parent, false);
            }

            ImageView profileView = (ImageView) convertView.findViewById(R.id.profile_thumbnail);
            String profile_Url = mCurrentUser.getString(Constants.PROFILE_PICTURE);
            if(profile_Url == null) profile_Url = getString(R.string.default_profile_url);
            if(c.isSent())
                Picasso.with(ChatActivity.this).load(profile_Url).transform(new CircleTransform()).into(profileView);
            else {
                Picasso.with(ChatActivity.this).load(buddy.getThumbnail()).transform(new CircleTransform()).into(profileView);
            }
            TextView lbl = (TextView) convertView.findViewById(R.id.lbl1);

            if(c.getDate() == null){
                c.setDate(new Date());
            }
            lbl.setText(Utility.getFriendlyDayAndTimeString(ChatActivity.this, c.getDate().getTime()));

            lbl = (TextView) convertView.findViewById(R.id.lbl2);
            lbl.setText(c.getMsg());

            lbl = (TextView) convertView.findViewById(R.id.lbl3);

            if (c.isSent()) {
                if (c.getStatus() == Conversation.STATUS_SENT)
                    lbl.setText(getString(R.string.delivered_text));
                else {
                    if (c.getStatus() == Conversation.STATUS_SENDING)
                        lbl.setText(getString(R.string.sending_text));
                    else {
                        lbl.setText(getString(R.string.failed_text));
                    }
                }
            }else{
                lbl.setText("");
            }

            return convertView;
        }


    }

}
