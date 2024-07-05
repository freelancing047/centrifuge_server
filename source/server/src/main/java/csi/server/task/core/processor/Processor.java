package csi.server.task.core.processor;

/**
 * Marks the start and stop of a processing operation.
 * 
 * @author dorel.matei
 *
 */
public interface Processor {

    void startProcessing();
    
    void stopProcessing();
}
