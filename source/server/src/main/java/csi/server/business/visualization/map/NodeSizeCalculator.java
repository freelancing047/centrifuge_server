package csi.server.business.visualization.map;

import java.io.Serializable;

public class NodeSizeCalculator implements Serializable {
    private double origin;
    private double divisor;

    public NodeSizeCalculator(double min, double max) {
        origin = min;
        divisor = (max - min) / 5.0;
    }

    public int calculate(Double value) {
        if (value == null) return 1;
        double dividend = value - origin;
        double quotient = dividend / divisor;
        double ceil = Math.ceil(quotient);
        int intValue = (int) ceil;
        if (intValue < 1) return 1;
        else if (intValue > 5) return 5;
        else return intValue;
    }
}
