package de.uniks.vs.jalica.engine.common;

import java.util.Objects;

public class Pair<A, B> {

    public final A fst;
    public B snd;

    public Pair(A value1, B value2) {
        this.fst = value1;
        this.snd = value2;
    }

    public String toString() {
        return "Pair[" + this.fst + "," + this.snd + "]";
    }

    public boolean equals(Object obj) {
        return obj instanceof Pair && Objects.equals(this.fst, ((Pair)obj).fst) && Objects.equals(this.snd, ((Pair)obj).snd);
    }

    public int hashCode() {
        if (this.fst == null) {
            return this.snd == null ? 0 : this.snd.hashCode() + 1;
        } else {
            return this.snd == null ? this.fst.hashCode() + 2 : this.fst.hashCode() * 17 + this.snd.hashCode();
        }
    }

    public static <A, B> Pair<A, B> of(A var0, B var1) {
        return new Pair(var0, var1);
    }
}
