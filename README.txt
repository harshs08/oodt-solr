The installation, configuration and execution of this project is divided in 5 basic steps:

1. Installing and configuring tika-parser and running it to generate json files to be posted to solr.
2. Configuring Solr.
3. Installing and configuring oodt and custom-workflow which ingests the input json files and sends to solr for indexing.
4. Running the launch.py to post the data to solr using oodt workflow manager and crawler.
4. Compiling and executing the results.java program to get the answers to the assignment questions.


1a. Steps for Installing and Configuring tika-parser:
-----------------------------------------------------
Compile and package using the command:
cd src/tika-parser
mvn package

Run tika-parser App using the command assuming you are in tika-parser directory:
java -cp "target/tika-parser-1.0-SNAPSHOT.jar:./target/lib/*" com.parse.tika.App /path/to/input/tsv/dir 1

Command Explanation
java -cp $CLASSPATH$ com.parse.tika.App [input folder] [deduplication switch]

[input folder] - String: Path to the folder containing all the TSV files
[deduplication switch] - int: Selector to chose whether to execute the crawler with or without Deduplication. 0 => Without, 1 => With.


2a. Configuring Solr:
---------------------
Copy the schema.xml file from src/files-to-copy/solr to solr conf directory in your solr installation.


3a. Steps for installing oodt:
------------------------------
Install the radix version of oodt in the src directory using the following link: https://cwiki.apache.org/confluence/display/OODT/RADiX+Powered+By+OODT

Note:
1. Update the version of OODT not lower than 0.7 in pom.xml if its less than 0.7. (Before building the source.)
2. In default installation, cas-pge-<version>.jar may not present in workflow and filemgr home, if so copy that file to both [WORKFLOW_HOME]/lib and [FILEMGR_HOME]/lib from [PGE_HOME]/lib. Also copy cas-filemgr-<version>.jar from [FILEMGR_HOME]/lib in [WORKFLOW_HOME]/lib and [RESMGR_HOME]/lib.
3. If using solr with Tomcat, make sure the port of oodt tomcat server is not conflicting. If both solr's tomcat and oodt's tomcat servers are using same port, change the Connector and Server ports in oodt installations in the oodt/tomcat/conf/server.xml and oodt/tomcat/conf/server-minimal.xml


3b. Steps for installing and configuring custom-pge with oodt (provided you are in the src folder):
---------------------------------------------------------------------------------------------------
1. Compiling custom-pge
Follow the steps from here: https://cwiki.apache.org/confluence/display/OODT/CAS-PGE+Learn+by+Example
Especially step 4.
cd /usr/local/src/custom-pge
mvn package
cp target/custom-pge-1.0.jar /oodt/$WORKFLOW_HOME/lib
cp target/custom-pge-1.0.jar /oodt/$RESMGR_HOME/lib
cp target/custom-pge-1.0.jar /oodt/$FILEMGR_HOME/lib
(check if the custom-pge-1.0.jar is copied in above mentioned directories)

2. Configure custom-workflow
Note: This directory contains the workflow which will post the json files to solr for indexing. Also assuming ETLlib is installed on the disk already.
Modify the custom-workflow/pge-configs/PGEConfig.xml as following:
i.   Update <metadata key="JobDir" val="/Users/harshsingh/Documents/Codes/IR/solroodt"/> to the path of oodt installation.
ii.  Update  the value of '/etllib/bin/poster' to the poster executable location in etllib and 'http://localhost:8080/solr/update/json?commit=true' to actual uri of solr installation in <cmd>echo [FileLocation]/[Filename] | ./etllib/bin/poster -v -u http://localhost:8080/solr/update/json?commit=true</cmd>

3. Configure oodt Crawler
Note: the below mentioned files are modified to integrate the custom-workflow
i.   Copy and replace crawler_launcher from src/files-to-copy/crawler/bin to /oodt/$CRAWLER_HOME/bin
ii.  Copy and replace action-beans.xml from src/files-to-copy/crawler/policy to /oodt/$CRAWLER_HOME/policy
iii. Copy and replace cmd-line-options.xml from src/files-to-copy/crawler/policy to /oodt/$CRAWLER_HOME/policy
iv.  Copy and mime-extractor-map.xml from src/files-to-copy/crawler/policy to /oodt/$CRAWLER_HOME/policy
v.   Copy mimetypes.xml from src/files-to-copy/crawler/policy to /oodt/$CRAWLER_HOME/policy
vi.  Copy tikaextractor.config from src/files-to-copy/crawler/policy to /oodt/$CRAWLER_HOME/policy

4. Configure oodt Workflow Manager
Note: the below mentioned files are modified to integrate the custom-workflow
i.   Copy and replace events.xml from src/files-to-copy/workflow/policy to /oodt/$WORKFLOW_HOME/policy
ii.  Copy and replace workflow-instance-met.xml from src/files-to-copy/workflow/policy to /oodt/$WORKFLOW_HOME/policy
iii. Copy CustomWorkflow.workflow.xml from src/files-to-copy/workflow/policy to /oodt/$WORKFLOW_HOME/policy
iv.  Copy tasks.xml from src/files-to-copy/workflow/policy to /oodt/$WORKFLOW_HOME/policy
	 Also, update <property name="PGETask_ConfigFilePath" value="/Users/harshsingh/Documents/Codes/IR/solroodt/custom-workflow/pge-configs/PGEConfig.xml" envReplace="true"/> to the actual path of PGEConfig.xml


4a. Executing launch.py
(this step crawls the json files and post to solr for indexing using oodt workflow manager & crawler):
------------------------------------------------------------------------------------------------------
Prerequisites before executing launch.py
i.   Solr is running (if running with port 8080 on tomcat, you need to change the port oodt).
ii.  OODT is running. (File manager running on port 9000, Workflow Manager running on port 9001 and Resource Manager running on 9002).
iii. OODT Batch Stub is running on port 2001.

Assuming you are in the src directory, run the following command:

python launch.py -i /path/to/json/files

where: /path/to/json/files should be pointing to json files directory.
Note: The files generated by tika-parser are present in (src/tika-parser/output)


5a. Executing the results.java to get query answers:
----------------------------------------------------
Compile and package using the command:
cd src/queries
mvn package

Run queries App using the command assuming you are in queries directory:
java -cp "target/queries-1.0-SNAPSHOT.jar:./target/lib/*" com.solr.queries.App "http://localhost:8080/solr/collection1/" " *" " *"

Command Explanation
java -cp $CLASSPATH$ [solr url] [period start date] [period end date]
[solr url] - String: http://localhost:8080/solr/collection1/
[period start date] - String: “ *” if you want to start at the beginning or date in YYYY-MM-DDThh:mm:ssZ format
[period end date] - String: “ *” if you want to end at the last date or date in YYYY-MM-DDThh:mm:ssZ format

NOTE: Do not forget the space before the “*” in “ *”

Important Debugging note:
-------------------------
If you are getting error while executing any of the following steps, following steps may help you solve the problem:
1. Check the that solr, oodt, oodt file manager, oodt workflow manager, oodt batch stub are running properly.
2. Check if all the above mentioned jars are files are copied correctly to assigned places.
3. The configuration files are properly updated.
4. The path provided to output and input directories exists and have proper permission to read and write.
5. If compile time errors are faced check if all the libraries are getting downloaded by maven.
