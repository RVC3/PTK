package ru.ppr.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Билдер SQL-запроса к БД.
 *
 * @author Aleksandr Brazhkin
 */
public class QueryBuilder {
    /**
     * Список аргументов запроса.
     */
    private final List<Object> selectionArgsList = new ArrayList<>();
    /**
     * Билдер строки запроса.
     */
    private final StringBuilder sb = new StringBuilder();

    public QueryBuilder addArg(Object arg) {
        selectionArgsList.add(arg);
        return this;
    }

    public QueryBuilder appendRaw(String str) {
        sb.append(str);
        return this;
    }

    public QueryBuilder appendRaw(char c) {
        sb.append(c);
        return this;
    }

    public QueryBuilder appendRaw(int i) {
        sb.append(i);
        return this;
    }

    public QueryBuilder appendRaw(long lng) {
        sb.append(lng);
        return this;
    }

    public QueryBuilder arg(Object arg) {
        sb.append("?");
        addArg(arg);
        return this;
    }

    public QueryBuilder appendArgList(Iterable args) {
        sb.append(" (");
        int size = 0;
        for (Object arg : args) {
            size++;
            addArg(arg);
        }
        sb.append(SqLiteUtils.makePlaceholders(size));
        sb.append(") ");
        return this;
    }

    public QueryBuilder in() {
        sb.append(" IN ");
        return this;
    }

    public QueryBuilder notIn() {
        sb.append(" NOT IN ");
        return this;
    }

    public QueryBuilder notIn(Iterable args) {
        sb.append(" NOT IN ");
        appendArgList(args);
        return this;
    }

    public QueryBuilder in(Iterable args) {
        sb.append(" IN ");
        appendArgList(args);
        return this;
    }

    public QueryBuilder f1EqF2(String f1, String f2) {
        sb.append(f1).append(" = ").append(f2);
        return this;
    }

    public QueryBuilder f1EqF2(String t1, String f1, String t2, String f2) {
        sb.append(t1).append(".").append(f1).append(" = ").append(t2).append(".").append(f2);
        return this;
    }

    public QueryBuilder f1NotEqF2(String t1, String f1, String t2, String f2) {
        sb.append(t1).append(".").append(f1).append(" <> ").append(t2).append(".").append(f2);
        return this;
    }

    public QueryBuilder trueCond() {
        sb.append(" 1 = 1 ");
        return this;
    }

    public QueryBuilder falseCond() {
        sb.append(" 1 = 0 ");
        return this;
    }

    public QueryBuilder eq() {
        sb.append(" = ");
        return this;
    }

    public QueryBuilder notEq() {
        sb.append(" <> ");
        return this;
    }

    public QueryBuilder eq(Object arg) {
        sb.append(" = ");
        arg(arg);
        return this;
    }

    public QueryBuilder notEq(Object arg) {
        sb.append(" <> ");
        arg(arg);
        return this;
    }

    public QueryBuilder like(String expr) {
        sb.append(" LIKE ");
        arg(expr);
        return this;
    }

    public QueryBuilder isNull() {
        sb.append(" IS NULL ");
        return this;
    }

    public QueryBuilder isNotNull() {
        sb.append(" IS NOT NULL ");
        return this;
    }

    public QueryBuilder field(String field) {
        sb.append(field);
        return this;
    }

    public QueryBuilder fields(String... fields) {
        int iMax = fields.length - 1;
        if (iMax == -1)
            return this;

        for (int i = 0; ; i++) {
            sb.append(fields[i]);
            if (i == iMax)
                return this;
            sb.append(", ");
        }
    }

    public QueryBuilder field(String table, String field) {
        sb.append(table).append(".").append(field);
        return this;
    }

    public QueryBuilder as(String name) {
        sb.append(" AS ").append(name);
        return this;
    }

    public QueryBuilder select() {
        sb.append("SELECT ");
        return this;
    }

    public QueryBuilder selectAll() {
        sb.append("SELECT * ");
        return this;
    }

    public QueryBuilder selectDistinct() {
        sb.append("SELECT DISTINCT ");
        return this;
    }

    public QueryBuilder from() {
        sb.append(" FROM ");
        return this;
    }

    public QueryBuilder from(String table) {
        sb.append(" FROM ").append(table);
        return this;
    }

    public QueryBuilder on() {
        sb.append(" ON ");
        return this;
    }

    public QueryBuilder where() {
        sb.append(" WHERE ");
        return this;
    }

    public QueryBuilder and() {
        sb.append(" AND ");
        return this;
    }

    public QueryBuilder or() {
        sb.append(" OR ");
        return this;
    }

    public QueryBuilder orderBy() {
        sb.append(" ORDER BY ");
        return this;
    }

    public QueryBuilder orderBy(String value) {
        orderBy().appendRaw(value);
        return this;
    }

    public QueryBuilder desc() {
        sb.append(" DESC ");
        return this;
    }

    public QueryBuilder asc() {
        sb.append(" ASC ");
        return this;
    }

    public QueryBuilder limit(int limit) {
        sb.append(" LIMIT ").append(limit);
        return this;
    }

    public QueryBuilder offset(int offset) {
        sb.append(" OFFSET ").append(offset);
        return this;
    }

    public QueryBuilder exists(Runnable runnable) {
        sb.append(" EXISTS ");
        appendInBrackets(runnable);
        return this;
    }

    public QueryBuilder notExists(Runnable runnable) {
        sb.append(" NOT EXISTS ");
        appendInBrackets(runnable);
        return this;
    }

    public QueryBuilder appendInBrackets(Runnable runnable) {
        sb.append(" ( ");
        runnable.run();
        sb.append(" ) ");
        return this;
    }

    public QueryBuilder innerJoin() {
        sb.append(" INNER JOIN ");
        return this;
    }

    public QueryBuilder innerJoin(String table) {
        innerJoin().table(table);
        return this;
    }

    public QueryBuilder leftJoin() {
        sb.append(" LEFT JOIN ");
        return this;
    }

    public QueryBuilder leftJoin(String table) {
        leftJoin().table(table);
        return this;
    }

    public QueryBuilder table(String table) {
        sb.append(table);
        return this;
    }

    public QueryBuilder unionAll() {
        sb.append(" UNION ALL ");
        return this;
    }

    public QueryBuilder indexedBy(String indexName) {
        sb.append(" INDEXED BY ").append(indexName);
        return this;
    }

    public QueryBuilder comma() {
        sb.append(", ");
        return this;
    }

    public QueryBuilder caseStart() {
        sb.append(" CASE ");
        return this;
    }

    public QueryBuilder caseWhen() {
        sb.append(" WHEN ");
        return this;
    }

    public QueryBuilder caseThen() {
        sb.append(" THEN ");
        return this;
    }

    public QueryBuilder caseElse() {
        sb.append(" ELSE ");
        return this;
    }

    public QueryBuilder caseEnd() {
        sb.append(" END ");
        return this;
    }

    public QueryBuilder count(String field) {
        sb.append(" COUNT(").append(field).append(") ");
        return this;
    }

    public QueryBuilder min(String field) {
        sb.append(" MIN(").append(field).append(") ");
        return this;
    }

    public QueryBuilder min(String table, String field) {
        sb.append(" MIN(").append(table).append(".").append(field).append(") ");
        return this;
    }

    public QueryBuilder max(String field) {
        sb.append(" MAX(").append(field).append(") ");
        return this;
    }

    public QueryBuilder max(String table, String field) {
        sb.append(" MAX(").append(table).append(".").append(field).append(") ");
        return this;
    }

    public QueryBuilder groupBy(String field) {
        sb.append(" GROUP BY ").append(field).append(" ");
        return this;
    }

    public QueryBuilder groupBy(String table, String field) {
        sb.append(" GROUP BY ").append(table).append(".").append(field).append(" ");
        return this;
    }

    public QueryBuilder more() {
        sb.append(" > ");
        return this;
    }

    public QueryBuilder less() {
        sb.append(" < ");
        return this;
    }

    public Query build() {
        return new Query(getQueryBody(), getQueryArgs());
    }

    private String getQueryBody() {
        return sb.toString();
    }

    private Object[] getQueryArgs() {
        Object[] selectionArgs = new Object[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        return selectionArgs;
    }
}
