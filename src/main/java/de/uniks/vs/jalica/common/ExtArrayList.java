package de.uniks.vs.jalica.common;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ExtArrayList<T> extends ArrayList<T> {

    private Supplier<T> supplier;
    Class<T> clazz;

    public ExtArrayList() {super();}

    public ExtArrayList(int size) {
        super(size);
    }

    public ExtArrayList(Supplier<T> supplier) {
        super();
        this.supplier = supplier;
    }

    public ExtArrayList(ExtArrayList<T> list, Supplier<T> supplier) {
        super(list);
        this.supplier = supplier;
    }

    T createContents() {
        return supplier.get();
    }

    public ExtArrayList(Supplier<T> supplier, int size) {
        this.supplier = supplier;

        for (int i = 0; i < size; i++) {
            this.add(createContents());
        }
    }

    private T createContents(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void resize(int size) {

        for (int i = this.size(); i < size; i++) {
            this.add(createContents());
        }
    }
}


