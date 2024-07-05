package csi.utils;

import java.text.MessageFormat;
import java.util.UUID;

import csi.server.common.util.Format;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import csi.server.business.visualization.SupportingRows;
import csi.server.business.visualization.graph.base.GraphSupportingRows;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.SortOrder;
import csi.server.common.model.chart.ChartField;
import csi.server.util.CacheUtil;

public class BundleExpressionTest {

    static String dimensionUUID;

    static String fieldUUID;

    protected ChartField dimension;

    @BeforeClass
    public static void bootstrap() {
        dimensionUUID = UUID.randomUUID().toString();
        fieldUUID = UUID.randomUUID().toString();
    }

    @Before
    public void setupField() {
        dimension = new ChartField();
        dimension.setDimName("txn date");
        dimension.setSortOrder(SortOrder.ASC);
        dimension.setUuid(dimensionUUID);
        dimension.setDimension(new FieldDef());

        FieldDef field = dimension.getDimension();
        field.setUuid(fieldUUID);
        field.setFieldType(FieldType.COLUMN_REF);
        field.setValueType(CsiDataType.Date);
    }

    @Test
    public void byYearAndMonth() {
        String pattern1 = "to_char( {0}, ''YYYY-MM'' )";
        String pattern2 = "to_char( date( {0} ), ''YYYY-MM'' )";
        String expected1 = MessageFormat.format(pattern1, CacheUtil.getQuotedColumnName(dimension.getDimension()));
        String expected2 = MessageFormat.format(pattern2, CacheUtil.getQuotedColumnName(dimension.getDimension()));

        dimension.setBundleFunction(SupportingRows.YEAR_MONTH);
        String fieldExpression = GraphSupportingRows.getFieldExpression(dimension, true);

        if (!(expected1.equals(fieldExpression) ||expected2.equals(fieldExpression)) ) {

            Assert.fail("EXPECTED: " + Format.value(expected1) + "\n"
                    + "OR: " + Format.value(expected2) + "\n"
                    + "ACTUAL: " + Format.value(fieldExpression) + "\n");
        }
    }

    @Test
    public void byYear() {
        String pattern1 = "cast(date_part( ''year'', {0} ) as integer)";
        String pattern2 = "cast(date_part( ''year'', date( {0} ) ) as integer)";
        String expected1 = MessageFormat.format(pattern1, CacheUtil.getQuotedColumnName(dimension.getDimension()));
        String expected2 = MessageFormat.format(pattern2, CacheUtil.getQuotedColumnName(dimension.getDimension()));
        dimension.setBundleFunction(SupportingRows.YEAR);
        String actual = GraphSupportingRows.getFieldExpression(dimension, true);

        if (!(expected1.equals(actual) ||expected2.equals(actual)) ) {

            Assert.fail("EXPECTED: " + Format.value(expected1) + "\n"
                    + "OR: " + Format.value(expected2) + "\n"
                    + "ACTUAL: " + Format.value(actual) + "\n");
        }
    }

}
