package de.uniks.vs.jalica.discovery;

import de.uniks.vs.jalica.common.utils.CommonNetworkUtils;
import guide.espresso;
import org.junit.Test;
import org.zeromq.*;
import org.zeromq.guide.EspressoTest;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestDiscovery {


//    private static final String MULTICAST_INTERFACE = "en7"; //"en0" //"eth0";
    private static final String MULTICAST_INTERFACE = "en0";
    private static final int MULTICAST_PORT = 4446;
    private static final String MULTICAST_IP = "230.0.0.1";
//    private static final String MULTICAST_IP = "localhost";//"230.0.0.1";
    private InetSocketAddress socketAddress;



    @Test
    public void testJavaDatagramChannelMulticast1() throws IOException, InterruptedException {
        System.setProperty("java.net.preferIPv6Stack", "true");
        CommonNetworkUtils.getAvailableNetworkInterfaces();

        Thread MulticastReceiver = new Thread() {

            @Override
            public void run() {
                DatagramChannelMulticastReceiver mr = new DatagramChannelMulticastReceiver();
                try {
                    System.out.println("Message received : " + mr.receiveMessage(MULTICAST_IP, MULTICAST_PORT, MULTICAST_INTERFACE));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        MulticastReceiver.start();

        Thread.sleep(4000);

        Thread MulticastPublisher = new Thread() {

            @Override
            public void run() {
                DatagramChannelMulticastPublisher mp = new DatagramChannelMulticastPublisher();
                try {
                    mp.sendMessage(MULTICAST_IP, MULTICAST_PORT, MULTICAST_INTERFACE, "Hi there!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        MulticastPublisher.start();

        Thread.sleep(4000);
    }

    @Test
    public void testJavaMulticast1() throws IOException, InterruptedException {

        Thread t = new Thread(new UDPMulticastClient(MULTICAST_INTERFACE, MULTICAST_IP, MULTICAST_PORT));
        t.start();

        Thread.sleep(2000);

        System.out.println("send");
        UDPMulticastServer.sendUDPMessage("This is a multicast messge", "230.0.0.0", MULTICAST_PORT);
        System.out.println("send");
        UDPMulticastServer.sendUDPMessage("This is the second multicast messge", "230.0.0.0", MULTICAST_PORT);
        System.out.println("send");
        UDPMulticastServer.sendUDPMessage("This is the third multicast messge", "230.0.0.0", MULTICAST_PORT);
        System.out.println("send");
        UDPMulticastServer.sendUDPMessage("OK", "230.0.0.0", MULTICAST_PORT);
        System.out.println("send");

        Thread.sleep(4000);

    }

    @Test
    public void testJavaMulticast2() throws InterruptedException, IOException {
//        System.setProperty("java.net.preferIPv4Stack", "true");

        final ReceiverThread receiverThread = new ReceiverThread(MULTICAST_INTERFACE, MULTICAST_IP, MULTICAST_PORT);
        final SenderThread senderThread = new SenderThread(MULTICAST_IP,MULTICAST_PORT);

        receiverThread.start();
        Thread.sleep(2000);
        senderThread.start();
        UDPMulticastServer.sendUDPMessage("UDPMulticastServer: This is the third multicast message", MULTICAST_IP, MULTICAST_PORT);

        DatagramChannelMulticastPublisher mp = new DatagramChannelMulticastPublisher();
        try {
            mp.sendMessage(MULTICAST_IP, MULTICAST_PORT, MULTICAST_INTERFACE, "DatagramChannelMulticastPublisher: Hi there!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                System.out.println("Stoppping...");
                senderThread.interrupt();
                receiverThread.interrupt();
            }
        });

        Thread.sleep(8000);
    }

    @Test
    public void testZeroMQMulticastDiscovery() throws IOException, InterruptedException {
        final int frontend = Utils.findOpenPort();
        final int backend = Utils.findOpenPort();

        try (
                final ZContext ctx = new ZContext()) {
            ZActor publisher =  new ZActor(ctx, new TestDiscovery.Publisher(frontend), "motdelafin");
            ZActor subscriber = new ZActor(ctx, new TestDiscovery.Subscriber(backend), "motdelafin");
            ZActor listener =   new ZActor(ctx, new TestDiscovery.Listener(), "motdelafin");

            ZProxy proxy = ZProxy.newZProxy(ctx, "Proxy", new TestDiscovery.Proxy(frontend, backend), "motdelafin");
            String status = proxy.start(true);
            assertThat(status, is(ZProxy.STARTED));

//            ZMQ.sleep(10);
            Thread.sleep(100);

            boolean rc = publisher.send("anything-sent-will-end-the-actor");
            assertThat(rc, is(true));
            // subscriber is already stopped after 5 receptions
            rc = listener.send("Did I really say ANYTHING?");
            assertThat(rc, is(true));

            status = proxy.exit();
            assertThat(status, is(ZProxy.EXITED));

            publisher.exit().awaitSilent();
            subscriber.exit().awaitSilent();
            listener.exit().awaitSilent();
            System.out.println("Espresso Finished");
        }
    }


    //  .split main thread
    //  The main task starts the subscriber and publisher, and then sets
    //  itself up as a listening proxy. The listener runs as a child thread:
    @Test
    public  void testZeroMQMulticastDiscovery2() throws InterruptedException {
        try (ZContext ctx = new ZContext()) {
            //  Start child threads
            ZThread.fork(ctx, new TestDiscovery.Publisher2());
            ZThread.fork(ctx, new TestDiscovery.Subscriber2());

            ZMQ.Socket subscriber = ctx.createSocket(SocketType.XSUB);
            subscriber.connect("tcp://localhost:6000");
            ZMQ.Socket publisher = ctx.createSocket(SocketType.XPUB);
            publisher.bind("tcp://*:6001");
            ZMQ.Socket listener = ZThread.fork(ctx, new TestDiscovery.Listener2());
            ZMQ.proxy(subscriber, publisher, listener);
            System.out.println(" interrupted");


            // NB: child threads exit here when the context is closed
        }
    }


    // --------------------------------------------------------------------------------

    //  The subscriber thread requests messages starting with
        //  A and B, then reads and counts incoming messages.
        private static class Subscriber2 implements ZThread.IAttachedRunnable
        {

            @Override
            public void run(Object[] args, ZContext ctx, ZMQ.Socket pipe)
            {
                //  Subscribe to "A" and "B"
                ZMQ.Socket subscriber = ctx.createSocket(SocketType.SUB);
                subscriber.connect("tcp://localhost:6001");
                subscriber.subscribe("A".getBytes(ZMQ.CHARSET));
                subscriber.subscribe("B".getBytes(ZMQ.CHARSET));

                int count = 0;
                while (count < 5) {
                    System.out.println("Subscriber2 wait");
                    String string = subscriber.recvStr();
                    System.out.println("Subscriber2 reseive " + string);
                    if (string == null)
                        break; //  Interrupted
                    count++;
                }
                ctx.destroySocket(subscriber);
            }
        }

        //  .split publisher thread
        //  The publisher sends random messages starting with A-J:
        private static class Publisher2 implements ZThread.IAttachedRunnable
        {
            @Override
            public void run(Object[] args, ZContext ctx, ZMQ.Socket pipe)
            {
                ZMQ.Socket publisher = ctx.createSocket(SocketType.PUB);
                publisher.bind("tcp://*:6000");
                Random rand = new Random(System.currentTimeMillis());

                while (!Thread.currentThread().isInterrupted()) {
                    String string = String.format("%c-%05d", 'A' + rand.nextInt(10), rand.nextInt(100000));
                    System.out.println("Publisher2 send " + string);
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

        //  .split listener thread
        //  The listener receives all messages flowing through the proxy, on its
        //  pipe. In CZMQ, the pipe is a pair of ZMQ_PAIR sockets that connect
        //  attached child threads. In other languages your mileage may vary:
        private static class Listener2 implements ZThread.IAttachedRunnable
        {
            @Override
            public void run(Object[] args, ZContext ctx, ZMQ.Socket pipe)
            {
                //  Print everything that arrives on pipe
                while (true) {
                    System.out.println("Listener2 wait ");
                    ZFrame frame = ZFrame.recvFrame(pipe);
                    System.out.println("Listener2 pipe " + frame);
                    if (frame == null)
                        break; //  Interrupted
                    frame.print(null);
                    frame.destroy();
                }
            }
        }




    // --------------------------------------------------------------------------------

    //  The subscriber thread requests messages starting with
    //  A and B, then reads and counts incoming messages.
    private static class Subscriber extends ZActor.SimpleActor
    {
        private final int port;
        private int       count;

        public Subscriber(int port)
        {
            this.port = port;
        }

        @Override
        public List<ZMQ.Socket> createSockets(ZContext ctx, Object... args)
        {
            ZMQ.Socket sub = ctx.createSocket(SocketType.SUB);
            assertThat(sub, notNullValue());
            return Collections.singletonList(sub);
        }

        @Override
        public void start(ZMQ.Socket pipe, List<ZMQ.Socket> sockets, ZPoller poller)
        {
            ZMQ.Socket subscriber = sockets.get(0);
            boolean rc = subscriber.connect("tcp://localhost:" + port);
            assertThat(rc, is(true));
            rc = subscriber.subscribe("A");
            assertThat(rc, is(true));
            rc = subscriber.subscribe("B".getBytes(ZMQ.CHARSET));
            assertThat(rc, is(true));
            rc = poller.register(subscriber, ZPoller.IN);
            assertThat(rc, is(true));
        }

        @Override
        public boolean stage(ZMQ.Socket socket, ZMQ.Socket pipe, ZPoller poller, int events)
        {
            String string = socket.recvStr();
            return string == null || count++ < 5;
        }
    }

    //  .split publisher thread
    //  The publisher sends random messages starting with A-J:
    private static class Publisher extends ZActor.SimpleActor
    {
        private final Random rand = new Random(System.currentTimeMillis());
        private final int    port;
        private int          count;

        public Publisher(int port)
        {
            this.port = port;
        }

        @Override
        public List<ZMQ.Socket> createSockets(ZContext ctx, Object... args)
        {
            ZMQ.Socket pub = ctx.createSocket(SocketType.PUB);
            assertThat(pub, notNullValue());
            return Collections.singletonList(pub);
        }

        @Override
        public void start(ZMQ.Socket pipe, List<ZMQ.Socket> sockets, ZPoller poller)
        {
            ZMQ.Socket publisher = sockets.get(0);
            boolean rc = publisher.bind("tcp://*:" + port);
            assertThat(rc, is(true));
            rc = poller.register(publisher, ZPoller.OUT);
            assertThat(rc, is(true));
        }

        @Override
        public boolean stage(ZMQ.Socket socket, ZMQ.Socket pipe, ZPoller poller, int events)
        {
            ZMQ.msleep(100);
            String string = String.format("%c-%05d", 'A' + rand.nextInt(10), ++count);
            System.out.println("Publisher sends " + string);
            return socket.send(string);
        }


    }

    //  .split listener thread
    //  The listener receives all messages flowing through the proxy, on its
    //  pipe. In CZMQ, the pipe is a pair of ZMQ_PAIR sockets that connect
    //  attached child threads. In other languages your mileage may vary:
    private static class Listener extends ZActor.SimpleActor
    {
        @Override
        public List<ZMQ.Socket> createSockets(ZContext ctx, Object... args)
        {
            ZMQ.Socket pull = ctx.createSocket(SocketType.PULL);
            assertThat(pull, notNullValue());
            System.out.println("Listener createSockets " + pull.getSocketType());
            return Collections.singletonList(pull);
        }

        @Override
        public void start(ZMQ.Socket pipe, List<ZMQ.Socket> sockets, ZPoller poller)
        {
            ZMQ.Socket subscriber = sockets.get(0);
            boolean rc = subscriber.connect("inproc://captured");
            System.out.println("Listener start 1 ");
            assertThat(rc, is(true));
            rc = poller.register(subscriber, ZPoller.IN);
            System.out.println("Listener start 2");
            assertThat(rc, is(true));
        }

        @Override
        public boolean stage(ZMQ.Socket socket, ZMQ.Socket pipe, ZPoller poller, int events)
        {
            ZFrame frame = ZFrame.recvFrame(socket);
            System.out.println("Listener stage " + frame.toString());
            assertThat(frame, notNullValue());
            frame.print(null);
            frame.destroy();
            return true;
        }
    }

    private static class Proxy extends ZProxy.Proxy.SimpleProxy
    {
        private final int frontend;
        private final int backend;

        public Proxy(int frontend, int backend)
        {
            this.frontend = frontend;
            this.backend = backend;
        }

        @Override
        public ZMQ.Socket create(ZContext ctx, ZProxy.Plug place, Object... args)
        {
            System.out.println("Proxy create " +place);
            switch (place) {
                case FRONT:
                    return ctx.createSocket(SocketType.XSUB);
                case BACK:
                    return ctx.createSocket(SocketType.XPUB);
                case CAPTURE:
                    return ctx.createSocket(SocketType.PUSH);
                default:
                    return null;
            }
        }

        @Override
        public boolean configure(ZMQ.Socket socket, ZProxy.Plug place, Object... args) throws IOException
        {
            System.out.println("Proxy configure " +place);
            switch (place) {
                case FRONT:
                    return socket.connect("tcp://localhost:" + frontend);
                case BACK:
                    return socket.bind("tcp://*:" + backend);
                case CAPTURE:
                    return socket.bind("inproc://captured");
                default:
                    return true;
            }
        }
    }

}