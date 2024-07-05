package csi.server.common.model.filter;

import java.io.Serializable;

public class OperandTypeAndValue<T> implements Serializable {
   private static final long serialVersionUID = 6967684067844646054L;

   private FilterOperandType type;
   private T value;

   public OperandTypeAndValue() {
   }

   public OperandTypeAndValue(final FilterOperandType type, final T value) {
      this.type = type;
      this.value = value;
   }

   public FilterOperandType getType() {
      return type;
   }
   public T getValue() {
      return value;
   }

   public void setType(final FilterOperandType type) {
      this.type = type;
   }
   public void setValue(final T value) {
      this.value = value;
   }
}
