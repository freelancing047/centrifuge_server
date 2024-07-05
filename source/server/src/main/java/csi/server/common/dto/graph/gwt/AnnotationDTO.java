package csi.server.common.dto.graph.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AnnotationDTO implements IsSerializable {

	private String htmlString;
	private String parentId;
	private String vizUuid;
	
	public static AnnotationDTO create(String vizUuid, String htmlString, String parentId){
        AnnotationDTO annotationDTO = new AnnotationDTO();
        annotationDTO.setHtmlString(htmlString);
        annotationDTO.setParentId(parentId);
        annotationDTO.setVizUuid(vizUuid);
        return annotationDTO;
    }
	
	public String getParentId() {
		return parentId;
	}

	private void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getHtmlString() {
		return htmlString;
	}

	public void setHtmlString(String htmlString) {
		this.htmlString = htmlString;
	}

	public String getVizUuid() {
		return vizUuid;
	}

	public void setVizUuid(String vizUuid) {
		this.vizUuid = vizUuid;
	}
		
}
