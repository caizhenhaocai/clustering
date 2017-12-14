/*
 * Created by benoit.audigier on 6/20/2017 5:30 PM.
 */
package NeurTools;

import java.util.Arrays;

class Art1Node extends Node {
    private double[] topDown;
    private double[] bottomUp;


    Art1Node(int id, int dataSize, double vigilanceRate, boolean frozen) { // For initialization
        super(id, vigilanceRate, frozen);
        this.topDown = new double[dataSize];
        Arrays.fill(topDown, 1.0);
        this.bottomUp = new double[dataSize];
        Arrays.fill(bottomUp, 1.0 / (dataSize + 1));
    }

    Art1Node(int id, Input input, double vigilanceRate, boolean frozen) { // When adding a node
        super(id, vigilanceRate, frozen);
        this.size++;
        this.topDown = input.getValues();
        this.bottomUp = mult(1.0 / (0.5 + norm(topDown)), topDown);
        elements.add(input);
    }

    Art1Node(int id){
        super(id);
    }


    public double[][] getWeights() {
        return new double[][]{topDown, bottomUp};
    }

    protected double calcAffinity(double[] x) {
        return norm(AND(x, bottomUp));
    }

    protected double calcQuality(Input t) throws NullPointerException{
        return (norm(AND(t.getValues(), topDown)) / norm(t.getValues()));
    }

    protected double[] AND(double[] a, double[] b) {
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = a[i] * b[i];
        }
        return res;
    }

    protected void updateWeights(double[] x) {
        bottomUp = mult(1.0 / (0.5 + norm(AND(topDown, x))), AND(topDown, x));
        topDown = AND(topDown, x);
    }

    void setWeights(double[] w){
        topDown = w;
    }

	@Override
	protected double calcQualitynode(Node t) throws NullPointerException {
		// TODO Auto-generated method stub
		return 0;
	}


}
