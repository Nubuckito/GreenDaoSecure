package repository;

import android.content.Context;
import android.database.Cursor;

import java.util.List;

import greendao.Box;
import greendao.BoxDao;

/**
 * Class used to access and modify Box.
 * Created by Buzinga on 09/03/2015.
 */
public class BoxRepository {
    private static BoxDao getBoxDao() {
        return DBRepository.getInstance().getDaoSession().getBoxDao();
    }

    public static void insertOrUpdate(Box box) {
        getBoxDao().insertOrReplace(box);
    }

    public static void clearBoxes(Context context) {
        getBoxDao().deleteAll();
    }

    public static void deleteBoxWithId(long id) {
        getBoxDao().delete(getBoxForId(id));
    }

    public static Box getBoxForId(long id) {
        return getBoxDao().load(id);
    }

    public static List<Box> getAllBoxes() {
        return getBoxDao().loadAll();
    }

    public static Cursor getAllBoxesCursor() {
        return getBoxDao().getDatabase().query(getBoxDao().getTablename(), getBoxDao().getAllColumns(), null, null, null, null, BoxDao.Properties.Name.columnName + " COLLATE LOCALIZED ASC");
    }
}
