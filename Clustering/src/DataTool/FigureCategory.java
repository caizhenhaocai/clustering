/*
 * Created by benoit.audigier on 7/24/2017 12:00 PM.
 */
package DataTool;

import java.io.Serializable;

class FigureCategory implements Serializable {
    // Used only to store values about figure categories.

    private String name;
    private double max;
    private double min;

    FigureCategory(String name) {
        this.name = name;
        min = 0;
        max = 0;
    }

    FigureCategory(String name, double max, double min) {
        this.name = name;
        this.max = max;
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }
}
