package csi.server.business.visualization.graph.stat;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.CsiUUID;

public interface GraphStatisticalPopulation extends IsSerializable {
   public String getUUID();
   public String getLabel();

   public abstract class AbstractGraphStatisticalPopulation implements GraphStatisticalPopulation {
      CsiUUID uuid = new CsiUUID();

      public String getUUID() {
         return uuid.toString();
      }
   }

   public class BundlePopulation extends AbstractGraphStatisticalPopulation {
      @Override
      public String getLabel() {
         return "Bundles";
      }
   }

   public class ComponentPopulation extends AbstractGraphStatisticalPopulation {
      @Override
      public String getLabel() {
         return "Components";
      }
   }

   public class LinkPopulation extends AbstractGraphStatisticalPopulation {
      @Override
      public String getLabel() {
         return "All Links";
      }
   }

   public class NodePopulation extends AbstractGraphStatisticalPopulation {
      @Override
      public String getLabel() {
         return "All Nodes";
      }
   }

   public class TypePopulation extends AbstractGraphStatisticalPopulation {
      private String type;

      public TypePopulation(String type) {
         this.type = type;
      }

      public TypePopulation() {
      }

      public String getType() {
         return type;
      }

      @Override
      public String getLabel() {
         return getType();
      }
   }

   public class LinkTypePopulation extends AbstractGraphStatisticalPopulation {
      private String type;

      public LinkTypePopulation(String type) {
         this.type = type;
      }

      public LinkTypePopulation() {
      }

      @Override
      public String getLabel() {
         return type;
      }

      public String getType() {
         return type;
      }
   }
}
