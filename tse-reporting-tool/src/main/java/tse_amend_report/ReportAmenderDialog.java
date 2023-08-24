package tse_amend_report;

import app_config.PropertiesReader;
import data_collection.*;
import global_utils.Warnings;
import i18n_messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import progress_bar.IndeterminateProgressDialog;
import providers.TseReportService;
import tse_report.GetAmendedReportsThread;
import report.IMassAmendReportDialog;
import report.ThreadFinishedListener;
import soap.DetailedSOAPException;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Download a report into the database
 * @author chalvatzaras
 *
 */
public abstract class ReportAmenderDialog {
	private final Shell shell;
	private final TseReportService reportService;

	public ReportAmenderDialog(Shell shell, TseReportService reportService) {
		this.shell = shell;
		this.reportService = reportService;
	}
	
	/**
	 * Amend reports in a data collection
	 * @throws DetailedSOAPException
	 */
	public void amend() throws DetailedSOAPException {
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		
		// select the data collection
		IDcfDataCollectionsList<IDcfDataCollection> list;
		try {
			list = GetAvailableDataCollections.getAvailableDcList();
			list = filterDcListWithAmendedAndAggregated(list);
		} catch(DetailedSOAPException e) {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			throw e;
		}
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));

		// if no data collection was retrieved
		if (list.isEmpty()) {
			Warnings.createFatal(Messages.get("dc.no.element.found", PropertiesReader.getSupportEmail())).open(shell);
			return;
		}
		
		IDataCollectionsDialog dcDialog = getDataCollectionsDialog(shell, list, "dc.dialog.button.amend.datasets");
		IDcfDataCollection selectedDc = dcDialog.open("dc.dialog.amend.title");
		if (selectedDc == null)
			return;


		// get amended reports list
		getAmendedReports(selectedDc.getCode(), new ThreadFinishedListener() {
			public void finished(Runnable runnable) {
				GetAmendedReportsThread getDatasetListThread = (GetAmendedReportsThread) runnable;
				TableRowList list = getDatasetListThread.getAmendedReportsList();

				// open the list of the reports of the data collection
				IMassAmendReportDialog dialog = getMassAmendReportDialog(shell,  list);
				dialog.setReportList(list);
				dialog.open();
//
//				// get the chosen dataset
//				Dataset selectedDataset = dialog.getSelectedDataset();
//				if (selectedDataset == null)  // user pressed cancel
//					return;
//
//				// get all the versions of the dataset that are present in the DCF
//				allVersions = dialog.getSelectedDatasetVersions();
//				if (allVersions == null)
//					return;
//
//				// extract the senderId from the composed field (senderId.version)
//				String senderId = selectedDataset.getDecomposedSenderId();
//				// if the report already exists locally, warn that it will be overwritten
//				if (reportService.isLocallyPresent(senderId)) {
//					// ask confirmation to the user
//					boolean confirm = askConfirmation();
//					if (!confirm)
//						return;
//				}
//
//				// download and import the dataset
//				ReportImporter downloader = getImporter(allVersions);
//				ReportImporterThread thread = new ReportImporterThread(downloader);
//
//				FormProgressBar progressBarDialog = new FormProgressBar(shell, Messages.get("download.progress.title"));
//				progressBarDialog.open();
//
//				thread.setProgressListener(new ProgressListener() {
//					@Override
//					public void progressCompleted() {
//						// show warning
//						shell.getDisplay().syncExec(() -> {
//							progressBarDialog.fillToMax();
//							progressBarDialog.close();
//							end();
//						});
//					}
//
//					@Override
//					public void progressChanged(double progressPercentage) {
//						progressBarDialog.setProgress(progressPercentage);
//					}
//
//					@Override
//					public void progressChanged(double currentProgress, double maxProgress) {}
//
//					@Override
//					public void progressStopped(Exception e) {
//						shell.getDisplay().syncExec(() -> {
//							progressBarDialog.close();
//							manageException(e);
//						});
//					}
//				});
//
//				thread.start();
			}

			@Override
			public void terminated(Runnable thread, Exception e) {
				shell.getDisplay().asyncExec(() -> {
					if (e instanceof DetailedSOAPException) {
						Warnings.showSOAPWarning(shell, (DetailedSOAPException) e);
					}
					else {
						Warnings.warnUser(shell, Messages.get("error.title"), Messages.get("download.dataset.error"));
					}
				});
			}
		});
	}

	/**
	 * Get the amended reports for the selected data collection
	 * @param dcCode
	 * @param listener
	 */
	private void getAmendedReports(String dcCode, ThreadFinishedListener listener) {
		IndeterminateProgressDialog progressBar = new IndeterminateProgressDialog(shell, SWT.APPLICATION_MODAL,
				Messages.get("get.amended.reports.list.progress.bar.label"));
		progressBar.open();
		
		GetAmendedReportsThread thread = new GetAmendedReportsThread(dcCode, reportService);
		thread.setListener(new ThreadFinishedListener() {
			@Override
			public void finished(Runnable thread) {
				shell.getDisplay().asyncExec(() -> {
					progressBar.close();
					listener.finished(thread);
				});
			}

			@Override
			public void terminated(Runnable thread, Exception e) {
				shell.getDisplay().asyncExec(() -> {
					progressBar.close();
					listener.terminated(thread, e);
				});
			}
		});
		thread.start();
	}
	private IDcfDataCollectionsList<IDcfDataCollection> filterDcListWithAmendedAndAggregated(IDcfDataCollectionsList<IDcfDataCollection> list) {
		Map<String, List<TableRow>> groupedReports = reportService.groupReportsByDataCollection();

		return list.stream()
				.filter(dc -> reportService.dcHasAmendedOrAggregatedReports(groupedReports, dc.getCode()))
				.collect(Collectors.toCollection(DcfDataCollectionsList::new));
	}
	
	/**
	 * Get a dialog for picking a data collection
	 * @return
	 */
	public abstract IDataCollectionsDialog getDataCollectionsDialog(Shell shell,
			IDcfDataCollectionsList<IDcfDataCollection> list, String buttonTextKey);
	
	/**
	 * Get the dialog which should be shown to mass send and submit the reports.
	 * Here it is possible to customize the columns that should be shown.
	 *
	 * @return
	 */
	public abstract IMassAmendReportDialog getMassAmendReportDialog(Shell shell, TableRowList reports);
	
	/**
	 * Called at the end of the process
	 */
	public abstract void end();
}
