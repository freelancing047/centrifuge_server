package csi.client.gwt.viz.graph.window.transparency;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.Graph.Model;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public class TransparencySettingsProxyTest {

    private static TransparencySettings settings;
    private TransparencySettingsProxy tsp;
    private static Graph graph;
    private static Model graphModel;
    private static RelGraphViewDef relGraphViewDef;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        settings = mock(TransparencySettings.class);
        graph = mock(Graph.class);
        graphModel = mock(Model.class);
        when(graph.getModel()).thenReturn(graphModel);
        when(settings.getGraph()).thenReturn(graph);
    }

    @Before
    public void setUp() throws Exception {
        relGraphViewDef = mock(RelGraphViewDef.class);
        when(relGraphViewDef.getNodeTransparency()).thenReturn(5);
        when(relGraphViewDef.getLinkTransparency()).thenReturn(6);
        when(relGraphViewDef.getLabelTransparency()).thenReturn(7);
        when(graphModel.getRelGraphViewDef()).thenReturn(relGraphViewDef);
        tsp = new TransparencySettingsProxy(settings);
    }

    @Test
    public void testGetNodeTransparency() {
        int x = tsp.getNodeTransparency();
        assertEquals(x, 5);
    }

    @Test
    public void testSetNodeTransparency() {
        tsp.setNodeTransparency(10);
        verify(relGraphViewDef).setNodeTransparency(10);
    }

    @Test
    public void testSetNodeTransparencyOver255() {
        tsp.setNodeTransparency(256);
        verify(relGraphViewDef).setNodeTransparency(255);
    }

    @Test
    public void testSetNodeTransparencyLessThan0() {
        tsp.setNodeTransparency(-1);
        verify(relGraphViewDef).setNodeTransparency(0);
    }

    @Test
    public void testGetLinkTransparency() {
        int x = tsp.getLinkTransparency();
        assertEquals(x, 6);
    }

    @Test
    public void testSetLinkTransparency() {
        tsp.setLinkTransparency(10);
        verify(relGraphViewDef).setLinkTransparency(10);
    }

    @Test
    public void testSetLinkTransparencyOver255() {
        tsp.setLinkTransparency(256);
        verify(relGraphViewDef).setLinkTransparency(255);
    }

    @Test
    public void testSetLinkTransparencyLessThan0() {
        tsp.setLinkTransparency(-1);
        verify(relGraphViewDef).setLinkTransparency(0);
    }

    @Test
    public void testGetLabelTransparency() {
        int x = tsp.getLabelTransparency();
        assertEquals(x, 7);
    }

    @Test
    public void testSetLabelTransparency() {
        tsp.setLabelTransparency(10);
        verify(relGraphViewDef).setLabelTransparency(10);
    }

    @Test
    public void testSetLabelTransparencyOver255() {
        tsp.setLabelTransparency(256);
        verify(relGraphViewDef).setLabelTransparency(255);
    }

    @Test
    public void testSetLabelTransparencyLessThan0() {
        tsp.setLabelTransparency(-1);
        verify(relGraphViewDef).setLabelTransparency(0);
    }

    @Test
    public void testGetRelGraphViewDef() {
        RelGraphViewDef rgvd = tsp.getRelGraphViewDef();
        assertEquals(rgvd, relGraphViewDef);
    }

    @Test(expected = NullPointerException.class)
    public void testGetRelGraphViewDefNullSafe() {
        // this is a very bad state and should error
        TransparencySettings mySettings = mock(TransparencySettings.class);
        when(mySettings.getGraph()).thenReturn(null);
        tsp = new TransparencySettingsProxy(mySettings);
        tsp.getRelGraphViewDef();
    }
}
