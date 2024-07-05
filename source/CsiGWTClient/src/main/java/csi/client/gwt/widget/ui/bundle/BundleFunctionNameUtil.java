package csi.client.gwt.widget.ui.bundle;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.util.sql.api.BundleFunction;

public class BundleFunctionNameUtil {

	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	public static String getName(BundleFunction bundle){
		String name = "";
		
		switch(bundle){
			case NONE : name = i18n.bundleFunctionNone();
			break;
			case LEFT : name = i18n.bundleFunctionLeft();
			break;
			case RIGHT : name = i18n.bundleFunctionRight();
			break;
			case SUBSTRING : name = i18n.bundleFunctionSubstring();
			break;
			case LENGTH : name = i18n.bundleFunctionLength();
			break;
			case TRIM : name = i18n.bundleFunctionTrim();
			break;
			case REGEX_REPLACE : name = i18n.bundleFunctionRegexReplace();
			break;
			case SPLIT : name = i18n.bundleFunctionSplit();
			break;
			case COUNT_TOKEN : name = i18n.bundleFunctionCountToken();
			break;
			case SINGLE_TOKEN : name = i18n.bundleFunctionSingleToken();
			break;
			case DATE : name = i18n.bundleFunctionDate();
			break;
			case QUARTER : name = i18n.bundleFunctionQuarter();
			break;
			case YEAR_MONTH : name = i18n.bundleFunctionYearAndMonth();
			break;
			case YEAR : name = i18n.bundleFunctionYear();
			break;
			case MONTH : name = i18n.bundleFunctionMonth();
			break;
			case DAY_OF_MONTH : name = i18n.bundleFunctionDayOfMonth();
			break;
			case DAY_OF_WEEK : name = i18n.bundleFunctionDayOfWeek();
			break;
			case DAY_OF_YEAR : name = i18n.bundleFunctionDayOfYear();
			break;
			case HOUR : name = i18n.bundleFunctionHour();
			break;
			case MINUTE : name = i18n.bundleFunctionMinute();
			break;
			case SECOND: name = i18n.bundleFunctionSecond();
				break;
			case MICROSECOND: name = i18n.bundleFunctionMicrosecond();
				break;
			case MILLISECOND: name = i18n.bundleFunctionMillisecond();
				break;
			case WEEK : name = i18n.bundleFunctionWeek();
			break;
			case DAY_TYPE : name = i18n.bundleFunctionDayType();
			break;
			case CEILING : name = i18n.bundleFunctionCeiling();
			break;
			case FLOOR : name = i18n.bundleFunctionFloor();
			break;
			case ROUND : name = i18n.bundleFunctionRound();
			break;
			case ABSOLUTE : name = i18n.bundleFunctionAbsolute();
			break;
			case MOD : name = i18n.bundleFunctionMod();
			break;
			case SIGN : name = i18n.bundleFunctionSign();
			break;
		}
		
		return name;
	}
	
}
