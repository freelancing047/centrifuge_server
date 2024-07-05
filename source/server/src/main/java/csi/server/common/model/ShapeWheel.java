package csi.server.common.model;


public class ShapeWheel {

    private static String[] shapes = {"Circle", "Diamond", "Hexagon", "Pentagon/House", "Octagon", "Pentagon", "Star", "Square", "Triangle"};

    private static int index = 0;

//    public static String next() {
//        GraphContext context = GraphContext.Current.get();
//        if (context != null) {
//            return next(context.getOptionSet());
//        }
//        return next(null);
//    }
//
//    public static String next(OptionSet optionSet) {
//        String[] localShapes = shapes;
//        if (optionSet != null) {
//            if (!optionSet.shapes.isEmpty()) {
//                localShapes = optionSet.shapes.toArray(new String[0]);
//            }
//        }
//        return localShapes[index++ % localShapes.length];
//
//    }
    
    public static String next() {
        String[] localShapes = shapes;
        return localShapes[index++ % localShapes.length];
    }
}
