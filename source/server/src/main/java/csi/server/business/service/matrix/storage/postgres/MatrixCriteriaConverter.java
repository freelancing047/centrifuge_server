package csi.server.business.service.matrix.storage.postgres;

import com.mongodb.DBObject;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import org.jdom.adapters.CrimsonDOMAdapter;

import java.util.List;
import java.util.function.Function;

public class MatrixCriteriaConverter implements Function<DBObject, List<String>> {

    List<ChartCriterion> criteria;
    MatrixViewDef viewDef;

    public MatrixCriteriaConverter(List<ChartCriterion> criterias, MatrixViewDef viewDefinition) {

        this.criteria = criterias;
        viewDef = viewDefinition;

    }

    @Override
    public List<String> apply(DBObject dbObject) {
        return null;
    }
}
