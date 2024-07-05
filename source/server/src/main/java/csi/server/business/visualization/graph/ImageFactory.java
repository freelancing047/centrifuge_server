package csi.server.business.visualization.graph;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Tuple;

import csi.server.business.service.icon.IconActionsService;
import csi.server.business.visualization.graph.renderers.ImageLocation;
import csi.server.common.exception.CentrifugeException;

/**
 * <p>Utility class that manages loading and storing images. Includes a
 * configurable LRU cache for managing loaded images. Also supports optional
 * image scaling of loaded images to cut down on memory and visualization
 * operation costs.</p>
 *
 * <p>By default images are loaded upon first request. Use the
 * {@link #preloadImages(Iterator, String)} method to load images before they
 * are requested for rendering.</p>
 *
 * @author alan newberger
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ImageFactory {
   private static final Logger LOG = LogManager.getLogger(ImageFactory.class);

    protected int m_imageCacheSize = 3000;
    protected int m_maxImageWidth = 100;
    protected int m_maxImageHeight = 100;
    protected boolean m_asynch = false;

    // a nice LRU cache courtesy of java 1.4
    protected Map imageCache = new LinkedHashMap((int) (m_imageCacheSize + (1 / .75F)), .75F, true) {

        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > m_imageCacheSize;
        }
    };
    protected Map loadMap = new HashMap(50);

    protected final Component component = new Component() {
    };
    protected final MediaTracker tracker = new MediaTracker(component);
    protected int nextTrackerID = 0;

    /**
     * Create a new ImageFactory. Assumes no scaling of loaded images.
     */
    public ImageFactory() {
    }

    /**
     * Create a new ImageFactory. This instance will scale loaded images
     * if they exceed the threshold arguments.
     * @param maxImageWidth the maximum width of input images
     *  (-1 means no limit)
     * @param maxImageHeight the maximum height of input images
     *  (-1 means no limit)
     */
    public ImageFactory(int maxImageWidth, int maxImageHeight) {
        setMaxImageDimensions(maxImageWidth, maxImageHeight);
    }

    /**
     * Sets the maximum image dimensions of loaded images, images larger than
     * these limits will be scaled to fit within bounds.
     * @param width the maximum width of input images (-1 means no limit)
     * @param height the maximum height of input images (-1 means no limit)
     */
    public void setMaxImageDimensions(int width, int height) {
        m_maxImageWidth = width;
        m_maxImageHeight = height;
    }

    /**
     * Sets the capacity of this factory's image cache
     * @param size the new size of the image cache
     */
    public void setImageCacheSize(int size) {
        m_imageCacheSize = size;
    }

    /**
     * Indicates if the given string location corresponds to an image
     * currently stored in this ImageFactory's cache.
     * @param imageLocation the image location string
     * @return true if the location is a key for a currently cached image,
     * false otherwise.
     */
    public boolean isInCache(String imageLocation) {
        return imageCache.containsKey(imageLocation);
    }

    /**
     * <p>Get the image associated with the given location string. If the image
     * has already been loaded, it simply will return the image, otherwise it
     * will load it from the specified location.</p>
     *
     * <p>The imageLocation argument must be a valid resource string pointing
     * to either (a) a valid URL, (b) a file on the classpath, or (c) a file
     * on the local filesystem. The location will be resolved in that order.
     * </p>
     *
     * @param iconid the image location as a resource string.
     * @return the corresponding image, if available
     */
    public Image getImage(String iconId,int w,int h) {
        /*
         * NOTE: This function always honors the height/width ratio of the params, but may return smaller images.
         * Up scaling is disallowed to prevent putting large objects in the heap.
         * Images are never returned larger than m_maxImageWidth by m_maxImageHeight.
         * If high resolution images are needed for a node, these parameters will need to change.
         * May return null if image is not found.
         */
        Image image = getImage(iconId);//try to open the image
        if (image != null) {
            //These checks to avoid storing large images in the cache.
            int iw = image.getWidth(null);
            int ih = image.getHeight(null);
            double r = (double)h/(double)w;
            double ir = (double)ih/(double)iw;
            if(r<ir){//requested h/w ratio is less than the source
                w = (int)(((double)iw*(double)h)/ih);
            }
            if(r>ir){//request h/w ration is greater than the source
                h = (int)(((double)ih*(double)w)/iw);
            }
            String s = new StringBuilder().append("w").append(w).append("h").append(h).append(iconId).toString();
            image = (Image) imageCache.get(s); //check cache

            if (image == null) { //missed cache
                image = getImage(iconId);
                image = addImage(s, w, h, image);
            }
        }
        return image;
    }

    public Image getImage(String iconId) {
        if((iconId == null) || iconId.isEmpty()){
            return null;
        }
        Image image = (Image) imageCache.get(iconId);
        if (image == null) {

            if(iconId.contains(":") || iconId.contains("/") || iconId.contains("\\")){
                image = readImageFromLocation(iconId);
            } else {

                try {
                    image = IconActionsService.getImageForServer(iconId);
                } catch (CentrifugeException e) {
                    image = readImageFromLocation(iconId);
                }
            }
            if (image == null) {
                return null;
            }
            //don't keep big images
            int w = Math.min(image.getWidth(null), m_maxImageWidth);
            int h = Math.min(image.getHeight(null), m_maxImageHeight);
            addImage(iconId, w, h, image);
        }
        return image;
    }

    public static Image readImageFromLocation(String iconId) {
        Image image;
        ImageLocation imageLocation = new ImageLocation(iconId);
        image = imageLocation.read();
        return image;
    }


    /**
     * <p>Get the image associated with the given location string. If the image
     * has already been loaded, it simply will return the image, otherwise it
     * will load it from the specified location.</p>
     *
     * <p>The imageLocation argument must be a valid resource string pointing
     * to either (a) a valid URL, (b) a file on the classpath, or (c) a file
     * on the local filesystem. The location will be resolved in that order.
     * </p>
     *
     * @param imageLocation the image location as a resource string.
     * @return the corresponding image, if available
     */
    public Image getImage(ImageLocation imageLocation,int w,int h) {
        /*
         * NOTE: This function always honors the height/width ratio of the params, but may return smaller images.
         * Up scaling is disallowed to prevent putting large objects in the heap.
         * Images are never returned larger than m_maxImageWidth by m_maxImageHeight.
         * If high resolution images are needed for a node, these parameters will need to change.
         * May return null if image is not found.
         */
        Image image = getImage(imageLocation);//try to open the image
        if (image != null) {
            //These checks to avoid storing large images in the cache.
            int iw = image.getWidth(null);
            int ih = image.getHeight(null);
            double r = (double)h/(double)w;
            double ir = (double)ih/(double)iw;
            if(r<ir){//requested h/w ratio is less than the source
                w = (int)(((double)iw*(double)h)/ih);
            }
            if(r>ir){//request h/w ration is greater than the source
                h = (int)(((double)ih*(double)w)/iw);
            }
            String s = new StringBuilder().append("w").append(w).append("h").append(h).append(imageLocation).toString();
            image = (Image) imageCache.get(s); //check cache

            if (image == null) { //missed cache
                image = getImage(imageLocation);
                image = addImage(s, w, h, image);
            }
        }
        return image;
    }

    public Image getImage(ImageLocation imageLocation) {
        Image image = (Image) imageCache.get(imageLocation.toString());
        if (image == null) {
            image = imageLocation.read();
            if (image == null) {
                return null;
            }
            //don't keep big images
            int w = Math.min(image.getWidth(null), m_maxImageWidth);
            int h = Math.min(image.getHeight(null), m_maxImageHeight);
            addImage(imageLocation.toString(), w, h, image);
        }
        return image;
    }

    public Image getImage(Image bufferedImage, String id,int w,int h) {
        /*
         * NOTE: This function always honors the height/width ratio of the params, but may return smaller images.
         * Up scaling is disallowed to prevent putting large objects in the heap.
         * Images are never returned larger than m_maxImageWidth by m_maxImageHeight.
         * If high resolution images are needed for a node, these parameters will need to change.
         * May return null if image is not found.
         */
        Image image = getImage(bufferedImage, id);//try to open the image
        if (image != null) {
            //These checks to avoid storing large images in the cache.
            int iw = image.getWidth(null);
            int ih = image.getHeight(null);
            double r = (double)h/(double)w;
            double ir = (double)ih/(double)iw;
            if(r<ir){//requested h/w ratio is less than the source
                w = (int)(((double)iw*(double)h)/ih);
            }
            if(r>ir){//request h/w ration is greater than the source
                h = (int)(((double)ih*(double)w)/iw);
            }
            String s = new StringBuilder().append("w").append(w).append("h").append(h).append(id).toString();
            image = (Image) imageCache.get(s); //check cache

            if (image == null) { //missed cache
                image = getImage(bufferedImage, id);
                image = addImage(s, w, h, image);
            }
        }
        return image;
    }

    public Image getImage(Image bufferedImage, String id) {
        Image image = (Image) imageCache.get(id);
        if (image == null) {
            image = bufferedImage;
            if (image == null) {
                return null;
            }
            //don't keep big images
            int w = Math.min(image.getWidth(null), m_maxImageWidth);
            int h = Math.min(image.getHeight(null), m_maxImageHeight);
            addImage(id, w, h, image);
        }
        return image;
    }
    /**
     * Adds an image associated with a location string to this factory's cache.
     * The image will be scaled as dictated by this current factory settings.
     *
     * @param key the uniquely identifying id for the image
     * @param w width
     * @param h height
     * @param image the actual image
     * @return the final image added to the cache. This may be a scaled version
     *         of the original input image.
     */
    public Image addImage(String key, int w, int h, Image image) {
        image = getScaledImage(image,w,h);
        image.flush();
        imageCache.put(key, image);
        return image;
    }

    /**
     * Wait for an image to load.
     * @param image the image to wait for
     */
    protected void waitForImage(Image image) {
        int id = ++nextTrackerID;
        tracker.addImage(image, id);
        try {
            tracker.waitForID(id, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tracker.removeImage(image, id);
    }

    /**
     * Scales an image to fit within the current size thresholds.
     * @param img the image to scale
     * @param h
     * @param w
     * @return the scaled image
     */
    protected Image getScaledImage(Image img, int w, int h) {
    	if((w == 0) || (h == 0)){
    	  LOG.warn("Height or width was zero when attempting to scale, using unscaled image by default");
    		return img;
    	}
        int iw = img.getWidth(null);
        int ih = img.getHeight(null);
        double r = (double)h/(double)w;
        double ir = (double)ih/(double)iw;
        if(r<ir){//requested h/w ratio is less than the source
            w = (int)(((double)iw*(double)h)/ih);
        }
        if(r>ir){//request h/w ration is greater than the source
            h = (int)(((double)ih*(double)w)/iw);
        }
        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        if (scaled instanceof BufferedImage) {
            return scaled;
        } else {
            BufferedImage bufimage = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufimage.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(scaled, null, null);
            g.dispose();
            scaled.flush();
            return bufimage;
        }
    }

    /**
     * <p>Preloads images for use in a visualization. Images to load are
     * determined by taking objects from the given iterator and retrieving
     * the value of the specified field. The items in the iterator must
     * be instances of the {@link prefuse.data.Tuple} class.</p>
     *
     * <p>Images are loaded in the order specified by the iterator until the
     * the iterator is empty or the maximum image cache size is met. Thus
     * higher priority images should appear sooner in the iteration.</p>
     *
     * @param iter an Iterator of {@link prefuse.data.Tuple} instances
     * @param field the data field that contains the image location
     */
    public void preloadImages(Iterator iter, String field) {
        boolean synch = m_asynch;
        m_asynch = false;

        String loc = null;
        while (iter.hasNext() && (imageCache.size() <= m_imageCacheSize)) {
            // get the string describing the image location
            Tuple t = (Tuple) iter.next();
            loc = t.getString(field);
            if (loc != null) {
                getImage(new ImageLocation(loc));
            }
        }
        m_asynch = synch;
    }

    /**
     * Helper class for storing an id/image pair.
     */
    private class LoadMapEntry {

        public int id;
        public Image image;

        public LoadMapEntry(int id, Image image) {
            this.id = id;
            this.image = image;
        }
    }


} // end of class ImageFactory
