package ru.ppr.chit.api.mapper;

import org.junit.Test;

import java.util.Date;

import ru.ppr.chit.api.BaseTest;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Nevolin
 */
public class UtcDateMapperTest extends BaseTest {

    @Test
    public void parseAllPossibleDates() throws Exception {
        String[] stringDates = {
                "2018-01-01T01:00:01Z",
                "2017-02-02T01:02:02.1Z",
                "2016-03-03T03:00:03.12Z",
                "2015-04-04T03:04:04.123Z",
                "2014-05-05T05:00:05.1234Z",
                "2013-06-06T05:06:06.12345Z",
                "2012-07-07T07:00:07.123456Z"
        };
        Date[] expectedDates = {
                new Date(1514768401000L),
                new Date(1485997322001L),
                new Date(1456974003012L),
                new Date(1428116644123L),
                new Date(1399266005123L),
                new Date(1370495166123L),
                new Date(1341644407123L)
        };
        for (int i = 0; i < stringDates.length; i++) {
            Date date = UtcDateMapper.INSTANCE.entityToModel(stringDates[i]);
            assertEquals(date, expectedDates[i]);
        }
    }

}
