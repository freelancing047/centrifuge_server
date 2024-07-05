package csi.server.business.service.kestrel.reader;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import csi.server.business.visualization.graph.GraphContext;

public class TransientGraphCodec {

    private static SAXParserFactory spf = SAXParserFactory.newInstance();
    private String graphId;

    public TransientGraphCodec(String graphId) {
        this.graphId = graphId;
    }

    public GraphContext loadGraph(InputStream ins) {
        try {
            SAXParser sp = spf.newSAXParser();
            PrefuseGraphHandler graphHandler = new PrefuseGraphHandler(graphId);
            sp.parse(ins, graphHandler);

            return graphHandler.getGraphContext();
        } catch (SAXException se) {
            throw new RuntimeException(se);
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        } catch (IOException ie) {
            throw new RuntimeException(ie);
        }
    }

}
