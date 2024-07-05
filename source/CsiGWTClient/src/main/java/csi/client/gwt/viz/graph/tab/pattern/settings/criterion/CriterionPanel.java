package csi.client.gwt.viz.graph.tab.pattern.settings.criterion;

import java.util.Map;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings;
import csi.shared.gwt.viz.graph.tab.pattern.settings.DirectionLinkPatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.FieldDefNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.LabelNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.LinkPatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NeighborNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.OccurrencePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;
import csi.shared.gwt.viz.graph.tab.pattern.settings.TypeNodePatternCriterion;

public class CriterionPanel extends Composite {
    private final FluidRow valueRow;
    Map<PatternCriterion, PatternCriterionWidget> myMap;
    private PatternSettings patternSettings;
    private HasPatternCriteria item;
    private PatternCriterion criterion;
    private ComboBox listBox;
    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public CriterionPanel(PatternSettings patternSettings, HasPatternCriteria item, PatternCriterion criterion) {
        this.patternSettings = patternSettings;
        this.item = item;
        this.criterion = criterion;
        FluidContainer container = new FluidContainer();
        container.setHeight("400px");

        FluidRow backRow = initBackRow();
        container.add(backRow);
        FluidRow nameRow = initNameRow();
        container.add(nameRow);
        FluidRow showInResultsRow = initShowInResultsRow();
        container.add(showInResultsRow);
        FluidRow typeRow = initTypeRow();
        container.add(typeRow);
        valueRow = initValueRow();
        container.add(valueRow);
        initWidget(container);
        {
//            listBox.setValue(criterion.getType(), true);//FIXME:i18n
            listBox.setValue(criterion, true);
            valueRow.clear();
            PatternCriterionWidget criterionWidget = null;
            for (Map.Entry<PatternCriterion, PatternCriterionWidget> entrySet : myMap.entrySet()) {
                if (entrySet.getKey().getType().equals(criterion.getType())) {//FIXME:i18n
                    criterionWidget = entrySet.getValue();
                }
            }
            if (criterionWidget != null) {
                initCriteriaWidget(criterionWidget);
            }
        }
    }

    private FluidRow initShowInResultsRow() {
        FluidRow row = new FluidRow();
        final CheckBox checkBox = new CheckBox(i18n.criterionPanel_checkbox_showInResults());
        checkBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                criterion.setShowInResults(checkBox.getValue());
            }
        });
        checkBox.setValue(criterion.isShowInResults());
        Column column = new Column(8, 2);
        row.add(column);
        column.add(checkBox);
        return row;

    }

    private FluidRow initValueRow() {
        //TODO: allow this to be different for each type
        FluidRow row = new FluidRow();
        row.getElement().getStyle().setPaddingTop(11, Unit.PX);
        return row;
    }

    private FluidRow initTypeRow() {
        FluidRow row = new FluidRow();
        InlineLabel label = new InlineLabel(i18n.criterionPanel_label_Type());
        label.getElement().getStyle().setPaddingRight(15.0D, Unit.PX);
        row.add(label);
        listBox = new ComboBox<>(new ComboBoxCell<>(
                new ListStore<>(new ModelKeyProvider<PatternCriterion>() {

                    @Override
                    public String getKey(PatternCriterion item) {
                        if (item.getType() == null) {
                            return "";
                        }
                        return item.getType();
                    }
                }), new LabelProvider<PatternCriterion>() {

            @Override
            public String getLabel(PatternCriterion item) {
                if (item.getType() == null) {
                    return "";
                }
                switch (item.getType()) {
                    case "Field Value":
                        return i18n.patternCriterionType_FIELD_VALUE();
                    case "Label":
                        return i18n.patternCriterionType_LABEL();
                    case "Number of Neighbors":
                        return i18n.patternCriterionType_NUMBER_OF_NEIGHBORS();
                    case "Type":
                        return i18n.patternCriterionType_TYPE();
                    case "Direction":
                        return i18n.patternCriterionType_DIRECTION();
                    case "Occurrence":
                        return i18n.patternCriterionType_OCCURRENCE();
                    case "":
                        return "";
                }
                return "";
            }
        }
        ));

        {
            myMap = Maps.newHashMap();
            myMap.put(new TypeNodePatternCriterion(), new TypePatternCriterionWidget());
            myMap.put(new LabelNodePatternCriterion(), new LabelPatternCriterionWidget());
            myMap.put(new NeighborNodePatternCriterion(), new NeighborPatternCriterionWidget());
            myMap.put(new OccurrencePatternCriterion(), new OccurrencePatternCriterionWidget());
            myMap.put(new FieldDefNodePatternCriterion(), new FieldDefPatternCriterionWidget());
            myMap.put(new DirectionLinkPatternCriterion(), new DirectionPatternCriterionWidget());
        }

        for (PatternCriterion s : myMap.keySet()) {
                if (item instanceof PatternLink) {
                    if (s instanceof LinkPatternCriterion) {
                        listBox.getStore().add(s);//FIXME:i18n
                    }
                } else if (item instanceof PatternNode) {
                    if (s instanceof NodePatternCriterion) {
                        listBox.getStore().add(s);//FIXME:i18n
                    }
                }
            }

        listBox.addSelectionHandler(new SelectionHandler<PatternCriterion>() {
            public void onSelection(SelectionEvent<PatternCriterion> event) {
                PatternCriterion value = event.getSelectedItem();
                valueRow.clear();
                for (PatternCriterion patternCriterion : myMap.keySet()) {
                    if (patternCriterion.getType().equals(value.getType())) {//FIXME:i18n
                        initCriteriaWidget(myMap.get(patternCriterion));
                    }
                }
            }
        });
        row.add(listBox);
        return row;
    }

    private void initCriteriaWidget(PatternCriterionWidget criterionWidget) {
        if (item instanceof PatternLink) {
            if (criterionWidget instanceof LinkPatternCriterionWidget) {
                LinkPatternCriterionWidget widget = (LinkPatternCriterionWidget) criterionWidget;
                PatternLink patternLink = (PatternLink) item;
                widget.setPatternLink(patternLink);
            }
        }
        item.removeCriterion(criterion);
        criterionWidget.setCriterion(criterion);
        criterion = criterionWidget.getCriterion();
        item.addCriterion(criterion);
        valueRow.add(criterionWidget);
    }

    private FluidRow initNameRow() {
        FluidRow row = new FluidRow();
        InlineLabel label = new InlineLabel(i18n.criterionPanel_label_Name());
        label.getElement().getStyle().setPaddingRight(10.0D, Unit.PX);
        row.add(label);
        final TextBox box = new TextBox();
        box.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                criterion.setName(box.getValue());
            }
        });
        box.setValue(criterion.getName());
        row.add(box);
        return row;
    }

    private FluidRow initBackRow() {
        FluidRow row = new FluidRow();
        Button backButton = initBackButton();
        Button removeButton = initRemoveButton();
        row.add(backButton);
        row.add(removeButton);
        return row;
    }

    private Button initRemoveButton() {
        Button button = new Button("");
        button.setIcon(IconType.TRASH);
        button.setType(ButtonType.LINK);
        {
            Style style = button.getElement().getStyle();
            style.setFloat(Style.Float.RIGHT);
            style.setFontSize(25.0D, Unit.PX);
            style.setTextDecoration(TextDecoration.NONE);
        }
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                patternSettings.removeCriterion(item, criterion);
            }
        });
        return button;
    }

    private Button initBackButton() {
        Button button = new Button();
        button.setIcon(IconType.CIRCLE_ARROW_LEFT);
        button.setType(ButtonType.LINK);
        button.setSize(ButtonSize.LARGE);
        button.getElement().getStyle().setFontSize(25.0D, Unit.PX);
        button.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                patternSettings.editCriteria(item);
            }
        });
        return button;
    }
}
