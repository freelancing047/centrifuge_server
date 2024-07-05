package csi.server.business.visualization.graph.layout;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.Integrator;

import com.google.common.collect.Iterators;

/**
 * Updates velocity and position data using the 4th-Order Runge-Kutta method.
 * It is slower but more accurate than other techniques such as Euler's Method.
 * The technique requires re-evaluating forces 4 times for a given timestep.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class CsiRungeKuttaIntegrator implements Integrator {

    public double minx = 0, miny = 0, maxx = 0, maxy = 0;
    public Rectangle2D bounds = new Rectangle2D.Double();

    public CsiRungeKuttaIntegrator() {
        super();
    }

    /**
     * @see prefuse.util.force.Integrator#integrate(prefuse.util.force.ForceSimulator, long)
     */
    public void integrate(ForceSimulator sim, long timestep) {
        float speedLimit = sim.getSpeedLimit();
        float vx, vy, v, coeff;
        float[][] k, l;

        bounds.setRect(Double.NaN, Double.NaN, 0, 0);

        Iterator iter = Iterators.filter(sim.getItems(), ForceItem.Functions.IsNotLocked);
        while (iter.hasNext()) {
            //TaskHelper.checkForCancel();

            ForceItem item = (ForceItem) iter.next();
            coeff = timestep / item.mass;
            k = item.k;
            l = item.l;
            item.plocation[0] = item.location[0];
            item.plocation[1] = item.location[1];
            k[0][0] = timestep * item.velocity[0];
            k[0][1] = timestep * item.velocity[1];
            l[0][0] = coeff * item.force[0];
            l[0][1] = coeff * item.force[1];

            // Set the position to the new predicted position
            item.location[0] += 0.5f * k[0][0];
            item.location[1] += 0.5f * k[0][1];

        }

        // recalculate forces
        sim.accumulate();

        iter = Iterators.filter(sim.getItems(), ForceItem.Functions.IsNotLocked);
        while (iter.hasNext()) {
            //TaskHelper.checkForCancel();

            ForceItem item = (ForceItem) iter.next();
            coeff = timestep / item.mass;
            k = item.k;
            l = item.l;
            vx = item.velocity[0] + .5f * l[0][0];
            vy = item.velocity[1] + .5f * l[0][1];
            v = (float) Math.sqrt(vx * vx + vy * vy);
            if (v > speedLimit) {
                vx = speedLimit * vx / v;
                vy = speedLimit * vy / v;
            }
            k[1][0] = timestep * vx;
            k[1][1] = timestep * vy;
            l[1][0] = coeff * item.force[0];
            l[1][1] = coeff * item.force[1];

            // Set the position to the new predicted position
            item.location[0] = item.plocation[0] + 0.5f * k[1][0];
            item.location[1] = item.plocation[1] + 0.5f * k[1][1];

        }

        // recalculate forces
        sim.accumulate();

        iter = Iterators.filter(sim.getItems(), ForceItem.Functions.IsNotLocked);
        while (iter.hasNext()) {
            //TaskHelper.checkForCancel();

            ForceItem item = (ForceItem) iter.next();
            coeff = timestep / item.mass;
            k = item.k;
            l = item.l;
            vx = item.velocity[0] + .5f * l[1][0];
            vy = item.velocity[1] + .5f * l[1][1];
            v = (float) Math.sqrt(vx * vx + vy * vy);
            if (v > speedLimit) {
                vx = speedLimit * vx / v;
                vy = speedLimit * vy / v;
            }
            k[2][0] = timestep * vx;
            k[2][1] = timestep * vy;
            l[2][0] = coeff * item.force[0];
            l[2][1] = coeff * item.force[1];

            // Set the position to the new predicted position
            item.location[0] = item.plocation[0] + 0.5f * k[2][0];
            item.location[1] = item.plocation[1] + 0.5f * k[2][1];

        }

        // recalculate forces
        sim.accumulate();

        // iter = sim.getItems();
        iter = Iterators.filter(sim.getItems(), ForceItem.Functions.IsNotLocked);
        while (iter.hasNext()) {
            //TaskHelper.checkForCancel();

            ForceItem item = (ForceItem) iter.next();
            coeff = timestep / item.mass;
            k = item.k;
            l = item.l;
            float[] p = item.plocation;
            vx = item.velocity[0] + l[2][0];
            vy = item.velocity[1] + l[2][1];
            v = (float) Math.sqrt(vx * vx + vy * vy);
            if (v > speedLimit) {
                vx = speedLimit * vx / v;
                vy = speedLimit * vy / v;
            }
            k[3][0] = timestep * vx;
            k[3][1] = timestep * vy;
            l[3][0] = coeff * item.force[0];
            l[3][1] = coeff * item.force[1];
            item.location[0] = p[0] + (k[0][0] + k[3][0]) / 6.0f + (k[1][0] + k[2][0]) / 3.0f;
            item.location[1] = p[1] + (k[0][1] + k[3][1]) / 6.0f + (k[1][1] + k[2][1]) / 3.0f;

            vx = (l[0][0] + l[3][0]) / 6.0f + (l[1][0] + l[2][0]) / 3.0f;
            vy = (l[0][1] + l[3][1]) / 6.0f + (l[1][1] + l[2][1]) / 3.0f;
            v = (float) Math.sqrt(vx * vx + vy * vy);
            if (v > speedLimit) {
                vx = speedLimit * vx / v;
                vy = speedLimit * vy / v;
            }
            item.velocity[0] += vx;
            item.velocity[1] += vy;

        }

        iter = sim.getItems();
        while (iter.hasNext()) {
            //TaskHelper.checkForCancel();

            ForceItem item = (ForceItem) iter.next();
            updateReferenceItem(item);
        }
    }

    private void updateReferenceItem(ForceItem fitem) {
        double x = fitem.location[0];
        double y = fitem.location[1];

        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }

        if (Double.isNaN(bounds.getX()) || Double.isNaN(bounds.getY())) {
            bounds.setRect(x, y, 0, 0);
        }
        bounds.add(x, y);

    }

}
