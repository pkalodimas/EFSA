package report;

import app_config.AppPaths;
import config.Config;
import dataset.DatasetList;
import dataset.IDataset;
import dataset.RCLDatasetStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import providers.IReportService;
import providers.ReportService;
import soap.DetailedSOAPException;
import soap.GetDatasetsList;
import table_skeleton.TableRowList;

import java.util.stream.Collectors;

public class GetAmendedReportsThread extends Thread {

	private TableRowList reports;

	private ThreadFinishedListener listener;

	private final IReportService reportService;
	private final String dcCode;

	public GetAmendedReportsThread(String dcCode, IReportService reportService) {
		this.dcCode = dcCode;
		this.reportService = reportService;
	}
	
	@Override
	public void run() {
		reports = getAmendedReports();
		if (listener != null)
			listener.finished(this);
	}
	
	public void setListener(ThreadFinishedListener listener) {
		this.listener = listener;
	}
	
	public TableRowList getAmendedReportsList() {
		return this.reports;
	}

	/**
	 * Get a list of all the amended reports of the data collection
	 *
	 * @return
	 */
	private TableRowList getAmendedReports() {
		return filterReports(reportService.getAllReports(), dcCode);
	}

	private TableRowList filterReports(TableRowList unfiltered, String dcCode) {
		// ean exw aggregated -> des gia poious mines einai, kai fere ta antistoixa

		// alliws fere ta amended (simainei oti den exw aggregated)


		return unfiltered.stream()
				.filter(r -> r.getCode(AppPaths.REPORT_DC_CODE).equals(dcCode))
				.filter(r -> RCLDatasetStatus.fromString(r.getCode(AppPaths.REPORT_STATUS)).equals(RCLDatasetStatus.LOCALLY_VALIDATED))
				.filter(r -> Integer.parseInt(r.getCode(AppPaths.REPORT_VERSION)) > 0)
				.collect(Collectors.toCollection(TableRowList::new));
	}
}
