package tse_report;

import app_config.AppPaths;
import global_utils.Message;
import providers.ITableDaoService;
import providers.TseReportService;
import report.ThreadFinishedListener;
import xlsx_reader.TableSchemaList;

import java.util.Objects;
import java.util.Optional;

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
        if ( this.report.isAggregated() ) {
            result = this.refreshAggregatedReport(report);
        } else {
            result = reportService.refreshStatus(report);
            // MOCK
//            report.setRCLStatus(RCLDatasetStatus.ACCEPTED_DWH);
//            this.daoService.update(report);
        }

        if (listener != null) {
            listener.finished(this);
        }
    }

    public Message getLog() {
        return result;
    }

    private Message refreshAggregatedReport(TseReport report){
        TseReport aggrReport = Optional.ofNullable(this.daoService.getById(TableSchemaList.getByName(AppPaths.REPORT_SHEET), report.getAggregatorId()))
                .map(TseReport::new)
                .orElse(null);

        if( Objects.isNull(aggrReport) ){
            return reportService.refreshStatus(report);
            // MOCK
//            report.setRCLStatus(RCLDatasetStatus.VALID);
//            this.daoService.update(report);
//            return null;
        }

        result = this.reportService.refreshStatus(aggrReport);
        // MOCK
//        aggrReport.setRCLStatus(RCLDatasetStatus.ACCEPTED_DWH);
//        this.daoService.update(aggrReport);

        this.daoService.getByStringField(
                        TableSchemaList.getByName(AppPaths.REPORT_SHEET), AppPaths.REPORT_AGGREGATOR_ID, String.valueOf(aggrReport.getDatabaseId())
                ).stream()
                .map(TseReport::new)
                .forEach(rep -> {
                    rep.setRCLStatus(aggrReport.getRCLStatus());
                    if( aggrReport.getRCLStatus().isFinalized() ){
                        rep.setAggregatorId(null);
                    }
                    daoService.update(rep);
                });

        if( aggrReport.getRCLStatus().isFinalized() ){
            this.daoService.delete(aggrReport);
        }
        return result;
    }
}
