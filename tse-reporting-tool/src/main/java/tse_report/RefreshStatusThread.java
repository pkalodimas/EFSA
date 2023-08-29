package tse_report;

import app_config.AppPaths;
import global_utils.Message;
import providers.ITableDaoService;
import providers.TseReportService;
import report.Report;
import report.ThreadFinishedListener;
import xlsx_reader.TableSchemaList;

import java.util.Comparator;
import java.util.Objects;

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
        TseReport aggrReport = this.reportService.getByDatasetId(report.getAggregatorId())
                .stream()
                .map(TseReport::new)
                .max(Comparator.comparing(Report::getVersion))
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
                        TableSchemaList.getByName(AppPaths.REPORT_SHEET), AppPaths.REPORT_AGGREGATOR_ID, aggrReport.getId()
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
            this.reportService.getBySenderId(aggrReport.getSenderId()).stream()
                    .forEach(r->{
                        this.daoService.delete(r);
                    });
        }
        return result;
    }
}
