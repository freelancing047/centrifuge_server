package csi.container.filter;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.DigestUtils;

public class EtagFilter extends org.springframework.web.filter.ShallowEtagHeaderFilter {
   protected static final Logger LOG = LogManager.getLogger(EtagFilter.class);
   
   private static final String JAVSCRIPT_FILE_EXT = ".js";
   private static final String CSI_NOCACHE_JS = "csi.nocache.js";
   private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public void destroy() {
       LOG.info("Removing etag filter.");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if(isNotJavascript(request)){ //Don't bother tagging filtering non-js
            filterChain.doFilter(request, response);
        } else if(isNoStore(request)){ //We don't cache the small no-cache of gwt, always download
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Pragma", "no-cache");
            response.setHeader(HEADER_CACHE_CONTROL, "no-store, must-revalidate");
            filterChain.doFilter(request, response);
        } else { //This checks all javascript under csi/* and uses etags for validation
            super.doFilterInternal(request, response, filterChain);
        }
        
    }
    
    
    protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException {
		// length of W/ + 0 + " + 32bits md5 hash + "
		StringBuilder builder = new StringBuilder(37);
		if (isWeak) {
			builder.append("W/");
		}
		builder.append("\"0");
		DigestUtils.appendMd5DigestAsHex(inputStream, builder);
		builder.append('"');
		return builder.toString();
	}
    
    private boolean isNoStore(HttpServletRequest request){
        String uri = request.getRequestURI();
        
        return uri.contains(CSI_NOCACHE_JS);
    }
    
    private boolean isNotJavascript(HttpServletRequest request){
        String uri = request.getRequestURI().trim();
        
        return !uri.endsWith(JAVSCRIPT_FILE_EXT);
    }
   
}
