package csi.client.gwt.viz.graph.button;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;

import csi.client.gwt.viz.graph.Graph;

@RunWith(GwtMockitoTestRunner.class)
//Mockito does not work with our ant build, so named this Junit until we can fix it.
//The ant build will not run anything that does not have Test in the class name.
public class TransparencyHandlerJunit {

    @Test
    public void testOnClick() {
        Graph graph = mock(Graph.class);
        TransparencyHandler th = new TransparencyHandler(graph);
        th.onClick(mock(ClickEvent.class));
        verify(graph).showTransparencyWindow();

    }

    @Test
    public void testBind() {
        Graph graph = mock(Graph.class);
        TransparencyHandler th = new TransparencyHandler(graph);
        Button button = mock(Button.class);
        th.bind(button);
        verify(button).addClickHandler(th);
    }

}
