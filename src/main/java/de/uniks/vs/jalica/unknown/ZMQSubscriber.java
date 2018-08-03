package de.uniks.vs.jalica.unknown;

import org.zeromq.ZMQ;

public class ZMQSubscriber {
    private String topic;
    private ZMQ.Socket subscriber;

    public ZMQSubscriber(String topic, ZMQ.Socket subscriber) {
        this.topic = topic;
        this.subscriber = subscriber;
        subscriber.subscribe(topic.getBytes(ZMQ.CHARSET));
    }
}
