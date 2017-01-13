package hohistar.sinde.baselibrary.db;

import android.text.TextUtils;

/**
 * Created by sinde on 16/4/26.
 */
public class SQLBuilder {

    String mTable = null;
    public boolean mIsQuery = true;
    Class<?> mClazz = null;

    private SQLBuilder(){}

    StringBuilder sql = new StringBuilder();

    public static SQLBuilder builderUpdate(String tableName){
        SQLBuilder builder = new SQLBuilder();
        builder.mTable = tableName;
        builder.sql.append("update ").append(tableName).append(" set ");
        builder.mIsQuery = false;
        return builder;
    }

    public static SQLBuilder builderInsert(String tableName){
        SQLBuilder builder = new SQLBuilder();
        builder.mTable = tableName;
        builder.sql.append("INSERT INTO ").append(tableName).append(" ");
        builder.mIsQuery = false;
        return builder;
    }

    public static SQLBuilder builderReplace(String tableName){
        SQLBuilder builder = new SQLBuilder();
        builder.mTable = tableName;
        builder.sql.append("REPLACE INTO ").append(tableName).append(" ");
        builder.mIsQuery = false;
        return builder;
    }

    public static SQLBuilder builderDelete(String tableName){
        SQLBuilder builder = new SQLBuilder();
        builder.mTable = tableName;
        builder.sql.append("DELETE FROM ").append(tableName);
        builder.mIsQuery = false;
        return builder;
    }

    public static SQLBuilder builderDeleteAll(String tableName){
        SQLBuilder builder = new SQLBuilder();
        builder.mTable = tableName;
        builder.sql.append("DELETE FROM ").append(tableName);
        builder.mIsQuery = false;
        return builder;
    }

    public static SQLBuilder builderQuery(String tableName, String queryField){
        SQLBuilder builder = new SQLBuilder();
        builder.mTable = tableName;
        builder.sql.append("select ");
        if (TextUtils.isEmpty(queryField)){
            builder.sql.append("* from ").append(tableName);
        }else {
            builder.sql.append(queryField).append(" from ").append(tableName);
        }
        builder.mIsQuery = false;
        return builder;
    }

    public static SQLBuilder builderQuery(String tableName){
        return builderQuery(tableName,null);
    }


    public static SQLBuilder builderCreate(String tableName){
        SQLBuilder builder = new SQLBuilder();
        builder.mTable = tableName;
        builder.sql.append("create table IF NOT EXISTS ").append(tableName).append(" ");
        return builder;
    }

    public SQLBuilder where(String where){
        sql.append(" where ").append(where);
        return this;
    }

    public SQLBuilder where(String key, Object value, String link){
        if (value == null)value = "null";
        sql.append(" where ").append(key).append(" ");
        if (link.equalsIgnoreCase("in")){
            sql.append(link).append(" (").append(value).append(")");
        }else{
            sql.append(link).append(" '").append(value).append("'");
        }
        return this;
    }

    public SQLBuilder where(WhereBuilder whereBuilder){
        String where = whereBuilder.toString();
        if (!TextUtils.isEmpty(where)){
            sql.append(" where ").append(where).append(" ");
        }
        return this;
    }

    public SQLBuilder and(String key, Object value, String link){
        if (value == null)value = "null";
        sql.append(" and ").append(key).append(" ");
        if (link.equalsIgnoreCase("in")){
            sql.append(link).append(" (").append(value).append(")");
        }else {
            sql.append(link).append(" '").append(value).append("'");
        }
        return this;
    }

    public SQLBuilder and(WhereBuilder whereBuilder){
        if (!TextUtils.isEmpty(whereBuilder.toString())){
            sql.append(" ").append(whereBuilder.toString()).append("");
        }
        return this;
    }

    public SQLBuilder or(String key, Object value, String link){
        if (value == null)value = "null";
        sql.append(" or ").append(key).append(" ");
        if (link.equalsIgnoreCase("in")){
            sql.append(link).append(" (").append(value).append(")");
        }else {
            sql.append(link).append(" '").append(value).append("'");
        }
        return this;
    }

    public SQLBuilder orderBy(String key,boolean DESC){
        if (key != null){
            sql.append(" ORDER BY ").append(key);
            if (DESC)sql.append(" DESC");
        }
        return this;
    }

    public SQLBuilder limit(int count){
        sql.append(" limit ").append(count);
        return this;
    }

    public SQLBuilder orderBy(String key){
        if (key != null){
            sql.append(" ORDER BY ").append(key);
        }
        return this;
    }

    public SQLBuilder groupBy(String column){
        if (column != null)sql.append("  GROUP BY ").append(column);
        return this;
    }

    public SQLBuilder append(String s){
        sql.append(s);
        return this;
    }

    public String toString(){
        return sql.toString();
    }

    public String getTable(){
        return mTable;
    }

    public Class<?> getTableClass(){
        return mClazz;
    }

}
