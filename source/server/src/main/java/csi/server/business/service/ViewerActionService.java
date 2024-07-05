package csi.server.business.service;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import csi.server.business.service.export.DownloadServlet;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.business.visualization.viewer.dto.ViewerGridHeader;
import csi.server.business.visualization.viewer.lens.FieldLens;
import csi.server.business.visualization.viewer.lens.*;
import csi.server.common.dto.CustomPagingResultBean;
import csi.server.common.dto.TableDataSet;
import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.visualization.viewer.ContextImageLensDef;
import csi.server.common.model.visualization.viewer.FieldLensDef;
import csi.server.common.model.visualization.viewer.HyperlinkLensDef;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.LinkLensDef;
import csi.server.common.model.visualization.viewer.NodeLensDef;
import csi.server.common.model.visualization.viewer.NodeNeighborLabelLensDef;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.common.model.visualization.viewer.field.SumFieldLensDef;
import csi.server.common.model.visualization.viewer.field.AverageFieldLensDef;
import csi.server.common.model.visualization.viewer.field.CountDistinctFieldLensDef;
import csi.server.common.model.visualization.viewer.field.CountFieldLensDef;
import csi.server.common.model.visualization.viewer.field.MaxFieldLensDef;
import csi.server.common.model.visualization.viewer.field.MinFieldLensDef;
import csi.server.common.model.visualization.viewer.field.ValueFieldLensDef;
import csi.server.common.model.visualization.viewer.field.VarianceFieldLensDef;
import csi.server.common.model.visualization.viewer.graph.NodeNeighborTypeLensDef;
import csi.server.common.service.api.ViewerActionServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensFieldDefSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensListSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensMultiListSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ViewerActionService implements ViewerActionServiceProtocol {

    private static ViewerActionService intance;
    @Autowired
    ServletContext servletContext;

    public ViewerActionService() {
        intance = this;
    }

    public static ViewerActionService getInstance() {
        return intance;
    }

    @Override
    public LensImage getLensImage(LensDef lensDef, Objective objective) {
        return LensLocator.getLens(lensDef).focus(lensDef, objective);

    }

    @Override
    public List<LensImage> getLensImage(String dvuuid, Objective objective) {
        List<LensImage> out = Lists.newArrayList();
        List<LensDef> lensDefs = ldMap.get(dvuuid);
        if (lensDefs == null) {
            return out;
        }
        for (LensDef lensDef : lensDefs) {
            LensImage lensImage = getLensImage(lensDef, objective);
            if (lensImage != null) {

                lensImage.setLensDef(lensDef.getId());
                out.add(lensImage);
            }
        }
        return out;
    }

    @Override
    public ViewerGridConfig getGridConfig(Objective objective, String id, String dvuuid) {
        List<LensDef> lensDefs = ldMap.get(dvuuid);
        LensDef lensDef = null;
        for (LensDef def : lensDefs) {
            if (def.getId().equals(id)) {
                lensDef = def;
            }
        }
        return LensLocator.getLens(lensDef).getGridConfig();

    }

    @Override
    public CustomPagingResultBean<List<?>> getGridData(Objective objective, String id, String token, PagingLoadConfig loadConfig, String dvuuid) {

        List<LensDef> lensDefs = ldMap.get(dvuuid);
        LensDef lensDef = null;
        for (LensDef def : lensDefs) {
            if (def.getId().equals(id)) {
                lensDef = def;
            }
        }
        ViewerGridConfig gridConfig = getGridConfig(objective, id, dvuuid);
        CustomPagingResultBean<List<?>> loadResult = new CustomPagingResultBean<List<?>>();
        List<List<?>> everything = LensLocator.getLens(lensDef).
                focus(lensDef, objective, token);

        //sort everything
        if (loadConfig.getSortInfo().size() == 1) {
            SortInfo sortInfo = loadConfig.getSortInfo().get(0);
            int sortField = Integer.parseInt(sortInfo.getSortField()) - 1;
            SortDir sortDir = sortInfo.getSortDir();
            everything.sort(new Comparator<List<?>>() {
                @Override
                public int compare(List<?> o1, List<?> o2) {
                    try {
                        int compare = ComparisonChain.start().compare((Integer) o1.get(sortField), (Integer) o2.get(sortField)).result();
                        return sortDir == SortDir.ASC ? compare : -compare;
                    } catch (Exception e) {
                    }
                    String left = String.valueOf(o1.get(sortField));
                    if (left.indexOf(';') > 0) {
                        left = left.substring(0, left.indexOf(';'));
                    }
                    String right = String.valueOf(o2.get(sortField));
                    if (right.indexOf(';') > 0) {
                        right = right.substring(0, right.indexOf(';'));
                    }

                    int compare = ComparisonChain.start().compare(Doubles.tryParse(left) != null ? Doubles.tryParse(left) : Double.MAX_VALUE, Doubles.tryParse(right) != null ? Doubles.tryParse(right) : Double.MAX_VALUE).compare(left, right).result();

//                    int compare = ComparisonChain.start().compare((String) o1.get(sortField), (String) o2.get(sortField)).result();
                    return sortDir == SortDir.ASC ? compare : -compare;

                }
            });
        }

        List<List<?>> everythingSorted = everything;

        //offset/limit everything
        int offset = loadConfig.getOffset();
        int limit = loadConfig.getLimit();

        int toIndex = offset + limit;
        toIndex = Math.min(everythingSorted.size(), toIndex);
        List<List<?>> results = everythingSorted.subList(offset, toIndex);


        TableDataSet tableDataSet = new TableDataSet();


        for (ViewerGridHeader header : gridConfig.getHeaders()) {
            tableDataSet.addHeader(header.getFieldDef().getName());

        }
//FIXME: this looks sloppy
        for (List<?> result : results) {
            List<Object> row;
            row = new ArrayList<Object>();
            for (Object o : result) {

                row.add(o);
            }
            tableDataSet.addRow(row);
        }
        loadResult.setHeaders(tableDataSet.getHeaders());
        loadResult.setData(tableDataSet.getRows());
        loadResult.setOffset(loadConfig.getOffset());
        loadResult.setTotalLength(everything.size());

        return loadResult;
    }

    @Override
    public Map<String, List<LensDefSettings>> getAvailableLenses() {
        HashMap<String, List<LensDefSettings>> stringListHashMap = Maps.newHashMap();
        Set<LensDef> lensDefs = Sets.newHashSet();
        //FIXME:Inject these
        lensDefs.add(new NodeNeighborLabelLensDef());
        lensDefs.add(new SumFieldLensDef());
        lensDefs.add(new AverageFieldLensDef());
        lensDefs.add(new CountDistinctFieldLensDef());
        lensDefs.add(new CountFieldLensDef());
        lensDefs.add(new HyperlinkLensDef());
        lensDefs.add(new MaxFieldLensDef());
        lensDefs.add(new MinFieldLensDef());
        lensDefs.add(new ValueFieldLensDef());
        lensDefs.add(new VarianceFieldLensDef());
        lensDefs.add(new ContextImageLensDef());
        for (LensDef lensDef : lensDefs) {
            Collection<String> strings = lensDef.getGroups();
            for (String s : strings) {
                if (stringListHashMap.get(s) == null) {
                    stringListHashMap.put(s, Lists.newArrayList());
                }
                stringListHashMap.get(s).add(lensDef.getSettings());
            }
        }
        return stringListHashMap;
    }

    @Override
    public List<LensDefSettings> getLensConfiguration(String dvuuid) {
        //FIXME: get from DV

        ArrayList<LensDefSettings> out = Lists.newArrayList();
        List<LensDef> lensDefs = ldMap.get(dvuuid);
        if (lensDefs == null) {
            return out;
        }
        for (LensDef def : lensDefs) {
            out.add(def.getSettings());
        }

        /*out.add((new NodeNeighborLabelLensDef()).getSettings());
        out.add((new SumFieldLensDef()).getSettings());
        out.add(new AverageFieldLensDef().getSettings());
        out.add(new CountDistinctFieldLensDef().getSettings());
        out.add(new CountFieldLensDef().getSettings());
        out.add(new HyperlinkLensDef().getSettings());
        out.add(new MaxFieldLensDef().getSettings());
        out.add(new MinFieldLensDef().getSettings());
        out.add(new ValueFieldLensDef().getSettings());
        out.add(new VarianceFieldLensDef().getSettings());*/

        return out;
    }

    @Override
    public void updateSettings(List<LensDefSettings> lensSettingsControls, String dvuuid) {
        ArrayList<LensDef> value = Lists.newArrayList();
        ldMap.put(dvuuid, value);
        for (LensDefSettings lensSettingsControl : lensSettingsControls) {
            LensDef ld = buildLens(lensSettingsControl);
            value.add(ld);
        }
    }

    @Override
    public String exportMoreGrid(Objective objective, String lensDefid, PagingLoadConfig loadConfig, String dvuuid) {
        String path = servletContext.getRealPath(DownloadServlet.TEMP_DIRECTORY);
        loadConfig.setLimit(10000000);
        loadConfig.setOffset(0);
        CustomPagingResultBean<List<?>> gridData = getGridData(objective, lensDefid, null, loadConfig, dvuuid);
        ViewerGridConfig gridConfig = getGridConfig(objective, lensDefid, dvuuid);
        path += File.separator + UUID.randomUUID().toString() + DownloadServlet.TEMP_FILE_EXT;
        File file = new File(path);

        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {

            List<ViewerGridHeader> headers = gridConfig.getHeaders();
            String[] hrow = new String[headers.size()];

            for (int i = 1; i < headers.size(); i++) {
                ViewerGridHeader header = headers.get(i);
                hrow[i-1] = header.getFieldDef().getName();
            }
            csvWriter.writeNext(hrow);
            for (List<?> datum : gridData.getData()) {

                String[] row = new String[datum.size()];
                for (int i = 1; i < datum.size(); i++) {
                    if (datum.get(i) instanceof Integer) {
                        row[i-1] = Integer.toString((Integer) datum.get(i));
                    } else {
                        row[i-1] = (String) datum.get(i);
                    }
                }
                csvWriter.writeNext(row);
            }
            return file.getName().substring(0, file.getName().length() - DownloadServlet.TEMP_FILE_EXT.length());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String exportMoreGrid(List<NodeListDTO> selItems) {
        return null;
    }

    private LensDef buildLens(LensDefSettings lensSettingsControl) {
        if (lensSettingsControl.getLensType().equals("ContextImage")) {
            ContextImageLensDef ld = new ContextImageLensDef();
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("AverageField")) {
            AverageFieldLensDef ld = new AverageFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("VarianceField")) {
            VarianceFieldLensDef ld = new VarianceFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("ValueField")) {
            ValueFieldLensDef ld = new ValueFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("SumField")) {
            SumFieldLensDef ld = new SumFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("CountDistinctField")) {
            CountDistinctFieldLensDef ld = new CountDistinctFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("SumField")) {
            SumFieldLensDef ld = new SumFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("CountField")) {
            CountFieldLensDef ld = new CountFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("MaxField")) {
            MaxFieldLensDef ld = new MaxFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("MinField")) {
            MinFieldLensDef ld = new MinFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("hyperlink")) {
            HyperlinkLensDef ld = new HyperlinkLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("NeighborLabels")) {
            NodeNeighborLabelLensDef ld = new NodeNeighborLabelLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Bundle")) {
                    if (control instanceof LensListSetting) {

                        LensListSetting lls = (LensListSetting) control;
                        switch (lls.getSelectedValue()) {
                            case "Bundles":
                                ld.setIncludeBundles(true);
                                ld.setIncludeBundled(false);
                                break;
                            case "Bundled":
                                ld.setIncludeBundles(false);
                                ld.setIncludeBundled(true);
                                break;
                            case "Bundles & Bundled":
                                ld.setIncludeBundles(true);
                                ld.setIncludeBundled(true);
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (control.getLabel().equals("Hidden")) {
                    if (control instanceof LensListSetting) {
                        LensListSetting lls = (LensListSetting) control;
                        switch (lls.getSelectedValue()) {
                            case "Visible":
                                ld.setIncludeHidden(false);
                                ld.setIncludeVisible(true);
                                break;
                            case "Hidden":
                                ld.setIncludeHidden(true);
                                ld.setIncludeVisible(false);
                                break;
                            case "Visible & Hidden":
                                ld.setIncludeHidden(true);
                                ld.setIncludeVisible(true);
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (control.getLabel().equals("Data/User")) {
                    if (control instanceof LensListSetting) {
                        LensListSetting lls = (LensListSetting) control;
                        switch (lls.getSelectedValue()) {
                            case "Data":
                                ld.setIncludeDataNodes(true);
                                ld.setIncludeUserNodes(false);
                                break;
                            case "User":
                                ld.setIncludeDataNodes(false);
                                ld.setIncludeUserNodes(true);
                                break;
                            case "Data & User":
                                ld.setIncludeDataNodes(true);
                                ld.setIncludeUserNodes(true);
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (control.getLabel().equals("Direction")) {
                    if (control instanceof LensListSetting) {
                        LensMultiListSetting lls = (LensMultiListSetting) control;
                        ld.setIncludeSources(false);
                        ld.setIncludeBidirectional(false);
                        ld.setIncludeIncoming(false);
                        ld.setIncludeNoDirection(false);
                        for (String selectedValue : lls.getSelectedValues()) {

                            switch (selectedValue) {
                                case "Source":
                                    ld.setIncludeSources(true);
                                    break;
                                case "Target":
                                    ld.setIncludeIncoming(true);
                                    break;
                                case "Both":
                                    ld.setIncludeBidirectional(true);
                                    break;
                                case "Neither":
                                    ld.setIncludeNoDirection(true);
                                    break;
                                default:

                                    break;
                            }
                        }
                    }
                }
            }

            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        if (lensSettingsControl.getLensType().equals("ValueField")) {
            ValueFieldLensDef ld = new ValueFieldLensDef();
            for (LensSettingsControl control : lensSettingsControl.getControls()) {
                if (control.getLabel().equals("Field")) {
                    if (control instanceof LensFieldDefSetting) {
                        LensFieldDefSetting control1 = (LensFieldDefSetting) control;

                        ld.setValue(control1.getValue());
                    }
                }
            }
            ld.setDisplayName(lensSettingsControl.getName());
            return ld;
        }
        return null;
    }


    Map<String, List<LensDef>> ldMap = Maps.newConcurrentMap();

    private static class LensLocator {
        //FIXME: Iterate over list of injected Lenses to see who handles LensDef
        static Lens getLens(LensDef lensDef) {
            if (lensDef instanceof FieldLensDef) {
                return new FieldLens();
            }
            if (lensDef instanceof NodeLensDef) {
                return new NodeLens();
            }
            if (lensDef instanceof LinkLensDef) {
                return new LinkLens();
            }
            if (lensDef instanceof NodeNeighborLabelLensDef) {
                return new NodeNeighborLabelLens();
            }
            if (lensDef instanceof SumFieldLensDef) {
                return new SumFieldLens();
            }
            if (lensDef instanceof AverageFieldLensDef) {
                return new AverageFieldLens();
            }
            if (lensDef instanceof CountDistinctFieldLensDef) {
                return new CountDistinctFieldLens();
            }
            if (lensDef instanceof CountFieldLensDef) {
                return new CountFieldLens();
            }
            if (lensDef instanceof MaxFieldLensDef) {
                return new MaxFieldLens();
            }
            if (lensDef instanceof MinFieldLensDef) {
                return new MinFieldLens();
            }
            if (lensDef instanceof ValueFieldLensDef) {
                return new ValueFieldLens();
            }
            if (lensDef instanceof VarianceFieldLensDef) {
                return new VarianceFieldLens();
            }
            if (lensDef instanceof NodeNeighborTypeLensDef) {
                return new NodeNeighborTypeLens();
            }
            if (lensDef instanceof HyperlinkLensDef) {
                return new HyperlinkLens();
            }
            if (lensDef instanceof ContextImageLensDef) {
                return new ContextImageLens();
            }
            return new NullLens();
        }
    }

    static class NullLens implements Lens {
        @Override
        public LensImage focus(LensDef lensDef, Objective objective) {
            return null;
        }

        @Override
        public List<List<?>> focus(LensDef lensDef, Objective objective, String token) {
            return null;
        }

        @Override
        public ViewerGridConfig getGridConfig() {
            return null;
        }
    }
}
