package com.csi.chart;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

import javax.imageio.IIOImage;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;

import com.csi.chart.data.DataService;
import com.csi.chart.data.DataServiceFactory;
import com.csi.chart.dto.ChartSummary;
import com.csi.chart.dto.DataRequest;
import com.csi.chart.dto.ImageOverviewRequest;
import com.csi.chart.dto.Page;
import com.jhlabs.image.ImageUtils;

@Path(value = "charting/{chartId}")
public class ChartResource {
    private static final Logger LOG = LogManager.getLogger(ChartResource.class);

    DataServiceFactory dataServiceFactory = new DataServiceFactory();

    private int DefaultPageSize = 50;

    public ChartResource() {
    }

    @POST
    @Path("summary")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChartSummary getSummary(@PathParam("chartId") String chartId, DataRequest dataRequest) throws Exception {
        ChartSummary dataSummary;
        try {
            DataService dataService = dataServiceFactory.getDataService(chartId);
            dataSummary = dataService.calculateSummaryInfo(dataRequest);
            return dataSummary;
        } catch (Exception e) {
           LOG.warn("Failure processing summary.  Chart id: " + chartId, e);
            throw e;
        }
    }

    @POST
    @Path("data")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Page dataComplex(@PathParam("chartId") String chartId, DataRequest dataRequest) throws Exception {
        try {
            if (dataRequest.size == 0) {
                dataRequest.size = DefaultPageSize;
            }

            DataService service = dataServiceFactory.getDataService(chartId);
            Page data;
            if (dataRequest.support2D == false) {
                data = service.getData(dataRequest);
            } else {
                if (dataRequest.ySize == 0) {
                    dataRequest.ySize = DefaultPageSize;
                }
                data = service.getData2D(dataRequest);
            }
            return data;
        } catch (Exception e) {
           LOG.warn("Failure retrieving data.  Chart Id: " + chartId, e);
            throw e;
        }
    }

    @POST
    @Path("image")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("image/png")
    public IIOImage overviewImage(@PathParam("chartId") String chartId, ImageOverviewRequest imageRequest)
            throws Exception {
        if (LOG.isTraceEnabled()) {
           LOG.trace("Processing overview request.");
        }

        if (imageRequest == null) {
            imageRequest = new ImageOverviewRequest();
        }

        if (imageRequest.width == 0) {
            imageRequest.width = 600;
        }

        if (imageRequest.height == 0) {
            imageRequest.height = 100;
        }

        try {
            DataService dataService = dataServiceFactory.getDataService(chartId);

            ChartBuilder builder = new ChartBuilder();
            builder.withDataService(dataService);
            builder.withResourceId(chartId);
            builder.withRequest(imageRequest);

            JFreeChart chart = builder.build();
            Plot plot = chart.getPlot();

            BufferedImage image = new BufferedImage(imageRequest.width, imageRequest.height,
                    BufferedImage.TYPE_INT_ARGB);
            image = new BufferedImage(imageRequest.width, imageRequest.height, BufferedImage.TYPE_INT_ARGB);
            Double area = new Rectangle2D.Double(0, 0, imageRequest.width, imageRequest.height);

            // image = chart.createBufferedImage(width, height);
            plot.draw(image.createGraphics(), area, null, null, null);
            ImageProducer alphaFilter = createAlphaFilter(image);
            BufferedImage adjustedImage = ImageUtils.createImage(alphaFilter);
            IIOImage ioImage = new IIOImage(adjustedImage, null, null);
            return ioImage;
        } catch (Exception e) {
           LOG.warn("Failure building overview image.  Chart Id: " + chartId, e);
            throw e;
        }

    }

    private ImageProducer createAlphaFilter(BufferedImage image) {
        RGBImageFilter filter = new RGBImageFilter() {

            @Override
            public int filterRGB(int x, int y, int rgb) {
                if (rgb == 0) {
                    return rgb;
                }

                int update = (0xFF << 24) | rgb;
                return update;
            }
        };

        FilteredImageSource source = new FilteredImageSource(image.getSource(), filter);
        return source;
    }

}
