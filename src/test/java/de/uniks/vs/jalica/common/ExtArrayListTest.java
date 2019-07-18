package de.uniks.vs.jalica.common;

import org.junit.Assert;
import org.junit.Test;

public class ExtArrayListTest {

    @Test
    public void testArray() {
        ExtArrayList<Integer> objects = new ExtArrayList<Integer>(() -> new Integer(42),2);
        Integer i = objects.get(0);
        Assert.assertEquals(42, i.intValue());

        ExtArrayList<String> stringObjects = new ExtArrayList<String>(String::new, 2);
        String s = stringObjects.get(0);
        Assert.assertEquals(true, s instanceof String);
    }
}
