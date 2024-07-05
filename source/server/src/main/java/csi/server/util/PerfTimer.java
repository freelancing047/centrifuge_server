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
package csi.server.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PerfTimer {
   private static final Logger LOG = LogManager.getLogger(PerfTimer.class);
   
    private static final int TIMEOUT_LOG_DELAY_S = 7;
    private static final int TIMEOUT_LOG_DELAY_MS = TIMEOUT_LOG_DELAY_S * 1000; // Milliseconds
    private static final BigDecimal MILLION = new BigDecimal(1000000);
    private long start;
    private String message;
    private boolean stopped = false;

    public PerfTimer start() {
        start = System.nanoTime();
        Timer timeout = new Timer();
        timeout.schedule(new TimerTask() {

            @Override
            public void run() {
                if (!stopped) {
                   LOG.debug(message + " has not completed after " + TIMEOUT_LOG_DELAY_S + " seconds");
                    this.cancel();
                }

            }

        }, TIMEOUT_LOG_DELAY_MS);
        return this;
    }

    public PerfTimer start(String methodSignature) {
        this.message = methodSignature;
        return start();
    }

    public void stopAndLog(String action) {
        stopped = true;
        long end = System.nanoTime();
        long diff = end - start;
        BigDecimal time = new BigDecimal(diff).divide(MILLION, 6, RoundingMode.HALF_EVEN);
        LOG.trace(action + " took " + time + " ms.");
    }
}
