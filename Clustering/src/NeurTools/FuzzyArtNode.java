/*
 * Created by benoit.audigier on 6/20/2017 5:30 PM.
 */
package NeurTools;


import java.util.Arrays;

import com.sun.glass.ui.TouchInputSupport;

import DataTool.ClusLogger;

class FuzzyArtNode extends Node {
    private final double tieBreaker = 0;
    private static double learningRate = 0.3;
    private double[] weights;



 
    FuzzyArtNode(int id, int dataSize, double vigilanceRate, boolean frozen){ // For initialization
        super(id, vigilanceRate, frozen);
        this.weights = new double[dataSize];
        Arrays.fill(weights, 1.0);
    }

    FuzzyArtNode(int id, Input input, double vigilanceRate, boolean frozen){ // When adding a node
        super(id, vigilanceRate, frozen);
        this.size++;
        this.weights = input.getValues();
        //double[] v = input.getValues();
        //int n = v.length;
        //this.weights=new double[n];
        //Arrays.fill(weights, 1.0);
        //this.weights= sum(mult(learningRate, AND(weights, v)), mult(1.0 - learningRate, weights));
        elements.add(input);
    }
    FuzzyArtNode(int id, Node node, double vigilanceRate, boolean frozen){ // When adding a node
        super(id, vigilanceRate, frozen);

        this.weights = node.getWeights()[0];
        //double[] v = node.getWeights()[0];
        //int n = v.length;
        //this.weights=new double[n];
        //Arrays.fill(weights, 1.0);
        //this.weights= sum(mult(learningRate, AND(weights, v)), mult(1.0 - learningRate, weights));
        for (Input input: node.getElements()) {
        	elements.add(input);
        	input.setCluster(this);
        }
        this.size=this.size+node.getElements().size();
    }
    

    FuzzyArtNode(int id){
        super(id);
    }




    public double[][] getWeights(){
        return new double[][] {weights};
    }

    protected double calcQuality(Input t) throws NullPointerException{
        return (norm(AND(t.getValues(), weights)) / norm(t.getValues()));
    }
    
    protected double calcQualitynode(Node t) throws NullPointerException{
        return (norm(AND(t.getWeights()[0], weights)) / norm(t.getWeights()[0]));
    }

    protected double calcAffinity(double[] x){
        return norm(AND(x, weights)) / (tieBreaker + norm(weights));
    }

    protected double[] AND(double[] a, double[] b){
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            if(a[i]<b[i]) res[i] = a[i];
            else res[i] = b[i];
        }
        return res;
    }

    protected void updateWeights(double[] x) {
        weights = sum(mult(learningRate, AND(weights, x)), mult(1.0 - learningRate, weights));
        if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Updating weights : "+Arrays.toString(weights));
    }

    void setWeights(double[] w){
        weights = w;
    }
    
    protected static void setlearningrate(double learningrate) {
    	learningRate=0.8;
    }

}
