package efsa.db;

import dataset.IDataset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import providers.*;
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
import tse_analytical_result.AnalyticalResult;
import tse_case_report.CaseReport;
import tse_config.CustomStrings;
import tse_report.TseReport;
import tse_summarized_information.SummarizedInfo;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static tse_config.CustomStrings.PROG_ID_COL;
import static tse_config.CustomStrings.RES_ID_COL;

public class DBTest {

    private final TableSchema REPORT_SCHEMA = TableSchemaList.getByName(CustomStrings.REPORT_SHEET);
    private final TableSchema SUMMARY_INFO_SCHEMA = TableSchemaList.getByName(CustomStrings.SUMMARIZED_INFO_SHEET);
    private final TableSchema CASE_INFO_SCHEMA = TableSchemaList.getByName(CustomStrings.CASE_INFO_SHEET);
    private final TableSchema ANALYTICAL_RESULTS_SCHEMA = TableSchemaList.getByName(CustomStrings.RESULT_SHEET);

    private static TseReportService reportService;
    private static ITableDaoService tableDaoService;

    private List<Integer> idsToClear = new ArrayList<>();

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
    public void testCreateDefaultResults() throws Exception {
        Database db = new Database();
        db.connect();

        TableRow byId = tableDaoService.getById(REPORT_SCHEMA, 514);
        assertNotNull(byId, "check id of table");
        TseReport sourceReport = new TseReport(byId);
        List<SummarizedInfo> sourceSI = reportService.getRecords(sourceReport, new TableSchema[]{SUMMARY_INFO_SCHEMA}).stream().map(SummarizedInfo::new).collect(Collectors.toList());
        List<CaseReport> sourceCR = reportService.getRecords(sourceReport, new TableSchema[]{CASE_INFO_SCHEMA}).stream().map(CaseReport::new).collect(Collectors.toList());

        SummarizedInfo summarizedInfo = sourceSI.stream().filter(si -> si.getType().equals(CustomStrings.SUMMARIZED_INFO_BSE_TYPE)).findFirst().orElse(null);
        CaseReport caseReport = sourceCR.stream().findFirst().orElse(null);
        assertNotNull(summarizedInfo);
        assertNotNull(caseReport);
        reportService.createDefaultResults(sourceReport, summarizedInfo, caseReport);

        List<AnalyticalResult> sourceAR = reportService.getRecords(sourceReport, new TableSchema[]{ANALYTICAL_RESULTS_SCHEMA}).stream().map(AnalyticalResult::new).collect(Collectors.toList());
        Assertions.assertEquals(3, sourceAR.size());
    }

    @Test
    public void testCopyReport() throws Exception {
        Database db = new Database();
        db.connect();

        TseReport sourceReport = new TseReport(tableDaoService.getById(REPORT_SCHEMA, 105));
        List<SummarizedInfo> sourceSI = reportService.getRecords(sourceReport, new TableSchema[] { SUMMARY_INFO_SCHEMA }).stream().map(SummarizedInfo::new).collect(Collectors.toList());
        List<CaseReport> sourceCR = reportService.getRecords(sourceReport, new TableSchema[] { CASE_INFO_SCHEMA }).stream().map(CaseReport::new).collect(Collectors.toList());
        List<AnalyticalResult> sourceAR = reportService.getRecords(sourceReport, new TableSchema[] { ANALYTICAL_RESULTS_SCHEMA }).stream().map(AnalyticalResult::new).collect(Collectors.toList());

//        TseReport amendedReport = reportService.amend(sourceReport);
//        idsToClear.add(amendedReport.getDatabaseId());

        TseReport targetReport = new TseReport(tableDaoService.getById(REPORT_SCHEMA, 514));
        reportService.copyReport(sourceReport, targetReport);

        List<SummarizedInfo> targetSI = reportService.getRecords(targetReport, new TableSchema[] { SUMMARY_INFO_SCHEMA }).stream().map(SummarizedInfo::new).collect(Collectors.toList());
        List<CaseReport> targetCR = reportService.getRecords(targetReport, new TableSchema[] { CASE_INFO_SCHEMA }).stream().map(CaseReport::new).collect(Collectors.toList());
        List<AnalyticalResult> targetAR = reportService.getRecords(targetReport, new TableSchema[] { ANALYTICAL_RESULTS_SCHEMA }).stream().map(AnalyticalResult::new).collect(Collectors.toList());

        Assertions.assertEquals(targetSI.size(), sourceSI.size());
        Assertions.assertEquals(targetCR.size(), sourceCR.size());
        Assertions.assertEquals(targetAR.size(), sourceAR.size());

        for (SummarizedInfo si : targetSI) {
            int id = si.getDatabaseId();
            String progId = si.get(PROG_ID_COL).getCode();
            String resId = si.get(RES_ID_COL).getCode().split("\\.")[0];
            assertTrue(resId.equals(progId), format("si (%s) incorrect resId/progId rel", id));
            assertNotNull(progId, "Prog id should not be null from source: " + id);
            assertFalse(progId.isEmpty(), "Prog id should not be empty from source: " + id);
        }

        for (AnalyticalResult ar: targetAR) {
            int id = ar.getDatabaseId();
            String progId = ar.get(PROG_ID_COL).getCode().split("\\.")[0];
            String resId = ar.get(RES_ID_COL).getCode().split("\\.")[0];
            assertTrue(resId.equals(progId), format("si (%s) incorrect resId/progId rel", id));
            assertNotNull(progId, "Prog id should not be null from source: " + id);
            assertFalse(progId.isEmpty(), "Prog id should not be empty from source: " + id);
        }

        sourceSI.forEach(source -> assertTrue(assertSI(source, targetSI), format("no SI for id %s", source.getDatabaseId())));
        sourceCR.forEach(source -> assertTrue(assertCR(source, targetCR), format("no CR for id %s", source.getDatabaseId())));
        sourceAR.forEach(source -> assertTrue(assertAR(source, targetAR), format("no AR for id %s", source.getDatabaseId())));
    }

    @AfterEach
    public void clearDb() {
        for (Integer id: idsToClear) {
            if (id == 1) break;
            System.out.println("Deleting report with id: " + id);
            tableDaoService.delete(REPORT_SCHEMA, id);
        }
    }

    private boolean assertSI(SummarizedInfo source, List<SummarizedInfo> targetList) {
        boolean foundMatch;
        for (SummarizedInfo target: targetList) {
            foundMatch = Objects.equals(source.getType(), target.getType()) &&
                    Objects.equals(source.getTypeBySpecies(), target.getTypeBySpecies()) &&
                    Objects.equals(source.getSpecies(), target.getSpecies()) &&
                    Objects.equals(source.get("animage"), target.get("animage")) &&
                    Objects.equals(source.get("sex"), target.get("sex"));
            if (foundMatch) {
                String targetProgId = target.get(CustomStrings.PROG_ID_COL).getCode();
                assertNotEquals(source.get(CustomStrings.PROG_ID_COL).getCode(), targetProgId, "SI prog id not equals");

                String targetResId = target.get(RES_ID_COL).getCode();
                assertNotNull(targetResId, "Res id should not be null from source: " + target.getDatabaseId());
                assertFalse(targetResId.isEmpty(), "Res id should not be empty from source: " + target.getDatabaseId());
                assertNotEquals(source.get(RES_ID_COL).getCode(), targetResId, "SI res id not equals");

                System.out.printf("matched SI %s with %s\n", source.getDatabaseId(), target.getDatabaseId());
                return true;
            }
        }
        return false;
    }

    private boolean assertCR(CaseReport source, List<CaseReport> targetList) {
        boolean foundMatch;
        for (CaseReport target: targetList) {
            foundMatch = Objects.equals(source.get("sampleEventAsses"), target.get("sampleEventAsses")) &&
                    Objects.equals(source.get("sampId"), target.get("sampId")) &&
                    Objects.equals(source.get("part"), target.get("part")) &&
                    Objects.equals(source.get("sex"), target.get("sex"));
            if (foundMatch) {
                System.out.printf("matched CR %s with %s\n", source.getDatabaseId(), target.getDatabaseId());
                return true;
            }
        }
        return false;
    }

    private boolean assertAR(AnalyticalResult source, List<AnalyticalResult> targetList) {
        boolean foundMatch;
        for (AnalyticalResult target: targetList) {
            foundMatch = Objects.equals(source.get("anMethType"), target.get("anMethType")) &&
                    Objects.equals(source.get("testAim"), target.get("testAim")) &&
                    Objects.equals(source.get("analysisY"), target.get("analysisY"));
            if (foundMatch) {
                String targetResId = target.get(RES_ID_COL).getCode();
                assertNotNull(targetResId, "Res id should not be null from source: " + target.getDatabaseId());
                assertFalse(targetResId.isEmpty(), "Res id should not be empty from source: " + target.getDatabaseId());
                assertNotEquals(source.get(RES_ID_COL).getCode(), targetResId, "AR res id not equals");
                System.out.printf("matched AR %s with %s\n", source.getDatabaseId(), target.getDatabaseId());
                return true;
            }
        }
        return false;

    }
}
