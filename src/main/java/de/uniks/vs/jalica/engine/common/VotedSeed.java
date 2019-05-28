package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.util.Vector;

public class VotedSeed {

    private Vector<Vector<Integer>> values;
    private Vector<Integer> supporterCount;
    private double hash;
    private int totalSupCount;
    private int dim;

    public VotedSeed(int dim, Vector<Vector<Integer>> v) {
        this.values = v;
        this.supporterCount = new Vector<>(dim);
        this.dim = dim;
        hash = 0;

        if(v!=null) {

            for (int i = 0; i < dim; ++i) {

                if(v.get(i) == null) {
                    continue;
                }

                if (v.get(i).size() > 0) {
                    this.totalSupCount++;
                }
            }
        }
    }

    public boolean takeVector(Vector<Vector<Integer>> v, Vector<Double> scaling, double distThreshold, boolean isDouble) {
        Vector<Double> v1 = deserializeToDoubleVec(v);
        Vector<Double> values1 = deserializeToDoubleVec(values);
        int nans = 0;
        double distSqr = 0;

        for (int i = 0; i < dim; ++i) {

            if (!Double.isNaN(v1.get(i)) && v1.get(i) != Double.MAX_VALUE){

                if (!Double.isNaN(values1.get(i))){

                    if (scaling.get(i) != 0) {
                        distSqr += ((values1.get(i) - v1.get(i)) * (values1.get(i) - v1.get(i))) / scaling.get(i);
                    }
                    else {
                        distSqr += (values1.get(i) - v1.get(i)) * (values1.get(i) - v1.get(i));
                    }
                }
            }
            else {
                nans++;
            }
        }

        if (dim == nans) {
            hash = -1;
            return true; //silently absorb a complete NaN vector
        }

        if (distSqr / (dim - nans) < distThreshold) {

            for (int i = 0; i < dim; ++i) {

                if (!Double.isNaN(v1.get(i))) {

                    if (Double.isNaN(values1.get(i))) {
                        this.supporterCount.set(i, 1);
                        this.totalSupCount++;

                        values1.set(i, v1.get(i));
                    }
						else
                    {
                        values1.set(i,values1.get(i) * this.supporterCount.get(i) + v1.get(i));
                        this.supporterCount.set(i,this.supporterCount.get(i)+1);
                        this.totalSupCount++;
                        values1.set(i, values1.get(i)/this.supporterCount.get(i));
                    }
                }
            }


            this.values = serializeFromDoubleVec(values1);

            hash = 0;
            for(double d : values1) {

                if(!Double.isNaN(d) && d != Double.MAX_VALUE)
                hash +=d;
            }
            return true;
        }
        hash = 0;

        for(double d : values1) {
            if(!Double.isNaN(d) && d != Double.MAX_VALUE)
            hash +=d;
        }
        return false;
    }

    Vector<Double> deserializeToDoubleVec(Vector<Vector<Integer>> v) {
        Vector<Double> singleseed = new Vector<Double>();
        CommonUtils.aboutNoImpl();
//        singleseed.reserve(v.size());
//
//        for ( serialvalue : v){
//
//            if (serialvalue != null) {
//                double v;
//                Integer pointer = (uint8_t*)&v;
//
//                if (serialvalue.size() == Double.SIZE){
//
//                    for (int k = 0; k < Double.SIZE; k++) {
//						pointer = serialvalue.get(k);
//                        pointer++;
//                    }
//                    singleseed.add(v);
//                }
//				else {
//                    cerr << "VSM: Received Seed that is not size of double" << endl;
//                    cout << "VSM: Received Seed that is not size of double" << endl;
//                    break;
//                }
//            }
//            else {
//                singleseed.add(std::numeric_limits<double>::max());
//            }
//        }
        return singleseed;
    }

    Vector<Vector<Integer>> serializeFromDoubleVec(Vector<Double> d) {
        Vector<Vector<Integer>> res = new Vector<Vector<Integer>>();

        for (int i = 0; i < d.size(); i++) {
            Integer tmp = d.get(i).intValue();
            Vector<Integer> result = new Vector<Integer>(Double.SIZE);

            for(int s = 0; s<Double.SIZE; s++) {
                result.set(s,tmp);
                tmp++;
            }
            res.add(result);
        }

        return res;
    }

    public double getHash() {
        return hash;
    }

    public int getTotalSupCount() {
        return totalSupCount;
    }

    public Vector<Vector<Integer>> getValues() {
        return values;
    }
}
