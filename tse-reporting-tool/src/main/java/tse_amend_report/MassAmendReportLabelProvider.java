package tse_amend_report;

import app_config.AppPaths;
import dataset.Dataset;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import table_skeleton.TableRow;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Label provider of the {@link Dataset}
 *
 * @author chalvatzaras
 *
 */
public class MassAmendReportLabelProvider extends ColumnLabelProvider {

	private String key;

	public MassAmendReportLabelProvider(String key) {
		this.key = key;
	}
	
	@Override
	public void addListener(ILabelProviderListener arg0) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {}

	@Override
	public Image getImage(Object arg0) {
		return null;
	}
	
	@Override
	public String getText(Object arg0) {

		TableRow tableRow = (TableRow) arg0;
		
		String text;
		switch(key) {
		case "year":
			text = String.valueOf(tableRow.getCode(AppPaths.REPORT_YEAR_COL));
			break;
		case "month":
			text = String.valueOf(tableRow.getCode(AppPaths.REPORT_MONTH_COL));
			if(Objects.nonNull(text) ){
				text = LocalDate.of(2000, Integer.parseInt(text), 1).getMonth().toString();
				text = text.substring(0,1) + text.substring(1).toLowerCase();
			}
			break;
		case "country":
			text = String.valueOf(tableRow.getLabel(AppPaths.REPORT_COUNTRY));
			break;
		default:
			text = "";
			break;
		}

		return text;
	}
}
