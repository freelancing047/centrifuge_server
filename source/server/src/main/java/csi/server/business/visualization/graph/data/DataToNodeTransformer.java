package csi.server.business.visualization.graph.data;

import java.awt.Point;
import java.util.Map;
import java.util.function.Function;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import prefuse.data.Graph;
import prefuse.data.Node;

import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.GraphConstants.eLayoutAlgorithms;

public class DataToNodeTransformer implements Function<DBObject,NodeStore> {
    private GraphStorage graphStorage;
    private Graph graph;
    private Map<String, Node> index;

    @Override
    public NodeStore apply(DBObject data) {
        NodeStore store = new NodeStore();

        readInternalAttributes(data, store);
        Data.readNonInternalAttributes(data, store, graphStorage);

        return store;
    }

    private void readInternalAttributes(DBObject data, NodeStore store) {

        Data.readDocId(data, store);
        Data.readId(data, store);
        Data.readLabel(data, store);
        Data.readTypes(data, store, graphStorage);

        if (data.containsField(Helper.INTERNAL)) {

            BasicDBObject wrapper = new BasicDBObject();
            wrapper.putAll((Map) data.get(Helper.INTERNAL));

            Data.readDefCounts(wrapper, store, graphStorage);

            if (wrapper.containsField(Data.Hidden)) {
                store.setHidden(wrapper.getBoolean(Data.Hidden));
            }

            if (wrapper.containsField(Data.Icon)) {
                store.setIcon(wrapper.getString(Data.Icon));
            }

            if (wrapper.containsField(Data.Shape)) {
                store.setShape(wrapper.getString(Data.Shape));
            }

            if (wrapper.containsField(Data.HideLabels)) {
                store.setHideLabels(wrapper.getBoolean(Data.HideLabels));
            }

            if (wrapper.containsField(Data.Anchored)) {
                store.setAnchored(wrapper.getBoolean(Data.Anchored));
            }

            if (wrapper.containsField(Data.Scale)) {
                store.setScale(wrapper.getInt(Data.Scale));
            }

            // let size override scale....should of never split these two!
            if( wrapper.containsField(Data.Size) ) {
                double size = wrapper.getDouble(Data.Size);
                store.setScale((int) size);
            }
            if( wrapper.containsField(Data.Transparency) ) {
                double size = wrapper.getDouble(Data.Transparency);
                store.setTransparency((int) size);
            }

            if (wrapper.containsField(Data.RelativeSize)) {
                store.setRelativeSize(wrapper.getDouble(Data.RelativeSize));
            }

            if (wrapper.containsField(Data.Color)) {
                store.setColor(wrapper.getInt(Data.Color));
            }

            if (wrapper.containsField(Data.SpecId)) {
                store.setSpecID(wrapper.getString(Data.SpecId));
            }

            if (wrapper.containsField(Data.Position)) {
                DBObject pos = (DBObject) wrapper.get(Data.Position);
                double x = (Double) pos.get(Data.X);
                double y = (Double) pos.get(Data.Y);
                Point point = new Point();
                point.x = (int) x;
                point.y = (int) y;
                store.setPosition(eLayoutAlgorithms.forceDirected, point );
            }

            if( wrapper.containsField(Data.Parent)) {
                Object pId = wrapper.get(Data.Parent);
                if( pId instanceof ObjectId) {
                    DBObject parentQuery = Helper.getIdQuery(pId);
                    DBObject parentData = (DBObject) graphStorage.findVertex(parentQuery);
                    NodeStore parentStore = this.apply(parentData);

                    if (index.containsKey(parentStore.getKey())) {
                       Node parentNode = index.get(parentStore.getKey());
                       parentStore = GraphManager.getNodeDetails(parentNode);
                    } else {
                        Node parentNode = graph.addNode();
                        parentNode.set(GraphConstants.DOC_ID, pId);
                        GraphManager.setNodeDetails(parentNode, parentStore);
                        parentStore.resetTypes();
                        parentStore.setType(GraphConstants.BUNDLED_NODES);
                        parentStore.setBundle(true);
                        index.put(parentStore.getKey(), parentNode);
                    }
                    store.setParent(parentStore);
                    parentStore.addChild(store);
                }
            }

            if( wrapper.containsField(Data.Document)) {
               Data.readPropertyValue( wrapper, store, ObjectAttributes.CSI_INTERNAL_DOCUMENT, Data.Document, graphStorage);

            }

            if( wrapper.containsField(Data.URL)) {
                Data.readPropertyValue( wrapper, store, ObjectAttributes.CSI_INTERNAL_URL, Data.URL, graphStorage);
            }

            if( wrapper.containsField(Data.PrimaryType)) {
                store.setType(wrapper.getString(Data.PrimaryType));
            }

            if( wrapper.containsField(Data.Visualized)) {
                store.setVisualized(wrapper.getBoolean(Data.Visualized));
            }

            if( wrapper.containsField(Data.SizeMode)) {
            	store.setSizeMode(wrapper.getInt(Data.SizeMode));
            }

            if( wrapper.containsField(Data.Plunked)){
                store.setPlunked(wrapper.getBoolean(Data.Plunked));
            }
        }
    }

    public void setGraphStorage(GraphStorage graphStorage) {
        this.graphStorage = graphStorage;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public void setIndex(Map<String, Node> nodeIndex) {
        this.index = nodeIndex;
    }

}
