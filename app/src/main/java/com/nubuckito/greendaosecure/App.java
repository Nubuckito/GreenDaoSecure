package com.nubuckito.greendaosecure;

import android.app.Application;

import info.guardianproject.cacheword.PRNGFixes;


public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // Apply the Google PRNG fixes to properly seed SecureRandom
        PRNGFixes.apply();
    }


}
