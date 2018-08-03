package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 10.11.17.
 */
public class UniqueVarStore {

    Vector<Vector<Variable>> store;


    void clear() { CommonUtils.aboutNoImpl();}

    void add(Variable v) {CommonUtils.aboutNoImpl();}

    Variable getRep(Variable v) {CommonUtils.aboutNoImpl(); return null;}

    void addVarTo(Variable representing, Variable toAdd) {CommonUtils.aboutNoImpl();}

    Vector<Variable> getAllRep() {CommonUtils.aboutNoImpl(); return null;}

    int getIndexOf(Variable v){CommonUtils.aboutNoImpl(); return -1;}
}
