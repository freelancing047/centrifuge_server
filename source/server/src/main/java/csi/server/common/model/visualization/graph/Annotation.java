package csi.server.common.model.visualization.graph;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import csi.server.common.model.DeepCopiable;
import csi.server.common.model.ModelObject;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Annotation extends ModelObject implements DeepCopiable<Annotation> {

	private String parentKey;
	
	@Transient
	public static int tooltipOrder = 100;

	//Adding this to store possibly large strings
	@Type(type="text")
	private String htmlString;
	
		
	@Override
	public Annotation copy(Map<String, Object> copies) {
		Annotation clone = new Annotation();
        super.cloneComponents(clone);
        
        clone.setParentKey(parentKey);
        clone.setHtmlString(htmlString);
        
        return clone;
	}
	
	public <T extends ModelObject> Annotation trueCopy(Map<String, T> copies) {
		
		Annotation clone = new Annotation();
        super.cloneComponents(clone);

        //normal nodekeys are constant, but this looks for plunked nodekeys, which can change
        if(copies.containsKey(parentKey) && copies.get(parentKey) instanceof PlunkedNode){
        	PlunkedNode node = (PlunkedNode) copies.get(parentKey);
        	clone.setParentKey(node.getNodeKey());
        } else {
        	clone.setParentKey(getParentKey());
        }
        clone.setHtmlString(htmlString);
        
        return clone;
	}

	public String getParentKey() {
		return parentKey;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}

	public String getHtmlString() {
		return htmlString;
	}

	public void setHtmlString(String htmlString) {
		this.htmlString = htmlString;
	}

}
