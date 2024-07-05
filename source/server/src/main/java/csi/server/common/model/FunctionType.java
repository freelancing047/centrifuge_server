package csi.server.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.core.util.HasLabel;

public enum FunctionType implements Serializable, IsSerializable, HasLabel {
   CONCAT("Concatenate Fields"), //
   SUBSTRING("Extract Partial Value"), //
   DURATION("Calculate Duration"), //
   MATH("Calculate Value"), //
   NONE("Execute JavaScript"); //

   private static List<FunctionType> _sortedFunctionTypes = new ArrayList<FunctionType>();
   private static Map<String,FunctionType> _labelToEnumMapping = new HashMap<String,FunctionType>();

   static {
      for (FunctionType myFunctionType : values()) {
         _labelToEnumMapping.put(myFunctionType._label, myFunctionType);

         if (myFunctionType != NONE) {
            _sortedFunctionTypes.add(myFunctionType);
         }
      }
      Collections.sort(_sortedFunctionTypes, new Comparator<FunctionType>() {

         @Override
         public int compare(FunctionType o1, FunctionType o2) {
            return o1.getLabel().compareTo(o2.getLabel());
         }
      });
   }

   private String _label;

   private FunctionType(String labelIn) {
      _label = labelIn;
   }

   @Override
   public String getLabel() {
      return _label;
   }

   public static FunctionType getValueByLabel(String labelIn) {
      FunctionType myType = null;

      if (labelIn != null) {
         myType = _labelToEnumMapping.get(labelIn);

         if (myType == null) {
            myType = FunctionType.NONE;
         }
      }

      return myType;
   }

   public static List<FunctionType> sortedValuesByLabel() {
      return _sortedFunctionTypes;
   }
}
