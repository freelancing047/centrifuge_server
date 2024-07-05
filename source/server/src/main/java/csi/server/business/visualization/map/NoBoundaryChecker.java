package csi.server.business.visualization.map;

class NoBoundaryChecker extends AbstractBoundaryChecker {
   private static AbstractBoundaryChecker instance;

   public static AbstractBoundaryChecker getInstance() {
      if (instance == null) {
         instance = new NoBoundaryChecker();
      }
      return instance;
   }

   @Override
   public boolean isPointInBoundary(double latitude, double longitude) {
      return true;
   }

//	@Override
//	boolean isPointInBoundary(MapNode mapNode) {
//		return true;
//	}
}
