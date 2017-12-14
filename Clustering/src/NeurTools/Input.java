/*
 * Created by benoit.audigier on 6/20/2017 4:58 PM.
 */
package NeurTools;

import java.io.Serializable;
import java.util.function.DoublePredicate;

import sun.security.util.Length;

public class Input implements Serializable {
    private int id;
    private double[] values;
    private Node cluster;
    private boolean justChanged;
    private int tradeNumber;



    Input(int id, int tradeNumber, double[] values) {
        this.id = id;
        //double[] v=m(1, values);
        //this.values = merge(values, v);
        this.values=values;
        this.justChanged = true;
        this.tradeNumber = tradeNumber;
    }

    public Input(int id){ // to compare algorithms with diagnosed data
        this.id = id;
    }



    private static double[] m(int a, double[] val){
    	double[] v= new double[val.length];
    	for(int i=0;i<val.length;i++) {
    		v[i]=a-val[i];
    		
    	}
    	return v;
    }
    
    private static double[] merge(double[] a, double[]b) {
    	double[] mer=new double[a.length+b.length];
    	for(int i=0;i<a.length;i++) {
    		mer[i]=a[i];
    	}
    	for(int i=0;i<b.length;i++) {
    		mer[i+a.length]=b[i];
    		
    	}
    	return mer;
    }

    public int getId() {
        return id;
    }

    public double[] getValues() {
        return values;
    }

    Node getCluster() {
        return cluster;
    }

    public int getTradeNumber() {
        return tradeNumber;
    }


    boolean isJustChanged() {
        return justChanged;
    }

    void setJustChanged(boolean justChanged) {
        this.justChanged = justChanged;
    }

    void setCluster(Node cluster) {
        this.cluster = cluster;
    }

    void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id+"";
    }
}
