package csi.server.common.model.visualization.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;

public abstract class AbstractMapSelection implements Selection {
   private boolean lockedByWriter = false;
   private int readerSequenceNumber = 1;
   private List<Integer> readers = new ArrayList<Integer>();

   private String mapViewDefUuid;

   public AbstractMapSelection() {
   }

   public AbstractMapSelection(String mapViewDefUuid) {
      this();
      setMapViewDefUuid(mapViewDefUuid);
   }

   public String getMapViewDefUuid() {
      return mapViewDefUuid;
   }

   public void setMapViewDefUuid(String mapViewDefUuid) {
      this.mapViewDefUuid = mapViewDefUuid;
   }

   public abstract Set<Geometry> getNodes();

   public abstract void setNodes(Set<Geometry> nodes);

   public abstract void addNode(Geometry geometry);

   public abstract void removeNode(Geometry geometry);

   public abstract boolean containsGeometry(Geometry geometry);

   public abstract Set<LinkGeometry> getLinks();

   public abstract void setLinks(Set<LinkGeometry> links);

   public abstract boolean containsLink(LinkGeometry link);

   public abstract void addLink(LinkGeometry link);

   public abstract void addLinks(Set<LinkGeometry> links);

   public abstract void removeLink(LinkGeometry link);

   public abstract void removeLinks(Set<LinkGeometry> linksToRemove);

   public void lock() {
      lockedByWriter = true;
   }

   public boolean isLockedByWriter() {
      return lockedByWriter;
   }

   public void unlock() {
      lockedByWriter = false;
   }

   public int getSequenceNumber() {
      int retVal = readerSequenceNumber;
      readerSequenceNumber++;
      return retVal;
   }

   public void registerReader(Integer reader) {
      readers.add(reader);
   }

   public void deregisterReader(Integer reader) {
      readers.remove(reader);
   }

   public boolean hasReaders() {
      return !readers.isEmpty();
   }
}
