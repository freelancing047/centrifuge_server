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
package csi.server.business.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import csi.server.common.service.api.ColorActionsServiceProtocol;
import csi.server.util.ImageUtil;
import csi.shared.core.color.BrewerColorSet;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.ColorUtil;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColorActionsService implements ColorActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(ColorActionsService.class);

    private Resource brewerFile;

    public Resource getBrewerFile() {
        return brewerFile;
    }

    public void setBrewerFile(Resource brewerFile) {
        this.brewerFile = brewerFile;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Map<Integer, List<BrewerColorSet>>> getBrewerColors() {
        try {
            List<String> lines = FileUtils.readLines(brewerFile.getFile());

            Map<String, Map<Integer, List<BrewerColorSet>>> colorsByTypeAndSize = new HashMap<String, Map<Integer, List<BrewerColorSet>>>();
            BrewerColorSet current = null;
            Map<Integer, List<BrewerColorSet>> colorsBySize = null;
            boolean firstLine = true;
            for (String line : lines) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                List<String> parts = Lists.newArrayList(Splitter.on(',').trimResults().split(line));
                current = new BrewerColorSet();

                if (parts.get(5).length() > 0) {
                    current.setName(parts.get(0));
                    colorsBySize = colorsByTypeAndSize.get(parts.get(5));
                    if (colorsBySize == null) {
                        colorsBySize = new HashMap<Integer, List<BrewerColorSet>>();
                        colorsByTypeAndSize.put(parts.get(5), colorsBySize);
                    }
                    List<BrewerColorSet> list = new ArrayList<BrewerColorSet>();
                    list.add(current);
                    colorsBySize.put(Integer.decode(parts.get(1)), list);
                    current.getColors().add(
                            ColorUtil.toColorString(Integer.parseInt(parts.get(2)), Integer.parseInt(parts.get(3)),
                                    Integer.parseInt(parts.get(4))));
                } else if (parts.get(1).length() > 0) {
                    // New color size.
                    BrewerColorSet set = new BrewerColorSet();
                    set.setName(current.getName());
                    if (colorsBySize == null) {
                       colorsBySize = new HashMap<Integer, List<BrewerColorSet>>();
                    }
                    List<BrewerColorSet> list = colorsBySize.get(Integer.decode(parts.get(1)));
                    if (list == null) {
                        list = new ArrayList<BrewerColorSet>();
                        colorsBySize.put(Integer.decode(parts.get(1)), list);
                    }
                    list.add(set);
                    current = set;
                    current.getColors().add(
                            ColorUtil.toColorString(Integer.parseInt(parts.get(2)), Integer.parseInt(parts.get(3)),
                                    Integer.parseInt(parts.get(4))));
                } else {
                    // Just additional colors for existing set.
                    current.getColors().add(
                            ColorUtil.toColorString(Integer.parseInt(parts.get(2)), Integer.parseInt(parts.get(3)),
                                    Integer.parseInt(parts.get(4))));
                }
            }
            return colorsByTypeAndSize;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

   @PostConstruct
   public void init() {
      if (LOG.isTraceEnabled()) {
         Map<String,Map<Integer,List<BrewerColorSet>>> c = getBrewerColors();

         for (Map.Entry<String,Map<Integer,List<BrewerColorSet>>> entry : c.entrySet()) {
            String type = entry.getKey();

            for (Integer size : entry.getValue().keySet()) {
               for (BrewerColorSet set : c.get(type).get(size)) {
                  LOG.trace("Type: {}, Size: {}, Colors: {}", () -> type, () -> size, () -> set);
               }
            }
         }
      }
   }

    /**
     *
     * @param width width of desired image
     * @param height height of desired image
     * @param colorModel Color model being used
     * @param direction Direction in which to vary (horizontal is min:max = left:right and vertical is top:bottom)
     * @return Base64 encoded image.
     */
    @Override
    public String getColorRangeSample(int width, int height, ColorModel colorModel, RangeDirection direction) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] data = new int[width * height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = direction == RangeDirection.HORIZONTAL ? colorModel.getColorRGB((x), 0, width) :
                            colorModel.getColorRGB((height - y), 0, height);
                data[(y * width) + x] = color;
            }
        }

        image.getWritableTile(0, 0).setDataElements(0, 0, width, height, data);
        return ImageUtil.toBase64String(image);
    }
}
