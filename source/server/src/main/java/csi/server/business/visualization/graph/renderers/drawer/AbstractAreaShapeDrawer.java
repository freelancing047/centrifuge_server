package csi.server.business.visualization.graph.renderers.drawer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

import csi.config.Configuration;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.pattern.selection.PatternSelection;
import csi.server.business.visualization.graph.renderers.VisualItemPropertiesExtractor;
import csi.server.common.model.visualization.selection.SelectionModel;

public abstract class AbstractAreaShapeDrawer {
    private static final double PLUNKED_GLOW_SIZE = 2.0;
    private static final Color PLUNKED_COLOR = new Color(255, 140, 255, 80);

    protected int alpha;
    protected Graphics2D g;
    protected Shape shape;
    protected VisualItem item;
    protected VisualItemPropertiesExtractor itemExtractor;

    protected AbstractAreaShapeDrawer(Graphics2D g, Shape shape, VisualItem item, int alpha) {
    	this.alpha = alpha;
        this.g = g;
        this.shape = shape;
        this.item = item;
        itemExtractor = new VisualItemPropertiesExtractor(item);
    }


    public void drawShape() {
        if (shape == null) {
            return;
        }

        Color origColor = g.getColor();
        Stroke origStroke = g.getStroke();

        boolean selected = itemExtractor.isSelected();
        boolean pathHighlighted = itemExtractor.isPathHighlighted();
        double thickness = itemExtractor.getThickness();

        if (selected) {
            drawSelectedShape(thickness, alpha);
        }

        if(itemExtractor.isPlunked()){
            drawPlunkedIndicator(thickness, alpha);
        }

        if (pathHighlighted){
            drawPathHighlightedShape(thickness, alpha);
        }
        drawPatternHighlightedShape(thickness, alpha);
        prepareGraphics(g, shape, thickness, origColor, alpha);
        drawSimpleShape(g, shape, item, alpha);

        if (item.isHighlighted()) {
            drawHighlightedShape(thickness, alpha);
        }

        g.setStroke(origStroke);
        g.setColor(origColor);
    }

    protected abstract void drawSimpleShape(Graphics2D g, Shape shape, VisualItem item, int alpha);

    private void drawSelectedShape(double thickness, int alpha) {
        double glowsize = thickness + 3;
        double absSize = glowsize;
        double scaleX = g.getTransform().getScaleX();
        if (scaleX < 1) {
            absSize = glowsize / scaleX;
        }
        prepareGraphics(g, shape, absSize, Configuration.getInstance().getGraphAdvConfig().getDefaultSelectionColor(), alpha);
        g.draw(shape);
    }

    private void drawPlunkedIndicator(double thickness, int alpha) {
    	double glowsize = thickness + PLUNKED_GLOW_SIZE;
        double absSize = glowsize;
        double scaleX = g.getTransform().getScaleX();
        if (scaleX < 1) {
            absSize = glowsize / scaleX;
        }
        prepareGraphics(g, shape, absSize, PLUNKED_COLOR, alpha);
        g.draw(shape);
    }


    private void drawPathHighlightedShape(double thickness, int alpha) {
        double glowsize = thickness + 3;
        double absSize = glowsize;
        double scaleX = g.getTransform().getScaleX();
        if (scaleX < 1) {
            absSize = glowsize / scaleX;
        }
        prepareGraphics(g, shape, absSize, Configuration.getInstance().getGraphAdvConfig().getPathHighlightColor(), alpha);
        g.draw(shape);
    }

    private void drawPatternHighlightedShape(double thickness, int alpha) {
        AffineTransform oldTransform = g.getTransform();
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        {
            double dx = .8;
            double dy = .8;
            if (shape instanceof Line2D) {
                double r;
                Line2D line = (Line2D) shape;
                dy = line.getY1() - line.getY2();
                dx = line.getX1() - line.getX2();
                r = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                dx /= r;
                dy /= r;
            }
            {
                List<PatternSelection> matchingPatterns = new ArrayList<PatternSelection>();
                List<PatternSelection> patternHighlights = GraphContext.Current.get().getPatternHighlights();
                if (patternHighlights != null) {
                    for (PatternSelection patternHighlight : patternHighlights) {
                        boolean isHighlighted = false;
                        int itemRow = item.getRow();
                        SelectionModel defSelectionModel = patternHighlight.getSelectionModel();
                        if (defSelectionModel != null) {
                            isHighlighted = defSelectionModel.links.contains(itemRow);
                        }
                        if (isHighlighted) {
                            matchingPatterns.add(patternHighlight);
                        }
                    }
                    boolean first = true;
                    double glowsize = thickness + 1;
                    double absSize = glowsize;
                    double scaleX = g.getTransform().getScaleX();
                    if (scaleX < 1) {
                        absSize = glowsize / scaleX;
                    }
                    Collections.reverse(matchingPatterns);

                    for (PatternSelection matchingPattern : matchingPatterns) {

                        //NOTE: first time need to translate by thickness, and then by pen size.
                        if (first) {
                            first = false;
                            if (scaleX < 1) {

                                g.translate((((thickness / scaleX) + absSize) / 2) * -dy, (((thickness / scaleX) + absSize) / 2) * dx);
                            } else {
                                g.translate(((thickness + absSize) / 2) * -dy, ((thickness + absSize) / 2) * dx);
                            }
                        } else {
                            g.translate(absSize * -dy, absSize * dx);
                        }
                        try {
                            Color c3 = new Color(Integer.parseInt(matchingPattern.getColor()));
                            prepareGraphics(g, shape, absSize, c3, alpha);
                            g.draw(shape);
                        } catch (NumberFormatException ignored) {
                        }
                    }

                }

            }
        }
        g.setTransform(oldTransform);
        g.setStroke(oldStroke);
        g.setColor(oldColor);
    }

    protected void drawHighlightedShape(double thickness, int alpha) {
        prepareGraphics(g, shape, thickness + 2, Configuration.getInstance().getGraphAdvConfig().getHighlightColor(), alpha);
        g.draw(shape);
    }

    protected void prepareGraphics(Graphics2D g, Shape shape, double thickness, Color color, int alpha) {
        g.setStroke(new BasicStroke((float) thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color c = ColorLib.getColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        g.setPaint(c);
    }

}
