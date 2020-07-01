package ru.ppr.cppk.entity.utils.builders.events;

import org.junit.Before;
import org.junit.Test;

import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.entity.TrainCategoryPrefix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Артем on 29.12.2015.
 */
public class TrainInfoGeneratorTest {

    private TrainCategory mockTrainCategory;

    @Before
    public void setUp() throws Exception {

        mockTrainCategory = new TrainCategory();
        mockTrainCategory.setVersionId(1);
        mockTrainCategory.prefix = TrainCategoryPrefix.PASSENGER;
        mockTrainCategory.code = 2;
        mockTrainCategory.category = "Я";
    }

    @Test
    public void testBuild() throws Exception {

        TrainInfo trainInfo = new TrainInfoGenerator()
                .setTrainCategory(mockTrainCategory)
                .build();

        assertNotNull(trainInfo);

        assertEquals(2, trainInfo.getTrainCategoryCode());
        assertEquals("Я", trainInfo.getTrainCategory());
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        new TrainInfoGenerator().build();
    }
}