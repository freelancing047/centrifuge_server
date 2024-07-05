package csi.client.gwt.mainapp;

import com.emitrom.lienzo.client.core.util.Console;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.common.collect.Maps;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.BrowserEvents;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.columns.CsiTextColumn;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.service.api.DataViewActionServiceProtocol;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class RecentDataviewsGrid extends Composite {

    private static final int _nameMax = 10000;
    // Create a Cell_table.
    private CellTable<ResourceBasics> _table = new CellTable<ResourceBasics>();

    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private boolean disableClick = false;
    private Map<String, Long> recentAnnotationMap = Maps.newHashMap();
    private AnnotationBadge annotationBadge;

    interface Templates extends SafeHtmlTemplates {

        @Template("<div class=\"graph-field-row\">{0}</div>")
        SafeHtml cell(SafeHtml value);
    }
    private static RecentDataviewsGrid.Templates templates = GWT.create(RecentDataviewsGrid.Templates.class);


    public RecentDataviewsGrid() {
        try {
            // Add a text column to show the address.
            CsiTextColumn<ResourceBasics> myNameColumn = new CsiTextColumn<ResourceBasics>() {

                @Override
                public String getValue(ResourceBasics object) {

                    try {

                         String myName = object.getName();

                        return (null != myName) ? myName : "";

                    } catch (Exception myException) {

                        Dialog.showException("RecentDataviewsGrid", 1, myException);
                    }
                    return null;
                }

                @Override
                public void render(Cell.Context context, ResourceBasics object, SafeHtmlBuilder sb) {
                    Long newAnnotationCount = recentAnnotationMap.get(object.getUuid());
                    if (newAnnotationCount == null) {
                        ApplicationToolbarLocator.getInstance().setNewMessageAlertVisible(false);
                        super.render(context, object, sb);
                    } else {
                        ApplicationToolbarLocator.getInstance().setNewMessageAlertVisible(true);
                        annotationBadge = new AnnotationBadge(newAnnotationCount.toString());
                        SafeHtml badgeHtml = new SafeHtml() {
                            @Override
                            public String asString() {
                                String safeHtml = "";
                                safeHtml = object.getName();
                                safeHtml += annotationBadge.getElement().getString();
                                return safeHtml;
                            }
                        };

                        SafeHtml annotationBadgeHtml = templates.cell(badgeHtml);
                        sb.append(annotationBadgeHtml);
                    }
                }
            };
            myNameColumn.setCellStyleNames("name-column");

            _table.addColumn(myNameColumn, _constants.name());

            TextColumn<ResourceBasics> myDateColumn = new TextColumn<ResourceBasics>() {

                @Override
                public String getValue(ResourceBasics object) {

                    try {

                        Date myTimeStamp = object.getLastAccess();

                        return (null != myTimeStamp)
                                ? DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(myTimeStamp)
                                : "";

                    } catch (Exception ignore) {

                    }
                    return null;
                }
            };
            _table.addColumn(myDateColumn, _constants.recentDataviewsGrid_lastOpened());

            TextColumn<ResourceBasics> myCountColumn = new TextColumn<ResourceBasics>() {

                @Override
                public String getValue(ResourceBasics object) {

                    try {

                        return Long.toString(object.getSize());

                    } catch (Exception myException) {

                        Dialog.showException("RecentDataviewsGrid", 2, myException);
                    }
                    return null;
                }
            };
            _table.addColumn(myCountColumn, _constants.sharingDialogs_SharingColumn_3());

            _table.setBordered(true);
            _table.setStriped(true);
            _table.setCondensed(true);
            _table.setHover(true);
            _table.setColumnWidth(myNameColumn, 60, Style.Unit.PCT );
            _table.setColumnWidth(myDateColumn, 20, Style.Unit.PCT);
            _table.setColumnWidth(myCountColumn, 20, Style.Unit.PCT );
            InlineLabel lbl = new InlineLabel();
            lbl.setText("No DataViews in your recent access list. Click the Open tab to access other DataViews.");

            _table.setEmptyTableWidget(lbl);

            _table.addCellPreviewHandler(new Handler<ResourceBasics>() {

                @Override
                public void onCellPreview(CellPreviewEvent<ResourceBasics> event) {

                    try {

                        int rowId = event.getIndex();
                        Element rowElement = _table.getRowElement(rowId);

                        switch (BrowserEvents.getValue(event.getNativeEvent().getType()))
                        {
                            case CLICK :
                                if(!disableClick){
                                    disableClick = true;
                                    WebMain.injector.getMainPresenter().beginOpenDataView(event.getValue());
                                    Scheduler.get().scheduleDeferred(new ScheduledCommand(){

                                        @Override
                                        public void execute() {
                                            disableClick = false;
                                        }});
                                }
                                break;

                            case MOUSEOVER :

                                rowElement.getStyle().setColor(Dialog.txtInfoColor);
                                rowElement.getStyle().setCursor(Cursor.POINTER);
                                break;

                            case MOUSEOUT :

                                rowElement.getStyle().setColor(Dialog.txtLabelColor);
                                break;

                            default :

                                break;
                        }

                    } catch (Exception myException) {

                        Dialog.showException("RecentDataviewsGrid", 4, myException);
                    }
                }
            });

            reloadData();

            initWidget(_table);

        } catch (Exception myException) {

            Dialog.showException("RecentDataviewsGrid", 5, myException);
        }
    }

    public void reloadData() {

        try {

            _table.setRowCount(0);
            _table.setHeight("60px");
            _table.setWidth("100%",true);
            VortexFuture<List<ResourceBasics>> future = WebMain.injector.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<List<ResourceBasics>>() {

                @Override
                public void onSuccess(List<ResourceBasics> result) {

                    try {

                        // Push the data into the widget.
                        _table.setRowData(0, result);
                        _table.setLoadingIndicator(null);
                        _table.redraw();

                        for (ResourceBasics resourceBasic: result) {
                            VortexFuture<Long> future2 = WebMain.injector.getVortex().createFuture();
                            future2.execute(DataViewActionServiceProtocol.class).getNewAnnotationCount(resourceBasic.getUuid(), resourceBasic.getLastAccess());
                            future2.addEventHandler(new AbstractVortexEventHandler<Long>() {
                                @Override
                                public void onSuccess(Long longResult) {
                                    if (longResult > 0) {
                                        recentAnnotationMap.put(resourceBasic.getUuid(), longResult);
                                    } else {
                                        recentAnnotationMap.remove(resourceBasic.getUuid());
                                    }
                                    _table.redraw();
                                }
                            });
                        }



                    } catch (Exception myException) {

                        Dialog.showException("RecentDataviewsGrid", 6, myException);
                    }
                }
            });

            future.execute(DataViewActionServiceProtocol.class).getRecentDataViews(20);

        } catch (Exception myException) {

            Dialog.showException("RecentDataviewsGrid", 7, myException);
        }
    }
    
    public CellTable<ResourceBasics> getTable() {
        
        return _table;
    }
}
