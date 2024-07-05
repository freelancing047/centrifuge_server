package csi.config;

/**
 * Configuration file for Matrix Viz.
 *
 * Properties are set in the application-config.xml
 */
public class MatrixConfig extends AbstractConfigurationSettings {

    /**
     * Max number of cells that matrix will render before displaying limit exceeded dialog.
     */
    private int maxCellCount;

    private int minMatrixSelectionRadius;

    public int getMinMatrixSelectionRadius() {
        return minMatrixSelectionRadius;
    }

    public void setMinMatrixSelectionRadius(int minMatrixSelectionRadius) {
        this.minMatrixSelectionRadius = minMatrixSelectionRadius;
    }

    public int getMaxCellCount() {
        return maxCellCount;
    }

    public void setMaxCellCount(int maxCellCount) {
        this.maxCellCount = maxCellCount;
    }
}
