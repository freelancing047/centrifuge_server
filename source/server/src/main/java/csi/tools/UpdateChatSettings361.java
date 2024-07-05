package csi.tools;

import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.Version;
import csi.shared.core.visualization.chart.ChartType;
import csi.shared.core.visualization.chart.MeasureChartType;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.util.Collection;
import java.util.List;

public class UpdateChatSettings361 implements CustomTaskChange {
    @Override
    public void execute(Database database) throws CustomChangeException {
        if (Version.getMajorVersion() == 3 && Version.getMinorVersion() == 6 && Version.getSubMajorVersion() == 2) {
            return;
        }
            CsiPersistenceManager.begin();
            Collection<ChartSettings> listE = CsiPersistenceManager.getMetaEntityManager().createQuery("SELECT e from ChartSettings e", ChartSettings.class).getResultList();
            for (ChartSettings chartSettings : listE) {
                fix(chartSettings.getCategoryDefinitions(), chartSettings.getMeasureDefinitions());
            }
            CsiPersistenceManager.commit();
            CsiPersistenceManager.close();
    }

    private void fix(List<CategoryDefinition> categoryDefinitions, List<MeasureDefinition> measureDefinitions) {
        if (measureDefinitions.size() > 1) {
            for (MeasureDefinition measureDefinition : measureDefinitions) {
                for (int i = 0; i < categoryDefinitions.size(); i++) {
                    CategoryDefinition categoryDefinition = categoryDefinitions.get(i);
                    switch (categoryDefinition.getChartType()) {
                        case AREA:
                            if (i == 0) {
                                fixMultimeasueType(measureDefinition, MeasureChartType.AREA);
                            }
                            categoryDefinition.setChartType(ChartType.COLUMN);
                            break;
                        case AREA_SPLINE:
                            if (i == 0) {
                                fixMultimeasueType(measureDefinition, MeasureChartType.AREA_SPLINE);
                            }
                            categoryDefinition.setChartType(ChartType.COLUMN);
                            break;
                        case COLUMN:
                            if (i == 0) {
                                fixMultimeasueType(measureDefinition, MeasureChartType.COLUMN);
                            }
                            categoryDefinition.setChartType(ChartType.COLUMN);
                            break;
                        case LINE:
                            if (i == 0) {
                                fixMultimeasueType(measureDefinition, MeasureChartType.LINE);
                            }
                            categoryDefinition.setChartType(ChartType.COLUMN);
                            break;
                        case BAR:
                            if (i == 0) {
                                fixMultimeasueType(measureDefinition, MeasureChartType.COLUMN);
                            }
                            categoryDefinition.setChartType(ChartType.BAR);
                            break;
                        case POLAR:
                        case SPIDER:
                        case PIE:
                        case DONUT:
                            if (i == 0) {
                                fixMultimeasueType(measureDefinition, MeasureChartType.COLUMN);
                            }
                            break;
                    }
                }
            }


        } else {
            for (MeasureDefinition measureDefinition : measureDefinitions) {
                switch (measureDefinition.getMeasureChartType()) {
                    case DEFAULT:
                        ChartType t = ChartType.COLUMN;
                        for (int i = 0; i < categoryDefinitions.size(); i++) {
                            switch (categoryDefinitions.get(i).getChartType()) {
                                case COLUMN:
                                    t = ChartType.COLUMN;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.COLUMN);
                                    }
                                    break;
                                case BAR:
                                    t = ChartType.BAR;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.COLUMN);
                                    }
                                    break;
                                case AREA:
                                    t = ChartType.COLUMN;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.AREA);
                                    }
                                    break;
                                case AREA_SPLINE:
                                    t = ChartType.COLUMN;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.AREA_SPLINE);
                                    }
                                    break;
                                case POLAR:
                                    t = ChartType.POLAR;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.COLUMN);
                                    }
                                    break;
                                case SPIDER:
                                    t = ChartType.SPIDER;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.COLUMN);
                                    }
                                    break;
                                case LINE:
                                    t = ChartType.COLUMN;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.LINE);
                                    }
                                    break;
                                case PIE:
                                    t = ChartType.PIE;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.COLUMN);
                                    }
                                    break;
                                case DONUT:
                                    t = ChartType.DONUT;
                                    if (i == 0) {
                                        measureDefinition.setMeasureChartType(MeasureChartType.COLUMN);
                                    }
                                    break;
                            }
                            categoryDefinitions.get(i).setChartType(t);
                        }
                        break;
                    case PIE:
                        measureDefinition.setMeasureChartType(MeasureChartType.COLUMN);
                        for (CategoryDefinition categoryDefinition : categoryDefinitions) {
                            categoryDefinition.setChartType(ChartType.PIE);
                        }
                        break;
                    case DONUT:
                        measureDefinition.setMeasureChartType(MeasureChartType.COLUMN);
                        for (CategoryDefinition categoryDefinition : categoryDefinitions) {
                            categoryDefinition.setChartType(ChartType.DONUT);
                        }
                        break;
                    case COLUMN:
                    case AREA:
                    case AREA_SPLINE:
                    case LINE:
                        for (CategoryDefinition categoryDefinition : categoryDefinitions) {
                            if (categoryDefinition.getChartType() != ChartType.BAR) {
                                categoryDefinition.setChartType(ChartType.COLUMN);
                            }
                        }
                        break;
                }
            }
        }
    }

    private void fixMultimeasueType(MeasureDefinition measureDefinition, MeasureChartType column) {
        switch (measureDefinition.getMeasureChartType()) {
            case DEFAULT:
            case DONUT:
            case PIE:
                measureDefinition.setMeasureChartType(column);
                break;
            case COLUMN:
            case AREA:
            case AREA_SPLINE:
            case LINE:
        }
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
