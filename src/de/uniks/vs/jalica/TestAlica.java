package de.uniks.vs.jalica;

import org.junit.Test;

public class TestAlica {

    @Test
    public void testALICA() {
        Alica alicaAgent1 = new Alica("nio_zero");
        Alica alicaAgent2 = new Alica("nio_one");
        alicaAgent1.start();
        alicaAgent2.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
