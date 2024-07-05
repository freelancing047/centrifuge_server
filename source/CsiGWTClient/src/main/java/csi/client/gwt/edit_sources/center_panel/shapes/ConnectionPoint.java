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
package csi.client.gwt.edit_sources.center_panel.shapes;

import com.emitrom.lienzo.client.core.shape.Circle;
import com.emitrom.lienzo.client.core.shape.Group;
import com.emitrom.lienzo.client.core.shape.Line;
import com.emitrom.lienzo.client.core.shape.Rectangle;
import com.emitrom.lienzo.client.core.types.Point2D;
import com.emitrom.lienzo.client.core.types.Point2DArray;
import com.emitrom.lienzo.shared.core.types.ColorName;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class ConnectionPoint extends Group {

    static final int CIRCLE_RADIUS = 5;
    private static final int FULL_LINE_LENGTH = 10;
    private static final int MINI_LINE_LENGTH = 1;

    private static int LINE_LENGTH = FULL_LINE_LENGTH;
    private static boolean _hideDisplay = false;

    private PortType _portType;
    private boolean _connected;
    private Rectangle _enclosingRect;

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public static void changeDisplayFormat(boolean isFullIn) {

        LINE_LENGTH = (isFullIn) ? FULL_LINE_LENGTH : MINI_LINE_LENGTH;
    }

    public static void _hideDisplay(boolean doHideIn) {

        _hideDisplay = doHideIn;
    }

    public static double getLength() {

        return CIRCLE_RADIUS * 2 + LINE_LENGTH;
    }

    public ConnectionPoint(PortType joinType) {
        super();
        this._portType = joinType;
        init();
    }

    public double getWidth() {

        switch (_portType) {
            case JOIN_LEFT:
            case JOIN_RIGHT:
                return CIRCLE_RADIUS * 2 + LINE_LENGTH;
            case APPEND_TOP:
            case APPEND_BOTTOM:
                return CIRCLE_RADIUS * 2;
            default:
                throw new RuntimeException(i18n.connectionPointExceptionMessage() + _portType); //$NON-NLS-1$
        }
    }

    public double getHeight() {

        switch (_portType) {
            case JOIN_LEFT:
            case JOIN_RIGHT:
                return CIRCLE_RADIUS * 2;
            case APPEND_TOP:
            case APPEND_BOTTOM:
                return CIRCLE_RADIUS * 2 + LINE_LENGTH;
            default:
                throw new RuntimeException(i18n.connectionPointExceptionMessage() + _portType); //$NON-NLS-1$
        }
    }

    public static double getOverlap() {

        return CIRCLE_RADIUS * 2;
    }

    private void init() {
        Circle circle = new Circle(CIRCLE_RADIUS);
        circle.setStrokeColor(ColorName.BLACK);
        Line line = new Line();
        line.setStrokeColor(ColorName.BLACK);

        switch (_portType) {
            case JOIN_LEFT: {
                circle.setX(CIRCLE_RADIUS);
                circle.setY(CIRCLE_RADIUS);
                line.setPoints(new Point2DArray(new Point2D(CIRCLE_RADIUS * 2, CIRCLE_RADIUS), new Point2D(
                        CIRCLE_RADIUS * 2 + LINE_LENGTH, CIRCLE_RADIUS)));
                _enclosingRect = new Rectangle(CIRCLE_RADIUS * 2 + LINE_LENGTH, CIRCLE_RADIUS * 2);
                break;
            }
            case JOIN_RIGHT: {
                circle.setX(LINE_LENGTH + CIRCLE_RADIUS);
                circle.setY(CIRCLE_RADIUS);
                line.setPoints(new Point2DArray(new Point2D(0, CIRCLE_RADIUS), new Point2D(LINE_LENGTH, CIRCLE_RADIUS)));
                _enclosingRect = new Rectangle(CIRCLE_RADIUS * 2 + LINE_LENGTH, CIRCLE_RADIUS * 2);
                break;
            }
            case APPEND_TOP: {
                circle.setX(CIRCLE_RADIUS);
                circle.setY(CIRCLE_RADIUS);
                line.setPoints(new Point2DArray(new Point2D(CIRCLE_RADIUS, CIRCLE_RADIUS * 2), new Point2D(
                        CIRCLE_RADIUS, CIRCLE_RADIUS * 2 + LINE_LENGTH)));
                _enclosingRect = new Rectangle(CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2 + LINE_LENGTH);
                break;
            }
            case APPEND_BOTTOM: {
                circle.setX(CIRCLE_RADIUS);
                circle.setY(LINE_LENGTH + CIRCLE_RADIUS);
                line.setPoints(new Point2DArray(new Point2D(CIRCLE_RADIUS, 0), new Point2D(CIRCLE_RADIUS, LINE_LENGTH)));
                _enclosingRect = new Rectangle(CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2 + LINE_LENGTH);
                break;
            }
        } // end switch

        _enclosingRect.setAlpha(0.2);
        _enclosingRect.setFillColor("#ecf0f1"); //$NON-NLS-1$

        if (!_hideDisplay) {
            add(circle);
            add(line);
            add(_enclosingRect);
        }
    }

    public boolean isConnected() {
        return _connected;
    }

    public void setConnected(boolean _connected) {
        this._connected = _connected;
    }

    public void highlight(boolean highlight) {
        _enclosingRect.setFillColor(highlight ? "#3498db" : "#ecf0f1"); //$NON-NLS-1$ //$NON-NLS-2$
        _enclosingRect.setAlpha(highlight ? 0.7 : 0.2);
        getLayer().draw();
    }

    public WienzoComposite getComposite() {
        return (WienzoComposite) getParent();
    }

    public PortType getPortType() {
        return _portType;
    }

}
