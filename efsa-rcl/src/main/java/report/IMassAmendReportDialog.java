package report;

import table_skeleton.TableRowList;

public interface IMassAmendReportDialog {
	/**
	 * Open the dialog
	 * @return
	 */
	void open();

	void setReportList(TableRowList list);
}
