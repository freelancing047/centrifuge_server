package csi.server.business.cachedb.spi;

import java.util.Collection;

/**
 * This implementation of a {@link ClassificationResolver} encapsulates <i>system high</i>
 * behavior.  This results in the following:
 * 
 * <ul>
 * <li>The highest classification is selected from the <i>classes</i> collection.
 * <li><i>compartments</i> are concatenated together with a <i>/</i> separator
 * <li><i>caveats</i> are concatenated together with a <i>/</i> separator.
 * </ul>
 * 
 * The results of the three steps above are then concatenated together using <i>//</i> as a separator.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class SystemHighClassificationResolver
    implements ClassificationResolver
{

    @Override
    public String resolveBanner(Collection<String> classes, Collection<String> compartments, Collection<String> caveats) {
        return null;
    }

}
