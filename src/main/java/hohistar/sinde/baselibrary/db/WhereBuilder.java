package hohistar.sinde.baselibrary.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sinde on 16/5/19.
 */
public class WhereBuilder {

    private final List<String> whereItems = new ArrayList();

    private StringBuilder sql = new StringBuilder();

    private WhereBuilder(){}

    public static WhereBuilder b(String key,String link,Object value){
        if (value == null)value="null";
        WhereBuilder whereBuilder = new WhereBuilder();
        whereBuilder.sql.append("(").append(key).append(" ");
        if (link.equalsIgnoreCase("in")){
            whereBuilder.sql.append(link).append(" (").append(value).append(")");
        }else{
            if(value.equals("null")){
                whereBuilder.sql.append(link).append(" ").append(value).append("");
            }else{
                whereBuilder.sql.append(link).append(" '").append(value).append("'");
            }
        }
        whereBuilder.expr(key+link+value);
        return whereBuilder;
    }

    public WhereBuilder and(String key, String link, Object value){
        if (value == null)value = "null";
        sql.append(" and ").append(key).append(" ");
        if (link.equalsIgnoreCase("in")){
            sql.append(link).append(" (").append(value).append(")");
        }else {
            sql.append(link).append(" '").append(value).append("'");
        }
        expr(key+link+value);
        return this;
    }

    public WhereBuilder or(String key, String link, Object value){
        if (value == null)value = "null";
        sql.append(" or ").append(key).append(" ");
        if (link.equalsIgnoreCase("in")){
            sql.append(link).append(" (").append(value).append(")");
        }else {
            sql.append(link).append(" '").append(value).append("'");
        }
        expr(key+link+value);
        return this;
    }

    public WhereBuilder orderBy(String key,boolean DESC){
        if (key != null){
            sql.append(" ORDER BY ").append(key);
            if (DESC)sql.append(" DESC");
        }
        return this;
    }

    public WhereBuilder limit(int count){
        sql.append(" limit ").append(count);
        return this;
    }

    public WhereBuilder orderBy(String key){
        if (key != null){
            sql.append(" ORDER BY ").append(key);
        }
        return this;
    }

    public WhereBuilder groupBy(String column){
        if (column != null)sql.append(" ").append(column);
        return this;
    }

    public WhereBuilder expr(String expr) {
        this.whereItems.add(" " + expr);
        return this;
    }

    public WhereBuilder append(String s){
        sql.append(s);
        return this;
    }

    public int getWhereItemSize() {
        return this.whereItems.size();
    }

    @Override
    public String toString() {
        return sql.append(" )").toString();
    }
}
