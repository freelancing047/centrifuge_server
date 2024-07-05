package csi.server.task.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the TaskGroupId's methods.
 *
 * @author cristina.nuna
 */
public class TaskGroupIdTest {

    @Test
    public void testIncludesSuccessFlow() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1", "dataView1", "visualization1"});
        boolean includes = dataviewGroup.includes(visualizationGroup);
        assertTrue(includes);
    }

    @Test
    public void testIncludesNullTaskGroupId() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        boolean includes = dataviewGroup.includes(null);
        assertFalse(includes);
    }

    @Test
    public void testIncludesSamePathLength() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        boolean includes = dataviewGroup.includes(visualizationGroup);
        assertTrue(includes);
    }

    @Test
    public void testIncludesSmallerPathLength() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1"});
        boolean includes = dataviewGroup.includes(visualizationGroup);
        assertFalse(includes);
    }

    @Test
    public void testIncludesDifferentPaths() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1", "dataView2"});
        boolean includes = dataviewGroup.includes(visualizationGroup);
        assertFalse(includes);
    }

    @Test
    public void testIncludesSameStringsDifferentOrder() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"dataView1", "clientId1"});
        boolean includes = dataviewGroup.includes(visualizationGroup);
        assertFalse(includes);
    }

    @Test
    public void testIncludesEmptyGroupId(){
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{});
        assertFalse(dataviewGroup.includes(visualizationGroup));
        assertFalse(visualizationGroup.isIncluded(dataviewGroup));
    }

    @Test
    public void testIsIncludedSuccessFlow() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1", "dataView1", "visualization1"});
        boolean isIncluded = visualizationGroup.isIncluded(dataviewGroup);
        assertTrue(isIncluded);
    }

    @Test
    public void testIsIncludedInNullTaskGroupId() {
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        boolean isIncluded = visualizationGroup.isIncluded(null);
        assertFalse(isIncluded);
    }

    @Test
    public void testIsIncludedSamePathLength() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        boolean isIncluded = visualizationGroup.isIncluded(dataviewGroup);
        assertTrue(isIncluded);
    }

    @Test
    public void testIsIncludedSmallerPathLength() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1"});
        boolean isIncluded = visualizationGroup.isIncluded(dataviewGroup);
        assertFalse(isIncluded);
    }

    @Test
    public void testIsIncludedDifferentPaths() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1", "dataView2"});
        boolean isIncluded = visualizationGroup.isIncluded(dataviewGroup);
        assertFalse(isIncluded);
    }

    @Test
    public void testIsIncludedSameStringsDifferentOrder() {
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"dataView1", "clientId1"});
        boolean isIncluded = visualizationGroup.isIncluded(dataviewGroup);
        assertFalse(isIncluded);
    }

    @Test
    public void testIsIncludedVsIncludes(){
        TaskGroupId dataviewGroup = new TaskGroupId(new String[]{"clientId1", "dataView1"});
        TaskGroupId visualizationGroup = new TaskGroupId(new String[]{"clientId1", "dataView1", "visualization1", "task1"});
        assertTrue(dataviewGroup.includes(visualizationGroup));
        assertFalse(visualizationGroup.includes(dataviewGroup));
        assertTrue(visualizationGroup.isIncluded(dataviewGroup));
        assertFalse(dataviewGroup.isIncluded(visualizationGroup));
    }

    @Test
    public void testToString(){
        TaskGroupId groupId = new TaskGroupId(new String[]{"clientId1", "dataView1", "visualization1", "task1"});
        assertEquals("TaskGroupId{pathIds=[clientId1, dataView1, visualization1, task1]}", groupId.toString());
    }
}
