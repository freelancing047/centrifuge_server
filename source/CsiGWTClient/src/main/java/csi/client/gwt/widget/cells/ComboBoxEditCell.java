package csi.client.gwt.widget.cells;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ListView;

import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;


public class ComboBoxEditCell<T> extends ComboBoxCell<T> {
    
    protected GridCellAssist<T> _gridCellAssist = null;
    protected String _formerQuery = null;
    protected Integer _formerRow = null;
    protected Integer _formerColumn = null;
    protected FieldDefComboBox _parent = null;
    
    public ComboBoxEditCell(ListStore<T> store, LabelProvider<? super T> labelProvider) {
        super(store, labelProvider);
    }

    public ComboBoxEditCell(ListStore<T> store, LabelProvider<? super T> labelProvider, ListView<T, ?> view) {
        super(store, labelProvider, view);
    }

    public ComboBoxEditCell(ListStore<T> store, LabelProvider<? super T> labelProvider, ListView<T, ?> view,
            TriggerFieldAppearance appearance) {
        super(store, labelProvider, view, appearance);
    }

    public ComboBoxEditCell(ListStore<T> store, LabelProvider<? super T> labelProvider, final SafeHtmlRenderer<T> renderer) {
        this(store, labelProvider, renderer, GWT.<TriggerFieldAppearance> create(TriggerFieldAppearance.class));
    }

    public ComboBoxEditCell(ListStore<T> store, LabelProvider<? super T> labelProvider, final SafeHtmlRenderer<T> renderer,
            TriggerFieldAppearance appearance) {
        super(store, labelProvider, renderer, appearance);
    }

    public ComboBoxEditCell(ListStore<T> store, LabelProvider<? super T> labelProvider, TriggerFieldAppearance appearance) {
        super(store, labelProvider, appearance);
    }

    public ComboBoxEditCell(ListStore<T> store, LabelProvider<? super T> labelProvider, final SafeHtmlRenderer<T> renderer,
            GridCellAssist<T> gridCellAssistIn) {
        super(store, labelProvider, renderer, new ModifiableTriggerFieldDefaultAppearance(gridCellAssistIn));
        _gridCellAssist = gridCellAssistIn;
    }
/*
    @Override
    public void doQuery(Context context, XElement parent, ValueUpdater<T> updater, T value, String query, boolean force) {
        
        _formerQuery = lastQuery;
        super.doQuery(context, parent, updater, value, query, force);
    }
*/
    @Override
    public void onBrowserEvent(Context contextIn, Element parentIn, T valueIn, NativeEvent eventIn, ValueUpdater<T> valueUpdaterIn) {

        super.onBrowserEvent(contextIn, parentIn, valueIn, eventIn, valueUpdaterIn);
        if ((null != _gridCellAssist) && (null != contextIn)) {
            String eventType = eventIn.getType();
            if ("click".equals(eventType)) {
                _formerRow = contextIn.getIndex();
                _formerColumn = contextIn.getColumn();
            }
        }
    }

    @Override
    public void finishEditing(Element parent, T value, Object key, ValueUpdater<T> valueUpdater) {
      super.finishEditing(parent, value, key, valueUpdater);
      if ((null != _gridCellAssist) && (null != _formerRow) && (null != _formerColumn)
              && (null != lastQuery) && (0 < lastQuery.length())) {
          
          _gridCellAssist.reportTextChange(lastQuery, _formerRow, _formerColumn);
        }
    }

    @Override
    protected void onKeyDown(Context contextIn, Element parentIn, T valueIn, NativeEvent eventIn, ValueUpdater<T> valueUpdaterIn) {

        if ((null != _gridCellAssist) && (null != contextIn)) {
            _formerRow = contextIn.getIndex();
            _formerColumn = contextIn.getColumn();
        }
      super.onKeyDown(contextIn, parentIn, valueIn, eventIn, valueUpdaterIn);
    }

    @Override
    protected void onKeyUp(Context contextIn, Element parentIn, T valueIn, NativeEvent eventIn, ValueUpdater<T> valueUpdaterIn) {

        if ((null != _gridCellAssist) && (null != contextIn)) {
            _formerRow = contextIn.getIndex();
            _formerColumn = contextIn.getColumn();
        }
      super.onKeyUp(contextIn, parentIn, valueIn, eventIn, valueUpdaterIn);
    }

    @Override
    public void render(Context contextIn, T valueIn, SafeHtmlBuilder htmlBuilderIn) {
      String myValue = "";

      if (valueIn != null) {
          myValue = getPropertyEditor().render(valueIn);
      }

      FieldViewData myViewData = checkViewData(contextIn, myValue);
      String myString = (null != myViewData) ? myViewData.getCurrentValue() : myValue;

      FieldAppearanceOptions myOptions = new FieldAppearanceOptions(width, height, isReadOnly());
      myOptions.setName(name);
      myOptions.setEmptyText(getEmptyText());
      myOptions.setHideTrigger(isHideTrigger());
      myOptions.setDisabled(isDisabled());
      myOptions.setEditable(isEditable());

      if (null != _gridCellAssist) {
          ((ModifiableTriggerFieldDefaultAppearance)getAppearance()).render(contextIn,
                  htmlBuilderIn, (null == myString) ? "" : myString, myOptions);
      } else {
            getAppearance().render(htmlBuilderIn, (null == myString) ? "" : myString, myOptions);
      }
    }

    public String getSelectionText() {
        return lastQuery;
   }
}
