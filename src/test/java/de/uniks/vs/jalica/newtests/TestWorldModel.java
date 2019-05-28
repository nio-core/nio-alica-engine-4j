package de.uniks.vs.jalica.newtests;

public class TestWorldModel {

    static TestWorldModel one = new TestWorldModel();
    static TestWorldModel two = new TestWorldModel();
    private boolean transitionCondition1413201367990;
    private boolean transitionCondition1413201052549;
    private boolean transitionCondition1413201389955;
    private boolean transitionCondition1413201227586;
    private boolean transitionCondition1413201370590;

    public TestWorldModel() {
        this.transitionCondition1413201227586 = false;
        this.transitionCondition1413201389955 = false;
        this.transitionCondition1413201052549 = false;
        this.transitionCondition1413201367990 = false;
        this.transitionCondition1413201370590 = false;
//        this.preCondition1418042929966 = false;
//        this.runtimeCondition1418042967134 = false;
//
//        this.transitionCondition1418825427317 = false;
//        this.transitionCondition1418825428924 = false;
//
//        this.x = 0;
    }

    public static TestWorldModel getOne() {
        return one;
    }

    public static TestWorldModel getTwo() {
        return two;
    }

    public void setTransitionCondition1413201227586(boolean value) {
        this.transitionCondition1413201227586 = value;
    }
    public boolean isTransitionCondition1413201227586() {
        return transitionCondition1413201227586;
    }

    public void setTransitionCondition1413201389955(boolean value) {
        this.transitionCondition1413201389955 = value;
    }
    public boolean isTransitionCondition1413201389955() {
        return transitionCondition1413201389955;
    }

    public void setTransitionCondition1413201052549(boolean b) {
        this.transitionCondition1413201052549 = b;
    }

    public boolean isTransitionCondition1413201052549() {
        return transitionCondition1413201052549;
    }

    public void setTransitionCondition1413201370590(boolean b) {
        this.transitionCondition1413201370590 = b;
    }

    public boolean isTransitionCondition1413201370590() {
        return transitionCondition1413201370590;
    }

    public void setTransitionCondition1413201367990(boolean transitionCondition1413201367990) {
        this.transitionCondition1413201367990 = transitionCondition1413201367990;
    }

    public boolean isTransitionCondition1413201367990() {
        return transitionCondition1413201367990;
    }
}
