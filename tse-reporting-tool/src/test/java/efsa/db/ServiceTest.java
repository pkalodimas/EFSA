package efsa.db;

import app_config.GlobalManager;
import config.Config;
import config.Environment;
import data_collection.*;
import dataset.DcfDatasetsList;
import dataset.IDcfDataset;
import formula.FormulaException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import report.Report;
import resource.DcfResourcesList;
import resource.IDcfResourceReference;
import soap.*;
import soap_interface.IGetDatasetsList;
import user.User;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceTest {
    private static User user;

    @BeforeAll
    public static void before() {
        user = User.getInstance();
        user.login("papagst", "Z7bkT@XP2");
        user.addData("orgCode", "EFSA");
    }

    @Test
    public void testAll() throws Exception {
        Set<String> names = new HashSet<>();
        IDcfDataCollectionsList<IDcfDataCollection> list = new DcfDataCollectionsList();

        GetDataCollectionsList<IDcfDataCollection> req = new GetDataCollectionsList<>(false);
        req.getList(Config.getEnvironment(), user, list);

        List<DcfDataCollection> ddcList = list.stream().map(DcfDataCollection.class::cast).collect(Collectors.toList());
        for (DcfDataCollection ddc :ddcList) {
            DcfResourcesList resourceList = new DcfResourcesList();
            GetResourcesList<IDcfResourceReference> request = new GetResourcesList<>();
            request.getList(Environment.PRODUCTION, user, ddc.getCode(), resourceList);

            for (IDcfResourceReference r: resourceList) {
                DCTableList output = new DCTableList();
                output.create();

                GetDataCollectionTables<DCTable> dataCollectionTables = new GetDataCollectionTables<>();
                dataCollectionTables.getTables(Config.getEnvironment(), user, r.getResourceId(), output);

                output.forEach(o -> names.add(o.getName()));
            }
        }
        System.out.println(String.join("\n", names));
    }

    @Test
    public void testOthers() throws Exception {
        String dcCode = "TSE.TEST";

        IDcfDataCollectionsList<IDcfDataCollection> datasetList = new DcfDataCollectionsList();
        GetDataCollectionsList<IDcfDataCollection> req = new GetDataCollectionsList<>(false);
        req.getList(Config.getEnvironment(), user, datasetList);
        IDcfDataCollection iDataset = datasetList.stream().filter(ds -> ds.getCode().equals(dcCode))
                .findFirst()
                .orElseThrow(() -> new Exception("No match found for dc code"));

        // find tableNames
        DcfDCTablesList tables = new DcfDCTablesList();
        GetDataCollectionTables<DcfDCTable> dataCollectionTables = new GetDataCollectionTables<>();
        dataCollectionTables.getTables(Config.getEnvironment(), user, iDataset.getResourceId(), tables);

        // get tableName
        String tableName = tables.stream()
                .map(DcfDCTable::getName)
                .filter(Objects::nonNull)
                .distinct()
                .filter(n -> n.startsWith("SSD2"))
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new Exception("No match found for dc code"));

        System.out.printf("Acquired fact table %s for dcCode %s", tableName, dcCode);

    }


    @Test
    public void testResource() throws Exception {
        GetFile getFile = new GetFile();
        File file = getFile.getFile(Environment.PRODUCTION, user, "05_293");
        System.out.println(file.getName());
    }
}
