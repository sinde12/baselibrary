package hohistar.sinde.baselibrary.db;

import android.database.Cursor;

import java.io.IOException;
import java.util.List;

/**
 * Created by sinde on 16/8/22.
 */
public abstract class DBManager {

    DBManager(){}

    private static DBManager mInstance = null;
    public static DBManager getInstance(String path){
        if (mInstance == null){
            synchronized (DBManager.class){
                if (mInstance == null)mInstance = new DBManagerDelegate(path);
            }
        }
        return mInstance;
    }

    public abstract String getPath();

    public abstract void executeNoneSql(String sql) throws Exception;

    public abstract Cursor executeQuery(String sql) throws Exception;

    public abstract  <T> List<T> findAll(String sql,Class<T> clazz) throws IOException;

    public abstract  <T> List<T> findAll(Selector selector);

    public abstract  <T> List<T> findAll(DbModelSelector selector);

    public abstract List<DbModel> findDbModelAll(DbModelSelector selector);

    public abstract  <T> List<T> findAll(Class<T> clazz) throws DBException;

    public abstract <T> T findFirst(Selector selector);

    public abstract <T> T findFirst(DbModelSelector selector);

    public abstract <T> T findFirst(String sql,Class<T> clazz) throws IOException;

    public abstract boolean dropTable(String table);

    public abstract boolean tableIsExist(String table);

    public abstract boolean tableIsExist(Class<?> table);

    public abstract int saveOrUpdateAll(List<?> models) throws IllegalArgumentException;

    public  abstract void saveOrUpdate(Object obj) throws Exception;

    public abstract void deleteAll(Class<?> clazz) throws Exception;

    public abstract <T> T findById(Class<T> clazz,String id);

    public abstract void delete(Object obj) throws Exception;

    public abstract void deleteById(Class<?> clazz,String id) throws Exception;

    public abstract boolean addColumns(String table, String[] fields, String[] types);

    public abstract void beginTransaction();

    public abstract void setTransactionSuccessful();

    public abstract void endTransaction();

    public abstract boolean inTransaction();

    public abstract boolean createTableIfNotExists(Class<?> clazz) throws DBException;

    public abstract boolean dropTable(Class<?> clazz);

}
