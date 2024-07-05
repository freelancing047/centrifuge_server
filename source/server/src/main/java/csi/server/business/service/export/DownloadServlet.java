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
package csi.server.business.service.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Throwables;

/**
 * Servlet that allows a requester to download a file.
 * @author Centrifuge Systems, Inc.
 *
 */
public class DownloadServlet extends HttpServlet {

    public static final String TEMP_FILE_EXT = ".file";
    public static final String TEMP_DIRECTORY = "resources/temp/";

    ServletContext servletContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        servletContext = config.getServletContext();
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        downloadFile(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        downloadFile(req, resp);
    }

    private void downloadFile(ServletRequest request, ServletResponse resp) throws ServletException {

        HttpServletResponse response = prepareResponse(request, (HttpServletResponse) resp);
        File file = createTempFile(request);
        streamFileAsResponse(response, file);
    }

    private void streamFileAsResponse(HttpServletResponse response, File file) {
       try (FileInputStream iStream = new FileInputStream(file);
            ServletOutputStream oStream = response.getOutputStream()) {
          IOUtils.copy(iStream, oStream);
       } catch (Exception e) {
          throw Throwables.propagate(e);
       } finally {
          file.delete();
       }
    }

    private File createTempFile(ServletRequest request) {
        String token = request.getParameter("token");
        return new File(servletContext.getRealPath(TEMP_DIRECTORY) + File.separator + token + TEMP_FILE_EXT);
    }

    private HttpServletResponse prepareResponse(ServletRequest request, HttpServletResponse response) {
        String filename = request.getParameter("filename");
        response.setHeader("Content-Description", "File Transfer");
        try {
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setContentType("application/octet-stream");
        return response;
    }

}
