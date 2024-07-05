package csi.server.business.visualization.map;

import java.util.concurrent.atomic.AtomicLong;

public class UUIDUtil {
	private static AtomicLong atomicLong = new AtomicLong();
	public static Long getUUIDLong() {
		Long longValue = atomicLong.incrementAndGet();
		return longValue;
	}
}
