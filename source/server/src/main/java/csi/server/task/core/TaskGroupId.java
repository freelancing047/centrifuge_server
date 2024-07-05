package csi.server.task.core;

import java.util.Arrays;

/**
 * Represents an id for a group of tasks. The group id of the task is represented by the logical path of the request.
 * For example, if request came from graph, the logical path is dataView, worksheet, graph visualization.
 *
 * @author cristina.nuna
 */
public class TaskGroupId {

    private String[] pathIds;


    public TaskGroupId() {
    }


    public TaskGroupId(String[] pathIds) {
        this.pathIds = pathIds;
    }


    /**
     * Builds a TaskGroupId object.
     * @param fullPath  Must be a string representing multiple words separated by "_", example: dataview1_worksheet2_table1
     */
    public TaskGroupId(String fullPath) {
        if (fullPath != null) {
            pathIds = fullPath.split("_");
        }
    }


    /**
     * Tells if the current instance has the same path as the TaskGroupId given as parameter.
     * The current instance has the path shortest or equal to the path of the taskGroupId given as parameter.
     * This corresponds to a case when current object is for example a dataView and the included object is a visualization.
     * @param taskGroupId object to perform the matching on.
     * @return true if current instance path matches the one given as parameter.
     */
    public boolean includes(TaskGroupId taskGroupId) {
        if (taskGroupId == null) {
            return false;
        }
        String[] includedPath = taskGroupId.pathIds;
        if (includedPath == null) {
            return false;
        }
        if (includedPath.length < this.pathIds.length) {
            return false;
        }
        for (int i = 0; i < this.pathIds.length; i++) {
            if (!(pathIds[i].equals(includedPath[i]))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Tells if the current instance path is included in the TaskGroupId parameter path.
     * The current instance has the path longer or equal to the path of the taskGroupId given as parameter.
     * This corresponds to a case when current object is for example a visualization and the including object (taskGroupId) is a dataView.
     * @param taskGroupId object to perform the matching on. This object is tested if includes the current instance.
     * @return true if current instance path matches the one given as parameter.
     */
    public boolean isIncluded(TaskGroupId taskGroupId) {
        if (taskGroupId == null) {
            return false;
        }
        String[] includingPath = taskGroupId.pathIds;
        if (includingPath == null) {
            return false;
        }
        if (this.pathIds.length < includingPath.length) {
            return false;
        }
        for (int i = 0; i < includingPath.length; i++) {
            if (!(pathIds[i].equals(includingPath[i]))) {
                return false;
            }
        }
        return true;
    }


    @Override
    public String toString() {
        return "TaskGroupId{" + "pathIds=" + (pathIds == null ? null : Arrays.asList(pathIds)) + '}';
    }
}
