package csi.server.common.model.themes.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.HasDefaultShape;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.util.ValuePair;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GraphTheme extends Theme implements HasDefaultShape {


    public GraphTheme() {
        super(AclResourceType.GRAPH_THEME, VisualizationType.RELGRAPH_V2);
    }


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<NodeStyle> nodeStyles = new ArrayList<NodeStyle>();


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<LinkStyle> linkStyles = new ArrayList<LinkStyle>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private NodeStyle bundleStyle;

    @Enumerated(EnumType.STRING)
    private ShapeType defaultShape;

    private Integer bundleThreshold;

    @Transient
    private Map<String, NodeStyle> _nodeStyleMap = null;
    @Transient
    private List<ValuePair<String, NodeStyle>> _nodeOverflow = null;
    @Transient
    private Map<String, LinkStyle> _linkStyleMap = null;
    @Transient
    private List<ValuePair<String, LinkStyle>> _linkOverflow = null;

    public GraphTheme(String uuidIn, String nameIn, String remarksIn, String ownerIn, ShapeType shapeTypeIn, Integer bundleThresholdIn,
                      NodeStyle bundleStyleIn, List<NodeStyle> nodeStylesIn, List<LinkStyle> linkStylesIn) {

        this();

        name = nameIn;
        remarks = remarksIn;
        owner = ownerIn;
        defaultShape = shapeTypeIn;
        bundleThreshold = bundleThresholdIn;
        bundleStyle = bundleStyleIn;

        if ((nodeStylesIn != null) && !nodeStylesIn.isEmpty()) {
            nodeStyles.addAll(nodeStylesIn);
        }
        if ((linkStylesIn != null) && !linkStylesIn.isEmpty()) {
            linkStyles.addAll(linkStylesIn);
        }
        setUuid(uuidIn);
    }

    public List<NodeStyle> getNodeStyles() {
        return nodeStyles;
    }


    public void setNodeStyles(List<NodeStyle> nodeStyles) {
        this.nodeStyles = nodeStyles;
    }


    public List<LinkStyle> getLinkStyles() {
        return linkStyles;
    }


    public void setLinkStyles(List<LinkStyle> linkStyles) {
        this.linkStyles = linkStyles;
    }


    public NodeStyle getBundleStyle() {
        return bundleStyle;
    }


    public void setBundleStyle(NodeStyle bundleStyle) {
        this.bundleStyle = bundleStyle;
    }


    public Integer getBundleThreshold() {
        return bundleThreshold;
    }


    public void setBundleThreshold(Integer bundleThreshold) {
        this.bundleThreshold = bundleThreshold;
    }


    public LinkStyle findLinkStyle(String linkType) {
        for (LinkStyle linkStyle : getLinkStyles()) {
            if (linkStyle.getFieldNames().contains(linkType)) {
                return linkStyle;
            }
        }
        return null;
    }

    public NodeStyle findNodeStyle(String nodeType) {
        for (NodeStyle nodeStyle : getNodeStyles()) {
            if (nodeStyle.getFieldNames().contains(nodeType)) {
                return nodeStyle;
            }
        }
        return null;
    }


    public ShapeType getDefaultShape() {
        return defaultShape;
    }


    public void setDefaultShape(ShapeType defaultShape) {
        this.defaultShape = defaultShape;
    }

    public void addNodeStyle(NodeStyle styleIn, String itemIn) {

        addStyle(styleIn, itemIn, getNodeStyleMap());
    }

    public void removeNodeStyle(NodeStyle styleIn, String itemIn) {

        removeStyle(styleIn, itemIn, getNodeStyleMap());
    }

    public void addLinkStyle(LinkStyle styleIn, String itemIn) {

        addStyle(styleIn, itemIn, getLinkStyleMap());
    }

    public void removeLinkStyle(LinkStyle styleIn, String itemIn) {

        removeStyle(styleIn, itemIn, getLinkStyleMap());
    }

    public void resetMaps() {

        resetNodeMaps();
        resetLinkMaps();
    }

    public NodeStyle findNodeConflict(String itemIdIn) {

        return getNodeStyleMap().get(itemIdIn);
    }

    public List<ValuePair<String, NodeStyle>> getNodeOverFlow() {

        List<ValuePair<String, NodeStyle>> myList = _nodeOverflow;

        if (null == myList) {

            initilizeNodeMaps();
            myList = _nodeOverflow;
        }
        _nodeOverflow = null;
        return myList;
    }

    protected Map<String, NodeStyle> getNodeStyleMap() {

        if (null == _nodeStyleMap) {

            initilizeNodeMaps();
        }
        return _nodeStyleMap;
    }

    protected List<ValuePair<String, NodeStyle>> getNodeOverflow() {

        if (null == _nodeOverflow) {

            initilizeLinkMaps();
        }
        return _nodeOverflow;
    }

    public void resetNodeMaps() {

        _nodeOverflow = null;
        _nodeStyleMap = null;
    }

    protected void initilizeNodeMaps() {

        _nodeOverflow = new ArrayList<ValuePair<String, NodeStyle>>();
        _nodeStyleMap = new TreeMap<String, NodeStyle>();

        buildStyleMaps(nodeStyles, _nodeStyleMap, _nodeOverflow);
    }

    public LinkStyle findLinkConflict(String itemIdIn) {

        return getLinkStyleMap().get(itemIdIn);
    }

    public List<ValuePair<String, LinkStyle>> getLinkOverFlow() {

        List<ValuePair<String, LinkStyle>> myList = _linkOverflow;

        if (null == myList) {

            initilizeNodeMaps();
            myList = _linkOverflow;
        }
        _linkOverflow = null;
        return myList;
    }

    protected Map<String, LinkStyle> getLinkStyleMap() {

        if (null == _linkStyleMap) {

            initilizeLinkMaps();
        }
        return _linkStyleMap;
    }

    protected List<ValuePair<String, LinkStyle>> getLinkOverflow() {

        if (null == _linkOverflow) {

            initilizeLinkMaps();
        }
        return _linkOverflow;
    }

    public void resetLinkMaps() {

        _linkOverflow = null;
        _linkStyleMap = null;
    }

    protected void initilizeLinkMaps() {

        _linkOverflow = new ArrayList<ValuePair<String, LinkStyle>>();
        _linkStyleMap = new TreeMap<String, LinkStyle>();

        buildStyleMaps(linkStyles, _linkStyleMap, _linkOverflow);
    }

    @Override
    public GraphTheme clone() {

        GraphTheme myClone = new GraphTheme();

        super.cloneComponents(myClone);
        return cloneValues(myClone);
    }

    private GraphTheme cloneValues(GraphTheme cloneIn) {

        cloneIn.setDefaultShape(defaultShape);
        cloneIn.setBundleStyle(bundleStyle);
        cloneIn.setBundleThreshold(bundleThreshold);
        cloneIn.setNodeStyles(cloneNodeStyles());
        cloneIn.setLinkStyles(cloneLinkStyles());
        return cloneIn;
    }

    private List<NodeStyle> cloneNodeStyles() {

        List<NodeStyle> myList = new ArrayList<NodeStyle>();

        for (NodeStyle myStyle : nodeStyles) {

            myList.add(myStyle.clone());
        }
        return myList;
    }

    private List<LinkStyle> cloneLinkStyles() {

        List<LinkStyle> myList = new ArrayList<LinkStyle>();

        for (LinkStyle myStyle : linkStyles) {

            myList.add(myStyle.clone());
        }
        return myList;
    }
}
