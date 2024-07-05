package csi.client.gwt.viz.shared.chrome.panel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public enum RenderSize {
    //disabling for now by setting the minimum size to be invalid.
    MICRO(-1) {

        @Override
        public boolean showButtons() {
            return false;
        }
    },
    COMPACT(-1) {

        @Override
        public boolean showButtons() {
            return false;
        }
    },
    NORMAL(0) {

        @Override
        public boolean showButtons() {
            return true;
        }
    };

    static {
        List<RenderSize> list = Lists.newArrayList(RenderSize.values());
        Collections.sort(list, new Comparator<RenderSize>() {

            @Override
            public int compare(RenderSize o1, RenderSize o2) {
                return Ints.compare(o1.getMinSize(), o2.getMinSize());
            }
        });
        Collections.reverse(list);
        orderedRenderSizes = list;
    }

    private static List<RenderSize> orderedRenderSizes;

    private int minSize;

    private RenderSize(int minSize) {
        this.minSize = minSize;
    }

    public int getMinSize() {
        return minSize;
    }

    public abstract boolean showButtons();

    public static RenderSize forSize(int width) {
        for (RenderSize renderSize : orderedRenderSizes) {
            if (width >= renderSize.getMinSize()) {
                return renderSize;
            }
        }
        return RenderSize.MICRO;
    }
}
