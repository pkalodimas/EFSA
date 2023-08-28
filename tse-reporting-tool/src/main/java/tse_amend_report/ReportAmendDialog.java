package tse_amend_report;

import app_config.PropertiesReader;
import data_collection.DcfDataCollectionsList;
import data_collection.GetAvailableDataCollections;
import data_collection.IDataCollectionsDialog;
import data_collection.IDcfDataCollection;
import data_collection.IDcfDataCollectionsList;
import dataset.RCLDatasetStatus;
import global_utils.Warnings;
import i18n_messages.Messages;
import i18n_messages.TSEMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import progress_bar.IndeterminateProgressDialog;
import providers.ITableDaoService;
import providers.TseReportService;
import report.IMassAmendReportDialog;
import report.Report;
import report.ThreadFinishedListener;
import soap.DetailedSOAPException;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import tse_data_collection.TSEDataCollectionsListDialog;
import tse_report.GetAmendedReportsThread;
import tse_report.TseReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Download a report into the database
 * @author chalvatzaras
 *
 */
public class ReportAmendDialog {

	private static final String AMEND_REPORT_LIST_DIALOG_WINDOW_CODE = "MassAmendReportsListDialog";

	private final Logger LOGGER;
	private final Shell shell;
	private final TseReportService reportService;
	private final ITableDaoService daoService;

	public ReportAmendDialog(Shell shell,
							 TseReportService reportService,
							 ITableDaoService daoService) {
		this.shell = Objects.requireNonNull(shell);
		this.reportService = Objects.requireNonNull(reportService);
		this.daoService = Objects.requireNonNull(daoService);
		this.LOGGER = LogManager.getLogger(this.getClass());
	}
	
	/**
	 * Amend reports in a data collection
	 * @throws DetailedSOAPException
	 */
	public void open() throws DetailedSOAPException {
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

		IDcfDataCollectionsList<IDcfDataCollection> dcWithAmends = this.getDataCollectionsWithAmendedReports();
		if (dcWithAmends.isEmpty()) {
			Warnings.create(Messages.get("dc.no.element.found", PropertiesReader.getSupportEmail())).open(shell);
			return;
		}
		
		IDataCollectionsDialog dcDialog = this.getDataCollectionsDialog(shell, dcWithAmends, "dc.dialog.button.amend.datasets");
		IDcfDataCollection selectedDc = dcDialog.open("dc.dialog.amend.title");
		if (Objects.isNull(selectedDc)) {
			return;
		}
		AmendReportListDialog amendedReportsDialog = this.getAmendedReportsDialog(shell, selectedDc.getCode());
		amendedReportsDialog.open();


		// get amended reports list
//		GetAmendedReportsThread thread = this.getAmendedReportThread(
//				this.getAmendedMonthlyReports(selectedDc.getCode()),
//				new ThreadFinishedListener() {
//					public void finished(Runnable runnable) {
//						GetAmendedReportsThread getDatasetListThread = (GetAmendedReportsThread) runnable;
//						TableRowList list = getDatasetListThread.getAmendedReportsList();
//						// open the list of the reports of the data collection
//						IMassAmendReportDialog dialog = getAmendedReportsDialog(shell,  list);
//						dialog.setReportList(list);
//						dialog.open();
//				}
//
//				@Override
//				public void terminated(Runnable thread, Exception e) {
//					shell.getDisplay().asyncExec(() -> {
//						if (e instanceof DetailedSOAPException) {
//							Warnings.showSOAPWarning(shell, (DetailedSOAPException) e);
//						}
//						else {
//							Warnings.warnUser(shell, Messages.get("error.title"), Messages.get("download.dataset.error"));
//						}
//					});
//				}
//			});
//		thread.start();
	}

	private IDcfDataCollectionsList<IDcfDataCollection> getDataCollectionsWithAmendedReports() throws DetailedSOAPException {
		try {
			List<String> dcCodes = this.reportService.getAllReports().stream()
					.map(TseReport::new)
					.filter(r->Integer.parseInt(r.getVersion()) > 0)
					.filter(r->Boolean.FALSE.equals(r.getRCLStatus().isFinalized()))
					.map(Report::getDcCode)
					.distinct()
					.collect(Collectors.toList());

			return GetAvailableDataCollections.getAvailableDcList()
					.stream()
					.filter(dc->dcCodes.contains(dc.getCode()))
					.collect(Collectors.toCollection(DcfDataCollectionsList::new));
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
	}

//	private GetAmendedReportsThread getAmendedReportThread(List<TseReport> reports,
//														   ThreadFinishedListener listener) {
//		IndeterminateProgressDialog progressBar = new IndeterminateProgressDialog(
//				shell,
//				SWT.APPLICATION_MODAL,
//				Messages.get("get.amended.reports.list.progress.bar.label")
//		);
//		progressBar.open();
//
//		GetAmendedReportsThread thread = new GetAmendedReportsThread(reports);
//
//		thread.setListener(new ThreadFinishedListener() {
//			@Override
//			public void finished(Runnable thread) {
//				shell.getDisplay().asyncExec(() -> {
//					progressBar.close();
//					listener.finished(thread);
//				});
//			}
//
//			@Override
//			public void terminated(Runnable thread, Exception e) {
//				shell.getDisplay().asyncExec(() -> {
//					progressBar.close();
//					listener.terminated(thread, e);
//				});
//			}
//		});
//		return thread;
//	}

	public IDataCollectionsDialog getDataCollectionsDialog(Shell shell1,
														   IDcfDataCollectionsList<IDcfDataCollection> list,
														   String buttonTextKey) {
		LOGGER.debug("Creating the DataCollectionsListDialog");
		return new TSEDataCollectionsListDialog(shell1, list, buttonTextKey);
	}

	public AmendReportListDialog getAmendedReportsDialog(Shell shell, String dcCode) {
		LOGGER.debug("Creating the MassAmendReportDialog");
		return new AmendReportListDialog(
				shell,
				AMEND_REPORT_LIST_DIALOG_WINDOW_CODE,
				dcCode,
				this.reportService,
				this.daoService
		);
	}

	public void end() {
		String title = TSEMessages.get("success.title");
		String message = TSEMessages.get("download.success");
		int style = SWT.ICON_INFORMATION;
		Warnings.warnUser(shell, title, message, style);
	}
}
