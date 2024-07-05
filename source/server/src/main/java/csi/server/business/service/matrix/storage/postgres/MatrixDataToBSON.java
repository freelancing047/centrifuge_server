package csi.server.business.service.matrix.storage.postgres;

import java.util.function.Function;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import csi.server.business.service.matrix.MatrixData;

/**
 * Created by Ivan on 11/21/2017.
 */
public class MatrixDataToBSON implements Function<MatrixData,DBObject> {
   @Override
   public DBObject apply(MatrixData matrixData) {
      BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

      builder.append(MatrixCacheKeys.CELLS, matrixData.getAllCells());
      builder.append(MatrixCacheKeys.CATEGORIES_X, matrixData.getxCategories());
      builder.append(MatrixCacheKeys.CATEGORIES_Y, matrixData.getyCategories());
      builder.append(MatrixCacheKeys.CELL_ID_MAP, matrixData.getCellIdMap());
      builder.append(MatrixCacheKeys.MIN_X, matrixData.getMinX());
      builder.append(MatrixCacheKeys.MIN_Y, matrixData.getMinY());
      builder.append(MatrixCacheKeys.MAX_X, matrixData.getMaxX());
      builder.append(MatrixCacheKeys.MAX_Y, matrixData.getMaxY());
      builder.append(MatrixCacheKeys.LIMIT, matrixData.isLimitExceeded());
      builder.append(MatrixCacheKeys.METRICS, matrixData.getMatrixMetrics());
      return builder.get();
   }
}
