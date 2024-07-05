package csi.server.business.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.TemplateCacheUtil;

public class CacheDataViewRunnable implements Runnable{
   private static final Logger LOG = LogManager.getLogger(CacheDataViewRunnable.class);
   
	private DataViewDef template;

	public CacheDataViewRunnable(DataViewDef template){
		this.template = template;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			TemplateCacheUtil.createCachedDataView(template);
	        CsiPersistenceManager.close();
			CsiPersistenceManager.releaseCacheConnection();
		} catch (CentrifugeException e) {
		   LOG.error("Failed to repopulate dataview cache", e);
		}
	};
	
}
