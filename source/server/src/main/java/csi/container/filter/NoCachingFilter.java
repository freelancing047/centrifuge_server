package csi.container.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This filter class ensures that content returned to a client contains
 * the appropriate response headers to disable caching.  For example 
 * returning a list of dataviews is cached by some clients; keeping
 * refreshes from being properly displayed.  
 * <p>
 * The headers set for Firefox and IE are:
 * <table>
 * <tr><th>Header Name</th>
 *     <th>Value</th>
 * </tr>
 * <tr>
 *      <td>Pragma</td>
 *      <td>no-cache</td>
 * <tr>
 *      <td>Cache-Control</td>
 *      <td>no-cache</td>
 * </tr>
 * <tr>
 *      <td>Expires</td>
 *      <td>0</td>
 * </tr>
 * </table>
 * @author Tildenwoods
 *
 */
public class NoCachingFilter implements Filter {
   protected static final Logger LOG = LogManager.getLogger(NoCachingFilter.class);

    public void destroy() {
        LOG.info("Removing cache filter.");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // only filter if we're dealing w/ HTTP requests....almost always.
        // if not the cast fails and we'll do default filtering...
        try {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // set the response headers before forwarding....
            // otherwise we might get a committed response and there's nothing
            // we can really
            // do about it--other than buffering the entire response.

            LOG.debug("NoCachingFilter: Setting no-cache headers");
            httpResponse.setHeader("Cache-Control", "no-cache");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);

            // chain.doFilter( httpRequest, httpResponse );
        } finally {
            chain.doFilter(request, response);
        }
    }

    // 
    // determine if servlet request came from MSIE
    //
    public boolean isMSIE(HttpServletRequest request) {
        // See if this is MSIE
        String useragent = request.getHeader("User-Agent");
        boolean isIE = false;

        if ((useragent != null) && (useragent.indexOf("MSIE") != -1)) {
            isIE = true;
        }
        return isIE;
    }

    // 
    // set cache headers to workaround MSIE and SSL problem
    //
    public void setIECacheHeaders(HttpServletResponse response) {
        response.setHeader("pragma", "");
        response.setHeader("cache-control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
        response.setHeader("expires", "0");
    }

    public void init(FilterConfig filterConfig) throws ServletException {
       LOG.debug("Initializing filter to pass commands to disable caching responses");
    }

}
