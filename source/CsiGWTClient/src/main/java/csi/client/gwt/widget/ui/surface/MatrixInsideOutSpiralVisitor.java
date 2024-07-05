/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.widget.ui.surface;

import java.util.HashSet;
import java.util.Set;

/**
 * A matrix visitor that starts at a given point and spirals out. 
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixInsideOutSpiralVisitor {

    public enum Direction {
        LEFT, RIGHT, UP, DOWN;

        static {
            for (Direction dir : values()) {
                switch (dir) {
                    case DOWN:
                        dir.next = Direction.LEFT;
                        break;
                    case LEFT:
                        dir.next = Direction.UP;
                        break;
                    case RIGHT:
                        dir.next = Direction.DOWN;
                        break;
                    case UP:
                        dir.next = Direction.RIGHT;
                }
            }
        }

        private Direction next;

        public Direction getNext() {
            return next;
        }

    }

    private int sizeX, sizeY;
    private Direction currentDirection = null;
    private Set<SurfaceLocation> locationsVisited = new HashSet<SurfaceLocation>();
    private int visitedWithinBoundsCount;

    /**
     * @param sizeX
     * @param sizeY
     */
    public MatrixInsideOutSpiralVisitor(int sizeX, int sizeY) {
        super();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    /**
     * @param current Current location 
     * @return Next location to be visited (for started location returns the location itself, once all locations have
     * been visited, returns null).
     */
    public SurfaceLocation getNext(SurfaceLocation current) {
        // A simple test to check if we have exhausted the full matrix.
        if (visitedWithinBoundsCount >= 100) {
            return null;
        } else if (currentDirection == null) {
            return visited(current, Direction.UP, true);
        } else {
            SurfaceLocation next = null;

            next = current.move(currentDirection.getNext());
            Direction nextDirection = currentDirection.getNext();
            while (visitedWithinBoundsCount <= sizeX * sizeY) {
                if (!isVisited(next) && next.isWithinBounds(sizeX, sizeY)) {
                    return visited(next, nextDirection, true);
                } else if (!isVisited(next) && !next.isWithinBounds(sizeX, sizeY)) {
                    current = visited(next, nextDirection, false);
                    next = current.move(currentDirection.getNext());
                    nextDirection = currentDirection.getNext();
                } else if (isVisited(next)) {
                    next = current.move(currentDirection);
                    nextDirection = currentDirection;
                }
            }
            return null;
        }
    }

    private boolean isVisited(SurfaceLocation location) {
        return locationsVisited.contains(location);
    }

    private SurfaceLocation visited(SurfaceLocation location, Direction nextDirection, boolean inBounds) {
        if (inBounds) {
            visitedWithinBoundsCount ++;
        }
        locationsVisited.add(location);
        currentDirection = nextDirection;
        return location;
    }

    // FIXME: Move this to a proper unit test.
//    public static void main(String[] args) {
//        MatrixInsideOutSpiralVisitor visitor = new MatrixInsideOutSpiralVisitor(20, 20);
//        SurfaceLocation l = new SurfaceLocation(18, 18);
//        for (int i = 0; i < 30; i++) {
//            SurfaceLocation t = visitor.getNext(l);
//            System.out.println(t);
//            l = t;
//        }
//    }
}
