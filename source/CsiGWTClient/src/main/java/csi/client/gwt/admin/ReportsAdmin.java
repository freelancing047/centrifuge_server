package csi.client.gwt.admin;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ListenerWrapper;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sun.xml.bind.v2.model.annotation.Quick;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.ui.GridInfo;
import csi.server.common.dto.EventsDisplay;
import csi.server.common.dto.ReportsDisplay;
import csi.server.common.dto.Response;
import csi.server.common.service.api.UserAdministrationServiceProtocol;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

public class ReportsAdmin {

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private ReportsTab _tab;
    private SharedItems _shared;

    private ReportsInfo _reportsInfo;
    private EventsInfo _eventsInfo;

    private Grid<ReportsDisplay> _reportsGrid;
    private Grid<EventsDisplay> _eventsGrid;
    private Map<Date, ReportsDisplay> _reportsMap;
    private Map<String, EventsDisplay> _eventsMap;

    private boolean _requestSent = false;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected VortexEventHandler<Response<String, Integer>> handleMaxConcurrencyResponse
            = new AbstractVortexEventHandler<Response<String, Integer>>() {

        @Override
        public boolean onError(Throwable t) {
            Dialog.showException(t);
            return true;
        }

        @Override
        public void onSuccess(Response<String, Integer> result) {
            if(ResponseHandler.isSuccess(result)) {
                _tab.maxConcurrencyLabel.setText("360 Day Max Concurrency: " + result.getResult());
            }
        }
    };

    protected VortexEventHandler<Response<String, List<EventsDisplay>>> handleEventInfoResponse
            = new AbstractVortexEventHandler<Response<String, List<EventsDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {
            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<EventsDisplay>> responseIn) {

            if(ResponseHandler.isSuccess(responseIn)) {
                List<EventsDisplay> myNewList = responseIn.getResult();
                ListStore<EventsDisplay> myGridStore = (ListStore<EventsDisplay>)_eventsGrid.getStore();

                myGridStore.clear();
                myGridStore.addAll(myNewList);
                if(null != _eventsMap) {
                    _eventsMap.clear();
                }

                for(EventsDisplay myEvent : myNewList) {
                    _eventsMap.put(myEvent.getUserId(), myEvent);
                }
                _eventsGrid.getView().refresh(false);
            }
        }
    };

    protected VortexEventHandler<Response<String, List<ReportsDisplay>>> handleReportInfoResponse
            = new AbstractVortexEventHandler<Response<String, List<ReportsDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<ReportsDisplay>> responseIn) {

            if(ResponseHandler.isSuccess(responseIn)) {
                List<ReportsDisplay> myNewList = responseIn.getResult();
                ListStore<ReportsDisplay> myGridStore = (ListStore<ReportsDisplay>)_reportsGrid.getStore();

                myGridStore.clear();
                myGridStore.addAll(myNewList);
                if(null != _reportsMap) {
                    _reportsMap.clear();
                }

                for(ReportsDisplay myReport : myNewList) {
                    _reportsMap.put(myReport.getDate(), myReport);
                }
                _reportsGrid.getView().refresh(false);
                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {
                        int offsetHeight = 25 * myNewList.size();
                        _tab.bottomContainer.getElement().getStyle().setTop(offsetHeight , Style.Unit.PX);
                        _tab.eventsContainer.getElement().getStyle().setTop(offsetHeight + 15, Style.Unit.PX);
                        return false;
                    }
                }, 500);
            }
        }
    };

    protected VortexEventHandler<Response<String, List<ReportsDisplay>>> handleReportRefreshResponse
            = new AbstractVortexEventHandler<Response<String, List<ReportsDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<ReportsDisplay>> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {
                ListStore<ReportsDisplay> myGridStore = (ListStore<ReportsDisplay>)_reportsGrid.getStore();
                List<ReportsDisplay> myOldList = myGridStore.getAll();
                List<ReportsDisplay> myNewList = responseIn.getResult();

                myGridStore.clear();
                _reportsMap.clear();

                for (ReportsDisplay myReport : myNewList) {
                    _reportsMap.put(myReport.getDate(), myReport);
                }

                for (ReportsDisplay myReport : myOldList) {
                    if(!_reportsMap.containsKey(myReport.getDate())) {
                        myGridStore.add(myReport);
                    }
                }
                _reportsGrid.getView().refresh(false);
            }
        }
    };

    protected VortexEventHandler<Response<String, List<EventsDisplay>>> handleEventRefreshResponse
            = new AbstractVortexEventHandler<Response<String, List<EventsDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<EventsDisplay>> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {
                ListStore<EventsDisplay> myGridStore = (ListStore<EventsDisplay>)_eventsGrid.getStore();
                List<EventsDisplay> myOldList = myGridStore.getAll();
                List<EventsDisplay> myNewList = responseIn.getResult();

                myGridStore.clear();
                _reportsMap.clear();

                for(EventsDisplay myEvent : myNewList) {
                    _eventsMap.put(myEvent.getUserId(), myEvent);
                }

                for (EventsDisplay myEvent : myOldList) {
                    if(!_eventsMap.containsKey(myEvent.getUserId())) {
                        myGridStore.add(myEvent);
                    }
                }
                _reportsGrid.getView().refresh(false);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ReportsAdmin(ReportsTab tabIn, SharedItems sharedIn) {
        _reportsMap = new HashMap<Date, ReportsDisplay>();
        _eventsMap = new HashMap<String, EventsDisplay>();

        _tab = tabIn;
        _shared = sharedIn;

        _reportsInfo = new ReportsInfo(_shared.doCapco());
        _eventsInfo = new EventsInfo(_shared.doCapco());
        _reportsGrid = _reportsInfo.createGrid();
        _reportsGrid.getView().setAutoExpandMax(150);
        _eventsGrid = _eventsInfo.createGrid();
        _eventsGrid.getView().setAutoExpandMax(150);
        _tab.reportsContainer.setGrid(_reportsGrid);
        _tab.reportsContainer.setWidth("600px");
        _tab.eventsContainer.setGrid(_eventsGrid);
        _tab.eventsContainer.setWidth("400px");

        requestData();
    }

    void requestData() {
        _requestSent = true;

        try {

            //Get the Usage Reports
            VortexFuture<Response<String, List<ReportsDisplay>>> vortexFutureReports =
                    WebMain.injector.getVortex().createFuture();
            vortexFutureReports.execute(UserAdministrationServiceProtocol.class).retrieveReports();
            vortexFutureReports.addEventHandler(handleReportInfoResponse);

            //Get the Events Reports
            VortexFuture<Response<String, List<EventsDisplay>>> vortexFutureEvents =
                    WebMain.injector.getVortex().createFuture();
            vortexFutureEvents.execute(UserAdministrationServiceProtocol.class).retrieveEvents();
            vortexFutureEvents.addEventHandler(handleEventInfoResponse);

            //Get the Max Concurrency in the last 360 Days
            VortexFuture<Response<String, Integer>> vortexFutureInfo =
                    WebMain.injector.getVortex().createFuture();
            vortexFutureInfo.execute(UserAdministrationServiceProtocol.class).retrieveMaxConcurrencyInformation();
            vortexFutureInfo.addEventHandler(handleMaxConcurrencyResponse);

        } catch (Exception myExcception) {
            Dialog.showException(myExcception);
        }
    }

    public void refreshReports() {

        //Retrieve New Reports
        List<String> myList = new ArrayList<String>();

        //Request Data from the server

        try{
            VortexFuture<Response<String, List<ReportsDisplay>>> vortexFuture =
                    WebMain.injector.getVortex().createFuture();
            vortexFuture.execute(UserAdministrationServiceProtocol.class).retrieveReports();
            vortexFuture.addEventHandler(handleReportRefreshResponse);


        } catch (Exception myException) {
            Dialog.showException(myException);
        }
    }
}
