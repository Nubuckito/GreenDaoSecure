package repository;

import android.content.Context;

import java.util.List;

import greendao.Box;
import greendao.BoxDao;

/**
 * Class used to access and modify Box.
 * Created by Buzinga on 09/03/2015.
 */
public class BoxRepository {
    private static BoxDao getBoxDao(Context c) {
        return DBRepository.getInstance().getDaoSession().getBoxDao();
    }

    public static void insertOrUpdate(Context context, Box box) {
        getBoxDao(context).insertOrReplace(box);
    }

    public static void clearBoxes(Context context) {
        getBoxDao(context).deleteAll();
    }

    public static void deleteBoxWithId(Context context, long id) {
        getBoxDao(context).delete(getBoxForId(context, id));
    }

    public static Box getBoxForId(Context context, long id) {
        return getBoxDao(context).load(id);
    }

    public static List<Box> getAllBoxes(Context context) {
        return getBoxDao(context).loadAll();
    }
}
