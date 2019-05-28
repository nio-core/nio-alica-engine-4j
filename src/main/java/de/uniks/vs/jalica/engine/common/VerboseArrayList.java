package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;

public class VerboseArrayList<T> extends ArrayList<T> {

    @Override
    public boolean add(T o) {
        if (CommonUtils.VERBOSE_ARRAY_LIST_DEBUG) CommonUtils.aboutCallNotification();
        return super.add(o);
    }

    @Override
    public void add(int index, T element) {
        if (CommonUtils.VERBOSE_ARRAY_LIST_DEBUG) CommonUtils.aboutCallNotification();
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection c) {
        if (CommonUtils.VERBOSE_ARRAY_LIST_DEBUG) CommonUtils.aboutCallNotification();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        if (CommonUtils.VERBOSE_ARRAY_LIST_DEBUG) CommonUtils.aboutCallNotification();
        return super.addAll(index, c);
    }
}
