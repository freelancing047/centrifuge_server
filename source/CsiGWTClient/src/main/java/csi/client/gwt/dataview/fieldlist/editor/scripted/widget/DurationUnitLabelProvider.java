package csi.client.gwt.dataview.fieldlist.editor.scripted.widget;

import com.sencha.gxt.data.shared.LabelProvider;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.DurationUnit;

public class DurationUnitLabelProvider implements LabelProvider<DurationUnit> {

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	@Override
	public String getLabel(DurationUnit item) {
		switch(item){
		case DAYS: return i18n.durationUnitDays();
		case HOURS: return i18n.durationUnitHours();
		case MILLISECONDS: return i18n.durationUnitMilliseconds();
		case MINUTES: return i18n.durationUnitMinutes();
		case MONTHS: return i18n.durationUnitMonths();
		case SECONDS: return i18n.durationUnitSeconds();
		case WEEKS: return i18n.durationUnitWeeks();
		case YEARS: return i18n.durationUnitYears();
		default: return "";
		}
	}

}
