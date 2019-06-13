package de.uniks.vs.jalica.communication.discovery;

import org.zeromq.*;

import java.util.Random;

public class ZMQProxyDiscovery extends Discovery {

    public ZMQProxyDiscovery() {
        init();
    }

    protected void init() {
        ZContext ctx = new ZContext();
        ZThread.fork(ctx, new Publisher());
        ZThread.fork(ctx, new Subscriber());

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    ZMQ.Socket subscriber = ctx.createSocket(SocketType.XSUB);
                    subscriber.connect("tcp://localhost:6000");
                    ZMQ.Socket publisher = ctx.createSocket(SocketType.XPUB);
                    publisher.bind("tcp://*:6001");
                    ZMQ.Socket listener = ZThread.fork(ctx, new Listener());
                    ZMQ.proxy(subscriber, publisher, listener);
                } catch (Exception e) {
                    System.out.println("Proxy: proxy runs");
                }

            }
//                ZMQ.Socket proxyPublisher = context.createSocket(SocketType.XPUB);
//                proxyPublisher.bind("tcp://*:6001");
//                ZMQ.Socket proxySubscriber = context.createSocket(SocketType.XSUB);
//                proxySubscriber.bind("tcp://*:6000");
//                ZMQ.Socket listener = ZThread.fork(context, new Listener());
//                ZMQ.proxy(proxySubscriber, proxyPublisher, listener);
//            }
        };
        thread.start();


//        ZMQ.Socket publisher = context.createSocket(SocketType.PUB);
//        publisher.bind("tcp://*:6000");
//        discoveryPublisher = new DiscoveryPublisher(topics.getTopic(Topics.Type.discoveryTopic), publisher);
//        ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
//        subscriber.connect("tcp://localhost:6001");
//        discoverySubscriber = new DiscoverySubscriber(topics.getTopic(Topics.Type.discoveryTopic), subscriber, this);
    }

    private static class Listener implements ZThread.IAttachedRunnable
    {
        @Override
        public void run(Object[] args, ZContext ctx, ZMQ.Socket pipe)
        {
            //  Print everything that arrives on pipe
            while (true) {
                ZFrame frame = ZFrame.recvFrame(pipe);
                if (frame == null)
                    break; //  Interrupted
                frame.print(null);
                frame.destroy();
            }
        }
    }

    //  The subscriber thread requests messages starting with
    //  A and B, then reads and counts incoming messages.
    private static class Subscriber implements ZThread.IAttachedRunnable
    {

        @Override
        public void run(Object[] args, ZContext ctx, ZMQ.Socket pipe)
        {
            //  Subscribe teamObserver "A" and "B"
            ZMQ.Socket subscriber = ctx.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:6001");
            subscriber.subscribe("A".getBytes(ZMQ.CHARSET));
            subscriber.subscribe("B".getBytes(ZMQ.CHARSET));

            int count = 0;
            while (count < 5) {
                String string = subscriber.recvStr();
                if (string == null)
                    break; //  Interrupted
                count++;
            }
            ctx.destroySocket(subscriber);
        }
    }

    //  .split publisher thread
    //  The publisher sends random messages starting with A-J:
    private static class Publisher implements ZThread.IAttachedRunnable
    {
        @Override
        public void run(Object[] args, ZContext ctx, ZMQ.Socket pipe)
        {
            ZMQ.Socket publisher = ctx.createSocket(SocketType.PUB);
            publisher.bind("tcp://*:6000");
            Random rand = new Random(System.currentTimeMillis());

            while (!Thread.currentThread().isInterrupted()) {
                String string = String.format("%c-%05d", 'A' + rand.nextInt(10), rand.nextInt(100000));
                if (!publisher.send(string))
                    break; //  Interrupted
                try {
                    Thread.sleep(100); //  Wait for 1/10th second
                }
                catch (InterruptedException e) {
                }
            }
            ctx.destroySocket(publisher);
        }
    }
}
