package csi.server.business.service.matrix;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.TreeMultimap;
import com.mongodb.DBObject;

import csi.config.Configuration;
import csi.server.business.service.ColorActionsService;
import csi.server.business.service.FilterActionsService;
import csi.server.business.service.matrix.storage.AbstractMatrixCacheStorageService;
import csi.server.business.service.matrix.storage.MatrixCacheStorage;
import csi.server.business.service.matrix.storage.postgres.BSONtoMatrixData;
import csi.server.business.service.matrix.storage.postgres.MatrixDataToBSON;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.PositiveIntegerTypeCriterion;
import csi.server.common.model.visualization.chart.ZeroToOneTypeCriterion;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.sql.SQLFactory;
import csi.server.util.sql.SelectSQL;
import csi.shared.core.visualization.matrix.AxisLabel;
import csi.shared.core.visualization.matrix.Cell;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixMetrics;
import csi.shared.core.visualization.matrix.MatrixSelectionRequest;

public class MatrixDataService {
   private static final Logger LOG = LogManager.getLogger(MatrixDataService.class);

   public static final String NULL = "\uE000NULL";
    private static SQLFactory sqlFactory;

    public static SQLFactory getSqlFactory() {
        return sqlFactory;
    }

    public static FilterActionsService getFilterActionsService() {
        return filterActionsService;
    }

    static ColorActionsService getColorActionsService() {
        return colorActionsService;
    }

    private static FilterActionsService filterActionsService;
    private static ColorActionsService colorActionsService;
    private static Cache<String, MatrixData> cache;

    public static MatrixMetrics getMetricsForMatrix(String visUuid) {
        AbstractMatrixCacheStorageService storageService = AbstractMatrixCacheStorageService.instance();
        boolean isCache = storageService.hasVisualizationData(visUuid);
        if (isCache) {
            MatrixCacheStorage matrixStorage = storageService.getMatrixCacheStorage(visUuid);
            DBObject storedMatrix = (DBObject) matrixStorage.getResult();
            BSONtoMatrixData transformer = new BSONtoMatrixData();
            return transformer.getMatrixMetrics(storedMatrix);

        } else {
            return new MatrixMetrics();
        }
    }

    public static int[] getIdsForCategory(MatrixSelectionRequest req) {
        AbstractMatrixCacheStorageService storageService = AbstractMatrixCacheStorageService.instance();
//        logger.info("Vizuuid " + req.getVizUuid());
        boolean isCache = storageService.hasVisualizationData(req.getVizUuid());
        if (!isCache) {
            queryMatrix(CsiPersistenceManager.findObject(MatrixViewDef.class, req.getVizUuid()), req.getDvUuid());
        }
        return retrieveIdsFromCache(AbstractMatrixCacheStorageService.instance(), req);
    }

    /**
     * @param viewDef
     * @param dataViewUuid
     * @param req
     * @return
     */
    public static MatrixData getMatrixDataSection(final MatrixViewDef viewDef, final String dataViewUuid, MatrixDataRequest req) {
        AbstractMatrixCacheStorageService storageService = AbstractMatrixCacheStorageService.instance();
        boolean isCache = storageService.hasVisualizationData(viewDef.getUuid());
        if (isCache) {
            return retrieveSectionFromCache(storageService, viewDef.getUuid(), viewDef, req);
        } else {
            // TODO: remove
            // basically we never hit this,because we call a diff function that will cache this.
            MatrixData m = getMatrixData(viewDef, dataViewUuid, false);
            MatrixData section = new MatrixData();
            section.addCells(m.get(req.getStartX(), req.getStartY(), req.getEndX(), req.getEndY()));
            section.postProcess();
            return section;
        }
    }


    //TODO: remove this whole chain
    public static int[] retrieveIdsFromCache(AbstractMatrixCacheStorageService storageService, MatrixSelectionRequest req) {
//        MatrixCacheStorage matrixStorage = storageService.getMatrixCacheStorage(req.getVizUuid());
//        DBObject storedMatrix = (DBObject) matrixStorage.getResult();
//        BSONtoMatrixData transformer = new BSONtoMatrixData();
        return new int[0];
//        return transformer.getIdsForCategory(req, storedMatrix);

    }

    public static MatrixData getSectionForSelection(AbstractMatrixCacheStorageService storageService, String vizUuid, final MatrixViewDef viewDef, MatrixDataRequest req) {
        MatrixData mData;
        MatrixCacheStorage matrixStorage = storageService.getMatrixCacheStorage(vizUuid);
        DBObject storedMatrix = (DBObject) matrixStorage.getResult();
        BSONtoMatrixData transformer = new BSONtoMatrixData();
        mData = transformer.getSelectionRange(storedMatrix, req);

        LOG.trace("Cells for region: " + "start: (" + req.getStartX() + ", " + req.getStartY() + ") end: (" + req.getEndX() + " , " + req.getEndY() + ")");
        return mData;
    }

    public static MatrixData retrieveSectionFromCache(AbstractMatrixCacheStorageService storageService, String vizUuid, final MatrixViewDef viewDef, MatrixDataRequest req) {
        MatrixData mData;
        MatrixCacheStorage matrixStorage = storageService.getMatrixCacheStorage(vizUuid);
        DBObject storedMatrix = (DBObject) matrixStorage.getResult();
        BSONtoMatrixData transformer = new BSONtoMatrixData();
        mData = transformer.applySection(storedMatrix, req);
        MatrixMetrics m = mData.getMatrixMetrics();

        if (mData.getTotalCount() != 0) {
            mData.postProcess();
            mData.createScaleImage(viewDef.getMatrixSettings(), getColorActionsService());
        }

        mData.setMatrixMetrics(m);

        return mData;
    }

   public static MatrixData getMatrixData(final MatrixViewDef viewDef, final String dataViewUuid, boolean summarize) {
      MatrixData ret = null;
      AbstractMatrixCacheStorageService storageService = AbstractMatrixCacheStorageService.instance();
      boolean isCache = storageService.hasVisualizationData(viewDef.getUuid());

      if (isCache) {
         try {
            ret = retrieveFromCache(storageService, viewDef.getUuid(), viewDef);
         } catch (Exception e) {
            ret = queryMatrix(viewDef, dataViewUuid);
         }
      } else {
         ret = queryMatrix(viewDef, dataViewUuid);
      }
      if (summarize && ret.isLimitExceeded()) {
         ret = getSummaryMatrixData(ret, viewDef.getMatrixSettings());
      }
      return ret;
   }

    public static MatrixData retrieveFromCache(AbstractMatrixCacheStorageService storageService, String vizUuid, final MatrixViewDef viewDef) {
        MatrixData mData;

        MatrixCacheStorage matrixStorage = storageService.getMatrixCacheStorage(vizUuid);
        DBObject storedMatrix = (DBObject) matrixStorage.getResult();
        BSONtoMatrixData transformer = new BSONtoMatrixData();
        mData = transformer.apply(storedMatrix);

        mData.postProcess();
        //FIXME: did we need this
//        mData.createTable(viewDef.getMatrixSettings());
        mData.createScaleImage(viewDef.getMatrixSettings(), getColorActionsService());

        return mData;
    }

    /**
     * @param viewDef
     * @param dataViewUuid
     * @return
     */
    public static MatrixData queryMatrix(final MatrixViewDef viewDef, final String dataViewUuid) {
        final MatrixData matrixData = new MatrixData();
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
        final MatrixSettings settings = viewDef.getMatrixSettings();
        MatrixQueryBuilder qb = getQueryBuilder(viewDef, dataView);

        final int maxCellCount = Configuration.getInstance().getMatrixConfig().getMaxCellCount();
        final AtomicInteger cellID = new AtomicInteger(0);
        final Set<Integer> nullXCells = new HashSet<Integer>();
        final Set<Integer> nullYCells = new HashSet<Integer>();
        final TreeMultimap<Integer, Integer> cellIdMultimap = TreeMultimap.create();
        final TreeMultimap<String, Integer> xCategoryMultimap = TreeMultimap.create();
        final TreeMultimap<String, Integer> yCategoryMultimap = TreeMultimap.create();


        HashMap<ChartCriterion, Double> criteriaValue = new HashMap<ChartCriterion, Double>();
        HashMap<ChartCriterion, Integer> threshholdIndex = new HashMap<ChartCriterion, Integer>();
        ArrayList<Double> matrixValues = new ArrayList<>();

        if ((settings.getFilterCriteria() != null) && !settings.getFilterCriteria().isEmpty()) {
            SelectSQL preFilterQuery = qb.getPreFilterQuery();

            preFilterQuery.scroll(resultSet -> {
                while (resultSet.next()) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    double value = getValue(metaData.getColumnType(3), resultSet);
                    matrixValues.add(value);
                }
                return null;
            });

            int cellCountMax;

            if (!matrixValues.isEmpty()) {
                for (ChartCriterion filterCriterion : settings.getFilterCriteria()) {
                    String operator = filterCriterion.getOperatorString();

                    if (operator.equals("Top") || operator.equals("Bottom")) {
                        if (filterCriterion instanceof PositiveIntegerTypeCriterion) {
                            PositiveIntegerTypeCriterion crit = (PositiveIntegerTypeCriterion) filterCriterion;
                            switch (operator) {
                                case "Top":

                                    threshholdIndex.put(filterCriterion, Math.min(crit.getTestValue(), matrixValues.size()));
                                    criteriaValue.put(filterCriterion, matrixValues.get(Math.min(crit.getTestValue(), matrixValues.size() - 1)));
                                    break;
                                case "Bottom":

                                    threshholdIndex.put(filterCriterion, Math.max(matrixValues.size() - crit.getTestValue(), 0));
                                    criteriaValue.put(filterCriterion, matrixValues.get(Math.max(matrixValues.size() - crit.getTestValue(), 0)));
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else if (operator.equals("Top%") || operator.equals("Bottom%")) {
                        if (filterCriterion instanceof ZeroToOneTypeCriterion) {
                            ZeroToOneTypeCriterion crit = (ZeroToOneTypeCriterion) filterCriterion;
                            switch (operator) {
                                case "Top%":

                                    cellCountMax = (int) Math.floor(matrixValues.size() * (crit.getTestValue() / 100));
                                    threshholdIndex.put(filterCriterion, cellCountMax);
                                    criteriaValue.put(filterCriterion, matrixValues.get(cellCountMax));
                                    break;
                                case "Bottom%":

                                    cellCountMax = (int) Math.ceil(matrixValues.size() * (crit.getTestValue() / 100));
                                    threshholdIndex.put(filterCriterion, matrixValues.size() - cellCountMax);
                                    criteriaValue.put(filterCriterion, matrixValues.get(matrixValues.size() - cellCountMax));
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        }

            qb.getQuery(criteriaValue, threshholdIndex, matrixValues.size()).scroll(resultSet -> {
                ResultSetMetaData metaData = resultSet.getMetaData();

                while (resultSet.next()) {
                    {//Wrap value in a cell, add to matrix data
                        double value = getValue(metaData.getColumnType(3), resultSet);
                        Cell cell = new Cell(value);
                        matrixData.addCell(cell);
                    }
                    //FIXME: format the result to two digits of precision if abs is gt 1
                /*if (value - Math.floor(value) != 0) {
                    value = Math.round(value * 100.0) / 100.0;
                }*/
                    {//Track xCat with multimap
                        String xCategoryString = resultSet.getString(1);
                        if (xCategoryString == null) {
                            nullXCells.add(cellID.get());
                        } else {
                            xCategoryMultimap.put(xCategoryString, cellID.get());
                        }
                    }
                    {//Track yCat with multimap
                        String yCategoryString = resultSet.getString(2);
                        if (yCategoryString == null) {
                            nullYCells.add(cellID.get());
                        } else {
                            yCategoryMultimap.put(yCategoryString, cellID.get());
                        }
                    }
                    {//Track Ids for each cell with multimap
                        Integer[] ids = (Integer[]) resultSet.getArray(4).getArray();
                        cellIdMultimap.putAll(cellID.get(), Arrays.asList(ids));
                    }
                    cellID.incrementAndGet();
                }
                return null;
            });

        if (matrixData.getTotalCount() >= maxCellCount) {
            matrixData.setLimitExceeded(true);
        }

        if (matrixData.getTotalCount() == 0) {
            return matrixData;
        }

        {
            //set Ordinals of X Axis and positional X index on each cell
            NavigableSet<String> strings = xCategoryMultimap.keySet();
            List<AxisLabel> xAxisCategories = new ArrayList<AxisLabel>(strings.size() + 1);
            for (String s : strings) {
                xAxisCategories.add(new AxisLabel(s));
            }
            if (!nullXCells.isEmpty()) {
                AxisLabel nullXLabel = new AxisLabel(NULL);
                xAxisCategories.add(nullXLabel);
                xCategoryMultimap.putAll(nullXLabel.getLabel(), nullXCells);
            }
            {
                for (AxisLabel xAxisCategory : xAxisCategories) {
                    double min = Double.MAX_VALUE;
                    double max = Double.MIN_VALUE;
                    double sum = 0.0;

                    for (Integer integer : xCategoryMultimap.get(xAxisCategory.getLabel())) {
                        double value = matrixData.getCell(integer).getValue().doubleValue();
                        if (value > max) {
                            max = value;
                        }
                        if (value < min) {
                            min = value;
                        }
                        sum = sum + value;
                    }
                    int count = xCategoryMultimap.get(xAxisCategory.getLabel()).size();
                    double avg = sum / count;
                    xAxisCategory.setCount(count);
                    xAxisCategory.setMin(min);
                    xAxisCategory.setMax(max);
                    xAxisCategory.setAvg(avg);
                    xAxisCategory.setSum(sum);
                }
            }
            xAxisCategories.sort(settings.getXAxisComparator());
            AtomicInteger index = new AtomicInteger(0);
            for (AxisLabel axisXCategory : xAxisCategories) {
                int position = index.getAndIncrement();
                axisXCategory.setOrdinalPosition(position);
                for (Integer integer : xCategoryMultimap.get(axisXCategory.getLabel())) {
                    Cell cell = matrixData.getCell(integer);
                    cell.setX(position);
                }

            }
            matrixData.setxCategories(xAxisCategories);
        }
        {
            //set Ordinals of Y Axis and positional Y index on each cell
            NavigableSet<String> strings = yCategoryMultimap.keySet();
            List<AxisLabel> yAxisCategories = new ArrayList<AxisLabel>(strings.size() + 1);
            for (String s : strings) {
                yAxisCategories.add(new AxisLabel(s));
            }
            if (!nullYCells.isEmpty()) {
                AxisLabel nullYLabel = new AxisLabel("\uE000NULL");
                yAxisCategories.add(nullYLabel);
                yCategoryMultimap.putAll(nullYLabel.getLabel(), nullYCells);
            }
            {
                for (AxisLabel yAxisCategory : yAxisCategories) {
                    int count = Integer.MIN_VALUE;
                    double min = Double.MAX_VALUE;
                    double max = Double.MIN_VALUE;
                    double avg = 0.0;
                    double sum = 0.0;
                    for (Integer integer : yCategoryMultimap.get(yAxisCategory.getLabel())) {
                        double value = matrixData.getCell(integer).getValue().doubleValue();
                        if (value > max) {
                            max = value;
                        }
                        if (value < min) {
                            min = value;
                        }
                        sum = sum + value;

                    }
                    count = yCategoryMultimap.get(yAxisCategory.getLabel()).size();
                    avg = sum / count;
                    yAxisCategory.setCount(count);
                    yAxisCategory.setMin(min);
                    yAxisCategory.setMax(max);
                    yAxisCategory.setAvg(avg);
                    yAxisCategory.setSum(sum);

                }
            }
            yAxisCategories.sort(settings.getYAxisComparator());
            AtomicInteger index = new AtomicInteger(0);
            for (AxisLabel axisYCategory : yAxisCategories) {
                int position = index.getAndIncrement();
                axisYCategory.setOrdinalPosition(position);
                for (Integer integer : yCategoryMultimap.get(axisYCategory.getLabel())) {
                    Cell cell = matrixData.getCell(integer);
                    cell.setY(position);
                }
            }

            matrixData.setyCategories(yAxisCategories);
        }


        matrixData.setCellIdMap(cellIdMultimap);

        matrixData.postProcess();
        matrixData.createScaleImage(settings, getColorActionsService());

        // might add more detail here..
        matrixData.setMaxX(matrixData.getMatrixMetrics().getAxisXCount());
        matrixData.setMaxY(matrixData.getMatrixMetrics().getAxisYCount());
        //save the full object

//        if(viewDef.getMatrixSettings().getFilterCriteria().stream().anyMatch(chartCriterion -> chartCriterion.getOperatorString().equals("Top%") ||chartCriterion.getOperatorString().equals("Bottom%") )){
//            matrixData.doFilter(viewDef.getMatrixSettings(), viewDef.getMatrixSettings().getFilterCriteria(), false);
//            MatrixSelectionToRowsConverter matrixSelectionToRowsConverter = new MatrixSelectionToRowsConverter(dataView, viewDef);
//
//            //            matrixSelectionToRowsConverter.convertToRows();
//
//        }
        cache(viewDef.getUuid(), matrixData);

        return matrixData;
    }

    private static void cache(String vizUuid, MatrixData mData) {
       LOG.debug("Saving Matrix Cache");
        MatrixDataToBSON trans = new MatrixDataToBSON();
        AbstractMatrixCacheStorageService service = AbstractMatrixCacheStorageService.instance();
        MatrixCacheStorage matrixStorage = service.createEmptyStorage(vizUuid);
        matrixStorage.setResult(trans.apply(mData));
        service.saveMatrixCacheStorage(vizUuid, matrixStorage);
    }

    private static MatrixData getSummaryMatrixData(MatrixData originalMatrix, MatrixSettings settings) {

        MatrixData summary = summarize(originalMatrix, settings);

        // these values are the original min max.Matr
        summary.setMaxY(originalMatrix.getMaxY());
        summary.setMaxX(originalMatrix.getMaxX());
        summary.setMinX(originalMatrix.getMinX());
        summary.setMinY(originalMatrix.getMinY());

        summary.postProcess();
        summary.setMatrixMetrics(originalMatrix.getMatrixMetrics());
        summary.createScaleImage(settings, getColorActionsService());
        summary.setLimitExceeded(true);

        return summary;
    }

    public static MatrixData getDataForSelection(MatrixDataRequest req, MatrixViewDef viewDef) {
        MatrixData fullMatrix = new MatrixData();

        AbstractMatrixCacheStorageService storageService = AbstractMatrixCacheStorageService.instance();
        boolean isCache = storageService.hasVisualizationData(viewDef.getUuid());
        if (isCache) {
            fullMatrix = getSectionForSelection(storageService, viewDef.getUuid(), viewDef, req);
        }

        return fullMatrix;
    }

    /**
     * This is the main way to get the matrix data from the server
     *
     * @param request
     * @return
     */
    public static MatrixData getData(MatrixDataRequest request, MatrixViewDef viewDef) {
        MatrixData fullMatrix = getMatrixDataSection(viewDef, request.getDvUuid(), request);

        fullMatrix.createScaleImage(viewDef.getMatrixSettings(), getColorActionsService());

        switch (request.getSummarizationPolicy()) {
            case ALLOW_SUMMARY:
                if (fullMatrix.getTotalCount() > Configuration.getInstance().getMatrixConfig().getMaxCellCount()) {
                    fullMatrix = getSummaryMatrixData(fullMatrix, viewDef.getMatrixSettings());
                }
                break;
            case DISALLOW_SUMMARY:
                break;
            case FORCE_SUMMARY:
                fullMatrix = getSummaryMatrixData(fullMatrix, viewDef.getMatrixSettings());
                break;
        }

        return fullMatrix;

    }

    /**
     * this will min/max regions so we always have a valid range
     *
     * @param viewDef
     * @param dataViewUuid
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static MatrixData getData(final MatrixViewDef viewDef, final String dataViewUuid, int x1, int y1, int x2, int y2) {
       LOG.trace("Cells for region: " + "start: (" + x1 + ", " + y1 + ") end: (" + x2 + " , " + y2 + ")");


        MatrixDataRequest req = new MatrixDataRequest();
        // make sure we don't have negs.
        x1 = x1 < 0 ? 0 : x1;
        y1 = y1 < 0 ? 0 : y1;
        req.setStartX(Math.min(x1, x2));
        req.setEndX(Math.max(x1, x2));
        req.setStartY(Math.min(y1, y2));
        req.setEndY(Math.max(y1, y2));

        MatrixData fullMatrix = getMatrixDataSection(viewDef, dataViewUuid, req);
        fullMatrix.createScaleImage(viewDef.getMatrixSettings(), getColorActionsService());

        if ((x1 == x2) && (y1 == y2)) {
            return fullMatrix;
        }

        if (fullMatrix.getTotalCount() > Configuration.getInstance().getMatrixConfig().getMaxCellCount()) {
            fullMatrix = getSummaryMatrixData(fullMatrix, viewDef.getMatrixSettings());
        }

        return fullMatrix;
    }

    private static int getBinCountForAxis(int catLength) {
        if (catLength <= (int) Math.sqrt(Configuration.getInstance().getMatrixConfig().getMaxCellCount() * 1.2)) {
            return 1;
        } else {
            return catLength / (int) Math.sqrt(Configuration.getInstance().getMatrixConfig().getMaxCellCount() * 1.2);
        }
    }

    private static MatrixData summarize(MatrixData data, MatrixSettings settings) {
        long startTime = System.nanoTime();
        int xCatCount = data.getMaxX() - data.getMinX();
        int yCatCount = data.getMaxY() - data.getMinY();
        int xBucketSize = getBinCountForAxis(xCatCount);
        int yBucketSize = getBinCountForAxis(yCatCount);
        int xRemainder = xCatCount % xBucketSize;
        int yRemainder = yCatCount % yBucketSize;
        int xBuckets = xCatCount / xBucketSize;
        int yBuckets = yCatCount / yBucketSize;

        LOG.trace("Size before summary: " + data.getAllCells().size() + "X Size: " + xBucketSize + "(" + xCatCount + ")" + " Y size: " + yBucketSize + "(" + yCatCount + ")");

        MatrixData m = new MatrixData();
        m.setSummary(true);

        ArrayList<Cell>[][] summaryMatrixCells = new ArrayList[xCatCount / xBucketSize][yCatCount / yBucketSize];

        int minX = data.getxCategories().get(0).getOrdinalPosition();
        int minY = data.getyCategories().get(0).getOrdinalPosition();

        for (Cell cell : data.getAllCells()) {
            int x = (cell.getX() - minX) / xBucketSize;
            x -= (x / xBuckets) * xRemainder;
            int y = (cell.getY() - minY) / yBucketSize;
            y -= (y / yBuckets) * yRemainder;
            ArrayList<Cell> list = summaryMatrixCells[x][y];
            if (list == null) {
                list = new ArrayList<Cell>();
                summaryMatrixCells[x][y] = list;
            }
            list.add(cell);
        }

        for (int x = 0; x < xBuckets; x++) {
            for (int y = 0; y < yBuckets; y++) {
                ArrayList<Cell> cells = summaryMatrixCells[x][y];
                if (cells != null) {
                    Number value = cells.get(0).getValue();
                    for (Cell cell : cells) {
                        if (cell.getValue().doubleValue() > value.doubleValue()) {
                            value = cell.getValue();
                        }
                    }
                    Cell cell = new Cell();
                    //FIXME:NEED to set correct value here....
                    cell.setValue(value);
                    int x1 = (int) ((x + .5) * xBucketSize) + minX;
                    if (x == (xCatCount / xBucketSize)) {
                        x1 = Math.min(x1, data.getMatrixMetrics().getAxisXCount() - 1);
                    }
                    cell.setX(x1);
                    int y1 = (int) ((y + .5) * yBucketSize) + minY;
                    if (y == (yCatCount / yBucketSize)) {
                        y1 = Math.min(y1, data.getMatrixMetrics().getAxisYCount() - 1);
                    }
                    cell.setY(y1);
                    m.addCell(cell);
                }
            }
        }
        {
            int index = 0;
            List<AxisLabel> xCat = new ArrayList<AxisLabel>();
            m.setxCategories(xCat);
            for (int i = 0; i < (xCatCount / xBucketSize); i++) {
                try {
                    AxisLabel axisLabel;
                    if (xBucketSize == 1) {
                        axisLabel = new AxisLabel(data.getxCategories().get(i * xBucketSize).getLabel());
                    } else {
                        String label = data.getxCategories().get(index).getLabel();
                        index += xBucketSize + (((i % (xBuckets / xRemainder)) == 0)?1:0);
                        int countOthers = xBucketSize - 1;
                        if ((i % (xBuckets / xRemainder)) == 0) {
                            countOthers++;
                        }
                        if (countOthers == 1) {
                            label += " and " + countOthers + " other";
                        } else {
                            label += " and " + (countOthers) + " others";
                        }
                        axisLabel = new AxisLabel(label);
                    }
                    xCat.add(axisLabel);
                    int ordinalPosition = (int) ((i + .5) * xBucketSize) + minX;
                    if (i == (xCatCount / xBucketSize)) {
                        int max = data.getMatrixMetrics().getAxisXCount() - 1;
                        ordinalPosition = Math.min(max, ordinalPosition);
                    }
                    axisLabel.setOrdinalPosition(ordinalPosition);
                } catch (IndexOutOfBoundsException out) {
                    // noop
                }
            }
        }
        {
            List<AxisLabel> yCat = new ArrayList<AxisLabel>();
            m.setyCategories(yCat);
            int index = 0;
            for (int i = 0; i < (yCatCount / yBucketSize); i++) {
                try {
                    AxisLabel axisLabel;
                    if (yBucketSize == 1) {
                        axisLabel = new AxisLabel(data.getyCategories().get(i * yBucketSize).getLabel());
                    } else {
                        String label = data.getyCategories().get(index).getLabel();
                        index += yBucketSize + (((i % (yBuckets / yRemainder)) == 0) ? 1 : 0);
                        int countOthers = yBucketSize - 1;
                        if ((i % (yBuckets / yRemainder)) == 0) {
                            countOthers++;
                        }
                        if (countOthers == 1) {
                            label += " and " + countOthers + " other";
                        } else {
                            label += " and " + (countOthers) + " others";
                        }
                        axisLabel = new AxisLabel(label);
                    }
                    yCat.add(axisLabel);
                    int ordinalPosition = (int) ((i + .5) * yBucketSize) + minY;
                    if (i == (yCatCount / yBucketSize)) {
                        int max = data.getMatrixMetrics().getAxisYCount() - 1;
                        ordinalPosition = Math.min(max, ordinalPosition);
                    }
                    axisLabel.setOrdinalPosition(ordinalPosition);
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        LOG.trace("Summary complete. Duration: " + duration);
        return m;
    }

    private static MatrixQueryBuilder getQueryBuilder(MatrixViewDef viewDef, DataView dataView) {
        MatrixQueryBuilder qb = new MatrixQueryBuilder();
        qb.setViewDef(viewDef);
        qb.setDataView(dataView);
        qb.setSqlFactory(getSqlFactory());
        qb.setFilterActionsService(getFilterActionsService());
        return qb;
    }

    //invalidates cache for the matrix.
    public static void invalidateMatrixData(String visUuid) {

        AbstractMatrixCacheStorageService service = AbstractMatrixCacheStorageService.instance();
        service.resetData(visUuid);
//        cache.invalidate(visUuid);
    }

    private static double getValue(int type, ResultSet resultSet) {
        double value = 0;
        try {
            if (type == Types.BIGINT) {
                value = resultSet.getLong(3);
            } else if ((type == Types.DOUBLE) || (type == Types.NUMERIC)) {
                value = resultSet.getDouble(3);
            } else if (type == Types.DATE) {
                value = resultSet.getDate(3).getTime();
            } else if (type == Types.TIME) {
                value = resultSet.getTime(3).getTime();
            } else if (type == Types.TIMESTAMP) {
                value = resultSet.getTimestamp(3).getTime();
            } else if (type == Types.VARCHAR) {
                value = resultSet.getString(3).length();
            }
        } catch (Exception exception) {
           LOG.error("Failed to coerce some Matrix Data", exception);
            return value;
        }

        return value;
    }

    public static void postConstruct(SQLFactory sf, FilterActionsService fas, ColorActionsService cas) {
        sqlFactory = sf;
        filterActionsService = fas;
        colorActionsService = cas;
        cache = CacheBuilder.newBuilder().softValues().build();
    }

    private static Map<AxisLabel, AxisLabel> getAxisSummary(List<AxisLabel> axisLabels, int bucketSize) {
        Map<AxisLabel, AxisLabel> summary = new HashMap<>();
        int count = 0,
                bundleName = 0;
        AxisLabel tmpName = new AxisLabel("1");
        for (AxisLabel label : axisLabels) {
            if (count != bucketSize) {
                count++;
                summary.put(label, tmpName);
            } else {
                bundleName++;
                tmpName = new AxisLabel(bundleName + "");
                summary.put(label, tmpName);
                count = 0;
            }
        }

        return summary;
    }
}
