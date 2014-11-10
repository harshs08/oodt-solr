package com.parse.tika;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class XHTMLToJSON extends XHTMLContentHandler {

	public static boolean documentStarted;
	
	public static StringBuilder JsonOutput; 
	
	
	public XHTMLToJSON(ContentHandler handler, Metadata metadata) {
		super(handler, metadata);
		documentStarted = false;
		JsonOutput = new StringBuilder(100);
		// TODO Auto-generated constructor stub
	}
	
	
    /**
     * Starts an XHTML document by setting up the namespace mappings 
     * when called for the first time.
     * The standard XHTML prefix is generated lazily when the first
     * element is started.
     */
    @Override
    public void startDocument() throws SAXException {
    	
		if(!documentStarted){
    		documentStarted = true;
            super.startDocument();
            startElement(0);
    	}
    }

    public void append(String value)
    {
    	JsonOutput.append(value);
    }
    
    public void startElement(int indentLevel){
    	StringBuilder element = new StringBuilder(10);
        for(int i = 0; i < indentLevel; i++)
        {
        	element.append("\t");
        }
        element.append("{\n");
        append(element.toString());
    }

    public void fillElement(String name, String value, boolean multipleValues, boolean lastNameValuePair, int indentLevel)
            throws SAXException {
        StringBuilder element = new StringBuilder(10);
        for(int i = 0; i < indentLevel; i++)
        {
        	element.append("\t");
        }
        element.append("\"");
        element.append(name);
        element.append("\" : ");
        if(multipleValues)
        {
        	element.append("\n");
        }
        else
        {
    		element.append("\"");
    		element.append(value);
    		element.append("\"");
        	if(lastNameValuePair)
        		element.append("\n");
        	else
        		element.append(",\n");
        }
        append(element.toString());
	}
	
    public void endElement(int indentLevel){
    	StringBuilder element = new StringBuilder(10);
        for(int i = 0; i < indentLevel; i++)
        {
        	element.append("\t");
        }
        element.append("}\n");
        append(element.toString());
    }
    
    public void endDocument(int indentLevel){
    	endElement(0);
    }
}

