package com.parse.tika;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class TSVToXHTML extends XHTMLContentHandler {

	public TSVToXHTML(ContentHandler handler, Metadata metdata){
		super(handler, metdata);
	}
	
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		 super.ignorableWhitespace(ch, start, length);
	}
	
	public void rowInsert(String id, String text) throws SAXException{
		if (text != null && text.length() > 0) {
			startElement("td", "id", id);
			characters(text);
			endElement("td");
		}
	}
}
