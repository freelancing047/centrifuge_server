package csi.client.gwt.util;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceChangeRequestEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

// See Activity Manager
// This class adds a mechanism for directly setting the currentActvity
public class CSIActivityManager extends ActivityManager {

    private class ProtectedDisplay implements AcceptsOneWidget {

        private final Activity activity;


        ProtectedDisplay(Activity activity) {
            this.activity = activity;
        }


        public void setWidget(IsWidget view) {
            if (this.activity == CSIActivityManager.this.currentActivity) {
                startingNext = false;
                showWidget(view);
            }
        }
    }

    private static final Activity NULL_ACTIVITY = new AbstractActivity() {

        public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
        }
    };

    private final ActivityMapper mapper;
    private final EventBus eventBus;
    private final ResettableEventBus stopperedEventBus;
    private Activity currentActivity = NULL_ACTIVITY;
    private AcceptsOneWidget display;
    private boolean startingNext = false;
    private HandlerRegistration handlerRegistration;


    public CSIActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(null, null);// I don't really need the super class
        this.mapper = mapper;
        this.eventBus = eventBus;
        this.stopperedEventBus = new ResettableEventBus(eventBus);
    }


    @Override
    public EventBus getActiveEventBus() {
        return stopperedEventBus;
    }


    public void setActivity(Activity nextActivity) {
        Throwable caughtOnStop = null;
        Throwable caughtOnCancel = null;
        Throwable caughtOnStart = null;
        if (nextActivity == null) {
            nextActivity = NULL_ACTIVITY;
        }
        if (currentActivity.equals(nextActivity)) {
            return;
        }
        if (startingNext) {
            caughtOnCancel = tryStopOrCancel(false);
            currentActivity = NULL_ACTIVITY;
            startingNext = false;
        } else if (!currentActivity.equals(NULL_ACTIVITY)) {
            showWidget(null);
            stopperedEventBus.removeHandlers();
            caughtOnStop = tryStopOrCancel(true);
        }
        currentActivity = nextActivity;
        if (currentActivity.equals(NULL_ACTIVITY)) {
            showWidget(null);
        } else {
            startingNext = true;
            caughtOnStart = tryStart();
        }
        if (caughtOnStart != null || caughtOnCancel != null || caughtOnStop != null) {
            Set<Throwable> causes = new LinkedHashSet<Throwable>();
            if (caughtOnStop != null) {
                causes.add(caughtOnStop);
            }
            if (caughtOnCancel != null) {
                causes.add(caughtOnCancel);
            }
            if (caughtOnStart != null) {
                causes.add(caughtOnStart);
            }
            throw new UmbrellaException(causes);
        }
    }


    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        setActivity(getNextActivity(event));
    }


    @Override
    public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
        event.setWarning(currentActivity.mayStop());
    }


    @Override
    public void setDisplay(AcceptsOneWidget display) {
        boolean wasActive = (null != this.display);
        boolean willBeActive = (null != display);
        this.display = display;
        if (wasActive != willBeActive) {
            updateHandlers(willBeActive);
        }
    }


    private Activity getNextActivity(PlaceChangeEvent event) {
        // if (display == null) {
        // return null;
        // }
        return mapper.getActivity(event.getNewPlace());
    }


    private void showWidget(IsWidget view) {
        if (display != null) {
            display.setWidget(view);
        }
    }


    private Throwable tryStart() {
        Throwable caughtOnStart = null;
        try {
            currentActivity.start(new ProtectedDisplay(currentActivity), stopperedEventBus);
        } catch (Throwable t) {
            caughtOnStart = t;
        }
        return caughtOnStart;
    }


    private Throwable tryStopOrCancel(boolean stop) {
        Throwable caughtOnStop = null;
        try {
            if (stop) {
                currentActivity.onStop();
            } else {
                currentActivity.onCancel();
            }
        } catch (Throwable t) {
            caughtOnStop = t;
        } finally {
            stopperedEventBus.removeHandlers();
        }
        return caughtOnStop;
    }


    private void updateHandlers(boolean activate) {
        if (activate) {
            final HandlerRegistration placeReg = eventBus.addHandler(PlaceChangeEvent.TYPE, this);
            final HandlerRegistration placeRequestReg = eventBus.addHandler(PlaceChangeRequestEvent.TYPE, this);
            this.handlerRegistration = new HandlerRegistration() {

                public void removeHandler() {
                    placeReg.removeHandler();
                    placeRequestReg.removeHandler();
                }
            };
        } else {
            if (handlerRegistration != null) {
                handlerRegistration.removeHandler();
                handlerRegistration = null;
            }
        }
    }
}
