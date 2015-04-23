package com.nubuckito.greendaosecure;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import com.nubuckito.greendaosecure.fragments.BoxListFragment;

import greendao.BoxDao;
import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import repository.BoxRepository;
import repository.DBRepository;


public class MainActivity extends ActionBarActivity implements ICacheWordSubscriber, BoxListFragment.OnFragmentInteractionListener {

    private static final String TAG = LoginActivity.class.getName();

    private SharedPreferences prefs = null;

    private CacheWordHandler mCacheWord;

    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(Constants.SHARED_PREFS_SECURE_APP, MODE_PRIVATE);

        String[] from = {BoxDao.Properties.Name.columnName, BoxDao.Properties.Description.columnName};
        int[] to = {0/*name emplacement in listview (R.id.)*/, 1/*"description emplacement in listview"*/};
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
                BoxRepository.getAllBoxesCursor(), from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mCacheWord = new CacheWordHandler(this, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCacheWord.connectToService();
        mCacheWord.setNotification(buildNotification(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Notify the CacheWordHandler
        mCacheWord.disconnectFromService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCacheWordUninitialized() {
        Log.d(TAG, "onCacheWordUninitialized");
        clearViewsAndLock();
    }

    @Override
    public void onCacheWordLocked() {
        Log.d(TAG, "onCacheWordLocked");
        clearViewsAndLock();
    }

    @Override
    public void onCacheWordOpened() {
        Log.d(TAG, "onCacheWordOpened");
        //Database already opened on login successful

        //TODO Populate data

    }

    void showLockScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("originalIntent", getIntent());
        startActivity(intent);
        finish();
    }

    void clearViewsAndLock() {
        DBRepository.getInstance().lock();
        showLockScreen();
    }

    /**
     * Build a welcome notification on successful login
     *
     * @param c Context
     * @return the notification to display
     */
    private Notification buildNotification(Context c) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(c);
        b.setSmallIcon(R.drawable.common_signin_btn_text_light);
        b.setContentTitle("Bienvenue !");
        b.setContentText("Vous êtes connecté.");
        //b.setTicker(c.getText(R.string.cacheword_notification_cached));
        b.setDefaults(Notification.DEFAULT_VIBRATE);
        b.setWhen(System.currentTimeMillis());
        b.setOngoing(true);
        b.setContentIntent(CacheWordHandler.getPasswordLockPendingIntent(c));
        return b.build();
    }

    @Override
    public void onFragmentInteraction(Long id) {

    }

    @Override
    public CursorAdapter getDataList() {
        return adapter;
    }
}
