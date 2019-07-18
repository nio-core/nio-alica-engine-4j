package de.uniks.vs.jalica.engine;

/**
 * Created by alex on 13.07.17.
 * Updated 21.6.19
 */
public class AlicaTime  {

    private static final long ns  = 1000000000l;
    private static final long mys = 1000000l;
    private static final long ms  = 1000l;

    public long time;

    public AlicaTime(long time) {
        this.time = time;
    }

    public static long microseconds(long microSeconds) {
        return (long) (microSeconds);
    }
    public static long milliseconds(long milliSeconds) {
        return (long) (milliSeconds * mys);
    }
    public static long seconds(long seconds) {
        return (long) (seconds * ns);
    }

    public AlicaTime() {
        this.time = 0;
    }

    public static AlicaTime zero() {
        return new AlicaTime();
    }

    //TODO: make static "AlicaTime time = AlicaTime.inSeconds(...)"
    public AlicaTime inSeconds(double seconds) {
        this.time = (long) (seconds * ns);
        return this;
    }

    public AlicaTime inMilliseconds(double milliSeconds) {
        this.time = (long) (milliSeconds * mys);
        return this;
    }

    public AlicaTime inMicroseconds(double microSeconds) {
        this.time = (long) (microSeconds * ms);
        return this;
    }

    public AlicaTime inNanoseconds(long nanoSeconds) {
        this.time = nanoSeconds;
        return this;
    }

    public long inNanoseconds() {
        return time;
    }
    public long inMicroseconds() {
        return time / ms;
    }
    public long inMilliseconds() {
        return time / mys;
    }
    public long inSeconds() {
        return time / ns;
    }
    public long inMinutes() {
        return time / (1000000000l * 60l);
    }
    public long inHours() {
        return time / (1000000000l * 60l * 60l);
    }


    // Operators
    public AlicaTime subtract(AlicaTime time) {
        return new AlicaTime().inNanoseconds(this.time - time.time);
    }

    public AlicaTime add(AlicaTime time) {
        return new AlicaTime().inNanoseconds(this.time + time.time);
    }

    @Override
    public String toString() {
        return String.valueOf(this.inNanoseconds());
    }
}
