package csi.shared.core.visualization.matrix;

import java.io.Serializable;

/**
 * I guess i need to pass in full extents to the client, which will map them somehow over what we have, and then we will pan or what not... ?
 *
 *
 *
 *
 */
public class MatrixWrapper implements Serializable{
    private MatrixMetrics metrics;
    private MatrixDataResponse data;
    private MatrixCategoryResponse categories;
    private boolean limitExceeded;
    private boolean empty = true;

    public MatrixWrapper() {}
    public MatrixWrapper(MatrixMetrics m, MatrixDataResponse d, MatrixCategoryResponse c) {
        this.metrics = m;
        this.data = d;
        this.categories = c;
        this.empty =false;
    }

    public boolean isLimitExceeded() {
        return limitExceeded;
    }

    public void setLimitExceeded(boolean limitExceeded) {
        this.limitExceeded = limitExceeded;
    }

    public MatrixMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(MatrixMetrics metrics) {
        this.metrics = metrics;
    }

    public MatrixDataResponse getData() {
        return data;
    }

    public void setData(MatrixDataResponse data) {
        this.data = data;
    }

    public MatrixCategoryResponse getCategories() {
        return categories;
    }

    public void setCategories(MatrixCategoryResponse categories) {
        this.categories = categories;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
