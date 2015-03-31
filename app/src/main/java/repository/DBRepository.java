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
    private DaoSession daoSession = null;


    /**
     * Instance unique non préinitialisée
     */
    private static DBRepository INSTANCE = null;


    /**
     * Constructeur privé
     */
    private DBRepository() {
    }


    /**
     * Holder
     */
    private static class DBRepositoryHolder {
        /**
         * Instance unique non préinitialisée
         */
        private final static DBRepository instance = new DBRepository();
    }


    /**
     * Point d'accès pour l'instance unique du singleton
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

    public void closeDB() {
        if (isSessionActive())
            daoSession.getDatabase().close();
    }

    public void clearSession() {
        if (isSessionActive())
            daoSession.clear();
    }

    public void lock() {
        if (isSessionActive()) {
            daoSession.getDatabase().close();
            daoSession.clear();
            daoSession = null;
        }
    }

    public void initializeDatabase(Context ctx, String dbName, CacheWordHandler cacheWordHandler, SQLiteDatabase.CursorFactory cursorFactory, boolean cypherDB) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ctx, dbName, cacheWordHandler, cursorFactory, cypherDB);
        SQLiteDatabaseWrapper db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
