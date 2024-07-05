package csi.server.business.visualization.graph.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import prefuse.data.io.GraphMLWriter.Tokens;
import prefuse.util.io.XMLWriter;

import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.graph.stat.GraphStatisticalEntity;
import csi.server.common.model.visualization.graph.GraphConstants;

public abstract class AbstractGraphObjectStore implements GraphStatisticalEntity {
   private static final Logger LOG = LogManager.getLogger(AbstractGraphObjectStore.class);

    public static final String HIGHLIGHT_PROPERTY = "highlighted";
    static final String                  LABEL_PROPERTY     = "Label";
    protected String                     key;

    protected String                     type               = GraphConstants.UNSPECIFIED_TYPE;

    // TODO protected String nodeSubtype;
    protected Integer                    color;                                                         // = -1;

    protected List<String>               labels             = new ArrayList<String>();

    protected Map<String, Integer>       types              = new HashMap<String, Integer>();

    protected Map<String, List<Integer>> rows               = new HashMap<String, List<Integer>>();

    protected boolean                    hidden;

    protected AbstractGraphObjectStore           parent             = null;

    protected List<AbstractGraphObjectStore>     children;
    // TODO how to make GraphObjectStore specific for subclasses?

    protected boolean                    bundleType;

    protected String                     primarySpecID;

    protected int                        scale              = 1;

    protected Map<String, Property> attributes = new HashMap<String, Property>();

    protected Object                     docId;

    protected boolean                    visualized         = true;

    protected int                        sizeMode;    // by Size, by Transparency
    protected boolean hideLabels;

    private boolean isPlunked;
    private double transparency =100;

    public boolean isPlunked() {
        return isPlunked;
    }

    public void setPlunked(boolean isPlunked) {
        this.isPlunked = isPlunked;
    }

    public boolean isVisualized() {
        return visualized;
    }

    public void setVisualized(boolean visualized) {
        this.visualized = visualized;
    }

    public boolean isBySize() {
        return sizeMode == ObjectAttributes.CSI_INTERNAL_SIZE_BY_SIZE;
    }

    public boolean isByStatic() {
    	return sizeMode == ObjectAttributes.CSI_INTERNAL_SIZE_BY_STATIC;
    }

    public int getSizeMode() {
    	return  this.sizeMode;
    }

    public void setSizeMode(int sizeMode) {
        this.sizeMode = sizeMode;
    }

    public Object getDocId() {
        return docId;
    }

    public void setDocId(Object docId) {
        this.docId = docId;
    }

    public Map<String, Property> getAttributes() {
        return attributes;
    }

    public void addLabel(String label) {
        if ((label != null) && (label.length() > 0)) {
            if (labels.indexOf(label) == -1) {
                labels.add(label);
            }
        }
    }

    public void resetTypes() {
        type = GraphConstants.UNSPECIFIED_TYPE;
        types.clear();
    }

    public boolean isHideLabels() {
        return hideLabels;
    }

    public void setHideLabels(boolean hideLabels) {
        this.hideLabels = hideLabels;
    }

    public void addType(String type) {
        // This should never be hit... but just in case.
        if (type.length() == 0) {
            type = GraphConstants.UNSPECIFIED_TYPE;
        }

        String typeName = type.trim();
        Integer count = types.get(typeName);
        if (count == null) {
            count = 0;
        }
        types.put(typeName, count + 1);
        if (this.type.equals(GraphConstants.UNSPECIFIED_TYPE)) {
            this.type = typeName;
        }
    }

    public void addSpecRow(String specKey, int row) {
        List<Integer> specRow = rows.get(specKey);
        if (specRow == null) {
            specRow = new ArrayList<Integer>();
            rows.put(specKey, specRow);
        }
        specRow.add(row);
    }

    public void addChild(AbstractGraphObjectStore child) {
        if (child == null) {
            throw new IllegalArgumentException("child");
        }

        if (this.children == null) {
            this.children = new ArrayList<AbstractGraphObjectStore>();
        }
        if (!children.contains(child) && !noChildrenByKey(child)) {
            this.children.add(child);

            if ((child.parent != null) && (child.parent != this)) {
                // reparenting the node...
                child.parent.removeChild(child);
            }

            child.parent = this;
            addType(child.getType());
        }
    }

    private boolean noChildrenByKey(AbstractGraphObjectStore child) {
        boolean hasChild = false;
        Iterator<AbstractGraphObjectStore> it = children.iterator();
        while (it.hasNext() && !hasChild) {
            AbstractGraphObjectStore details = it.next();
            if (details.getKey().equals(child.getKey())) {
                hasChild = true;
            }
        }

        return hasChild;
    }

    public int getWeight() {
        int weight = 0;
        if (rows != null) {
            for (List<Integer> defRows : rows.values()) {
                if (defRows != null) {
                    weight += defRows.size();
                }
            }
        }
        return weight;
    }

   public void removeChild(AbstractGraphObjectStore child) {
      if ((this.children != null) && (children.contains(child))) {
         children.remove(child);
         child.parent = null;

         // only do this computation if we have other children...otherwise
         // the default behavior is to eventually remove this node.
         if (hasChildren()) {
            resetTypes();
            internal_calculateTypes();
         }
      }
   }

    private void internal_calculateTypes() {
        this.type = GraphConstants.BUNDLED_NODES;
        if (children != null) {
            for (AbstractGraphObjectStore child : children) {
                if (child.isBundle()) {
                    addType(GraphConstants.BUNDLED_NODES);
                } else {
                    addType(child.getType());
                }
            }
        }
    }

    public abstract void writeGraphML(XMLWriter xml);

    protected void writeSuperML(XMLWriter xml) {
        xml.contentTag("key", this.key);
        xml.contentTag("type", this.type);
        if (this.color > -1) {
            xml.contentTag("color", this.color.toString());
        }

        if (this.hidden) {
            xml.contentTag("hidden", "true");
        }

        if (this.isBundled()) {
            xml.contentTag("bundled", "true");
        }

        String compositeLabel = this.getLabel();
        if (compositeLabel.length() > 0) {
            xml.contentTag("label", compositeLabel);
        }

        xml.start("types");
        Iterator<Map.Entry<String, Integer>> iter = this.types.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = iter.next();
            xml.contentTag(Tokens.DATA, "type", entry.getKey(), entry.getValue().toString());
        }
        xml.end();

        /*
         * xml.start("rows"); iter = this.rows.entrySet().iterator(); while (iter.hasNext()) { Map.Entry entry =
         * (Map.Entry) iter.next(); //xml.start("specID", "key", entry.getKey().toString());
         * //xml.contentTag(Tokens.DATA, Tokens.KEY, "rows", ((List<Integer>)entry.getValue()).toString());
         * xml.contentTag("spec", Tokens.KEY, entry.getKey().toString(), ((List<Integer>)entry.getValue()).toString());
         * //xml.end(); } xml.end();
         */

        int nestedLevel = 0;
        AbstractGraphObjectStore myParent = this.parent;
        while (myParent != null) {
            nestedLevel += 1;
            myParent = myParent.parent;
        }
        xml.contentTag("nestedLevel", ((Integer) nestedLevel).toString());

        for (Entry<String, Property> entry : attributes.entrySet()) {
            Property prop = entry.getValue();
            List<Object> values = prop.getValues();
            xml.start(Tokens.DATA, "key", prop.getName());
            for (Object o : values) {
                if (o != null) {
                    xml.contentTag(Tokens.DATA, o.toString());
                }
            }
            xml.end();
        }
    }

    // compose label by concatenating its parts
    public String getLabel() {
        StringBuilder compositeBuff = new StringBuilder("");
        for (String label : labels) {
            compositeBuff.append("; ").append(label);
        }
        String compositeLabel = compositeBuff.toString();
        if (compositeLabel.length() > 0) {
            compositeLabel = compositeLabel.substring(2);
        }

        return compositeLabel;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        if ((value == null) || (value.length() == 0)) {
            this.type = GraphConstants.UNSPECIFIED_TYPE;
        } else {
            this.type = value;
        }

    }

    // should not be called directly
    // the first call to addType sets the object's type
    // public void setType(String type) {
    // this.type = type;
    // }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public boolean incorporatesNodeDef(String nodeDefNameIn) {

        boolean mySuccess = rows.containsKey(nodeDefNameIn);

        if ((!mySuccess) && (null != children)) {

            for (AbstractGraphObjectStore myChild : children) {
                mySuccess = myChild.incorporatesNodeDef(nodeDefNameIn);
                if (mySuccess) {
                    break;
                }
            }
        }
        return mySuccess;
    }

    public Map<String, List<Integer>> getRows() {
        Map<String, List<Integer>> ref = new HashMap<String, List<Integer>>();
        ref.putAll(rows);
        if (children != null) {
            ref = new HashMap<String, List<Integer>>();
            HashMultimap<String, Integer> supportingRows = HashMultimap.<String, Integer> create();
            this.updateRows(supportingRows);
            for (String specKey : supportingRows.keySet()) {
                List<Integer> li = new LinkedList<Integer>();
                Set<Integer> set = supportingRows.get(specKey);
                li.addAll(set);
                ref.put(specKey, li);
            }
        }

        return ref;

    }

    public void setRows(Map<String, List<Integer>> rows) {
        this.rows = rows;
    }

    protected void updateRows(SetMultimap<String, Integer> supporting) {
        if (children == null) {
            if (rows == null) {
                return;
            }
            for (Entry<String, List<Integer>> entry : rows.entrySet()) {
                String spec = entry.getKey();
                List<Integer> rows = entry.getValue();
                supporting.putAll(spec, rows);
            }
        } else {
            for (AbstractGraphObjectStore store : children) {
                store.updateRows(supporting);
            }
        }
    }

    public Map<String, Integer> getTypes() {
        return types;
    }

    public void setTypes(Map<String, Integer> types) {
        this.types = types;
    }

    public boolean hasChildren() {
        return ((children != null) && !children.isEmpty());
    }

    public List<AbstractGraphObjectStore> getChildren() {
        return children;
    }

    public void setChildren(List<AbstractGraphObjectStore> children) {
        this.children = children;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isBundle() {
        return bundleType;
        // return (children != null && children.size() > 0);
    }

    public void setBundle(boolean value) {
        bundleType = value;

        if (isBundle()) {
            setType(GraphConstants.BUNDLED_NODES);
        } else {
            rectifyCounts();
        }
    }

    public boolean isBundled() {
        return (parent != null);
    }

    public boolean isDisplayable() {
        return (!this.hidden && !this.isBundled() && this.visualized);
    }

    public AbstractGraphObjectStore getParent() {
        return parent;
    }

    public void setParent(AbstractGraphObjectStore parent) {
        this.parent = (parent == this) ? null : parent;
    }

    // return first-encountered spec ID for bundling
    public String getSpecID() {
        return primarySpecID;
    }

    public void setSpecID(String value) {
        primarySpecID = value;
    }

    // return first-encountered spec ID for bundling
    /*
     * public int getFirstDataRow() { int row = 0; Iterator iter = rows.entrySet().iterator(); while (iter.hasNext()) {
     * Map.Entry entry = (Map.Entry) iter.next(); List<Integer>rowList = (List<Integer>)entry.getValue(); row =
     * rowList.get(0); break; } return row; }
     */

    public void rectifyBundle() {
        HashSet<String> keySet = new HashSet<String>();

        rectifyBundle(keySet);
    }

    protected void rectifyBundle(HashSet<String> keySet) {
        String key = this.getKey();

        if (keySet.contains(key)) {
           LOG.warn(String.format("Recursive list membership found for node %s", key));
            this.parent.children.remove(this);
            return;
        }

        keySet.add(key);

        // Recurse through children
        if ((this.children != null) && !this.children.isEmpty()) {
            /*
             * Copy the list, because when a child executes checkBundle it
             * updates this.children.
             */
            ArrayList<AbstractGraphObjectStore> childList = new ArrayList<AbstractGraphObjectStore>(children);
            for (AbstractGraphObjectStore child : childList) {
                child.rectifyBundle(keySet);
            }

            // now do me
            checkBundle();
        }
    }

    // make sure bundle has more than one child
    protected void checkBundle() {
        // move single child to parent
        if ((this.children != null) && (this.children.size() == 1)) {
            AbstractGraphObjectStore singleChild = this.children.remove(0);
            if (this.parent != null) {
                this.parent.children.remove(this);
                if (this.parent != singleChild) {
                    this.parent.addChild(singleChild);
                }

            }

            singleChild.parent = (singleChild == this) ? null : this.parent;
            if (this.parent != null) {
                this.parent.rectifyCounts();
            }
        }
    }

    protected void rectifyCounts() {
        resetTypes();
        internal_calculateTypes();
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getFirstType() {
        if (types.size() == 1) {
            return types.keySet().iterator().next();
        }
        return null;
    }

    @Override
    public boolean isVisible() {
        return !isHidden();
    }

    public double getTransparency() {
        return transparency;
    }

    public void setTransparency(double transparency) {
        this.transparency = transparency;
    }

    public String getPrimarySpecID() {
        return primarySpecID;
    }

    public void setPrimarySpecID(String primarySpecID) {
        this.primarySpecID = primarySpecID;
    }
}
