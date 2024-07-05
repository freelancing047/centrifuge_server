package csi.client.gwt.viz.graph.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.TakesValue;

import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.ConditionalExpression;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.Property;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

class GraphSettingsModel implements TakesValue<String>{

    // FIXME: move to server
    private static final String DEFAULT_BACKGROUND_COLOR_STRING = "#FEFEFE";//NON-NLS

    // FIXME:
    private static final int DEFAULT_RENDER_THRESHOLD = 500;
    private GraphSettings graphSettings;
    private boolean loadAfterSave = true;

    private RelGraphViewDef relGraphViewDef;

    public GraphSettingsModel(GraphSettings graphSettings) {
        this.graphSettings = graphSettings;
    }

    public String getBackgroundColorString() {
        String backgroundColorString = DEFAULT_BACKGROUND_COLOR_STRING;
        int backgroundColorInt = ClientColorHelper.rgb_to_int(254, 254, 254);
        if ((relGraphViewDef == null) || (relGraphViewDef.getSettings() == null)) {
            return backgroundColorString;
        }
        String myColorString = relGraphViewDef.getSettings().getPropertiesMap()
                .get("csi.relgraph.backgroundColor");//NON-NLS
        if ((null != myColorString) && (0 < myColorString.length())) {
            int color = Integer.parseInt(myColorString);
            if(color == 16777215){
                color = 16711422;
            }
            Color myColor = ClientColorHelper.get().make(color);
            backgroundColorString = myColor.toString();
            backgroundColorInt = color;
        }
            // push default background color into server model
            relGraphViewDef.setPropertyValue("csi.relgraph.backgroundColor", backgroundColorInt + "");//NON-NLS
        return backgroundColorString;
    }

    public List<BundleDef> getBundleDefs() {
        return relGraphViewDef.getBundleDefs();
    }

    public List<LinkDef> getLinkDefs() {
        return relGraphViewDef.getLinkDefs();
    }

    public void setLinkDefs(List<LinkDef> linkDefs) {
        relGraphViewDef.setLinkDefs(linkDefs);
    }

    public boolean getLoadAfterSave() {
        return loadAfterSave;
    }

    public boolean getLoadOnStartup() {
        CsiMap<String, String> clientProperties = relGraphViewDef.getClientProperties();
        String loadString = clientProperties.get("vizBox.loadOnStartup");//NON-NLS
        if (loadString != null) {
            return 0 == "true".compareToIgnoreCase(loadString);//NON-NLS
        }
        return true;
    }

    public List<NodeDef> getNodeDefs() {
        return relGraphViewDef.getNodeDefs();
    }

    public void setNodeDefs(List<NodeDef> nodeDefs) {
        relGraphViewDef.setNodeDefs(nodeDefs);
    }

    public String getOptionSetName() {
        return relGraphViewDef.getOptionSetName();
    }

    public void setOptionSetName(String value) {
        relGraphViewDef.setOptionSetName(value);
    }

    public int getRenderThreshold() {

        if (relGraphViewDef.getClientProperties().containsKey(RelGraphViewDef.PROPERTY_RENDER_THRESHOLD)) {
            String renderThreshold = relGraphViewDef.getClientProperties().get(RelGraphViewDef.PROPERTY_RENDER_THRESHOLD);
            try {
                return Integer.parseInt(renderThreshold);
            } catch (NumberFormatException ignored) {
            }
            // } else if (startParams.hasOption("graphRenderThreshold")) {
            // parseInt(startParams.options.graphRenderThreshold);
        }
        return DEFAULT_RENDER_THRESHOLD;
    }
    
    public String getThemeUuid() {
        return relGraphViewDef.getThemeUuid();
    }

    public String getTitle() {
        return relGraphViewDef.getName();
    }

    public String getUuid() {
        return relGraphViewDef.getUuid();
    }

    public void setName(String value) {
        relGraphViewDef.setName(value.trim());
    }

    public void setPropertyValue(String string, String string2) {
        relGraphViewDef.setPropertyValue(string, string2);
    }

    public RelGraphViewDef getRelGraphViewDef() {
        return relGraphViewDef;
    }

    public void setRelGraphViewDef(RelGraphViewDef original) {
        if (original == null) {
            relGraphViewDef = null;
        } else {
            // we are going to make a defensive copy at this point.
            HashMap<String, Object> copies = new HashMap<>();
            List<FieldDef> goodFieldDefs = FieldDefUtils.getAllSortedFields(graphSettings.getDataViewDef()
                    .getModelDef(), SortOrder.ALPHABETIC);
            for (FieldDef fieldDef : goodFieldDefs) {
                copies.put(fieldDef.getUuid(), fieldDef);
            }
            this.relGraphViewDef = original.copySettings(copies);
        }
    }

    public void apply(VisualizationDef copyTo) {
        if (copyTo instanceof RelGraphViewDef) {
            RelGraphViewDef target = (RelGraphViewDef) copyTo;
            List<FieldDef> goodFieldDefs = FieldDefUtils.getAllSortedFields(graphSettings.getDataViewDef()
                    .getModelDef(), SortOrder.ALPHABETIC);

            {

                RelGraphViewDef copyFrom = this.relGraphViewDef;
                target.setOptionSetName(copyFrom.getOptionSetName());
                target.setThemeUuid(copyFrom.getThemeUuid());
                // settings
                {
                    GenericProperties settings2 = copyFrom.getSettings();
                    target.getSettings().setClientProperties(settings2.getClientProperties());
                    List<Property> properties = Lists.newArrayList();
                    List<Property> propertiesOld = target.getSettings().getProperties();
                    List<Property> propertiesNew = settings2.getProperties();
                    for (Property p : propertiesNew) {
                        boolean isNew = true;
                        for (Property p2 : propertiesOld) {
                            if (p.getUuid().equals(p2.getUuid())) {
                                isNew = false;
                                properties.add(p2);
                                p2.setClientProperties(p.getClientProperties());
                                p2.setName(p.getName());
                                p2.setValue(p.getValue());
                                break;
                            }
                        }
                        if (isNew) {
                            properties.add(p);
                        }
                    }
                    target.getSettings().setProperties(properties);
                }
                target.setType(copyFrom.getType());
                target.setName(copyFrom.getName());
                target.setClientProperties(copyFrom.getClientProperties());
                // NodeDefs
                {
                    List<NodeDef> _nodeDefs = Lists.newArrayList();
                    List<NodeDef> nodeDefsNew = copyFrom.getNodeDefs();
                    List<NodeDef> nodeDefsOld = target.getNodeDefs();
                    for (NodeDef n : nodeDefsNew) {
                        boolean isNew = true;
                        for (NodeDef n2 : nodeDefsOld) {
                            if (n.getUuid().equals(n2.getUuid())) {
                                isNew = false;
                                _nodeDefs.add(n2);
                                n2.setAddPrefixId(n.isAddPrefixId());
                                {
                                    Set<AttributeDef> attributeDefs = mergeAttributeDefs(n.getAttributeDefs(),
                                            n2.getAttributeDefs());
                                    n2.setAttributeDefs(attributeDefs);
                                }
                                n2.setClientProperties(n.getClientProperties());

                                if (n.getCreateConditional() == null) {
                                    n2.setCreateConditional(null);
                                } else if (n2.getCreateConditional() == null) {
                                    n2.setCreateConditional(n.getCreateConditional());
                                } else {
                                    ConditionalExpression _old = n2.getCreateConditional();
                                    ConditionalExpression _new = n.getCreateConditional();
                                    _old.setClientProperties(_new.getClientProperties());
                                    _old.setExpression(_new.getExpression());
                                }

                                if (n.getHiddenConditional() == null) {
                                    n2.setHiddenConditional(null);
                                } else if (n2.getHiddenConditional() == null) {
                                    n2.setHiddenConditional(n.getHiddenConditional());
                                } else {
                                    ConditionalExpression _old = n2.getHiddenConditional();
                                    ConditionalExpression _new = n.getHiddenConditional();
                                    _old.setClientProperties(_new.getClientProperties());
                                    _old.setExpression(_new.getExpression());
                                }

                                n2.setHideLabels(n.getHideLabels());
                                n2.setInitiallyHidden(n.isInitiallyHidden());
                                n2.setName(n.getName());
                                n2.setNodeDefType(n.getNodeDefType());
                                break;
                            }

                        }
                        if (isNew) {
                            // still need to swap in good fields?
                            _nodeDefs.add(n);
                        }
                    }
                    target.setNodeDefs(_nodeDefs);
                }
                {
                    List<LinkDef> _linkDefs = Lists.newArrayList();
                    List<LinkDef> linkDefsNew = copyFrom.getLinkDefs();
                    List<LinkDef> linkDefsOld = target.getLinkDefs();
                    for (LinkDef l : linkDefsNew) {
                        boolean isNew = true;
                        for (LinkDef l2 : linkDefsOld) {
                            if (l2.getUuid().equals(l.getUuid())) {
                                isNew = false;
                                _linkDefs.add(l2);
                                l2.setClientProperties(l.getClientProperties());

                                if (l.getCreateConditional() == null) {
                                    l2.setCreateConditional(null);
                                } else if (l2.getCreateConditional() == null) {
                                    l2.setCreateConditional(l.getCreateConditional());
                                } else {
                                    ConditionalExpression _old = l2.getCreateConditional();
                                    ConditionalExpression _new = l.getCreateConditional();
                                    _old.setClientProperties(_new.getClientProperties());
                                    _old.setExpression(_new.getExpression());
                                }

                                if (l.getHiddenConditional() == null) {
                                    l2.setHiddenConditional(null);
                                } else if (l2.getHiddenConditional() == null) {
                                    l2.setHiddenConditional(l.getHiddenConditional());
                                } else {
                                    ConditionalExpression _old = l2.getHiddenConditional();
                                    ConditionalExpression _new = l.getHiddenConditional();
                                    _old.setClientProperties(_new.getClientProperties());
                                    _old.setExpression(_new.getExpression());
                                }
                                if (l.getName().equals(l.getUuid())) {
                                	l2.setName(null);
                                } else {
                                	l2.setName(l.getName());
                                }
                                NodeDef nodeDef1New = l.getNodeDef1();
                                for (NodeDef n : target.getNodeDefs()) {
                                    if (n.getUuid().equals(nodeDef1New.getUuid())) {
                                        l2.setNodeDef1(n);
                                    }
                                }
                                NodeDef nodeDef2New = l.getNodeDef2();
                                for (NodeDef n : target.getNodeDefs()) {
                                    if (n.getUuid().equals(nodeDef2New.getUuid())) {
                                        l2.setNodeDef2(n);
                                    }
                                }

                                {
                                    Set<AttributeDef> attributeDefs = mergeAttributeDefs(l.getAttributeDefs(),
                                            l2.getAttributeDefs());
                                    
                                    AttributeDef toRemove = null;
                                    for(AttributeDef attributeDef: attributeDefs){
                                        if(attributeDef.getName().equals(ObjectAttributes.CSI_INTERNAL_ID)){
                                            toRemove = attributeDef;
                                        }
                                    }
                                    if(toRemove != null)
                                        attributeDefs.remove(toRemove);
                                    
                                    l2.setAttributeDefs(attributeDefs);
                                }
                                {
                                    DirectionDef _directionDef = l.getDirectionDef();
                                    if (l.getDirectionDef() == null) {
                                        l2.setDirectionDef(null);
                                    } else if (l2.getDirectionDef() == null) {

                                        FieldDef _fieldDef = l.getDirectionDef().getFieldDef();
                                        if (_fieldDef != null) {
                                            for (FieldDef f : goodFieldDefs) {
                                                if (_fieldDef.getUuid().equals(f.getUuid())) {
                                                    l.getDirectionDef().setFieldDef(f);
                                                }
                                            }
                                        }
                                        l2.setDirectionDef(l.getDirectionDef());
                                    } else {
                                        _directionDef = l2.getDirectionDef();
                                        _directionDef.setClientProperties(l.getDirectionDef().getClientProperties());
                                        FieldDef _fieldDef = l.getDirectionDef().getFieldDef();
                                        _directionDef.setFieldDef(_fieldDef);
                                        if (_fieldDef.getUuid() != null) {
                                            for (FieldDef f : goodFieldDefs) {
                                                if (_fieldDef.getUuid().equals(f.getUuid())) {
                                                    _directionDef.setFieldDef(f);
                                                }
                                            }
                                        }
                                        _directionDef.setForwardValues(l.getDirectionDef().getForwardValues());
                                        _directionDef.setReverseValues(l.getDirectionDef().getReverseValues());
                                    }
                                }
                                {
                                    l2.setHideLabels(l.isHideLabels());
                                }
                            }
                        }
                        if (isNew) {
                            _linkDefs.add(l);
                            NodeDef nodeDef1New = l.getNodeDef1();
                            for (NodeDef n : target.getNodeDefs()) {
                                if (n.getUuid().equals(nodeDef1New.getUuid())) {
                                    l.setNodeDef1(n);
                                }
                            }
                            NodeDef nodeDef2New = l.getNodeDef2();
                            for (NodeDef n : target.getNodeDefs()) {
                                if (n.getUuid().equals(nodeDef2New.getUuid())) {
                                    l.setNodeDef2(n);

                                }
                            }
                        }
                    }
                    target.setLinkDefs(_linkDefs);
                }
                {
                    if (copyFrom.getBundleDefs() != null) {
                        List<BundleDef> _bundelDefs = Lists.newArrayList();
                        List<BundleDef> bundleDefsNew = copyFrom.getBundleDefs();
                        List<BundleDef> bundleDefsOld = target.getBundleDefs();
                        for (BundleDef b : bundleDefsNew) {
                            boolean isNew = true;
                            for (BundleDef b2 : bundleDefsOld) {
                                if (b2.getUuid().equals(b.getUuid())) {
                                    isNew = false;
                                    _bundelDefs.add(b2);
                                    b2.setClientProperties(b.getClientProperties());
                                    List<BundleOp> _operations = Lists.newArrayList();
                                    List<BundleOp> _operationsNew = b.getOperations();
                                    List<BundleOp> _operationsOld = b2.getOperations();
                                    for (BundleOp bo : _operationsNew) {
                                        boolean _isNew = true;
                                        for (BundleOp bo2 : _operationsOld) {
                                            if (bo2.getUuid().equals(bo.getUuid())) {
                                                _isNew = false;
                                                _operations.add(bo2);
                                                bo2.setClientProperties(bo.getClientProperties());
                                                FieldDef _field = bo.getField();
                                                bo2.setField(_field);
                                                if (_field != null) {
                                                    for (FieldDef f : goodFieldDefs) {
                                                        if (f.getUuid().equals(_field.getUuid())) {
                                                            bo2.setField(f);
                                                        }
                                                    }
                                                } else {
                                                    bo2.setField(null);
                                                }
                                                if (bo.getNodeDef() != null) {
                                                    for (NodeDef n : target.getNodeDefs()) {
                                                        if (bo.getNodeDef().getUuid().equals(n.getUuid())) {
                                                            bo2.setNodeDef(n);
                                                        }
                                                    }
                                                } else {
                                                    bo2.setNodeDef(null);
                                                }
                                                bo2.setPriority(bo.getPriority());
                                                break;
                                            }
                                        }
                                        if (_isNew) {
                                            _operations.add(bo);
                                            FieldDef _field = bo.getField();
                                            if (_field != null) {
                                                for (FieldDef f : goodFieldDefs) {
                                                    if (f.getUuid().equals(_field.getUuid())) {
                                                        bo.setField(f);
                                                    }
                                                }
                                            } else {
                                                bo.setField(null);
                                            }
                                            if (bo.getNodeDef() != null) {
                                                for (NodeDef n : target.getNodeDefs()) {
                                                    if (bo.getNodeDef().getUuid().equals(n.getUuid())) {
                                                        bo.setNodeDef(n);
                                                    }
                                                }
                                            } else {
                                                bo.setNodeDef(null);
                                            }
                                        }
                                    }

                                    b2.setOperations(_operations);
                                    break;
                                }
                            }
                            if (isNew) {
                                _bundelDefs.add(b);
                                for (BundleOp bo : b.getOperations()) {
                                    FieldDef _field = bo.getField();
                                    if (_field != null) {
                                        for (FieldDef f : goodFieldDefs) {
                                            if (f.getUuid().equals(_field.getUuid())) {
                                                bo.setField(f);
                                            }
                                        }
                                    } else {
                                        bo.setField(null);
                                    }
                                    if (bo.getNodeDef() != null) {
                                        for (NodeDef n : target.getNodeDefs()) {
                                            if (bo.getNodeDef().getUuid().equals(n.getUuid())) {
                                                bo.setNodeDef(n);
                                            }
                                        }
                                    } else {
                                        bo.setNodeDef(null);
                                    }
                                }
                            }
                        }
                        target.setBundleDefs(_bundelDefs);
                    }
                }
            }
            relGraphViewDef = target;
        }
    }

    private Set<AttributeDef> mergeAttributeDefs(Set<AttributeDef> attributeDefsNew,
            Set<AttributeDef> attributeDefsOld) {
        List<FieldDef> goodFieldDefs = FieldDefUtils.getAllSortedFields(graphSettings.getDataViewDef().getModelDef(),
                SortOrder.ALPHABETIC);
        Set<AttributeDef> attributeDefs = Sets.newHashSet();

        for (AttributeDef a : attributeDefsNew) {
            boolean _isNew = true;
            for (AttributeDef a2 : attributeDefsOld) {
                if (a.getUuid().equals(a2.getUuid())) {
                    _isNew = false;
                    attributeDefs.add(a2);
                    a2.setAggregateFunction(a.getAggregateFunction());
                    a2.setBySize(a.getBySize());
                    a2.setByStatic(a.getByStatic());
                    a2.setClientProperties(a.getClientProperties());

                    {
                        FieldDef _fieldDef = a.getFieldDef();
                        a2.setFieldDef(_fieldDef);
                        if (_fieldDef != null) {
                            for (FieldDef f : goodFieldDefs) {
                                if (_fieldDef.getUuid().equals(f.getUuid())) {
                                    a2.setFieldDef(f);
                                    break;
                                }
                            }
                        }
                    }
                    a2.setHideEmptyInTooltip(a.isHideEmptyInTooltip());
                    a2.setIncludeInTooltip(a.isIncludeInTooltip());
                    a2.setKind(a.getKind());
                    a2.setName(a.getName());
                    a2.setReferenceName(a.getReferenceName());
                    {
                        FieldDef _fieldDef = a.getTooltipLinkFeildDef();
                        if(_fieldDef == null) {
                            a2.setTooltipLinkFeildDef(null);
                        } else {
                            if(a2.getTooltipLinkFeildDef() == null || !_fieldDef.equals(a2.getTooltipLinkFeildDef())) {
                                a2.setTooltipLinkFeildDef(_fieldDef);
                            } else {
                                //Bit of a hack, we assume this is static here and create a new one to avoid problems of
                                // CEN-4068, not fully sure why this is a problem though.
                                a2.setTooltipLinkFeildDef(new FieldDef());
                                a2.getTooltipLinkFeildDef().setFieldName(_fieldDef.getFieldName());
                                a2.getTooltipLinkFeildDef().setStaticText(_fieldDef.getStaticText());
                                a2.getTooltipLinkFeildDef().setFieldType(_fieldDef.getFieldType());
                            }
                            
                            for (FieldDef f : goodFieldDefs) {
                                if (_fieldDef.getUuid().equals(f.getUuid())) {
                                    a2.setTooltipLinkFeildDef(f);
                                    break;
                                }
                            }
                        }
                    }
                    a2.setTooltipLinkText(a.getTooltipLinkText());
                    a2.setTooltipLinkType(a.getTooltipLinkType());
                    a2.setTooltipOrdinal(a.getTooltipOrdinal());
                    break;
                }
            }
            if (_isNew) {
                attributeDefs.add(a);
            }
        }
        return attributeDefs;
    }

    private GraphSettings getGraphSettings() {
        return graphSettings;
    }

    @Override
    public String getValue() {
        return getTitle();
    }

    @Override
    public void setValue(String value) {

    }

    public String getPropertyValue(String value) {
        return relGraphViewDef.getSettings().getPropertiesMap().get(value);
    }

    public void setThemeId(String uuid) {
        relGraphViewDef.setThemeUuid(uuid);
    }
}
