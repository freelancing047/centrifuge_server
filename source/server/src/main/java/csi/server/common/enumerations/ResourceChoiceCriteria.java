package csi.server.common.enumerations;

import java.util.StringJoiner;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 5/5/2016.
 */
public enum ResourceChoiceCriteria implements IsSerializable {
   UUID("Matches resource uuid."),
   NAME("Matches resource name."),
   OWNER("Matches resource owner."),
   ALL_LIST_IDS("Matches all required field local ids."),
   ALL_LIST_NAMES("Matches all required field names."),
   SOME_LIST_IDS("Matches some required field local ids."),
   SOME_LIST_NAMES("Matches some required field names.");

   private String _description;

   public static String expand(Integer maskIn) {
      String result = null;

      if ((maskIn != null) && (maskIn.intValue() > 0)) {
         StringJoiner descriptions = new StringJoiner("\n");

         for (int i = 0; i < values().length; i++) {
            if (((1 << i) & maskIn.intValue()) != 0) {
               descriptions.add(values()[i]._description);
            }
         }
         result = descriptions.toString();
      } else {
         result = "!! NOT A VALID MATCH !!";
      }
      return result;
   }

   public static int setBit(int maskIn, ResourceChoiceCriteria criteriaIn) {
      return (criteriaIn == null) ? maskIn : criteriaIn.setBit(maskIn);
   }

   public static int setBit(ResourceChoiceCriteria criteriaIn) {
      return (criteriaIn == null) ? 0 : criteriaIn.setBit(0);
   }

   public static int clearBit(int maskIn, ResourceChoiceCriteria criteriaIn) {
      return (criteriaIn == null) ? maskIn : criteriaIn.clearBit(maskIn);
   }

   public static boolean isSet(int maskIn, ResourceChoiceCriteria criteriaIn) {
      return (criteriaIn != null) && criteriaIn.isSet(maskIn);
   }

   public int setBit(int maskIn) {
      return maskIn | (1 << ordinal());
   }

   public int setBit() {
      return setBit(0);
   }

   public int clearBit(int maskIn) {
      return (maskIn & (~(1 << ordinal())));
   }

   public boolean isSet(int maskIn) {
      return (0 != ((1 << ordinal()) & maskIn));
   }

   private ResourceChoiceCriteria(String descriptionIn) {
      _description = descriptionIn;
   }
}
