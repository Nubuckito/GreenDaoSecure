package com.nubuckito.greendaosecure;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import repository.DBRepository;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor>, ICacheWordSubscriber {

    private static final String TAG = LoginActivity.class.getName();

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    //Shared preference change the login display flag
    private SharedPreferences prefs = null;

    // Handler to talk to the CacheWordService
    private CacheWordHandler mCacheWord;

    // UI references.
    private AutoCompleteTextView mEmail;
    private EditText mPasswordView;
    private EditText mNewPasswordView;
    private EditText mConfirmPasswordView;
    private View mProgressView;
    private View mEmailLoginView;
    private View mRootLoginFormView;
    private View mLoginFormView;
    private View mCreateFormView;
    private CheckBox mEncryptCheckBox;
    private Button btnLogin;

    //Activity variables
    private int failedAttempts = 0;
    private int totalFailedAttempts = 0;
    private CountDownTimer countDownTimer;
    private long interval = 1000;
    private boolean timerProcessing=false;
    private boolean passPhraseInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(Constants.SHARED_PREFS_SECURE_APP, MODE_PRIVATE);
        mCacheWord = new CacheWordHandler(this);


        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailLoginView = findViewById(R.id.email_login_form);
        mEmail = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    promptPassphrase();
                    return true;
                }
                return false;
            }
        });

        mNewPasswordView = (EditText) findViewById(R.id.newPassword);
        mConfirmPasswordView = (EditText) findViewById(R.id.confirmPassword);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.confirmLogin || id == EditorInfo.IME_NULL) {
                    promptPassphrase();
                    return true;
                }
                return false;
            }
        });

        mEncryptCheckBox = (CheckBox) findViewById(R.id.encryptionCheck);

        mRootLoginFormView = findViewById(R.id.root_login_form);
        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
        mCreateFormView = findViewById(R.id.create_form);
        btnLogin = (Button) findViewById(R.id.email_sign_in_button);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Notify the CacheWordHandler
        mCacheWord.connectToService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Notify the CacheWordHandler
        mCacheWord.disconnectFromService();
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }





    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRootLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRootLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRootLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRootLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmail.setAdapter(adapter);
    }

    /**
     * Called when CacheWord is reset and there are no secrets to unlock.
     */
    @Override
    public void onCacheWordUninitialized() {
        initializePassphrase();
    }

    /**
     * Called when the cached secrets are wiped from memory.
     */
    @Override
    public void onCacheWordLocked() {
        promptPassphrase();
    }

    /**
     * Called when the secrets are available.
     */
    @Override
    public void onCacheWordOpened() {
        initializeDatabase();
        Intent intent = getIntent().getParcelableExtra("originalIntent");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void validateCreate(){
        mConfirmPasswordView.setError(null);
        mNewPasswordView.setError(null);
        mEmail.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a confirmation password, if the user entered one.
        if (TextUtils.isEmpty(mConfirmPasswordView.getText().toString())) {
            mConfirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = mConfirmPasswordView;
            cancel = true;
        } else {
            // Check for a valid password, if the user entered one.
            if (!Constants.validatePassword(mConfirmPasswordView.getText().toString().toCharArray())) {
                mConfirmPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mConfirmPasswordView;
                cancel = true;
            }else if(!Arrays.equals(mConfirmPasswordView.getText().toString().toCharArray(),mNewPasswordView.getText().toString().toCharArray())){
                mConfirmPasswordView.setError(getString(R.string.error_different_password));
                focusView = mConfirmPasswordView;
                cancel = true;
            }
        }

        // Check for a password, if the user entered one.
        if (TextUtils.isEmpty(mNewPasswordView.getText().toString())) {
            mNewPasswordView.setError(getString(R.string.error_field_required));
            focusView = mNewPasswordView;
            cancel = true;
        } else {
            // Check for a valid password, if the user entered one.
            if (!Constants.validatePassword(mConfirmPasswordView.getText().toString().toCharArray())) {
                mNewPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mNewPasswordView;
                cancel = true;
            }
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail.getText().toString())) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!Constants.isEmailValid(mEmail.getText().toString())) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            register();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void register() {

        //TODO account create.
        // => sauvegarder le mail dans les pref utilisateurs
        prefs.edit().putString(Constants.CURRENT_USER_MAIL, mEmail.getText().toString()).commit();

        try {
            mCacheWord.setPassphrase(mConfirmPasswordView.getText().toString().toCharArray());
        } catch (GeneralSecurityException e) {
            // TODO initialization failed
            Log.e(TAG, "Cacheword pass initialization failed: " + e.getMessage());
        }

        initializeDatabase();
        prefs.edit().putBoolean(Constants.IS_CYPHER_DB, mEncryptCheckBox.isChecked()).commit();
    }

    /**
     * The passphrase has never been initialized.
     *  =>forms validation
     *  =>account create
     *  =>setPassphrase in CacheWord
     */
    private void initializePassphrase() {
        passPhraseInit = true;

        // Passphrase is not set, so allow the user to create one

        mCreateFormView.setVisibility(View.VISIBLE);
        mEmailLoginView.setVisibility(View.VISIBLE);
        mLoginFormView.setVisibility(View.GONE);

        mNewPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                    mNewPasswordView.setError(null);
                    if (!Constants.validatePassword(mNewPasswordView.getText().toString().toCharArray()))
                        mNewPasswordView.setError(getString(R.string.error_invalid_password));
                }
                return false;
            }
        });

        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mConfirmPasswordView.setError(null);
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    if (!Constants.validatePassword(mConfirmPasswordView.getText().toString().toCharArray()))
                        mConfirmPasswordView.setError(getString(R.string.error_invalid_password));
                    else if (actionId == EditorInfo.IME_ACTION_GO) {
                                validateCreate();
                        }
                }
                return false;
            }
        });


        btnLogin = (Button) findViewById(R.id.email_sign_in_button);
        btnLogin.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {

                validateCreate();

            }
        });
    }

    private void promptPassphrase() {
        // Passphrase is set, so allow the user to enter one

        mCreateFormView.setVisibility(View.GONE);
        mEmailLoginView.setVisibility(View.GONE);
        mLoginFormView.setVisibility(View.VISIBLE);



        btnLogin.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (!timerProcessing) {
                    mPasswordView.setError(null);

                    if (mPasswordView.getText().toString().length() == 0)
                        return;
                    // Check passphrase
                    try {
                        mCacheWord.setPassphrase(mPasswordView.getText().toString().toCharArray());
                    } catch (GeneralSecurityException e) {

                        //Wrong PassPhrase
                        // TODO implement wipe if fail in user option

                        Log.e(TAG, "Cacheword pass verification failed: " + e.getMessage());
                        mPasswordView.setText("");

                        //count for wrong attempts. Waiting if 5 wrongs in a row
                        failedAttempts++;


                        //incremental waiting every 5 attempts
                        if ((5-failedAttempts) == 0) {
                            totalFailedAttempts++;
                            failedAttempts = 0;

                            countDownTimer = new CountDownTimer(5000 * totalFailedAttempts, interval) {

                                @Override
                                public void onTick(long millisUntilFinished) {
                                    btnLogin.setText( getString(R.string.wait_too_many_attempts,Long.toString(millisUntilFinished / 1000)));
                                    timerProcessing=true;
                                }

                                @Override
                                public void onFinish() {
                                    timerProcessing=false;
                                    btnLogin.setText(getString(R.string.action_sign_in));
                                }
                            };
                            mPasswordView.setError(getString(R.string.error_too_many_attempts));
                            countDownTimer.start();
                        }else{
                            mPasswordView.setError(getString(R.string.error_incorrect_password, 5 - failedAttempts));
                        }

                        return;
                    }

                }
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_GO)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    Handler threadHandler = new Handler();
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0, new ResultReceiver(
                            threadHandler)
                    {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            btnLogin.performClick();
                        }
                    });
                    return true;
                }
                return false;
            }
        });

    }

    /**
     * Database initialization when cacheword (or app) is first initialized.
     */
    private void initializeDatabase() {
        DBRepository.getInstance().connectToDataBase(this, ((App) getApplicationContext()).getApplicationName(this), mCacheWord, null, mEncryptCheckBox.isChecked());
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

}



