package tse_report;

import app_config.AppPaths;
import global_utils.Message;
import providers.ITableDaoService;
import providers.TseReportService;
import report.ThreadFinishedListener;
import xlsx_reader.TableSchemaList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RefreshStatusThread extends Thread {

    private TseReportService reportService;
    private ITableDaoService daoService;
    private ThreadFinishedListener listener;
    private TseReport report;
    private Message result;

    public RefreshStatusThread(TseReport report,
                               TseReportService reportService,
                               ITableDaoService daoService) {
        this.report = Objects.requireNonNull(report);
        this.reportService = Objects.requireNonNull(reportService);
        this.daoService = Objects.requireNonNull(daoService);
    }

    public void setListener(ThreadFinishedListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        if (this.report.isAggregated()) {
            result = this.refreshAggregatedReport(report);
        } else {
            result = reportService.refreshStatus(report);
        }

        if (listener != null) {
            listener.finished(this);
        }
    }

    public Message getLog() {
        return result;
    }

    private Message refreshAggregatedReport(TseReport report) {
        TseReport aggrReport = Optional.ofNullable(this.daoService.getById(TableSchemaList.getByName(AppPaths.REPORT_SHEET), report.getAggregatorId()))
                .map(TseReport::new)
                .orElse(null);

        if (Objects.isNull(aggrReport)) {
            return reportService.refreshStatus(report);
        }

        result = this.reportService.refreshStatus(aggrReport);

        List<TseReport> aggregatedReports = this.daoService.getByStringField(
                        TableSchemaList.getByName(AppPaths.REPORT_SHEET), AppPaths.REPORT_AGGREGATOR_ID, String.valueOf(aggrReport.getDatabaseId())
                ).stream()
                .map(TseReport::new)
                .collect(Collectors.toList());

        aggregatedReports.forEach(rep -> {
                    rep.setRCLStatus(aggrReport.getRCLStatus());
                    // After the amendment is finalized, the aggregator report will be dropped.
                    if (aggrReport.getRCLStatus().isFinalized()) {
                        rep.setAggregatorId(null);
                    }
                    daoService.update(rep);
                });

        // If the amendment is finalized, drop the aggregator report. Is not needed any more.
        if (aggrReport.getRCLStatus().isFinalized()) {
            this.daoService.delete(aggrReport);
        }

        this.report = aggregatedReports.stream()
                .filter(r -> r.getDatabaseId() == this.report.getDatabaseId())
                .findAny()
                .orElse(this.report);

        return result;
    }
}
