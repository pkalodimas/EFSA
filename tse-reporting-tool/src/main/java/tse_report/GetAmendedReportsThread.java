package tse_report;

import dataset.RCLDatasetStatus;
import providers.IReportService;
import report.Report;
import report.ThreadFinishedListener;
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
		return unfiltered.stream()
				.map(TseReport::new)
				.filter(Report::isVisible)
				.filter(r -> r.getDcCode().equals(dcCode))
				.filter(r -> r.getRCLStatus().equals(RCLDatasetStatus.LOCALLY_VALIDATED))
				.filter(r -> Integer.parseInt(r.getVersion()) > 0)
				.collect(Collectors.toCollection(TableRowList::new));
	}
}
