package csi.server.business.service.icon;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.common.base.Throwables;
import com.google.common.io.BaseEncoding.DecodingException;

import csi.server.business.service.GraphActionsService;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.color.ClientColorHelper;

@SuppressWarnings("serial")
public class IconProviderServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(IconProviderServlet.class);

    @Autowired
    IconActionsService iconActionsService;
    @Autowired
    GraphActionsService graphActionsService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        downloadFile(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String[] iconRequestStrings = null;
        ObjectMapper om = new ObjectMapper();

        try (InputStream inputStream = request.getInputStream()) {
            iconRequestStrings = om.readValue(inputStream, String[].class);
        } catch (Exception e) {
            LOG.error("Could not open request for processing");
        }
        String bulkload = request.getParameter("bulkload");
        if ((bulkload != null) && bulkload.equals("true") && (iconRequestStrings != null)) {
            List<String> imageStrings = new ArrayList<String>();
            for (String iconRequestString : iconRequestStrings) {
                IconRequest iconRequest = new IconRequest(iconRequestString);
                String base64Image = getBase64Image(iconRequest);
                imageStrings.add("\"" + iconRequestString + "\": \"" + base64Image + "\"");
            }
            response.setHeader("Content-Description", "File Transfer");
            response.setContentType("text/plain");

            try (PrintWriter writer = new PrintWriter(response.getOutputStream())) {
                writer.write("{" + imageStrings.stream().collect(Collectors.joining(", ")) + "}");
            } catch (IOException ioe) {
                LOG.error("Could not write images to output stream");
            }
        } else {
            downloadFile(request, response);
        }
    }

    private String getBase64Image(IconRequest iconRequest) {
        String base64Image = "";

        try {
            if (iconRequest.isGetBase64Image()) {
                if (iconRequest.isValid()) {
                    base64Image = iconActionsService.getBase64Image(iconRequest.id);
                }
            } else {
                String dataUrl = graphActionsService.getNodeAsImageNew(iconRequest.id, true, iconRequest.getShapeType(),
                        iconRequest.getIntColor(), iconRequest.getFloatAlpha(), iconRequest.isSelected(), iconRequest.isHighlighted(),
                        iconRequest.isCombined(), iconRequest.getIconSize(), 1.0, 1, iconRequest.isUseSummary(),
                        iconRequest.isNew(), iconRequest.isUpdated());
                if ((dataUrl == null) || (dataUrl.length() == 0) || dataUrl.equals(GraphActionsService.NO_SHAPE_ICON)) {
                    base64Image = "";
                } else if (dataUrl.equals(GraphActionsService.BROKEN_IMAGE)) {
                    base64Image = dataUrl;
                } else {
                    String encodingPrefix = "base64,";
                    int contentStartIndex = dataUrl.indexOf(encodingPrefix) + encodingPrefix.length();
                    base64Image = dataUrl.substring(contentStartIndex);
                }
            }
        } catch (Exception exception) {
            LOG.error("Malformed query string", exception);
        } finally {
            CsiPersistenceManager.close();
            CsiPersistenceManager.releaseCacheConnection();
        }
        return base64Image;
    }

    private void downloadFile(HttpServletRequest request, HttpServletResponse response) {
        IconRequest iconRequest = new IconRequest(request);
        String base64Image = getBase64Image(iconRequest);
        prepareResponse(iconRequest, response);
        streamFileAsResponse(iconRequest, response, base64Image);
    }

    private void prepareResponse(IconRequest iconRequest, HttpServletResponse response) {
        response.setHeader("Content-Description", "File Transfer");
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(URLEncoder.encode(iconRequest.id, "UTF-8"));
            if (!iconRequest.isGetBase64Image()) {
                sb.append(URLEncoder.encode(iconRequest.shape, "UTF-8"));
                sb.append(URLEncoder.encode(iconRequest.color, "UTF-8"));
            }
            if (iconRequest.iconsize != null) {
                sb.append(URLEncoder.encode(iconRequest.iconsize, "UTF-8"));
            }
            if (iconRequest.isSelected()) {
                sb.append(URLEncoder.encode("selected", "UTF-8"));
            }
            if (iconRequest.isHighlighted()) {
                sb.append(URLEncoder.encode("highlighted", "UTF-8"));
            }
            if (iconRequest.isCombined()) {
                sb.append(URLEncoder.encode("combined", "UTF-8"));
            }
            if (iconRequest.isUseSummary()) {
                sb.append(URLEncoder.encode("usesummary", "UTF-8"));
            }
            if (iconRequest.isNew()) {
                sb.append(URLEncoder.encode("new", "UTF-8"));
            }
            if (iconRequest.isUpdated()) {
                sb.append(URLEncoder.encode("updated", "UTF-8"));
            }
            response.setHeader("Content-Disposition", "inline; filename=" + sb.toString());
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
        response.setHeader("Cache-Control", "max-age=300");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setContentType("image/png");
    }

    private void streamFileAsResponse(IconRequest iconRequest, HttpServletResponse response, String icon) {
        try (BufferedOutputStream output = new BufferedOutputStream(response.getOutputStream())) {
            output.write(Base64.getDecoder().decode(icon));
        } catch (DecodingException de) {
            LOG.error("Could not write icon image to output stream: illegal character: " + iconRequest.toString());
        } catch (IOException ioe) {
            LOG.error("Could not write icon image to output stream");
        }
    }

    private class IconRequest {
        private String id;
        private String shape;
        private String color;
        private String alpha;
        private String isselected;
        private String ishighlighted;
        private String iscombined;
        private String iconsize;
        private String isusesummary;
        private String isnew;
        private String isupdated;
        private boolean valid;

        IconRequest(String line) {
            String[] parameters = line.split("&");
            for (String parameter : parameters) {
                String[] keyAndValue = parameter.split("=");
                switch (keyAndValue[0]) {
                    case "id":
                        if (keyAndValue.length > 1) {
                            id = keyAndValue[1];
                            id = id.replace("_CQMC_", "?");
                            id = id.replace("_CAMPC_", "&");
                            id = id.replace("_CPERC_", "%");
                        }
                        break;
                    case "sh":
                        shape = keyAndValue[1];
                        break;
                    case "col":
                        color = keyAndValue[1];
                        break;
                    case "a":
                        alpha = keyAndValue[1];
                        break;
                    case "s":
                        isselected = keyAndValue[1];
                        break;
                    case "h":
                        ishighlighted = keyAndValue[1];
                        break;
                    case "com":
                        iscombined = keyAndValue[1];
                        break;
                    case "is":
                        iconsize = keyAndValue[1];
                        break;
                    case "us":
                        isusesummary = keyAndValue[1];
                        break;
                    case "in":
                        isnew = keyAndValue[1];
                        break;
                    case "iu":
                        isupdated = keyAndValue[1];
                        break;
                }
            }
            validateId();
        }

        IconRequest(HttpServletRequest request) {
            id = request.getParameter("id");
            id = id.replace("_CQMC_", "?");
            id = id.replace("_CAMPC_", "&");
            id = id.replace("_CPERC_", "%");
            shape = request.getParameter("sh");
            color = request.getParameter("col");
            alpha = request.getParameter("a");
            isselected = request.getParameter("s");
            ishighlighted = request.getParameter("h");
            iscombined = request.getParameter("com");
            iconsize = request.getParameter("is");
            isusesummary = request.getParameter("us");
            isnew = request.getParameter("in");
            isupdated = request.getParameter("iu");
            validateId();
        }

        private void validateId() {
            valid = ((id != null) && (id.length() > 5));
        }

        boolean isGetBase64Image() {
            return (shape == null) || (color == null);
        }

        public boolean isSelected() {
            return isselected != null;
        }

        public boolean isHighlighted() {
            return ishighlighted != null;
        }

        public boolean isCombined() {
            return iscombined != null;
        }

        public ShapeType getShapeType() {
            return ShapeType.getShape(shape);
        }

        public int getIntColor() {
            try {
                return ClientColorHelper.get().makeFromHex(color).getIntColor();
            } catch (Exception e) {
                LOG.error(e);
                return 0;
            }
        }

        float getFloatAlpha() {
            try {
                return Float.parseFloat(alpha);
            } catch (Exception e) {
                LOG.error("getFloatAlpha", e);
                return 1.0f;
            }
        }

        private int getIconSize() {
            if ((iconsize == null) || iconsize.isEmpty()) {
                return 20;
            }
            Double doubleSize = Math.ceil(Double.parseDouble(iconsize));
            return doubleSize.intValue();
        }

        boolean isUseSummary() {
            return isusesummary != null;
        }

        public boolean isNew() {
            return isnew != null;
        }

        public boolean isUpdated() {
            return isupdated != null;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
      public String toString() {
            String retVal = "";
            if (id != null) {
                retVal += "id = " + id + ";";
            }
            if (shape != null) {
                retVal += "shape = " + shape + ";";
            }
            if (color != null) {
                retVal += "color = " + color + ";";
            }
            if (alpha != null) {
                retVal += "alpha = " + alpha + ";";
            }
            if (isselected != null) {
                retVal += "isselected = " + isselected + ";";
            }
            if (ishighlighted != null) {
                retVal += "ishighlighted = " + ishighlighted + ";";
            }
            if (iscombined != null) {
                retVal += "iscombined = " + iscombined + ";";
            }
            if (iconsize != null) {
                retVal += "iconsize = " + iconsize + ";";
            }
            if (isusesummary != null) {
                retVal += "isusesummary = " + isusesummary + ";";
            }
            if (isnew != null) {
                retVal += "isnew = " + isnew + ";";
            }
            if (isupdated != null) {
                retVal += "isupdated = " + isupdated + ";";
            }
            return retVal;
        }
    }
}
