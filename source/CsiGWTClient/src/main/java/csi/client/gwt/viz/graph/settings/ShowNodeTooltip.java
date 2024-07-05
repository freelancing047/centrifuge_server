package csi.client.gwt.viz.graph.settings;

import java.util.List;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Panel;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings.View;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.attribute.AttributeKind;

class ShowNodeTooltip implements GraphSettings.Presenter {

    private GraphSettings graphSettings;
    private NodeProxy nodeProxy;

    public ShowNodeTooltip(NodeProxy nodeProxy, GraphSettings graphSettings) {
        this.nodeProxy = nodeProxy;
        this.graphSettings = graphSettings;
    }

    public void showDetails(NodeProxy nodeProxy) {
        View view = graphSettings.getView();
        Panel detailsContainer = view.getDetailsContainer(nodeProxy);
        view.showDetails();
        Column detailColumn = new Column(12);
        String rowHTML = "";
        if (nodeProxy.hasName()) {
            rowHTML = "<div class=\"detail-label\">" + CentrifugeConstantsLocator.get().graphSettings_nodeTooltip_nodeName() + "</div>";//NON-NLS
            rowHTML += nodeProxy.getName();
            detailColumn.add(new FluidRow(rowHTML));
        }
        if (nodeProxy.hasLabel()) {
            rowHTML = "<div class=\"detail-label\">" + CentrifugeConstantsLocator.get().label() + "</div>";//NON-NLS
            // Perhaps this logic should move within nodeProxy
            if (nodeProxy.isLabelAnonymous()) {
                rowHTML += "\"" + nodeProxy.getLabel() + "\"";
            } else {
                // Are you serious?
                rowHTML += nodeProxy.getLabelFieldName();
            }
            detailColumn.add(new FluidRow(rowHTML));
        }
        // do I always have a Type?
        rowHTML = "<div class=\"detail-label\">" + CentrifugeConstantsLocator.get().type() + "</div>";//NON-NLS
        if (nodeProxy.hasType()) {
            if (nodeProxy.getFieldType() == FieldType.STATIC) {
                rowHTML += "\"" + nodeProxy.getType() + "\"";
            } else {
                rowHTML += nodeProxy.getFieldName();
            }
        } else {
            if (!Strings.isNullOrEmpty(nodeProxy.getLabel()) && nodeProxy.isLabelAnonymous()) {
                rowHTML += "\"" + nodeProxy.getLabel() + "\" " + CentrifugeConstantsLocator.get().graphSettings_nodeTooltip_fromLabelPostfix();
            } else if (!Strings.isNullOrEmpty(nodeProxy.getLabel())) {
                rowHTML += "\"" + nodeProxy.getFieldName() + "\" " + CentrifugeConstantsLocator.get().graphSettings_nodeTooltip_fromLabelPostfix();
            } else if (nodeProxy.hasID()) {
                if (nodeProxy.isIDAnonymous()) {
                    rowHTML += "\"" + nodeProxy.getID() + "\" " + CentrifugeConstantsLocator.get().graphSettings_nodeTooltip_fromIdPostfix();
                } else {
                    rowHTML += "\"" + nodeProxy.getIDfieldName() + "\" " + CentrifugeConstantsLocator.get().graphSettings_nodeTooltip_fromIdPostfix();
                }
            }
        }
        detailColumn.add(new FluidRow(rowHTML));
        // FIXME: could use some serious improvement
        Set<AttributeDef> tooltipFields = nodeProxy.getAttributeDefs();
        //TODO: this list can probably be static
        List<String> blackList = Lists.newArrayList("csi.internal.Type", "csi.internal.Label", "csi.internal.xPos",//NON-NLS
                "csi.internal.yPos", "csi.internal.Shape", "csi.internal.Color", ObjectAttributes.CSI_INTERNAL_SCALE, ObjectAttributes.CSI_INTERNAL_SIZE, ObjectAttributes.CSI_INTERNAL_SIZE_FUNCTION,//NON-NLS
                "csi.internal.Icon", ObjectAttributes.CSI_INTERNAL_TRANSPARENCY, ObjectAttributes.CSI_INTERNAL_TRANSPARENCY_FUNCTION, ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE, ObjectAttributes.CSI_INTERNAL_SHAPE_OVERRIDE, ObjectAttributes.CSI_INTERNAL_ICON_OVERRIDE);//NON-NLS
        for (AttributeDef attributeDef : tooltipFields) {
            if (!blackList.contains(attributeDef.getName())) {
                if (attributeDef.getName().indexOf("csi.internal.") == 0) {//NON-NLS
                	String attribute = attributeDef.getName().substring("csi.internal.".length());
                	if(attribute.equals("ID")){
                		attribute = CentrifugeConstantsLocator.get().tooltipLabel_ID();
                	}
                    rowHTML = "<div class=\"detail-label\">"//NON-NLS
                            + attribute + "</div>";//NON-NLS
                } else {
                    rowHTML = "<div class=\"detail-label\">" + attributeDef.getName() + "</div>";//NON-NLS
                }
                if (attributeDef.getKind() == AttributeKind.REFERENCE) {
                    rowHTML += "refers to \"" + attributeDef.getReferenceName() + "\"";//NON-NLS
                } else if (attributeDef.getFieldDef() != null) {
                    if ((attributeDef.getFieldDef().getFieldType() == FieldType.STATIC) || attributeDef.getFieldDef().isAnonymous()) {
                        rowHTML += "\"" + attributeDef.getFieldDef().getStaticText() + "\"";
                    } else {
                        rowHTML += attributeDef.getFieldDef().getFieldName();
                    }
                }
                detailColumn.add(new FluidRow(rowHTML));
            }
        }
        detailsContainer.add(detailColumn);
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        showDetails(nodeProxy);
        // graphSettings.show();// return to state of rest
    }
}
