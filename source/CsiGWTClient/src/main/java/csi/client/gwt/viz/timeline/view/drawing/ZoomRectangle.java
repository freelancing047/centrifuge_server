package csi.client.gwt.viz.timeline.view.drawing;

import com.google.gwt.canvas.dom.client.Context2d;

import csi.client.gwt.widget.drawing.Rectangle;

public class ZoomRectangle extends Rectangle {

	public ZoomRectangle(int x, int y, int width, int height) {
		super(x, y, width, height);
	}


	@Override
	public void render(Context2d context2d) {
		context2d.setGlobalAlpha(.3);
		super.render(context2d);
		context2d.setGlobalAlpha(1);

		
		
	}
	
}
