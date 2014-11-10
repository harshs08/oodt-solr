package com.parse.tika;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.tika.sax.xpath.Matcher;
import org.apache.tika.sax.xpath.MatchingContentHandler;
import org.apache.tika.sax.xpath.XPathParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.parse.deduplication.Deduplication;

@SuppressWarnings("serial")
public class CustomParser implements Parser {

	private static final Set<MediaType> SUPPORTED_TYPES = Collections
			.singleton(MediaType.text("tab-separated-values"));

	private long count = 0;

	private boolean withDedup;

	private boolean preprocessing = false;

	private Deduplication dedup;

	public Set<MediaType> getSupportedTypes(ParseContext context) {
		return SUPPORTED_TYPES;
	}

	public static final XPathParser xpathparser = new XPathParser("xhtml", XHTMLContentHandler.XHTML);

	private static final Matcher rowMatcher = xpathparser.parse("/xhtml:html/xhtml:body/xhtml:table/xhtml:tr/descendant::node()");

	public long  parseWithCount(InputStream stream, ContentHandler handler,
			Metadata metadata, ParseContext context , Deduplication dedup, boolean withDedup, boolean preprocessing) throws IOException,
			SAXException, TikaException{
		this.dedup = dedup;
		this.withDedup = withDedup;
		this.preprocessing = preprocessing;
		parse(stream, handler, metadata, context);


		return count;
	}

	@Override
	public void parse(InputStream stream, ContentHandler handler,
			Metadata metadata, ParseContext context) throws IOException,
			SAXException, TikaException {

		// CharsetDetector expects a stream to support marks
		if (!stream.markSupported()) {
			stream = new BufferedInputStream(stream);
		}

		// Detect the CONTENT_ENCODING (the stream is reset to the beginning)
		CharsetDetector detector = new CharsetDetector();
		String incomingCharset = metadata.get(Metadata.CONTENT_ENCODING);

		// Detect the CONTENT_TYPE from incoming stream metadata
		String incomingType = metadata.get(Metadata.CONTENT_TYPE);

		//Check for incomingCharset and incomingType
		if (incomingCharset == null && incomingType != null) {
			MediaType mt = MediaType.parse(incomingType);
			if (mt != null) {
				incomingCharset = mt.getParameters().get("charset");
			}
		}

		if (incomingCharset != null) {
			detector.setDeclaredEncoding(incomingCharset);
		}

		//Set the CONTENT_ENCODING in metadata object
		detector.setText(stream);
		for (CharsetMatch match : detector.detectAll()) {
			if (Charset.isSupported(match.getName())) {
				metadata.set(Metadata.CONTENT_ENCODING, match.getName());
				break;
			}
		}

		//Detect CONTENT_ENCODING from Metadata object passed
		String encoding = metadata.get(Metadata.CONTENT_ENCODING);
		if (encoding == null) {
			throw new TikaException(
					"Text encoding could not be detected and no encoding"
							+ " hint is available in document metadata");
		}

		//Set the CONTENT_TYPE for output handler object
		metadata.set(Metadata.CONTENT_TYPE, "application/xhtml+xml");
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(stream, encoding));

			// TIKA-240: Drop the BOM when extracting plain text
			reader.mark(1);
			int bom = reader.read();
			if (bom != '\ufeff') { // zero-width no-break space
				reader.reset();
			}

			Map<String, String> map = new HashMap<String, String>();

			count = 0;

			//XHTMLContentHandler html = new XHTMLContentHandler(handler,metadata);

			String jsonFile = "";
			String outputDir = "output";

			//get and set of header content
			//getLineFromTSV(map, FieldConstants.HEADER);
			//setLineToXML(xhtml, map);

			//get and set of lines content
			OutputStream fileOutput;
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {

				//creating unique json file names
				jsonFile = new StringBuffer("job_").append(App.totalCount+count).append(".json").toString();

				//creating the new files from the output stream emitted
				File file = new File(outputDir,jsonFile);
				fileOutput = new FileOutputStream(file);

				ContentHandler toJsonContentHandler = new ToJsonContentHandler(fileOutput,App.totalCount+count);

				ContentHandler matcher = new MatchingContentHandler(toJsonContentHandler,rowMatcher);

				TSVToXHTML xhtml = new TSVToXHTML(matcher, metadata);

				xhtml.startDocument();
				xhtml.startDocument();
				xhtml.startElement("table");
				getLineFromTSV(map, line);

				setLineToXML(xhtml, map);
				xhtml.endElement("table");
				xhtml.endDocument();
				count++;
				boolean b=false;
				if(preprocessing){
					dedup.preprocessing(map);
				}else{
				if(withDedup)
					b = dedup.calculateOriginalDocs(file,map);

				}
				//System.out.println(count);
				map.clear();
				if(!b || preprocessing)
				file.delete();
				fileOutput.close();

			}
			map.clear();//clear the map after the read and write

		} catch (UnsupportedEncodingException e) {
			throw new TikaException("Unsupported text encoding: " + encoding, e);
		}
		catch (Exception e) {

		}finally {
			reader.close();
		}
	}

	private void getLineFromTSV(final Map<String, String> map, final String line) {
		String[] params = line.split("\t");

		try {
				map.put(FieldConstants.POSTED_DATE, params[0]+"T17:33:18Z");

				map.put(FieldConstants.LOCATION1, params[1]);

				map.put(FieldConstants.DEPARTMENT, params[2]);

				map.put(FieldConstants.TITLE, params[3]);

				map.put(FieldConstants.SALARY, params[5]);

				map.put(FieldConstants.START_DATE, params[6]);

				map.put(FieldConstants.DURATION, params[7]);

				map.put(FieldConstants.JOB_TYPE, params[8]);

				map.put(FieldConstants.APPLICATIONS, params[9]);

				map.put(FieldConstants.COMPANY, params[10]);

				map.put(FieldConstants.CONTACT_PERSON, params[11]);

				map.put(FieldConstants.PHONE_NUMBER, params[12]);

				map.put(FieldConstants.FAX_NUMBER, params[13]);

				map.put(FieldConstants.LOCATION2, params[14]);

				map.put(FieldConstants.LATITUDE, params[15]);

				map.put(FieldConstants.LONGITUDE, params[16]);

				map.put(FieldConstants.FIRST_SEEN_DATE, params[17]+"T17:33:18Z");

				map.put(FieldConstants.URL, params[18]);

				map.put(FieldConstants.LAST_SEEN_DATE, params[19]+"T17:33:18Z");


		} catch (ArrayIndexOutOfBoundsException e) {
		//	e.printStackTrace();
		}
	}

	private void setLineToXML(TSVToXHTML xhtml, Map<String, String> map) throws SAXException {
		xhtml.startElement("tr");
		xhtml.rowInsert(FieldConstants.POSTED_DATE,
				map.get(FieldConstants.POSTED_DATE));

		xhtml.rowInsert(FieldConstants.LOCATION1,
				map.get(FieldConstants.LOCATION1));

		xhtml.rowInsert(FieldConstants.DEPARTMENT,
				map.get(FieldConstants.DEPARTMENT));

		xhtml.rowInsert(FieldConstants.TITLE,
				map.get(FieldConstants.TITLE));

		xhtml.rowInsert(FieldConstants.SALARY,
				map.get(FieldConstants.SALARY));

		xhtml.rowInsert(FieldConstants.START_DATE,
				map.get(FieldConstants.START_DATE));

		xhtml.rowInsert(FieldConstants.DURATION,
				map.get(FieldConstants.DURATION));

		xhtml.rowInsert(FieldConstants.JOB_TYPE,
				map.get(FieldConstants.JOB_TYPE));

		xhtml.rowInsert(FieldConstants.APPLICATIONS,
				map.get(FieldConstants.APPLICATIONS));

		xhtml.rowInsert(FieldConstants.COMPANY,
				map.get(FieldConstants.COMPANY));

		xhtml.rowInsert(FieldConstants.CONTACT_PERSON,
				map.get(FieldConstants.CONTACT_PERSON));

		xhtml.rowInsert(FieldConstants.PHONE_NUMBER,
				map.get(FieldConstants.PHONE_NUMBER));

		xhtml.rowInsert(FieldConstants.FAX_NUMBER,
				map.get(FieldConstants.FAX_NUMBER));

		xhtml.rowInsert(FieldConstants.LOCATION2,
				map.get(FieldConstants.LOCATION2));

		xhtml.rowInsert(FieldConstants.LATITUDE,
				map.get(FieldConstants.LATITUDE));

		xhtml.rowInsert(FieldConstants.LONGITUDE,
				map.get(FieldConstants.LONGITUDE));

		xhtml.rowInsert(FieldConstants.FIRST_SEEN_DATE,
				map.get(FieldConstants.FIRST_SEEN_DATE));

		xhtml.rowInsert(FieldConstants.URL,
				map.get(FieldConstants.URL));

		xhtml.rowInsert(FieldConstants.LAST_SEEN_DATE,
				map.get(FieldConstants.LAST_SEEN_DATE));

		xhtml.endElement("tr");

	}
}
