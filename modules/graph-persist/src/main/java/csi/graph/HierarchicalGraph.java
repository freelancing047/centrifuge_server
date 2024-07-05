package csi.graph;

import edu.uci.ics.jung.graph.Graph;

public interface HierarchicalGraph<V, E> extends Graph<V, E> {

    /**
     * Adds an immediate child to the parent.  
     * @param child
     * @param parent
     * @return
     */
    boolean addNode(V child, V parent);

    /**
     * Returns the number of immediate children for the given vertex.  
     * @param vertex
     * @return
     */
    int getChildrenCount(V vertex);

    /**
     * Returns the total number of children under the provided vertex.  This count
     * includes all leaf children and inner children.
     * @param vertex
     * @return
     */
    int getDescendantCount(V vertex);

    /**
     * Returns the parent of the provided vertex.  
     * @param vertex
     * @return parent vertex or null
     */
    V getParent(V vertex);

    /**
     * Obtain the immediate children of the provided vertex.  The iterable
     * will never be null.   
     * @param vertex
     * @return iterable container of the children.
     */
    Iterable<V> getChildren(V vertex);

    Iterable<V> getDescendants(V vertex);
    
    Iterable<E> getInteriorEdges( V vertex );
    
    Iterable<E> getExteriorEdges( V vertex );
    
    Iterable<E> getMetaEdges();
    Iterable<E> getMetaEdges( V container );
    int getMetaDegree( V container );
    void removeMetaEdges( V container );
    
    Iterable<V> getTopVertices();
    Iterable<V> getNodes( int level );
    
    
    boolean isDescendant( V vertex, V descendant );
    boolean isAncestor( V vertex, V ancestor );
    boolean isParent( V vertex, V parent );
    
    
    int getHeight();
    int getLevelSize( int level );
    int getLevel( V vertex );
    
    void moveTo( V vertex, V container );
    void removeFromParent( V vertex );
    void group( V container, V[] vertices );
    void ungroup( V container );
}
