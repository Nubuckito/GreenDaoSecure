package com.nubuckito.greendaosecure;


public class Constants {
    public static final String SHARED_PREFS_SECURE_APP = "privacy_is_not_a_comfort";
    public static final String IS_CYPHER_DB = "isCypherDB";
    public static final String CURRENT_USER_MAIL = "currentUserMail";

    public static final String DBNAME = "SecuredDB";
    public static final String TAG = "NoteCipher";
    public static final int MIN_PASS_LENGTH = 8;
    // public final static int MAX_PASS_ATTEMPTS = 3;
    // public final static int PASS_RETRY_WAIT_TIMEOUT = 30000;

    /**
     * Checks if the password is valid based on it's length
     * @param pass
     * @return True if the password is a valid one, false otherwise
     */
    public static final boolean validatePassword(char[] pass) {
        return pass.length >= Constants.MIN_PASS_LENGTH;
    }

    /**
     * Checks if the mail is valid based on a pattern
     * @param email
     * @return True if the mail is a valid one, false otherwise
     */
    public static final boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
