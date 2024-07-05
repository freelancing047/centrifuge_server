package csi.server.business.service.matrix.storage.postgres;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.TreeMultimap;
import com.mongodb.DBObject;

import csi.server.business.service.matrix.MatrixData;
import csi.shared.core.visualization.matrix.AxisLabel;
import csi.shared.core.visualization.matrix.Cell;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixMetrics;

/**
 * Created by Ivan on 11/21/2017.
 */
public class BSONtoMatrixData implements Function<DBObject, MatrixData> {
    @Override
    public MatrixData apply(DBObject data) {
        MatrixData fullMatrix = new MatrixData();
        List<Cell> m = (List<Cell>) data.get(MatrixCacheKeys.CELLS);
        fullMatrix.addCells(m);
        fullMatrix.setMaxX((int) data.get(MatrixCacheKeys.MAX_X));
        fullMatrix.setMinX((int) data.get(MatrixCacheKeys.MIN_X));
        fullMatrix.setMaxY((int) data.get(MatrixCacheKeys.MAX_Y));
        fullMatrix.setMinY((int) data.get(MatrixCacheKeys.MIN_Y));

        fullMatrix.setLimitExceeded((boolean) data.get(MatrixCacheKeys.LIMIT));
        fullMatrix.setMatrixMetrics((MatrixMetrics) data.get(MatrixCacheKeys.METRICS));
        fullMatrix.setxCategories((List<AxisLabel>) data.get(MatrixCacheKeys.CATEGORIES_X));
        fullMatrix.setyCategories((List<AxisLabel>) data.get(MatrixCacheKeys.CATEGORIES_Y));
        fullMatrix.setCellIdMap((TreeMultimap<Integer, Integer>) data.get(MatrixCacheKeys.CELL_ID_MAP));
        return fullMatrix;
    }

    public MatrixMetrics getMatrixMetrics(DBObject data) {
        return (MatrixMetrics) data.get(MatrixCacheKeys.METRICS);
    }


    /**
     * TODO: limit the iteration to the request view port
     *
     * @param data
     * @param req
     * @return
     */
    public MatrixData applySection(DBObject data, MatrixDataRequest req) {
        MatrixData fullMatrix = new MatrixData();

        List<Cell> m = (List<Cell>) data.get(MatrixCacheKeys.CELLS);

        for (Cell c : m) {
            if (((req.getStartX() <= c.getX()) && (req.getEndX() >= c.getX())) &&
                    ((req.getStartY() <= c.getY()) && (req.getEndY() >= c.getY()))) {
                fullMatrix.addCell(c);
            }

        }
        {
            List<AxisLabel> axisLabels = (List<AxisLabel>) data.get(MatrixCacheKeys.CATEGORIES_X);
            int fromIndex = Math.max(0, req.getStartX());
            int toIndex = Math.min(axisLabels.size(), req.getEndX());
            List<AxisLabel> xCat;
            if (fromIndex > toIndex) {
                xCat = new ArrayList<AxisLabel>();
            } else {
                if ((toIndex + 1) < axisLabels.size()) {
                    xCat = new ArrayList<AxisLabel>(axisLabels.subList(fromIndex, toIndex + 1));
                } else {
                    // edge case for end
                    xCat = new ArrayList<AxisLabel>(axisLabels.subList(fromIndex, toIndex));
                }
            }
            fullMatrix.setxCategories(xCat);
        }
        {
            List<AxisLabel> axisLabels = (List<AxisLabel>) data.get(MatrixCacheKeys.CATEGORIES_Y);
            int toIndex = Math.min(axisLabels.size(), req.getEndY());
            int fromIndex = Math.max(0, req.getStartY());
            List<AxisLabel> yCat;
            if (fromIndex > toIndex) {
                yCat = new ArrayList<AxisLabel>();
            } else {
                if ((toIndex + 1) < axisLabels.size()) {
                    yCat = new ArrayList<AxisLabel>(axisLabels.subList(fromIndex, toIndex));
                } else {
                    // edge case for end
                    yCat = new ArrayList<AxisLabel>(axisLabels.subList(fromIndex, toIndex));
                }
            }
            fullMatrix.setyCategories(yCat);
        }

        fullMatrix.setMaxX((int) data.get(MatrixCacheKeys.MAX_X));
        fullMatrix.setMinX((int) data.get(MatrixCacheKeys.MIN_X));
        fullMatrix.setMaxY((int) data.get(MatrixCacheKeys.MAX_Y));
        fullMatrix.setMinY((int) data.get(MatrixCacheKeys.MIN_Y));
        fullMatrix.setMatrixMetrics((MatrixMetrics) data.get(MatrixCacheKeys.METRICS));

        return fullMatrix;

    }


    public MatrixData getSelectionRange(DBObject data, MatrixDataRequest req) {
        MatrixData fullMatrix = new MatrixData();

        List<Cell> m = (List<Cell>) data.get(MatrixCacheKeys.CELLS);

        for (Cell c : m) {
            if (((req.getStartX() <= c.getX()) && (req.getEndX() >= c.getX())) &&
                    ((req.getStartY() <= c.getY()) && (req.getEndY() >= c.getY()))) {
                fullMatrix.addCell(c);
            }

        }
        return fullMatrix;
    }

    public static int[] merge(int[] ids, int[] ids2, int position) {
        System.arraycopy(ids2, 0, ids, position, ids2.length);
        return ids;
    }
}
