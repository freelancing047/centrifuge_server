package csi.server.common.codec.xstream.converter;

import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import csi.server.task.api.TaskStatus;
import csi.server.task.api.TaskStatusCode;

public class TaskStatusConverter implements Converter {

    protected Mapper _mapper;

    public TaskStatusConverter(Mapper mapper) {
        _mapper = mapper;
    }

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        TaskStatus status = (TaskStatus) o;

        Class type = null;

        writeTag(writer, "taskStatus", status.getTaskStatus().toString());
        writeTag(writer, "taskId", status.getTaskId());
        writeTag(writer, "sessionId", status.getSessionId());

        writeTag(writer, "errorMessage", status.getErrorMessage());
        writeTag(writer, "errorDetail", status.getErrorDetail());

        // result data is already xml don't marshal it, just embed it.
        writer.startNode("resultData");

        if (status.getResultData() != null) {
            type = status.getResultData().getClass();
            String tag = _mapper.serializedClass(type);
            writer.startNode(tag);
            marshallingContext.convertAnother(status.getResultData());
            writer.endNode();
        } else {
            String tag = _mapper.serializedClass(String.class);
            writer.startNode(tag);
            writer.setValue("");
            writer.endNode();
        }
        // }

        // TODO: remove the need for this...flex should be using the TaskStatusCode
        if (status.getTaskStatus() == TaskStatusCode.TASK_STATUS_COMPLETE) {
            writeTag(writer, "operationStatus", "CENTRIFUGE_SUCCESS");
        } else {
            writeTag(writer, "operationStatus", "CENTRIFUGE_FAILURE");
        }

        writer.endNode();

        writeTag(writer, "progressLabel", status.getProgressLabel());

        writer.startNode("progress");
        marshallingContext.convertAnother(status.getProgress());
        writer.endNode();

        Map<String, Object> feedbackData = status.getFeedbackData();
        if (feedbackData != null) {
            writer.startNode("feedback");

            type = status.getFeedbackData().getClass();
            String tag = _mapper.serializedClass(type);
            writer.startNode(tag);
            marshallingContext.convertAnother(status.getFeedbackData());
            writer.endNode();

            writer.endNode();
        }

        if (status.getCreatedAt() != null) {
            writer.startNode("createdAt");
            marshallingContext.convertAnother(status.getCreatedAt());
            writer.endNode();
        }

        if (status.getLastUpdate() != null) {
            writer.startNode("lastUpdate");
            marshallingContext.convertAnother(status.getLastUpdate());
            writer.endNode();
        }

        writer.startNode("updated");
        marshallingContext.convertAnother(status.isUpdated());
        writer.endNode();
    }

    private void writeTag(HierarchicalStreamWriter writer, String tagName, String tagValue) {
        if (tagValue != null) {
            writer.startNode(tagName);
            writer.setValue(tagValue);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        return null;
    }

    public boolean canConvert(Class aClass) {
        return aClass.equals(TaskStatus.class);
    }
}
