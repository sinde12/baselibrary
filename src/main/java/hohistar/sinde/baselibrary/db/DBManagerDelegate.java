package hohistar.sinde.baselibrary.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import hohistar.sinde.baselibrary.utility.Utility;
import hohistar.sinde.baselibrary.utility.Utility_File;

/**
 * Created by sinde on 16/8/22.
 */
public class DBManagerDelegate extends DBManager {

    private final String TAG = "DBManagerDelegate";

    private final String QUERY = "query";

    private final String UPDATE = "update";

    Map<String,List<String>> OPERATION_TABLE = new HashMap<String, List<String>>();
    private Set<SoftReference<Model>> mCursor = new HashSet<>();

    Lock mLock = new ReentrantLock();
    SQLiteDatabase mDB;

    private String mDBPath;
    DBManagerDelegate(String path){
        mDBPath = path;
        OPERATION_TABLE.put(QUERY,new ArrayList<String>());
        OPERATION_TABLE.put(UPDATE,new ArrayList<String>());
        try {
            open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPath(){
        return mDBPath;
    }

    private int failCount = 0;
    private void open() throws IOException {
        try {
            mDB = SQLiteDatabase.openOrCreateDatabase(new File(mDBPath),null);
            failCount = 0;
        }catch (Exception e){
            e.printStackTrace();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            if (mDB != null && mDB.isOpen()){
                mDB.close();
            }
            Utility_File.createFile(mDBPath);
            if (failCount++<10)
                open();
            else
                throw new IOException("database open fail");
        }
    }

    private Cursor executeSql(String sql) throws IOException {
        if (!mDB.isOpen()){
            open();
        }
        return mDB.rawQuery(sql,null);
    }

    public void executeNoneSql(String sql) throws Exception {
        mLock.lock();
        handleCursor();
        try {
            if (!mDB.isOpen()){
                open();
            }
            mDB.execSQL(sql);
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            mLock.unlock();
        }
    }

    @Override
    public Cursor executeQuery(String sql) throws Exception {
        Cursor c = null;
        if (!TextUtils.isEmpty(sql)){
            mLock.lock();
            handleCursor();
            try {
                c = executeSql(sql);
            }catch (Exception e){
                e.printStackTrace();
                throw e;
            }finally {
                mLock.unlock();
            }
        }
        return c;
    }

    @Override
    public <T> List<T> findAll(String sql, Class<T> clazz) throws IOException {
        mLock.lock();
        List<T> list = new ArrayList<>();
        try {
            handleCursor();
            Cursor c = executeSql(sql);
            if (c != null){
                try {
                    while (c.moveToNext()){
                        T obj = clazz.newInstance();
                        fillFromCursor(obj,c);
                        list.add(obj);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }finally {
                    c.close();
                }
            }
            if (c!= null) c.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLock.unlock();
        }
        return list;
    }

    @Override
    public <T> List<T> findAll(Selector selector) {
        String sql = selector.toString();
        List list = new ArrayList();
        if (!TextUtils.isEmpty(sql)){
            try {
                list = findAll(sql,selector.entityType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public <T> List<T> findAll(DbModelSelector selector) {
        String sql = selector.toString();
        List list = new ArrayList();
        if (!TextUtils.isEmpty(sql)){
            try {
                list = findAll(sql,selector.getEntityType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public List<DbModel> findDbModelAll(DbModelSelector selector) {
        Cursor cursor = null;
        List<DbModel> list = new ArrayList<>();
        try {
            cursor = executeSql(selector.toString());
            while (cursor.moveToNext()){
                DbModel model = convertCursor(cursor);
                if (model != null)list.add(model);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (cursor!= null && !cursor.isClosed())cursor.isClosed();
        }
        return list;
    }

    private DbModel convertCursor(Cursor cursor){
        DbModel dbModel = null;
        if (cursor != null){
            int columnCount = cursor.getColumnCount();
            for(int i = 0; i < columnCount; ++i) {
                dbModel.add(cursor.getColumnName(i), cursor.getString(i));
            }
        }
        return dbModel;
    }

    @Override
    public <T> List<T> findAll(Class<T> clazz) throws DBException {
        if (clazz.isAnnotationPresent(DbTable.class)){
            return findAll(Selector.from(clazz));
        }else {
            Log.e("DBManagerDelegate",clazz.getName()+" is not table");
            throw new DBException(clazz.getName()+" is not table");
        }
    }

    @Override
    public <T> T findFirst(Selector selector) {
        T obj = null;
        try {
            List list = findAll(selector.toString(),selector.entityType);
            if (!list.isEmpty()){
                obj = (T)list.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public <T> T findFirst(DbModelSelector selector) {
        T obj = null;
        try {
            List list = findAll(selector.toString(),selector.getEntityType());
            if (!list.isEmpty()){
                obj = (T)list.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public <T> T findFirst(String sql, Class<T> clazz) throws IOException {
        List<T> all = findAll(sql,clazz);
        if (all.isEmpty()){
            return null;
        }
        return all.get(0);
    }

    @Override
    public boolean dropTable(String table) {
        String sql = String.format("DROP TABLE %s",table);
        mLock.lock();
        if (!mDB.isOpen()){
            try {
                open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        handleCursor();
        Cursor cursor = null;
        try {
            cursor = executeQuery("SELECT COUNT(*) AS c FROM sqlite_master WHERE type=\'table\' AND name=\'" + table + "\'");
            if (cursor != null){
                if(cursor.moveToNext()) {
                    int e = cursor.getInt(0);
                    cursor.close();
                    if(e > 0) {
                        mDB.execSQL(sql);
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null && !cursor.isClosed()){
                cursor.close();
            }
            mLock.unlock();
        }
        return false;
    }

    @Override
    public boolean dropTable(Class<?> table) {
        if (table.isAnnotationPresent(DbTable.class)){
            DbTable dbTable = table.getAnnotation(DbTable.class);
            String tableName = dbTable.table().equalsIgnoreCase("")?table.getSimpleName():dbTable.table();
            return dropTable(tableName);
        }else {
            Log.e("DBManagerDelegate","table "+table.getSimpleName()+" is not exists");
        }
        return false;
    }

    public boolean tableIsExist(String table){
        mLock.lock();
        if (!mDB.isOpen()){
            try {
                open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        handleCursor();
        Cursor cursor = null;
        try {
            cursor = executeQuery("SELECT COUNT(*) AS c FROM sqlite_master WHERE type=\'table\' AND name=\'" + table + "\'");
            if (cursor != null){
                if(cursor.moveToNext()) {
                    int e = cursor.getInt(0);
                    cursor.close();
                    if(e > 0) {
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null && !cursor.isClosed()){
                cursor.close();
            }
            mLock.unlock();
        }
        return false;
    }

    @Override
    public boolean tableIsExist(Class<?> table) {
        mLock.lock();
        if (!mDB.isOpen()){
            try {
                open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        handleCursor();
        Cursor cursor = null;
        try {
            if (!table.isAnnotationPresent(DbTable.class)){
                Log.e(TAG,table.getName()+" is not a table");
                return false;
            }
            String tableName = table.getAnnotation(DbTable.class).table();
            cursor = executeQuery("SELECT COUNT(*) AS c FROM sqlite_master WHERE type=\'table\' AND name=\'" + tableName + "\'");
            if (cursor != null){
                if(cursor.moveToNext()) {
                    int e = cursor.getInt(0);
                    cursor.close();
                    if(e > 0) {
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null && !cursor.isClosed()){
                cursor.close();
            }
            mLock.unlock();
        }
        return false;
    }

    @Override
    public int saveOrUpdateAll(List<?> models) throws IllegalArgumentException{
        int count = 0;
        for (Object obj:models){
            try {
                saveOrUpdate(obj);
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public void saveOrUpdate(Object obj) throws Exception {
        Class clazz = obj.getClass();
        if (clazz.isAnnotationPresent(DbTable.class)){
            DbTable table = (DbTable) clazz.getAnnotation(DbTable.class);
            String tableName = table.table().equalsIgnoreCase("")?clazz.getSimpleName():table.table();
            StringBuilder builder = new StringBuilder("replace into ");
            StringBuilder value = new StringBuilder(" values (");
            builder.append(tableName).append(" (");
            Field[] fields = clazz.getDeclaredFields();
            for (Field field:fields){
                if (field.isAnnotationPresent(DbId.class)){
                    String id = (String)field.get(obj);
                    DbId dbId = field.getAnnotation(DbId.class);
                    String f = dbId.id().equalsIgnoreCase("")?field.getName():dbId.id();
                    builder.append(f).append(",");
                    value.append("'").append(id).append("'").append(",");
                }else if (field.isAnnotationPresent(DbField.class)){
                    Object v = field.get(obj);
                    if (v != null){
                        DbField dbField = field.getAnnotation(DbField.class);
                        String f = dbField.field().equalsIgnoreCase("")?field.getName():dbField.field();
                        builder.append(f).append(",");
                        value.append("'").append(v).append("'").append(",");
                    }
                }
            }
            builder.delete(builder.length()-1,builder.length()).append(") ");
            value.delete(value.length()-1,value.length()).append(")");
            builder.append(value);
            executeNoneSql(builder.toString());
        }else {
            Log.e("DBManagerDelegate",clazz.getName()+" is not table");
            throw new DBException(clazz.getName()+" is not table");
        }
    }

    @Override
    public void deleteAll(Class<?> clazz) throws Exception {
        DbTable dbTable = clazz.getAnnotation(DbTable.class);
        if (dbTable != null && !TextUtils.isEmpty(dbTable.table())){
            String sql = String.format("delete  from %s",dbTable.table());
            executeNoneSql(sql);
        }else {
            Log.e("DBManagerDelegate",clazz.getName()+" is not table");
            throw new DBException(clazz.getName()+" is not table");
        }
    }

    @Override
    public void delete(Object model) throws Exception {
        Class<?> clazz = model.getClass();
        if (clazz.isAnnotationPresent(DbTable.class)){
            DbTable dbTable = clazz.getAnnotation(DbTable.class);
            for (Field field:clazz.getDeclaredFields()){
                if (field.isAnnotationPresent(DbId.class)){
                    Object obj = field.get(model);
                    String sql = String.format("delete from %s where id = '%s'",dbTable.table(),obj);
                    executeNoneSql(sql);
                    break;
                }
            }
        }else {
            Log.e("DBManagerDelegate",clazz.getName()+" is not table");
            throw new DBException(clazz.getName()+" is not table");
        }
    }

    @Override
    public void deleteById(Class<?> clazz, String id) throws Exception {
        if (clazz.isAnnotationPresent(DbTable.class)){
            DbTable dbTable = clazz.getAnnotation(DbTable.class);
            String sql = String.format("delete from %s where id = '%s'",dbTable.table(),id);
            executeNoneSql(sql);
        }else {
            Log.e("DBManagerDelegate",clazz.getName()+" is not table");
            throw new DBException(clazz.getName()+" is not table");
        }
    }

    @Override
    public <T> T findById(Class<T> clazz, String id) {
        return findFirst(Selector.from(clazz).where("id","=",id));
    }

    public boolean addColumns(String table, String[] fields, String[] types){
        if (tableIsExist(table)){
            mLock.lock();
            if (!mDB.isOpen()){
                try {
                    open();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handleCursor();
            String sql = String.format("select * from %s limit 1",table);
            Cursor cursor = null;
            try {
                cursor = executeSql(sql);
                String[] columns = cursor.getColumnNames();
                List<String> rFields = new ArrayList<String>();
                List<String> rTypes = new ArrayList<String>();
                for (int i=0;i<fields.length;i++){
                    String field = fields[i];
                    boolean tag = false;
                    for (String column:columns){
                        if (column.equalsIgnoreCase(field)){
                            tag = true;
                            break;
                        }
                    }
                    if (!tag){
                        rFields.add(field);
                        rTypes.add(types[i]);
                    }
                }
                for (int i=0;i<rFields.size();i++){
                    sql = String.format("ALTER table %s add column %s %s",table,rFields.get(i),rTypes.get(i));
                    mDB.execSQL(sql);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (cursor != null){
                    cursor.close();
                }
                mLock.unlock();
            }
        }
        return false;
    }

    @Override
    public void beginTransaction() {
        mLock.lock();
        if (!mDB.isOpen()){
            try {
                open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mDB.beginTransaction();
        mLock.unlock();
    }

    @Override
    public void setTransactionSuccessful() {
        mLock.lock();
        if (!mDB.isOpen()){
            try {
                open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mDB.setTransactionSuccessful();
        mLock.unlock();
    }

    @Override
    public void endTransaction() {
        mLock.lock();
        if (!mDB.isOpen()){
            try {
                open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mDB.endTransaction();
        mLock.unlock();
    }

    @Override
    public boolean inTransaction() {
        mLock.lock();
        if (!mDB.isOpen()){
            try {
                open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean fag = mDB.inTransaction();
        mLock.unlock();
        return fag;
    }

    @Override
    public boolean createTableIfNotExists(Class<?> clazz) throws DBException {
        if (clazz.isAnnotationPresent(DbTable.class)){
            DbTable table = clazz.getAnnotation(DbTable.class);
            String tableName = table.table();
            if (tableName.equalsIgnoreCase(""))tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            StringBuilder builder = new StringBuilder("create table if not exists ");
            builder.append(tableName).append(" (");
            List<Field> list = new ArrayList<>();
            for (Field field:fields){
                if (field.isAnnotationPresent(DbId.class)){
                    list.add(0,field);
                }else if (field.isAnnotationPresent(DbField.class)){
                    list.add(field);
                }
            }
            for (Field field:list){
                if (field.isAnnotationPresent(DbId.class)){
                    DbId id = field.getAnnotation(DbId.class);
                    String sid = id.id().equalsIgnoreCase("")?field.getName():id.id();
                    builder.append(sid).append(" TEXT PRIMARY KEY NOT NULL,");
                }else if (field.isAnnotationPresent(DbField.class)){
                    DbField dbField = field.getAnnotation(DbField.class);
                    String f = dbField.field().equalsIgnoreCase("")?field.getName():dbField.field();
                    Class<?> c = field.getType();
                    builder.append(f);
                    if (c.getName().equals(Integer.class.getName()) || c.getName().equals(Long.class.getName())
                            || c == Integer.TYPE || c == Long.TYPE || c.getName().equals(Boolean.class.getName()) || c == Boolean.TYPE){
                        builder.append(" INT");
                    }else if (c.getName().equals(Float.class.getName()) || c.getName().equals(Double.class.getName())
                            || c == Float.TYPE || c == Double.TYPE){
                        builder.append(" REAL");
                    }else {
                        builder.append(" TEXT");
                    }
                    if (field.isAnnotationPresent(DbNotNull.class)){
                        builder.append(" NOT NULL");
                    }
                    builder.append(",");
                }
            }
            builder.deleteCharAt(builder.length()-1);
            builder.append(")");
            mDB.beginTransaction();
            mDB.execSQL(builder.toString());
            mDB.setTransactionSuccessful();
            mDB.endTransaction();
            return true;
        }else {
            throw new DBException(clazz.getName()+" is not Table class");
        }
    }

    private void fillFromCursor(Object o, Cursor c) {
        Class t = o.getClass();
        int bound = c.getColumnCount();
        for (int i = 0; i < bound; i++) {
            String col = c.getColumnName(i);
            if (col.equalsIgnoreCase("ID")){
                col = "id";
            }
            try {
                Field f = t.getField(col);
                if (f != null) {
                    Object col_v = null;
                    Class ft = f.getType();
                    if (ft.equals(String.class)) {
                        col_v = c.getString(i);
                    } else if (ft.equals(Integer.class)) {
                        col_v = c.getInt(i);
                    } else if (ft.equals(Float.class)) {
                        col_v = c.getFloat(i);
                    } else if (ft.equals(Double.class)) {
                        col_v = c.getDouble(i);
                    } else if (ft.equals(Boolean.class)){
                        col_v = c.getInt(i)!=0;
                    }
                    //赋值
                    try {
                        f.set(o, col_v);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCursor(){
        if (mCursor.size()>10){
            mCursor.size();
        }
        List<SoftReference<Model>> temp = new ArrayList<>();
        for (SoftReference<Model> reference : mCursor){
            if (reference.get() == null || reference.get().cursor.isClosed()
                    || (reference.get().cursor.getPosition() != -1 && reference.get().cursor.isAfterLast())){
                temp.add(reference);
            }else {
                long between = System.currentTimeMillis()-reference.get().time;
                int p = reference.get().cursor.getPosition();
                boolean flag = false;
                if (reference.get().position == -10){
                    reference.get().position = p;
                }else {
//                    if (reference.get().position == p)flag = true;
                    reference.get().position = p;
                }
                if (flag || between>1000*20){
                    reference.get().cursor.close();
                    temp.add(reference);
                }
            }
        }
        mCursor.removeAll(temp);
//        if (!mCursor.isEmpty()){
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            handleCursor();
//        }
        if (mCursor.size()>3){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handleCursor();
        }
    }

    private class Model{

        Model(long time,Cursor c){
            this.time = time;
            cursor = c;
        }

        long time;

        Cursor cursor;

        long position = -10;

    }

}
