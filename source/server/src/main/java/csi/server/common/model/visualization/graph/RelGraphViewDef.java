package csi.server.common.model.visualization.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import csi.server.common.dto.CsiMap;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Property;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.shared.core.visualization.graph.GraphLayout;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RelGraphViewDef extends VisualizationDef {

    public static final String PROPERTY_RENDER_THRESHOLD = "render.threshold";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SELECT)
    private List<NodeDef> nodeDefs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SELECT)
    private List<LinkDef> linkDefs;

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @Fetch(FetchMode.SELECT)
    private List<BundleDef> bundleDefs;

    @OneToOne(cascade = CascadeType.ALL)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private GraphPlayerSettings playerSettings;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval=true)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @XStreamOmitField
    private SelectionModel oldSelection;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval=true)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private SelectionModel shadowSelection;

    @OneToMany(cascade = { CascadeType.ALL })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SELECT)
    private List<PlunkedNode> plunkedNodes = new ArrayList<PlunkedNode>();

    @OneToMany(cascade = { CascadeType.ALL })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SELECT)
    private List<PlunkedLink> plunkedLinks = new ArrayList<PlunkedLink>();

    @OneToMany(cascade = { CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SELECT)
    private List<Annotation> annotations = new ArrayList<Annotation>();
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private GraphCachedState state;
    
    
	@Transient
    @XStreamOmitField
    private SelectionModel transientSelection = new SelectionModel();

    @OneToOne(cascade = CascadeType.ALL)
    private GenericProperties settings;

    private Integer nodeTransparency = 255;

    private Integer linkTransparency = 255;

    private Integer labelTransparency = 255;

    private double minimumNodeScaleFactor = 8;

    private String optionSetName;
    
    @Enumerated(value = EnumType.STRING)
    private GraphLayout layout = null;
    
    @Transient
    private Map<String, NodeDef> _cloneNodeDefMap;
    
    public RelGraphViewDef() {
        super();
        setType(VisualizationType.RELGRAPH_V2);
        clientProperties = new CsiMap<String, String>();
        settings = new GenericProperties();
        playerSettings = new GraphPlayerSettings();
    }

    public List<NodeDef> getNodeDefs() {
        if (nodeDefs == null) {
            nodeDefs = new ArrayList<NodeDef>();
        }
        return nodeDefs;
    }

    public void setNodeDefs(List<NodeDef> nodeDefs) {
        this.nodeDefs = nodeDefs;
    }

    public List<LinkDef> getLinkDefs() {
        if (linkDefs == null) {
            linkDefs = new ArrayList<LinkDef>();
        }
        return linkDefs;
    }

    public void setLinkDefs(List<LinkDef> linkDefs) {
        if (linkDefs == null) {
            linkDefs = new ArrayList<LinkDef>();
        }
        this.linkDefs = linkDefs;
    }

    public List<BundleDef> getBundleDefs() {
        if(bundleDefs==null){
            bundleDefs = Lists.newArrayList();
        }
        return bundleDefs;
    }

    public void setBundleDefs(List<BundleDef> bundleDefs) {
        if (bundleDefs == null) {
            bundleDefs = new ArrayList<BundleDef>();
        }
        this.bundleDefs = bundleDefs;
    }

    public String getOptionSetName() {
        // return ( optionSetName == null ) ? BASELINE_OPTION_FILE : optionSetName;
        return optionSetName;
    }

    public void setOptionSetName(String optionSetName) {
        this.optionSetName = optionSetName;
    }

    public GraphPlayerSettings getPlayerSettings() {
        if (playerSettings == null) {
            playerSettings = new GraphPlayerSettings();
        }
        return playerSettings;
    }

    public void setPlayerSettings(GraphPlayerSettings playerSettings) {
        this.playerSettings = playerSettings;
    }

    public SelectionModel getOldSelection() {
        return oldSelection;
    }

    public void setOldSelection(SelectionModel oldSelection) {
        this.oldSelection = oldSelection;
    }

    @PrePersist
    @PreUpdate
    public void internalValidate() {
        // if (playerSettings == null) {
        // playerSettings = new GraphPlayerSettings();
        // }
        if (linkTransparency == null) {
            linkTransparency = 255;
        }
        if (nodeTransparency == null) {
            nodeTransparency = 255;
        }
        if (labelTransparency == null) {
            labelTransparency = 255;
        }
    }

    public Integer getNodeTransparency() {
        return nodeTransparency;
    }

    public void setNodeTransparency(Integer nodeTransparency) {
        this.nodeTransparency = nodeTransparency;
    }

    public Integer getLinkTransparency() {
        return linkTransparency;
    }

    public void setLinkTransparency(Integer linkTransparency) {
        this.linkTransparency = linkTransparency;
    }

    public Integer getLabelTransparency() {
        return labelTransparency;
    }

    public void setLabelTransparency(Integer labelTransparency) {
        this.labelTransparency = labelTransparency;
    }

    public SelectionModel getShadowSelection() {
        return shadowSelection;
    }

    public void setShadowSelection(SelectionModel shadowSelection) {
        this.shadowSelection = shadowSelection;
    }

    public GenericProperties getSettings() {
        return settings;
    }

    public void setSettings(GenericProperties settings) {
        this.settings = settings;
    }

    public List<PlunkedNode> getPlunkedNodes() {
        return plunkedNodes;
    }

    public void setPlunkedNodes(List<PlunkedNode> plunkedNodes) {
        this.plunkedNodes = plunkedNodes;
    }

    public List<PlunkedLink> getPlunkedLinks() {
        return plunkedLinks;
    }

    public void setPlunkedLinks(List<PlunkedLink> plunkedLinks) {
        this.plunkedLinks = plunkedLinks;
    }

    @Transient
    public void setPropertyValue(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("setPropertyValue cannot assign a value to a null");
        }

        Property targetProp = null;
        for (Property prop : this.getSettings().getProperties()) {
            if (name.equals(prop.getName())) {
                targetProp = prop;
                break;
            }
        }

        if (targetProp == null) {
            targetProp = new Property();
            targetProp.setName(name);
            this.getSettings().getProperties().add(targetProp);
        }
        targetProp.setValue(value);
    }

    public RelGraphViewDef copySettings(Map<String, Object> copies) {
        if (copies == null) {
            copies = Maps.newHashMap();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (RelGraphViewDef) copyOfThis;
            }
        }
        RelGraphViewDef copy = new RelGraphViewDef();
        copies.put(getUuid(), copy);//
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.setName(this.getName());//
        copy.setOptionSetName(this.getOptionSetName());//
        copy.setThemeUuid(this.getThemeUuid());

        if (this.getNodeDefs() != null) {
            List<NodeDef> nodeDefsCopy = Lists.newArrayList();
            for (NodeDef input : this.getNodeDefs()) {
                nodeDefsCopy.add(input.copy(copies));
            }
            copy.setNodeDefs(nodeDefsCopy);
        }
        if (this.getBundleDefs() != null) {
            List<BundleDef> bundleDefsCopy = Lists.newArrayList();
            for (BundleDef bundleDef : this.getBundleDefs()) {
                bundleDefsCopy.add(bundleDef.copy(copies));
            }
            copy.setBundleDefs(bundleDefsCopy);
        }
        if (this.getLinkDefs() != null) {
            List<LinkDef> linkDefs = Lists.newArrayList();

            for (LinkDef linkDef : this.getLinkDefs()) {
                linkDefs.add(linkDef.copy(copies));
            }
            copy.setLinkDefs(linkDefs);
        }
        copy.setName(this.getName());
        copy.optionSetName = this.optionSetName;//
        copy.settings = this.settings.copy(copies);//
        copy.setType(this.getType());
        return copy;
    }
   
    @Override
    public Selection getSelection() {
        return transientSelection;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> RelGraphViewDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
        
        RelGraphViewDef myClone = new RelGraphViewDef();
        
        super.cloneComponents(myClone, fieldMapIn, filterMapIn);
        
        _cloneNodeDefMap = new HashMap<String, NodeDef>();

        myClone.setNodeTransparency(getNodeTransparency());
        myClone.setLinkTransparency(getLinkTransparency());
        myClone.setLabelTransparency(getLabelTransparency());
        myClone.setOptionSetName(getOptionSetName());
        myClone.setThemeUuid(getThemeUuid());
        if (null != getSettings()) {
            myClone.setSettings(getSettings().clone());
        }
        if (null != getPlayerSettings()) {
            myClone.setPlayerSettings(getPlayerSettings().clone(fieldMapIn));
        }
        myClone.setNodeDefs(cloneNodeDefs(fieldMapIn, _cloneNodeDefMap));
        myClone.setLinkDefs(cloneLinkDefs(fieldMapIn, _cloneNodeDefMap));
        myClone.setBundleDefs(cloneBundleDefs(fieldMapIn, _cloneNodeDefMap));
        
        return myClone;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ModelObject, S extends ModelObject> List<NodeDef> cloneNodeDefs(Map<String, T> fieldMapIn, Map<String, S> nodeMapIn) {
        
        if (null != getNodeDefs()) {
            
            List<NodeDef>  myList = new ArrayList<NodeDef>();
            
            for (NodeDef myItem : getNodeDefs()) {
                
                myList.add((NodeDef)cloneFromOrToMap(nodeMapIn, (S)myItem, fieldMapIn));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ModelObject, S extends ModelObject> List<NodeDef> copyNodeDefs(Map<String, T> fieldMapIn, Map<String, T> nodeMapIn) {
        
        if (null != getNodeDefs()) {
            
            List<NodeDef>  myList = new ArrayList<NodeDef>();
            
            for (NodeDef myItem : getNodeDefs()) {
                
            	NodeDef copy = myItem.trueCopy(nodeMapIn);
            	nodeMapIn.put(myItem.getUuid(), (T) copy);
                myList.add(copy);
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    private <T extends ModelObject> List<LinkDef> cloneLinkDefs(Map<String, T> fieldMapIn, Map<String, NodeDef> nodeMapIn) {
        
        if (null != getLinkDefs()) {
            
            List<LinkDef>  myList = new ArrayList<LinkDef>();
            
            for (LinkDef myItem : getLinkDefs()) {
                
                myList.add(myItem.clone(fieldMapIn, nodeMapIn));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    private <T extends ModelObject> List<LinkDef> copyLinkDefs(Map<String, T> fieldMapIn, Map<String, T> nodeMapIn) {
        
        if (null != getLinkDefs()) {
            
            List<LinkDef>  myList = new ArrayList<LinkDef>();
            
            for (LinkDef myItem : getLinkDefs()) {
                
                myList.add(myItem.trueCopy(fieldMapIn, nodeMapIn));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    private <T extends ModelObject> List<BundleDef> cloneBundleDefs(Map<String, T> fieldMapIn, Map<String, NodeDef> nodeMapIn) {
        
        if (null != getBundleDefs()) {
            
            List<BundleDef>  myList = new ArrayList<BundleDef>();
            
            for (BundleDef myItem : getBundleDefs()) {
                
                myList.add(myItem.clone(fieldMapIn, nodeMapIn, false));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    private <T extends ModelObject> List<BundleDef> copyBundleDefs(Map<String, T> fieldMapIn, Map<String, T> nodeMapIn) {
        
        if (null != getBundleDefs()) {
            
            List<BundleDef>  myList = new ArrayList<BundleDef>();
            
            for (BundleDef myItem : getBundleDefs()) {
                
                myList.add(myItem.clone(fieldMapIn, nodeMapIn, true));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    private <T extends ModelObject> List<PlunkedNode> copyPlunkedNodeDefs(Map<String, T> fieldMapIn, Map<String, T> nodeMapIn) {
        
        if (null != getLinkDefs()) {
            
            List<PlunkedNode>  myList = new ArrayList<PlunkedNode>();
            
            for (PlunkedNode myItem : getPlunkedNodes()) {
                
                myList.add(myItem.trueCopy(nodeMapIn));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    private <T extends ModelObject> List<PlunkedLink> copyPlunkedLinkDefs(Map<String, T> fieldMapIn, Map<String, T> nodeMapIn) {
        
        if (null != getLinkDefs()) {
            
            List<PlunkedLink>  myList = new ArrayList<PlunkedLink>();
            
            for (PlunkedLink myItem : getPlunkedLinks()) {
                
                myList.add(myItem.trueCopy(nodeMapIn));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }

	private <T extends ModelObject> List<Annotation> copyAnnotations(Map<String, T> nodeMapIn) {
    
	    if (null != getLinkDefs()) {
	        
	        List<Annotation>  myList = new ArrayList<Annotation>();
	        
	        for (Annotation myItem : getAnnotations()) {
	            
	            myList.add(myItem.trueCopy(nodeMapIn));
	        }
	        
	        return myList;
	        
	    } else {
	        
	        return null;
	    }
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		RelGraphViewDef myCopy = new RelGraphViewDef();
		Map<String, T> nodeMapIn = new HashMap<String, T>();
		filterMapIn.putAll((Map<? extends String, ? extends S>) fieldMapIn);
        super.copyComponents(myCopy, fieldMapIn, filterMapIn);
        
        _cloneNodeDefMap = new HashMap<String, NodeDef>();

        myCopy.setNodeTransparency(getNodeTransparency());
        myCopy.setLinkTransparency(getLinkTransparency());
        myCopy.setLabelTransparency(getLabelTransparency());
        myCopy.setOptionSetName(getOptionSetName());
        myCopy.setThemeUuid(getThemeUuid());
        if (null != getSettings()) {
            myCopy.setSettings(getSettings().copy());
        }
        if (null != getPlayerSettings()) {
            myCopy.setPlayerSettings(getPlayerSettings().copy(fieldMapIn));
        }
        myCopy.setNodeDefs(copyNodeDefs(fieldMapIn, nodeMapIn));
        myCopy.setLinkDefs(copyLinkDefs(fieldMapIn, nodeMapIn));
        myCopy.setBundleDefs(copyBundleDefs(fieldMapIn, nodeMapIn));
        myCopy.setPlunkedLinks(copyPlunkedLinkDefs(fieldMapIn, nodeMapIn));
        myCopy.setPlunkedNodes(copyPlunkedNodeDefs(fieldMapIn, nodeMapIn));
        myCopy.setAnnotations(copyAnnotations(nodeMapIn));
        myCopy.setLayout(getLayout());
        
        return myCopy;
	}

    public GraphLayout getLayout() {
        return layout;
    }

    public void setLayout(GraphLayout layout) {
        this.layout = layout;
    }

    public GraphCachedState getState() {
        return state;
    }

    public void setState(GraphCachedState state) {
        this.state = state;
    }


    public double getMinimumNodeScaleFactor() {
        return minimumNodeScaleFactor;
    }

    public void setMinimumNodeScaleFactor(double minimumNodeScaleFactor) {
        this.minimumNodeScaleFactor = minimumNodeScaleFactor;
    }
}
