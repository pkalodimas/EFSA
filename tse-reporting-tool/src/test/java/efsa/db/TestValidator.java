package efsa.db;

import dataset.IDataset;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import providers.*;
import report_validator.ReportError;
import soap.GetAck;
import soap.GetDataset;
import soap.GetDatasetsList;
import soap.SendMessage;
import soap_interface.IGetAck;
import soap_interface.IGetDataset;
import soap_interface.IGetDatasetsList;
import soap_interface.ISendMessage;
import table_database.Database;
import table_database.ITableDao;
import table_database.TableDao;
import table_skeleton.TableRow;
import tse_config.CustomStrings;
import tse_report.TseReport;
import tse_validator.TseReportValidator;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestValidator {

    private final TableSchema REPORT_SCHEMA = TableSchemaList.getByName(CustomStrings.REPORT_SHEET);
    private final TableSchema SUMMARY_INFO_SCHEMA = TableSchemaList.getByName(CustomStrings.SUMMARIZED_INFO_SHEET);
    private final TableSchema CASE_INFO_SCHEMA = TableSchemaList.getByName(CustomStrings.CASE_INFO_SHEET);
    private final TableSchema ANALYTICAL_RESULTS_SCHEMA = TableSchemaList.getByName(CustomStrings.RESULT_SHEET);

    private static TseReportService reportService;
    private static ITableDaoService tableDaoService;

    @BeforeAll
    public static void before() {
        ITableDao dao = new TableDao();
        tableDaoService = new TableDaoService(dao);
        IFormulaService formulaService = new FormulaService(tableDaoService);

        IGetAck getAck = new GetAck();
        IGetDatasetsList<IDataset> getDatasetsList = new GetDatasetsList<>();
        ISendMessage sendMessage = new SendMessage();
        IGetDataset getDataset = new GetDataset();

        reportService = new TseReportService(getAck, getDatasetsList, sendMessage, getDataset,
                tableDaoService, formulaService);
    }


    @Test
    public void validator() throws Exception {
        Database db = new Database();
        db.connect();

        TableRow byId = tableDaoService.getById(REPORT_SCHEMA, 823);
        assertNotNull(byId, "check id of table");
        TseReport report = new TseReport(byId);

        TseReportValidator validator = new TseReportValidator(report, reportService, tableDaoService);

        // validate the report
        Collection<ReportError> errors = validator.validate();
    }
}
