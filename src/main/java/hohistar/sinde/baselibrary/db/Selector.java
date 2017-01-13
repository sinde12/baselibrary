package hohistar.sinde.baselibrary.db;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sinde on 16/8/22.
 */
public class Selector {

    protected Class<?> entityType;
    protected String tableName;
    protected WhereBuilder whereBuilder;
    protected List<OrderBy> orderByList;
    protected int limit = 0;
    protected int offset = 0;

    private Selector(Class<?> entityType) {
        this.entityType = entityType;
        if (entityType.isAnnotationPresent(DbTable.class)){
            this.tableName = entityType.getAnnotation(DbTable.class).table();
        }else {
            Log.e("DBManagerDelegate",entityType.getName()+" is not table");
        }
    }

    private Selector(String table) {
        this.tableName = table;
    }

    public static Selector from(Class<?> entityType) {
        return new Selector(entityType);
    }

    public static Selector from(String tableName) {
        return new Selector(tableName);
    }

    public Selector where(WhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }

    public Selector where(String columnName, String op, Object value) {
        this.whereBuilder = WhereBuilder.b(columnName, op, value);
        whereBuilder.expr(")");
        return this;
    }

    public Selector and(String columnName, String op, Object value) {
        this.whereBuilder.and(columnName, op, value);
       // whereBuilder.expr(")");
        return this;
    }

    public Selector and(WhereBuilder where) {
        this.whereBuilder.expr("AND (" + where.toString() + ")");
        return this;
    }

    public Selector or(String columnName, String op, Object value) {
        this.whereBuilder.or(columnName, op, value);
        return this;
    }

    public Selector or(WhereBuilder where) {
        this.whereBuilder.expr("OR (" + where.toString() + ")");
        return this;
    }

    public DbModelSelector groupBy(String columnName) {
        return new DbModelSelector(this, columnName);
    }

    public DbModelSelector select(String... columnExpressions) {
        return new DbModelSelector(this, columnExpressions);
    }

    public Selector orderBy(String columnName) {
        if(this.orderByList == null) {
            this.orderByList = new ArrayList(2);
        }

        this.orderByList.add(new Selector.OrderBy(columnName));
        return this;
    }

    public Selector orderBy(String columnName, boolean desc) {
        if(this.orderByList == null) {
            this.orderByList = new ArrayList(2);
        }

        this.orderByList.add(new Selector.OrderBy(columnName, desc));
        return this;
    }

    public Selector limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Selector offset(int offset) {
        this.offset = offset;
        return this;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("SELECT ");
        result.append("*");
        result.append(" FROM ").append(this.tableName);
        if(this.whereBuilder != null && this.whereBuilder.getWhereItemSize() > 0) {
            result.append(" WHERE ").append(this.whereBuilder.toString());
        }

        if(this.orderByList != null) {
            for(int i = 0; i < this.orderByList.size(); ++i) {
                result.append(" ORDER BY ").append((this.orderByList.get(i)).toString());
            }
        }

        if(this.limit > 0) {
            result.append(" LIMIT ").append(this.limit);
            result.append(" OFFSET ").append(this.offset);
        }

        return result.toString();
    }

    public Class<?> getEntityType() {
        return this.entityType;
    }

    protected class OrderBy {
        private String columnName;
        private boolean desc;

        public OrderBy(String columnName) {
            this.columnName = columnName;
        }

        public OrderBy(String columnName, boolean desc) {
            this.columnName = columnName;
            this.desc = desc;
        }

        public String toString() {
            return this.columnName + (this.desc?" DESC":" ASC");
        }
    }

}
