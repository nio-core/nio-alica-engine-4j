package de.uniks.vs.jalica.newtests;

import de.uniks.vs.jalica.Alica;
import org.junit.Test;

public class TestAlica {

    @Test
    public void testALICA() {
        Alica alicaAgent1 = new Alica("nio_zero");
        alicaAgent1.start();

//        waitFor(2000);

        Alica alicaAgent2 = new Alica("nio_one");
        alicaAgent2.start();

        while (true) {
            waitFor(100000);
        }
    }

    private void waitFor( long mseconds) {
        try {
            Thread.sleep(mseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
