package com.social.messapp34;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.interceptors.ParseLogInterceptor;
import com.parse.interceptors.ParseStethoInterceptor;

/**
 * Created by ousmane on 1/18/17.
 */

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getApplicationContext().getString(R.string.app_id))
                .clientKey("")
                .addNetworkInterceptor(new ParseStethoInterceptor())
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server(getApplicationContext().getString(R.string.server))
                .build()
        );

        // Need to register GCM token
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser().getObjectId());
        installation.saveInBackground();
    }
}
