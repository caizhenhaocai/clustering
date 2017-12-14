/*
 * Created by benoit.audigier on 6/29/2017 11:48 AM.
 */

package NeurTools;

public class KMeansNode extends Node{ // The K-Means algorithm was not efficient, thus is not used. It is still operational though.

    KMeansNode(int id, Input input, boolean frozen){
        super(id, 0, frozen);
        this.centroid = input.getValues();
    }

    KMeansNode(int id, boolean frozen){
        super(id, 0, frozen);
    }


    public double[][] getWeights() {
        return new double[][]{centroid};
    }

    protected double calcAffinity(double[] x) {
        return 1-distance(x, centroid);
    }

    protected double calcQuality(Input t) {
        return 1;
    }

    protected double[] AND(double[] a, double[] b) {
        return null;
    }

    protected void updateWeights(double[] x) {
    }

    void setWeights(double[] w){
    }

	@Override
	protected double calcQualitynode(Node t) throws NullPointerException {
		// TODO Auto-generated method stub
		return 0;
	}

}
