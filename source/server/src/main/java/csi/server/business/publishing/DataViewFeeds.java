package csi.server.business.publishing;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

import csi.security.CsiSecurityManager;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.publishing.Asset;
import csi.server.dao.CsiPersistenceManager;
import csi.server.dao.jpa.PublishedAssetsBean;

public class DataViewFeeds {
    public DataViewFeeds() {
    }

    /**
     * Builds a feed from the table named 'Asset' from the DataSource.  This feed
     * provides references to sub-feeds based on each set of unique DataView names.
     * <p>
     * If from is provided, the feed assumes that only a certain range of newly published
     * assets are desired; up to and including <code>to</code>.  If <code>to</code> is null,
     * the current time is assumed.  If <code>from</code> is null, then all DataViews are returned.
     * <p>
     *
     */
   public SyndFeed buildFeed(String rootURL) throws CentrifugeException, SQLException {
      SyndFeed feed = new SyndFeedImpl();

      try (Connection connection = CsiPersistenceManager.getCacheConnection()) {
         PublishedAssetsBean dao = new PublishedAssetsBean();
         List<Asset> findAll = dao.findAll();
         findAll = CsiSecurityManager.filterAssets("read", findAll);

         feed.setFeedType("rss_2.0");
         feed.setTitle("Published Assets");
         feed.setLink(rootURL + "/feeds/main.xml");
         feed.setDescription("Provides a top-level view of the published assets for all known Dataviews.");

         for (Asset asset : findAll) {
            addFeedItem(feed, asset, rootURL);
         }
      }
      return feed;
   }

   @SuppressWarnings("unchecked")
   private static void addFeedItem(SyndFeed feed, Asset asset, String rootURL) {
      SyndEntryImpl entry = new SyndEntryImpl();

      entry.setTitle(asset.getName());
      entry.setLink(new StringBuilder(rootURL).append("/asset/").append(asset.getAssetID()).toString());

      SyndContentImpl desc = new SyndContentImpl();

      desc.setType("text/html");

      String buf = new StringBuilder(asset.getName()).append(" for Dataview <i>")
                             .append(asset.getDataViewName()).append("</i> created by ")
                             .append(asset.getCreatedBy()).append(" on ")
                             .append(asset.getCreationTime().toString())
                             .toString();
      desc.setValue(buf);

      List<SyndCategory> categories = entry.getCategories();
      SyndCategory cat = new SyndCategoryImpl();

      cat.setName(asset.getDataViewName());
      categories.add(cat);

      cat = new SyndCategoryImpl();

      cat.setName(asset.getCreatedBy());
      categories.add(cat);
      entry.setDescription(desc);
      feed.getEntries().add(entry);
   }
}
