package ru.ppr.cppk.db;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Артем on 17.03.2016.
 */
public class SqlQueryBuilderTest {

    @Test
    public void testBuildQueryJoin() throws Exception {

        SqlQueryBuilder builder = SqlQueryBuilder.newBuilder();
        builder.selectAll().from("Table1")
                .join("Table2")
                .onEquals("Table1.table2id",
                        "Table2.id")
                .join("Table3")
                .onEquals("Table2.table3id",
                        "Table3.id")
                .join("Table4").onEquals("Table1.table4id",
                    "Table4.id")
                .where("Table4.field1 > 100500")
                .and("Table2.field2 < 100500")
                .orderBy("Table4.field3").desc().limit(4);

        String expected = "select * from Table1 join Table2 on Table1.table2id=Table2.id " +
                "join Table3 on Table2.table3id=Table3.id " +
                "join Table4 on Table1.table4id=Table4.id" +
                " where Table4.field1 > 100500 and (table2.field2 < 100500) order by Table4.field3 desc limit 4";

        String query = builder.buildQuery();
        assertNotNull(query);
        String actual = query.toLowerCase().replace("  ", " "); // заменим двойные пробелы на одинарные
        assertEquals(expected.toLowerCase(), actual);
    }

    @Test
    public void testBuildQuerySelect() throws Exception {

        SqlQueryBuilder builder = SqlQueryBuilder.newBuilder();
        builder.selectAll().from("Table1")
                .whereLarger("Key1", 20)
                .and("key2 = 10");

        String expected = "Select * from Table1 where Key1>20 and (key2 = 10)";
        String query = builder.buildQuery();
        assertNotNull(query);
        String actual = query.toLowerCase().replace("  ", " "); // заменим двойные пробелы на одинарные
        assertEquals(expected.toLowerCase().trim(), actual.trim());
    }
}