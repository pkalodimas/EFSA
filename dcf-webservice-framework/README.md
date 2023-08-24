<p align="center">
	<img src="http://www.efsa.europa.eu/profiles/efsa/themes/responsive_efsa/logo.png" alt="European Food Safety Authority"/>
</p>

# DCF Webservice Framework
This Maven project is a Java library which can be used to call the Data Collection Framework web services and retrieve their responses with standardised Java objects.
These objects provide several built-in useful functionalities which can also be extended to add custom methods for the specific business logic required.

In particular, all the web services inherit from the SOAPRequest object, which handles the SOAP connection.
This class uses the DcfUser class to retrieve the account credentials.

## Dependencies
All project dependencies are listed in the [pom.xml](pom.xml) file.

## Import the project
In order to import the project correctly into the integrated development environment (e.g. Eclipse), it is necessary to download the project together with all its dependencies.
The project and all its dependencies are based on the concept of "project object model" and hence Apache Maven is used for the specific purpose.
In order to correctly import the project into the IDE it is firstly required to create a parent POM Maven project (check the following [link](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html) for further information). 
Once the parent project has been created add the project and all the dependencies as "modules" into the pom.xml file as shown below: 

	<modules>

		<!-- dependency modules -->
		<module>module_1</module>
		...
		...
		...
		<module>module_n</module>
		
	</modules>
	
Next, close the IDE and extract all the zip packets inside the parent project.
At this stage you can simply open the IDE and import back the parent project which will automatically import also the project and all its dependencies.

_Please note that the "SWT.jar" and the "Jface.jar" libraries (if used) must be downloaded and installed manually in the Maven local repository since are custom versions used in the tool ((install 3rd party jars)[https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html])._

## Documentation
The procedure to call a web service is always the same:
1. Create an object which implements the IDcfUser interface (or extends the DcfUser class) that contains the credentials of the DCF user
		
		IDcfUser user = new DcfUser();  // default implementation provided by the library
		user.login("myUsername", "myPassword");
		
2. Create the output object (i.e. an object where all the data retrieved from the web service are saved). This object must implements the interface defined in the constructor signature of the web service object called, e.g., for the GetDatasetsList request it is necessary to provide an object which implements the IDcfDatasetsList interface (or extends the DcfDatasetsList class).

		IDcfDatasetsList output = new DcfDatasetsList();  // default implementation provided by the library
		
3. Instantiate the object related to the required web service
		
		GetDatasetsList<IDcfDataset> req = new GetDatasetsList<>(user, outputObject);
		
	Note that in this case, since we are dealing with a **List** object, it is needed to specify the type of the single object of 		the list. For the GetDatasetsList, the type of the object must implement the IDcfDataset interface. This is useful while dealing 	with custom implementations of the IDcfDataset interface, because it allows to use customized objects as input/output of the web 	service call.

4. Send the request to DCF

		req.getList();  // this will fill the output object
	
	Note that this call will fail for example if your user credentials are wrong or if no connection is available. If an error of this type occurs, then a MySOAPException will be thrown.
		
5. Access the data retrieved using the reference to the output object

		System.out.println(output);
		if (!output.isEmpty()) {
			System.out.println(output.get(0));
		}

## MySOAPException

A MySOAPException is a customized implementation of a SOAPException. In particular, it provides some methods to detect if the exception was thrown for wrong credentials reasons or for connection problems.

### Methods

		* isConnectionProblem()  // check whether the exception was thrown for connection issues
		* isUnauthorized() // check whether the exception was thrown for wrong credentials


## Available webservices

For each call, we suppose that we have already instantiated the user class with:

		IDcfUser user = new DcfUser();  // default implementation provided by the library
		user.login("myUsername", "myPassword");

### Ping
Send a ping to DCF, in order to check if it is active or not.

		Ping ping = new Ping(user);
		boolean ok = ping.ping();
		
		if (!ok)
			System.err.print("The DCF is not responding");
		else
			System.out.println("The DCF is active");
You can use this method also to verify the credentials of an user. In fact, if no exception is thrown then the credentials are correct. Otherwise, it may be possible that the user has inserted wrong credentials (you need to check the MySoapException object to check whether the exception was caused by this or other things, as explained before).

### GetAck
Get an acknowledgment (ack) for a specified message sent to DCF.

		GetAck request = new GetAck(user, "DCFmessageId");
		DcfAck ack = request.getAck();

#### DcfAck Methods
		boolean ready = ack.isReady();

Check if the ack was indeed created in DCF for the specified message.

		DcfAckLog log = ack.getLog();
		
Check the contents of the ack. Only available if the ack is ready, otherwise it returns null.
The DcfAckLog contains all the information related to the DcfAck, as the ack operation result code, the data collection involved, etc...
The DcfAckLog exposes the following methods:
* getDCCode(); get the code of the data collection in which the message was sent
* getDatasetId(); get the dataset id of the dataset involved in the message
* getDatasetStatus(); get the DcfDatasetStatus enumerator which contains the new status of the dataset after sending the considered message
* getMessageValResCode(); get an object which contains all the information of the messageValResCode node of the log
* getOpResCode(); get an object which contains all the information related to the operation result code node of the log
* isOk(); check if the operation result code is OK or KO (it returns true if it is OK)
* getOpResLog(); get an object which contains all the information related to the operation result log node of the log
* hasErrors(); check if the DcfAckLog contains errors in the opResLog node
* getOpResError(); get the most relevant error contained in the opResLog node. Errors detected:

	* USER_NOT_AUTHORIZED, "Account not authorized for the Data Collection", highest priority
	* NOT_EXISTING_DC, "The specified dcCode value is not a valid code registered in the system", medium priority
	* OTHER, "General error for op res error", lowest priority

### GetCataloguesList
Get a list containing all the metadata of the published DCF catalogues (only last versions).

		DcfCataloguesList output = new DcfCataloguesList();
		GetCataloguesList<IDcfCatalogue> req = new GetCataloguesList<>(user, output);
		req.getList();
		
### GetDataCollectionsList
Get a list containing all the metadata of the data collections related to the considered user.

		DcfDataCollectionsList output = new DcfDataCollectionsList();
		
		GetDataCollectionsList<IDcfDataCollection> request = new GetDataCollectionsList<>(user, output);
		request.getList();
		
### GetDatasetsList
Get a list containing all the metadata of the datasets related to the considered user for a specific data collection.
				
		String dcCode = "TSE.TEST";  // code of the data collection which will be considered
		DcfDatasetsList output = new DcfDatasetsList();
		
		GetDatasetsList request = new GetDatasetsList(user, dcCode, output);
		request.getList();
		
### GetResourcesList
Get a list containing all the metadata of the resources related to a considered data collection

		String dcCode = "TSE.TEST";  // code of the data collection which will be considered
		DcfResourcesList output = new DcfResourcesList();
		GetResourcesList request = new GetResourcesList(user, dcCode, output);
		request.getList();
		
### GetDataset
Get a dataset using its id.

		GetDataset request = new GetDataset(user, "datasetId");  // set the dataset id you prefer
		File file = request.getDatasetFile();  // get the file where the dataset was downloaded

### GetXsdFile
Get an xsd resource file using its id.

		GetXsdFile request = new GetXsdFile(user, "xsdFileId");  // set the id you prefer
		Document xsd = request.getXsdFile();

### UploadCatalogueFile
Upload a file to DCF.

		UploadCatalogueFile request = new UploadCatalogueFile(user);
		String logCode = request.send(file);  // send your File object to DCF
		
It returns the log code which can be used to retrieve the result of the operation with an ExportCatalogueFile request.
You can use this request for example to reserve/unreserve/publish/edit catalogues, with the proper attachment.

### ExportCatalogueFile
Get one among the following:
* The last published version of a catalogue
* The last internal version of a catalogue
* The log of an UploadCatalogueFile request

#### Export the last published version
Export the last published version of the chosen catalogue and returns a File object pointing to the downloaded file.

		ExportCatalogueFile request = new ExportCatalogueFile(user);
		File file = request.exportCatalogue("catalogueCode");  // set the code you prefer, as MTX
		
#### Export the last internal version
Export the last internal version of the catalogue if present, and returns a File object pointing to the downloaded file.

		ExportCatalogueFile request = new ExportCatalogueFile(user);
		File file = request.exportLastInternalVersion("catalogueCode");  // set the code you prefer, as MTX

#### Export a log
Export a log related to an UploadCatalogueFile request, and returns a File object pointing to the downloaded file.

		ExportCatalogueFile request = new ExportCatalogueFile(user);
		File file = request.exportLog("logCode");  // put the code of the log retrieved from the UploadCatalogueFile request, as 20180103_001_WS
		
### SendMessage
Send a message to the DCF. It can be used to insert/replace/submit/amend/accept/reject datasets.

		SendMessage request = new SendMessage(user, file);  // send the file to DCF
		MessageResponse response = request.send();
		
It returns a MessageResponse object which contains the detail of the DCF response. It exposes the following methods:

* isCorrect(); check if the message was sent correctly (true/false)
* getMessageId(); get the message id assigned to the message
* getTrxState(); get the transmission state in the response (either OK or KO)
* getTrxError(); get the error if present
* getErrorType(); get the type of error (it analyses the getTrxError and categorizes the error)

### GetDataCollectionTables
Get a list containing all the metadata of the tables of a data collection with the data collection configurations.

		DcfDCTablesList output = new DcfDCTablesList();
		GetDataCollectionTables req = new GetDataCollectionTables(user, output, "resourceId");  // set the id you prefer
		req.getTables();

## How to extends the DcfUser class
If your application needs an extended concept of 'user', you can extend the DcfUser class to still be able to call the dcf web services maintaining your implementation. In particular, make sure that your class either inherits from the DcfUser class, or implements the IDcfUser interface.

	public class CustomUser extends DcfUser

## How to use the DcfUser class

	DcfUser user = new DcfUser();
		
	user.login(username, password);

	// call the web services here passing the user reference


If you want you can extend the DcfUser class in order to make it a singleton. This has the advantage that the user credentials can be accessed everywhere in your application. Use this approach only if only one user at a time is active in the local application.

## How to extend a DCF class (a class which contains the result of a web service call)

### Simple objects
It is sufficient to extend the base class provided by the library. While calling a web service, you will be asked to provide the output object which will be filled with the web service information. This object must implement the related interface or extend the base class.

For example, image we want to extend the base class for Dcf datasets:

	public class CustomDataset extends DcfDataset {
		public void myCustomMethod() {
			...
		}
	}

In this class we can define whatever custom method. Most importantly we can use this class for the web service calls, in order to fill it with the web service information.

If you want to provide a complete new implementation, then you can simply implement the IDcfDataset interface in your custom dataset object:

	public class DcfDataset implements IDcfDataset {
		public void myCustomMethod() {
			...
		}
	}
	
However, note that this approach will not provide you any already created method which is present in the DcfDataset class.

### Lists
Lists are more complicated. In particular, it is necessary to implement a method which instantiate the objects of the lists and add them to the list (add and create methods).
The custom list should either implement the **IDcfList** interface or extend the base class provided by the library, as for the objects. In your custom list you will need to implement the add and create methods, in order to specify which are your custom elements of the list.

For example, image we need to create a custom list for storing a IDcfDataset list (IDcfDataset is the interface for the datasets objects):

	public class CustomDatasetList extends ArrayList<IDcfDataset> implements IDcfDatasetList

As you can see we extend the ArrayList class to make our class indeed a list and then we implement the IDcfDatasetList interface in order to be able to use the list as input/output of the Dcf web services methods.

The interface will require to implement the following methods:

	* public IDcfDataset create()
	* public boolean add(IDcfDataset elem)

Since we want a list of custom objects, we need to specify their type (this was done to decouple the web service class from the implementation of the input/output objects). Therefore, in the create method we will return our custom implementation of the IDcfDataset interface:

	
	@Override
	public IDcfDataset create() {
		return new CustomDcfDataset();
	}

And since we extended the ArrayList class, the **add method is already implemented inside it.**

In this way it is possible to use the list in the web services call. For example, for the GetDatasetList call it will be:

	CustomDatasetList output = new CustomDatasetList();
	
	// the user is not specified here, but it is an object which implements IDcfuser
	GetDataCollectionsList<IDcfDataCollection> request = new GetDataCollectionsList<>(user, output);
	request.getList();  // fill the output list
	
	// here the list contains all the data retrieved from the web service
	// and we can access them and also call our custom methods defined in
	// our customized class
	output.get(0).myCustomMethod();