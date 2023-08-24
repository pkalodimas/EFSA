<p align="center">
	<img src="http://www.efsa.europa.eu/profiles/efsa/themes/responsive_efsa/logo.png" alt="European Food Safety Authority"/>
</p>

# External installer
This maven project module, written in Java, can be used for launching the tool. In addition the module also checks, each time is launched, if new versions of the application are released in the GitHub repository.
If a new release is present, then it asks the user if to download the updated software.
In that case, the old version is replaced with the new one (maintaining some special folders which should not be lost while updating, as the database).
The installer can be configured by setting up correctly the "config/githubConfig.xml" file.

_Please note that if no version is present while launching the application, the library will install the latest release without asking anything._

_**Please note that the external installer differs from the internal installer in that it contains the JRE included in the root folder and saves the application information directly in the folder where the installer is located.**_

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

## Project structure

		├── app (where the application is downloaded)
		├── config
		|   ├── githubConfig.xml
		├── jre1.8.0_152 (example of JRE which contains java)
		|   ├── bin
		|       └── java.exe
		├── temp (temporary files are stored here)
		|   ├── temp1
		|   └── temp2
		├── launcher.exe (executable which starts the library)


## githubConfig.xml file

* Github.RepositoryName: this is the name of the repository where the library should look to see if there are new releases. Example: catalogue-browser
* Github.RepositoryOwner: this is the username of the owner of the repository. Example: openefsa
* Application.JavaPath: this is the path where java.exe is located (needed to start the application). Example: ..\jre1.8.0_111\bin\java.exe
* Application.Keyword: keyword which will be searched in the new release assets to identify the file which contains the new application. Example: onlyapp-win-64bit
* Application.Folder: name of the folder which will contain the application. Default is app
* Application.ConfigFile: name of the configuration file of the application inside the Application.Folder directory. Example: config\appConfig.xml  This is used
to retrieve the current version of the application.
* Application.JarPath: the application jar which will be executed after the updates checking.
* Application.IconFolder: folder of the icons of the application inside the Application.Folder (if needed)
* Application.NameEntry: the entry which contains the name of the application inside the Application.ConfigFile file
* Application.VersionEntry: the entry which contains the version of the application inside the Application.ConfigFile file
* Application.IconEntry: the entry which contains the filename of the icon of the application inside the Application.ConfigFile file
* Application.DatabaseFolder: the name of the folder inside the Application.Folder which contains the database of the application (this will not be deleted with the new releases!)
