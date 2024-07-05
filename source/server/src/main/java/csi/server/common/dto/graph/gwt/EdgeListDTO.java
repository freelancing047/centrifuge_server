package csi.server.common.dto.graph.gwt;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EdgeListDTO implements IsSerializable {
   public int ID;
   public String type;
   public boolean hidden;
   public boolean displayable;
   public boolean isVisualized;
   public boolean plunked;
   public boolean editable;
   public String label;
   public List<GraphTypesDTO> types;
   public String source;
   public String target;
   public int sourceId;
   public int targetId;
   public boolean selected;
   public boolean annotation;
   public double width;
   private double opacity;

   public class EdgeListFieldNames {
      public static final String FIELD_ID = "id";
      public static final String FIELD_LABEL = "label";
      public static final String FIELD_SOURCE = "source";
      public static final String FIELD_TARGET = "target";
      public static final String FIELD_TYPE = "type";
      public static final String FIELD_TYPES = "types";
      public static final String FIELD_HIDDEN = "hidden";
      public static final String FIELD_SELECTED = "selected";
      public static final String FIELD_PLUNKED = "plunked";
      public static final String FIELD_ANNOTATION = "annotation";
      public static final String FIELD_SIZE = "width";
      public static final String OPACITY = "opacity";
   }

   public EdgeListDTO() {
   }

   public int getID() {
      return ID;
   }

   public void setID(int iD) {
      ID = iD;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public boolean isHidden() {
      return hidden;
   }

   public boolean isPlunked() {
      return plunked;
   }

   public String getHiddenString() {
      return Boolean.toString(hidden);
   }

   public void setPlunked(boolean plunked) {
      this.plunked = plunked;
   }

   public void setHiddenString(String hiddenString) {
      hidden = Boolean.parseBoolean(hiddenString);
   }

   public void setHidden(boolean hidden) {
      this.hidden = hidden;
   }

   public boolean isDisplayable() {
      return displayable;
   }

   public void setDisplayable(boolean displayable) {
      this.displayable = displayable;
   }

   public boolean isVisualized() {
      return isVisualized;
   }

   public void setVisualized(boolean isVisualized) {
      this.isVisualized = isVisualized;
   }

   public boolean isEditable() {
      return editable;
   }

   public void setEditable(boolean editable) {
      this.editable = editable;
   }

   public String getLabel() {
      return label;
   }

   public void setLabel(String label) {
      this.label = label;
   }

   public List<GraphTypesDTO> getTypes() {
      return types;
   }

   public void setTypes(List<GraphTypesDTO> types) {
      this.types = types;
   }

   public String getSource() {
      return source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public String getTarget() {
      return target;
   }

   public void setTarget(String target) {
      this.target = target;
   }

   public int getSourceId() {
      return sourceId;
   }

   public void setSourceId(int sourceId) {
      this.sourceId = sourceId;
   }

   public int getTargetId() {
      return targetId;
   }

   public void setTargetId(int targetId) {
      this.targetId = targetId;
   }

   public void setAnnotation(boolean annotation) {
      this.annotation = annotation;
   }

   public boolean hasAnnotation() {
      return this.annotation;
   }

   public boolean isSelected() {
      return selected;
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   public String getAllTypesAsString() {
      return types.stream().map(i -> i.toString()).collect(Collectors.joining(","));
   }

   public double getWidth() {
      return width;
   }

   public void setWidth(double width) {
      this.width = width;
   }

   public double getOpacity() {
      return opacity;
   }

   public void setOpacity(double opacity) {
      this.opacity = opacity;
   }
}
