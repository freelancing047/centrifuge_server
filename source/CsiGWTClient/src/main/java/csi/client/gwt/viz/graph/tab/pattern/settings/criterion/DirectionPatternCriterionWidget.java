package csi.client.gwt.viz.graph.tab.pattern.settings.criterion;

import java.util.Map;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.gwt.viz.graph.LinkDirection;
import csi.shared.gwt.viz.graph.tab.pattern.settings.DirectionLinkPatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;

public class DirectionPatternCriterionWidget extends Composite implements LinkPatternCriterionWidget {

    private final FluidContainer fc;
    DirectionLinkPatternCriterion criterion;
    Map<NavLink, LinkDirection> navLinkLinkDirectionMap = Maps.newHashMap();
    private PatternLink link;
    private CheckBox bidirectionalCB;
    private CheckBox forwardCB;
    private CheckBox reverseCB;
    private CheckBox undirectedCB;


    public DirectionPatternCriterionWidget() {
        fc = new FluidContainer();

        addCheckboxes();
        FluidRow row = new FluidRow();
        InlineLabel label = new InlineLabel(CentrifugeConstantsLocator.get().directionCriteria_valueLabel());
        label.getElement().getStyle().setPaddingRight(10.0D, Style.Unit.PX);
        row.add(label);

        final DropdownButton direction = new DropdownButton(CentrifugeConstantsLocator.get().directionCriteria_directionLabel());
        for (LinkDirection linkDirection : LinkDirection.values()) {
            NavLink widget = new NavLink(linkDirection.toString());
            navLinkLinkDirectionMap.put(widget, linkDirection);
            direction.add(widget);
        }
        direction.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                NavLink lastSelectedNavLink = direction.getLastSelectedNavLink();
                LinkDirection linkDirection = navLinkLinkDirectionMap.get(lastSelectedNavLink);
                criterion.setValue(linkDirection.toString());
            }
        });
        row.add(direction);
        initWidget(fc);
    }

    private void addCheckboxes() {
        addUndirectedCheckbox();
        addForwardCheckbox();
        addReverseCheckbox();
        addBidirectionalCheckbox();
    }

    private void addUndirectedCheckbox() {
        undirectedCB = makeCheckbox(CentrifugeConstantsLocator.get().directionCriteria_directionOption_undirected());
        undirectedCB.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                criterion.setUndirected(undirectedCB.getValue());
            }
        });
    }

    private void addReverseCheckbox() {
        reverseCB = makeCheckbox(CentrifugeConstantsLocator.get().directionCriteria_directionOption_reverse());
        reverseCB.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                criterion.setReverse(reverseCB.getValue());
            }
        });
    }

    private void addForwardCheckbox() {
        forwardCB = makeCheckbox(CentrifugeConstantsLocator.get().directionCriteria_directionOption_forward());
        forwardCB.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                criterion.setForward(forwardCB.getValue());
            }
        });
    }

    private void addBidirectionalCheckbox() {
        bidirectionalCB = makeCheckbox(CentrifugeConstantsLocator.get().directionCriteria_directionOption_bidirectional());
        bidirectionalCB.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                criterion.setBidirectional(bidirectionalCB.getValue());
            }
        });
    }

    private CheckBox makeCheckbox(String bidirectional) {
        CheckBox checkBox = new CheckBox(bidirectional);
        FluidRow row = new FluidRow();
        row.add(checkBox);
        fc.add(row);
        return checkBox;
    }

    @Override
    public PatternCriterion getCriterion() {
        return criterion;
    }

    @Override
    public void setCriterion(PatternCriterion criterion) {
        if (criterion instanceof DirectionLinkPatternCriterion) {
            this.criterion = (DirectionLinkPatternCriterion) criterion;
            forwardCB.setValue(this.criterion.isForward());
            reverseCB.setValue(this.criterion.isReverse());
            undirectedCB.setValue(this.criterion.isUndirected());
            bidirectionalCB.setValue(this.criterion.isBidirectional());
        } else {
            this.criterion = new DirectionLinkPatternCriterion();
            this.criterion.setShowInResults(criterion.isShowInResults());
        }
        this.criterion.setName(criterion.getName());
    }

    @Override
    public void setPatternLink(PatternLink link) {
        this.link = link;
        updateCheckboxLabels();
    }

    private void updateCheckboxLabels() {
        String node1Name = link.getNode1().getName();
        String node2Name = link.getNode2().getName();
        undirectedCB.setText(node1Name + " -- " + node2Name);
        forwardCB.setText(node1Name + " -->" + node2Name);
        reverseCB.setText(node1Name + "<-- " + node2Name);
        bidirectionalCB.setText(node1Name + "<-->" + node2Name);
    }
}
