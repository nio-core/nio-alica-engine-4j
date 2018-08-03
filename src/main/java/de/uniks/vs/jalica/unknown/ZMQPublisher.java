package de.uniks.vs.jalica.unknown;

import org.zeromq.ZMQ;

public abstract class ZMQPublisher {

    protected String topic;
    protected ZMQ.Socket publisher;

    public ZMQPublisher(String topic, ZMQ.Socket publisher) {
        this.topic = topic;
        this.publisher = publisher;
    }


    public abstract void publish(Message message);
}
