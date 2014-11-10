#!/usr/bin/python
#!/usr/bin/env python
# Years till 100
import optparse, os, sys, subprocess

def callWorkflow(dir):
	# subprocess.call(["./tomcat/bin/startup.sh"])
	# subprocess.call(["./oodt/bin/oodt", "start"])
	# subprocess.call(["./oodt/resmgr/bin/batch_stub", "2001"])
	
	print "Ingestion Start Time:"
	subprocess.call("date")
	
	subprocess.call(["./oodt/crawler/bin/crawler_launcher", "--operation", "--launchAutoCrawler", "--filemgrUrl",
	"http://localhost:9000", "--clientTransferer", "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory",
	"--productPath", dir, "--mimeExtractorRepo",
	"../policy/mime-extractor-map.xml", "--workflowMgrUrl", "http://localhost:9001",
	"-ais", "TriggerPostIngestWorkflow"])
	
	print "Ingestion End Time:"
	subprocess.call("date")

parser = optparse.OptionParser()
parser.add_option('-i', '--inputDir', dest='inputDir', help='Input Dir')

(options, args) = parser.parse_args()

if options.inputDir is None:
	options.inputDir = raw_input('Enter Input Dir Name:')

if options.inputDir:
	print options.inputDir
	if os.path.isdir(options.inputDir):
		print "Dir Exist"
		try:
			fileList = os.listdir(options.inputDir)
		except OSError:
			print "Error: Input Dir doesn't exists!"
			sys.exit(2)
		# elif len(fileList) == 0:
		#    print "Error: Blank Dir, please pass the correct dir!"
		#    sys.exit(2)
		else:
			if len(fileList) == 0:
				print "Error: Blank Dir, please pass the correct dir!"
				sys.exit(2)
			else:
				print "Launching Workflow Manager"
				callWorkflow(options.inputDir)
	else:
		print "Error: Input dir doesn\'t exists!"
		sys.exit(2)



