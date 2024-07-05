package csi.client.gwt.viz.timeline.scheduler;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import csi.client.gwt.viz.timeline.events.TrackingCompleteEvent;
import csi.client.gwt.viz.timeline.model.DetailedEventProxy;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.TimelineTrackModel;
import csi.client.gwt.viz.timeline.presenters.DetailedTimelinePresenter;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.view.DetailedTimelineView;
import csi.client.gwt.viz.timeline.view.TimelineView;
import csi.client.gwt.viz.timeline.view.drawing.TrackRenderable;
import csi.shared.core.visualization.timeline.CommonTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeferredLayoutCommand implements Scheduler.RepeatingCommand {

    private static final int TRACK_DEFERRAL_MIN_TIME = 30000;

    private static final int RIGHT_BUFFER = 10;
    private static final int LABEL_BUFFER = RIGHT_BUFFER + 25;
    private static final int MAX_COLLISIONS = 2000;
    int largestY = 0;
    private boolean interrupt = false;
    private List<DetailedEventProxy> events;
    private TimelineTrackModel track;
    private TimelineView view;
    private double top = 0;
    private double currentTrackingHeight = 0;
    private TimelinePresenter presenter;
    private List<String> orderedTrackList;
    private Map<String, TimelineTrackModel> trackModels;
    private EventBus eventBus;
    private double collapsedHeight;
    private double lastTrackTime = 0;
    private boolean beginningOfTrack = true;

    public DeferredLayoutCommand(DetailedTimelineView detailedTimelineView) {
        this.view = detailedTimelineView;
    }
    private static int gcd(int a, int b) {
        int mod = a % b;
        if (a % b == 0)
            return b;
        return gcd(b, mod);
    }

    private static int nearAndRelPrime(int target, int modulus) {
        if (target < 2)
            return 1;
        while (gcd(modulus, target) > 1)
            target--;
        return target;
    }

    @Override
    public boolean execute() {
        int index = orderedTrackList.indexOf(track.getLabel());
//        Console.log("DeferredLayoutCommand.execute() index: " + index);
        while (true) {
            if (processTrack() == 2) {
                    lastTrackTime = Duration.currentTimeMillis();
                return true;
            }
            index++;
            if (index < orderedTrackList.size()) {
//                Console.log("DeferredLayoutCommand.execute() nextTrack");
                nextTrack(orderedTrackList.get(index));
                if (Duration.currentTimeMillis() - lastTrackTime > TRACK_DEFERRAL_MIN_TIME) {
                    lastTrackTime = Duration.currentTimeMillis();
                    return true;
                }
            } else {
                if (interrupt) {
//                    Console.log("DeferredLayoutCommand.execute() cancelled");
                } else {
//                    Console.log("DeferredLayoutCommand.execute() done");
                    eventBus.fireEvent(new TrackingCompleteEvent(track));
                }
                return false;
            }
        }
    }

    private int processTrack() {
        while (true) {
            List<DetailedEventProxy> collisions = new ArrayList<>();

            currentTrackingHeight = calculateSafeHeight();

            int startY = ((int) (top)); //+ TimelineTrackModel.CELL_HEIGHT;
            int endY = ((int) (top + currentTrackingHeight));// + TimelineTrackModel.CELL_HEIGHT;
            if (beginningOfTrack) {
                startY = ((int) (top) + TimelineTrackModel.CELL_HEIGHT);
                if (track.hasSummary() && track.isGroupSpace()) {
                    startY = startY + TrackRenderable.FULL_SUMMARY_HEIGHT;
                }
                beginningOfTrack = false;
            }

            //We want this floored for consistency
            int numCells = ((endY - startY) / TimelineTrackModel.CELL_HEIGHT);
            //Something bad happened here with height, so we exit
            if (numCells < 1) {
                return 1;
            }


            //This holds every event that is the right-most event on a given y position
            DetailedEventProxy[] rights = new DetailedEventProxy[numCells];

            int largestCell = 0;
            DetailedEventProxy lastEvent;
            for (DetailedEventProxy event : events) {
                if (collisions.size() > MAX_COLLISIONS) {
                    collisions.add(event);
                    continue;
                }
                if (!event.isVisible() || event.getStartTime() == null)
                    continue;

                checkCancelled();
                if (event.getEvent().getLabel() != null) {
                    event.setSpaceToRight(LABEL_BUFFER);
                } else {
                    event.setSpaceToRight(RIGHT_BUFFER);
                }
                TimeScale timeScale = presenter.getTimeScale();
                double num = timeScale.toNum(event.getStartTime());
                event.setRight(event.getStartTime());
                int x = (int) num;

                //Assume y position of 0
                int cell = 0;

                //Set x, y of event
                int y = cell * TimelineTrackModel.CELL_HEIGHT + startY;
                event.setX(x);
                event.setY(y);
                if (event.getEndTime() != null) {
                    double endNum = (int) timeScale.toNum(event.getEndTime());
                    event.setEndX((int) endNum);
                    event.setRight(event.getEndTime());
                }

                //We detect if the new x,y are in collision with previous event
                //While there is a collision
                boolean collision = true;
                while (cell < numCells) {
                    if (rights[cell] != null) {
                        if (rights[cell].getEndX() + rights[cell].getSpaceToRight() < x) {
                            int space = x - rights[cell].getX();
                            rights[cell].setSpaceToRight(space);
                            rights[cell].setRight(timeScale.toTime(rights[cell].getX() + space));
                            event.setY(rights[cell].getY());
                            y = event.getY();
                            rights[cell] = event;
                            collision = false;
                            break;
                        } else {
                            cell++;
                        }
                    } else {
                        event.setY(cell * TimelineTrackModel.CELL_HEIGHT + startY);
                        y = event.getY();
                        rights[cell] = event;
                        collision = false;
                        break;
                    }


                }

                if (collision) {
                    collisions.add(event);
                } else {
                    if (cell > largestCell) {
                        largestCell = cell;
                    }

                    if (event.getY() > largestY) {
                        largestY = event.getY();
                    }
                }
            }


            //If we reserved too much vertical space, we trim it here
            trimHeightIfNecessary(largestCell, numCells, largestY);

            checkCancelled();
            top = top + currentTrackingHeight;
            events = collisions;
            if (collisions.isEmpty()) {
                break;
            } else {
                if (Duration.currentTimeMillis() - lastTrackTime > TRACK_DEFERRAL_MIN_TIME) {
                    return 2;
                }
            }
        }
//        Console.log("DeferredLayoutCommand.processTrack() collision isEmpty");

        int height = calculateSafeHeight();
        //Finalize this track
        if (track.hasSummary() && track.isGroupSpace()) {
            //currentTrackingHeight += (double)TrackRenderable.FULL_SUMMARY_HEIGHT;
            collapsedHeight = (double) TrackRenderable.FULL_SUMMARY_HEIGHT;
        } else {
            //currentTrackingHeight += (double)TrackRenderable.EMPTY_SUMMARY_HEIGHT;
            collapsedHeight = (double) TrackRenderable.EMPTY_SUMMARY_HEIGHT;
        }
        track.setCollapsedHeight(collapsedHeight);
        track.setHeight(top + currentTrackingHeight - track.getTop());


        if (track.isCollapsed()) {
            track.setEndY((int) (height * (track.getTop() + track.getCollapsedHeight())));
        } else {
            track.setEndY((int) (height * (track.getTop() + track.getHeight())));
        }
        return 0;
    }

    private int calculateSafeHeight() {
        int offsetHeight = view.getOffsetHeight();
        int remainder = offsetHeight % TimelineTrackModel.CELL_HEIGHT;
        return offsetHeight - remainder;
    }

    private void checkCancelled() {
        if (interrupt) {
//            Console.log("DeferredLayoutCommand.checkCancelled() cancelled");
            events.clear();
            orderedTrackList.clear();
            trackModels.clear();
        }
    }

    private void trimHeightIfNecessary(int largestCell, int numCells, int largestY) {
        largestCell++;
        if (largestCell < numCells) {

            //(numCells - largestCell) * TimelineTrackModel.CELL_HEIGHT;

            int pixelGapSize = TimelineTrackModel.CELL_HEIGHT * (numCells - largestCell);
            currentTrackingHeight = (currentTrackingHeight - pixelGapSize);

        }
    }

    private void nextTrack(String name) {
        beginningOfTrack = true;
        TimelineTrackModel oldTrack = track;
        track = trackModels.get(name);
        track.setShadow(!oldTrack.isShadow());
        events = track.getEvents();
        if (oldTrack.isCollapsed()) {
            top = oldTrack.getTop() + oldTrack.getCollapsedHeight();
        } else {
            top = oldTrack.getTop() + oldTrack.getHeight();
        }
        currentTrackingHeight = calculateSafeHeight();
        track.setTop(top);
        events = DetailedTimelinePresenter.sortEvents(events);
        track.setStartY(((int) (top)));
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public List<DetailedEventProxy> getEvents() {
        return events;
    }

    public void setEvents(List<DetailedEventProxy> events) {
        this.events = events;
    }

    public TimelineTrackModel getTrack() {
        return track;
    }

    public void setTrack(TimelineTrackModel track) {
        this.track = track;
    }

    public TimelinePresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(TimelinePresenter presenter) {
        this.presenter = presenter;
    }

    public List<String> getOrderedTrackList() {
        return orderedTrackList;
    }

    public void setOrderedTrackList(List<String> orderedTrackList) {
        this.orderedTrackList = orderedTrackList;
    }

    public Map<String, TimelineTrackModel> getTrackModels() {
        return trackModels;
    }

    public void setTrackModels(HashMap<String, CommonTrack> hashMap) {

        this.trackModels = new HashMap<>();
        for (Map.Entry<String, CommonTrack> entry : hashMap.entrySet()) {
            trackModels.put(entry.getKey(), (TimelineTrackModel) entry.getValue());
        }

    }


    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }


}
