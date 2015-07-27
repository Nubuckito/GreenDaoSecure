package repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import de.greenrobot.dao.wrapper.SQLiteDatabaseWrapper;
import greendao.DaoMaster;
import greendao.DaoSession;
import info.guardianproject.cacheword.CacheWordHandler;

/**
 * Class used to manage access to the database.
 * Created by Buzinga on 10/03/2015.
 */
public class DBRepository {
    /**
     * Instance unique non preinitialisee
     */
    private static DBRepository INSTANCE = null;
    private DaoSession daoSession = null;


    /**
     * Constructeur prive
     */
    private DBRepository() {
    }

    /**
     * Point d'acces pour l'instance unique du singleton
     */
    public static DBRepository getInstance() {
        return DBRepositoryHolder.instance;
    }

    public boolean isSessionActive() {
        return daoSession != null;
    }

    public boolean isDbOpen() {
        if (isSessionActive()) {
            if (daoSession.isOpen())
                return true;
        }
        return false;
    }

    private void closeDB() {
        if (isSessionActive())
            daoSession.getDatabase().close();
    }

    private void clearSession() {
        if (isSessionActive())
            daoSession.clear();
    }

    /**
     * Close the database and clear the session.
     * Call this method when onCacheWordLocked
     */
    public void lock() {
        if (isSessionActive()) {
            daoSession.getDatabase().close();
            daoSession.clear();
            daoSession = null;
            System.gc();
        }
    }

    public void connectToDataBase(Context ctx, String dbName, CacheWordHandler cacheWordHandler, SQLiteDatabase.CursorFactory cursorFactory, boolean cypherDB) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ctx, dbName, cacheWordHandler, cursorFactory, cypherDB);
        SQLiteDatabaseWrapper db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    /**
     * Holder
     */
    private static class DBRepositoryHolder {
        /**
         * Instance unique non preinitialisee
         */
        private final static DBRepository instance = new DBRepository();
    }
}
