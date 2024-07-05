package csi.server.task.api;

import java.util.EventListener;

public interface InvokeListener
    extends EventListener
{
    void onEvent();
}
