package de.uniks.vs.jalica;

import org.junit.Test;

public class TestAlica {

    @Test
    public void testALICA() {
        Alica alicaAgent1 = new Alica("nio_zero");
        alicaAgent1.start();

        waitFor(2000);

        Alica alicaAgent2 = new Alica("nio_one");
        alicaAgent2.start();

        while (true) {
            waitFor(100);
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
