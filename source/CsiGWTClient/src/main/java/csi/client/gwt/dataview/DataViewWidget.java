package csi.client.gwt.dataview;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import csi.client.gwt.mainapp.CsiDisplay;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.client.gwt.worksheet.tab.WorksheetTabPanel;
import csi.client.gwt.worksheet.tab.events.*;
import csi.shared.core.color.ClientColorHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the centerpiece of the Analysis view. It supports all worksheet
 * interactions such as adding, removing, and renaming. This is called a DataViewWidget because the standard naming
 * convention would yield DataViewView!
 *
 */
public class DataViewWidget extends FullSizeLayoutPanel implements CsiDisplay {

    private DataViewPresenter dataViewPresenter;
    private WorksheetTabPanel tabPanel;
    private Map<Widget, WorksheetPresenter> presentersByView = new HashMap<Widget, WorksheetPresenter>();
    private SelectionHandler<Widget> selectionHandler;
    private String textShadowValue = "rgba(250, 250, 250, .3) 1px 0px 0px, rgba(250, 250, 250, .3) -01px 0px 0px, " +
            "rgba(250, 250, 250, .3) 0px 1px 0px, rgba(250, 250, 250, .3) 0px -1px 0px, " +
            "rgba(250, 250, 250, .3) 1px 1px 0px, rgba(250, 250, 250, .3) -1px -1px 0px, " +
            "rgba(250, 250, 250, .3) 1px -1px 0px, rgba(250, 250, 250, .3) -1px 1px 0px";


    boolean delay = true;

    private HashMap<String, CancellableScheduleCommand> resizeHandlers = new HashMap<>();

//    private CancellableScheduleCommand cmd;

    public DataViewWidget(DataViewPresenter dataViewPresenterIn) {
        dataViewPresenter = dataViewPresenterIn;
        setWidth("100%"); //$NON-NLS-1$
        setHeight("100%"); //$NON-NLS-1$
        if (!dataViewPresenter.isBlocked()) {

            tabPanel = new WorksheetTabPanel();
            createSelectionHandler();
            addHandlers();
            add(tabPanel);
            colorWorksheet(tabPanel.getAddTabWidget(), "F5F5F5");
        }
    }

    public void saveState() {}

    public void restoreState() {}

    public void forceExit() {}

    private void createSelectionHandler() {
        selectionHandler = new SelectionHandler<Widget>() {

            @Override
            public void onSelection(SelectionEvent<Widget> event) {

                final WorksheetPresenter worksheetPresenter = presentersByView.get(event.getSelectedItem());

//                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
//                    @Override
//                    public void execute() {
                        if(dataViewPresenter == null || dataViewPresenter.getActiveWorksheet() == null || !worksheetPresenter.getUuid().equalsIgnoreCase(dataViewPresenter.getActiveWorksheet().getUuid()))
                        {
                            dataViewPresenter.setActiveWorksheet(worksheetPresenter);
                            for (Visualization visualization : worksheetPresenter.getVisualizations()) {
                                final Visualization vizfin = visualization;

                                CancellableScheduleCommand cmd = resizeHandlers.get(vizfin.getUuid());

                                if(cmd != null) {
                                    cmd.cancel();
                                }
                                cmd = new CancellableScheduleCommand() {
                                    private boolean cancel = false;
                                    
                                    @Override
                                    public void cancel() {
                                        cancel = true;
                                    }
                                    
                                    @Override
                                    public void execute() {
                                        if(!cancel)
                                            vizfin.getChrome().onResize();
                                        
                                    }
                                };

                                resizeHandlers.put(vizfin.getUuid(), cmd);

                                Scheduler.get().scheduleFinally(cmd);

                            }
                        }
            }
        };
    }

    private void addHandlers() {


        addSelectionHandlerDelayed();

        tabPanel.addTabDragHandler(new TabDragEventHandler() {

            @Override
            public void onDrag(int newIndex, int oldIndex) {
                dataViewPresenter.reOrderWorksheet(newIndex, oldIndex);
            }
        });
        tabPanel.addDeleteHandler(new TabDeleteEventHandler() {

            @Override
            public void onDelete(Widget tabContentWidget) {
                dataViewPresenter.deleteWorksheet(presentersByView.get(tabContentWidget));
            }
        });
        tabPanel.addRenameHandler(new TabRenameEventHandler() {

            @Override
            public void onRename(Widget tabContentWidget) {
                dataViewPresenter.renameWorksheet(presentersByView.get(tabContentWidget));
            }
        });

        tabPanel.addColorHandler(new TabColorEventHandler() {
            @Override
            public void onColor(Widget tabContentWidget) {
                double tabBottom = tabPanel.getCurrentTab(tabContentWidget).getAbsoluteBottom();
                double tabLeft = tabPanel.getCurrentTab(tabContentWidget).getAbsoluteLeft();
                double windowRight = dataViewPresenter.getView().getElement().getAbsoluteRight();
                dataViewPresenter.colorWorksheet(presentersByView.get(tabContentWidget), tabBottom, tabLeft, windowRight);
            }
        });

        tabPanel.addCreateHandler(new TabCreateEventHandler() {

            @Override
            public void onCreate() {
                dataViewPresenter.createWorksheet();
            }
        });

        tabPanel.addPublishHandler(new TabPublishEventHandler() {

            @Override
            public void onPublish(Widget tabContentWidget) {
                dataViewPresenter.publishWorksheet();
            }
        });
    }

    private void addSelectionHandlerDelayed() {

        tabPanel.addSelectionHandler(selectionHandler);
    }

    public void addWorksheet(final WorksheetPresenter worksheetPresenter) {
        presentersByView.put(worksheetPresenter.getView().asWidget(), worksheetPresenter);
        TabItemConfig config = new TabItemConfig(worksheetPresenter.getName());
        config.setClosable(false);
        String worksheetName = worksheetPresenter.getName() != null ? worksheetPresenter.getName() : ""; //$NON-NLS-1$
        config.setText(worksheetName);
        int widgetCount = tabPanel.getWidgetCount();
        tabPanel.insert(worksheetPresenter.getView().asWidget(), widgetCount - 1, config);
        colorWorksheet(worksheetPresenter.getView().asWidget(), "FFFFFF");
        Integer worksheetColor = null;
        if (worksheetPresenter.getColor() != null) {
            worksheetColor = worksheetPresenter.getColor();
            Widget widget = worksheetPresenter.getView().asWidget();
            colorWorksheet(widget, worksheetColor);
        }

    }

    public void renameWorksheet(WorksheetPresenter worksheetPresenter) {
        tabPanel.update(worksheetPresenter.getView().asWidget(), new TabItemConfig(worksheetPresenter.getName(), false));

    }

    public void colorWorksheet(Widget widget, Integer color) {
        XElement currentTab = tabPanel.getCurrentTab(widget);
        NodeList<Element> aElements = currentTab.getElementsByTagName("a");
        Element a = aElements.getItem(1);
//        String bgColorAsString = ClientColorHelper.get().make(color).toString().toUpperCase();
//        a.getStyle().setProperty("background", bgColorAsString);
//        a.getStyle().setProperty("borderRadius", "6px 6px 0px 0px");
        a.getStyle().setPadding(0, Style.Unit.PX);
        NodeList<Element> emElements = currentTab.getElementsByTagName("em");
        Element em = emElements.getItem(0);
//        em.getStyle().setProperty("background", bgColorAsString);
        em.getStyle().setPadding(0, Style.Unit.PX);
        NodeList<Element> spanElements = em.getElementsByTagName("span");
        Element backgroundSpan = spanElements.getItem(1);
        backgroundSpan.addClassName("colored-worksheet");
//        if (isDefault) {
//            backgroundSpan.addClassName("default-worksheet-tab");
//        } else {
//            backgroundSpan.removeClassName("default-worksheet-tab");
//        }
//        backgroundSpan.getStyle().setProperty("background", bgColorAsString);
        styleWorksheet(backgroundSpan);
        backgroundSpan.getStyle().setBackgroundColor(ClientColorHelper.get().make(color).toString().toUpperCase());
        if (ClientColorHelper.get().make(color).getLuma() >= .45) {
            backgroundSpan.getStyle().setColor("rgba(20,20,20,.7");
//            backgroundSpan.getStyle().setProperty("textShadow","rgba(250, 250, 250, 0.2) 1px 0px 0px, rgba(250, 250, 250, 0.2) -1px 0px 0px, rgba(250, 250, 250, 0.2) 0px 1px 0px, rgba(250, 250, 250, 0.2) 0px -1px 0px, rgba(250, 250, 250, 0.2) 1px 1px 0px, rgba(250, 250, 250, 0.2) -1px -1px 0px, rgba(250, 250, 250, 0.2) 1px -1px 0px, rgba(250, 250, 250, 0.2) -1px 1px 0px");
            backgroundSpan.getStyle().setProperty("textShadow","none");

        }
        if (ClientColorHelper.get().make(color).getLuma() < .45) {
            backgroundSpan.getStyle().setColor("rgba(255,255,255.7)");
//            backgroundSpan.getStyle().setProperty("textShadow","rgba(0, 0, 0, 0.2) 1px 0px 0px, rgba(0, 0, 0, 0.2) -1px 0px 0px, rgba(0, 0, 0, 0.2) 0px 1px 0px, rgba(0, 0, 0, 0.2) 0px -1px 0px, rgba(0, 0, 0, 0.2) 1px 1px 0px, rgba(0, 0, 0, 0.2) -1px -1px 0px, rgba(0, 0, 0, 0.2) 1px -1px 0px, rgba(0, 0, 0, 0.2) -1px 1px 0px");
            backgroundSpan.getStyle().setProperty("textShadow","none");
        }
    }

    public void colorWorksheet(Widget widget, String hexColor) {
        colorWorksheet(widget, ClientColorHelper.get().makeFromHex(hexColor).getIntColor());
    }

    public void styleWorksheet(Element tabElement) {
        tabElement.getStyle().setPaddingLeft(10.0, Style.Unit.PX);
        tabElement.getStyle().setPaddingRight(10.0, Style.Unit.PX);
        tabElement.getStyle().setProperty("textShadow", textShadowValue);
        tabElement.getStyle().setProperty("borderRadius", "6px 6px 0px 0px");
        tabElement.getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        tabElement.getStyle().setBorderWidth(1.0, Style.Unit.PX);
        tabElement.getStyle().setProperty("borderTopColor" , "#d0d0d0");
        tabElement.getStyle().setProperty("borderBottomColor", "#d0d0d0");
        tabElement.getStyle().setProperty("borderLeftColor", "#d0d0d0");
        tabElement.getStyle().setProperty("borderRightColor", "#d0d0d0");
        tabElement.getStyle().setColor("#222");
    }

    public void deleteWorksheet(WorksheetPresenter presenter) {
        tabPanel.remove(presenter.getView().asWidget());
    }

    public void setActiveWorksheet(WorksheetPresenter worksheetPresenter) {
        Widget worksheetWidget = worksheetPresenter.getView().asWidget();
        tabPanel.setActiveWidget(worksheetWidget);
    }

    public HandlerRegistration addSelectionHandler(SelectionHandler selectionHandler) {
        return tabPanel.addSelectionHandler(selectionHandler);
    }

    public void setReadOnly() {

        if (null != tabPanel) {

            tabPanel.setReadOnly();

            DeferredCommand.add(new Command() {
                public void execute() {
                    tabPanel.onResize();
                }
            });
        }
    }

    public void disableWorksheet(WorksheetPresenter worksheetPresenter) {
        tabPanel.getConfig(worksheetPresenter.getView().asWidget()).setEnabled(false);
    }

    public void enableWorksheet(WorksheetPresenter worksheetPresenter) {
        tabPanel.getConfig(worksheetPresenter.getView().asWidget()).setEnabled(true);

    }
}
