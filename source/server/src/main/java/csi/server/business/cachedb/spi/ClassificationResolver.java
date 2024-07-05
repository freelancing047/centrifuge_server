package csi.server.business.cachedb.spi;

import java.util.Collection;

public interface ClassificationResolver
{
    String resolveBanner( Collection<String> classes, Collection<String> compartments, Collection<String> caveats);

}
