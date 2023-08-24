package soap_test;

import org.junit.jupiter.api.Tag;

// run only a subset of tests
/*@RunWith(Categories.class)
@IncludeCategory(DownloadLogTests.class)
@SuiteClasses(WebserviceTest.class)
public class RunTests {

}*/

@Tag("DownloadLogTests")
@Tag("WebserviceTest")
public class RunTests {

}
