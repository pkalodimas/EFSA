package tse_amend_report;

import data_collection.IDataCollectionsDialog;
import data_collection.IDcfDataCollection;
import data_collection.IDcfDataCollectionsList;
import global_utils.Warnings;
import i18n_messages.TSEMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import providers.IReportService;
import providers.ITableDaoService;
import providers.TseReportService;
import report.IMassAmendReportDialog;
import report_downloader.TSEDataCollectionsListDialog;
import table_skeleton.TableRowList;

/**
 * Mass amend reports in a data collection.
 *
 * @author chalvatzaras
 *
 */
public class TSEDataCollectionAmender extends ReportAmenderDialog {

	static final Logger LOGGER = LogManager.getLogger(TSEDataCollectionAmender.class);

	private final Shell shell;

	private final TseReportService reportService;

	private final ITableDaoService daoService;

	public TSEDataCollectionAmender(Shell shell, TseReportService reportService, ITableDaoService daoService) {
		super(shell, reportService);
		this.shell = shell;
		this.reportService = reportService;
		this.daoService = daoService;
	}

	@Override
	public void end() {
		String title = TSEMessages.get("success.title");
		String message = TSEMessages.get("download.success");
		int style = SWT.ICON_INFORMATION;
		Warnings.warnUser(shell, title, message, style);
	}

	@Override
	public IDataCollectionsDialog getDataCollectionsDialog(Shell shell1,
			IDcfDataCollectionsList<IDcfDataCollection> list, String buttonTextKey) {
		LOGGER.debug("Creating the DataCollectionsListDialog");
		return new TSEDataCollectionsListDialog(shell1, list, buttonTextKey);
	}

	@Override
	public IMassAmendReportDialog getMassAmendReportDialog(Shell shell, TableRowList reports) {
		LOGGER.debug("Creating the MassAmendReportDialog");
		return new TSEAmendReportsDialog(shell, this.reportService, this.daoService, reports);
	}
}
