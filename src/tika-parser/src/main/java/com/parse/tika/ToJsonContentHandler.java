package com.parse.tika;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.tika.sax.ToTextContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.parse.deduplication.Deduplication;

public class ToJsonContentHandler extends ToTextContentHandler {
	
	long id;
	String jobType;
	String firstSeenDate;
	String startdate;
	String endDate;
	String key;
	float latitude;
	float longitude;

	public ToJsonContentHandler(final OutputStream stream,long id) {
		super(stream);
		this.id=id;
	}
	
	@Override
	public void startDocument() throws SAXException {
		//write("{");
		//write("\"job\"");
		write("{");
	}
	
	protected void write(final String string) throws SAXException {
		super.characters(string.toCharArray(), 0, string.length());
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		super.startElement(null, name, name, null);
		write("\"");
		key = atts.getValue("id");
		write(key);
		//System.out.println(atts.getValue("id"));
		write("\":");
	}

	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(null, name, name);
		write(",");
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		boolean flag = false;
		String s = "";
		for(char c : ch)
		{
			s +=c;
			if(!s.equals("\t"))
				flag = true;
		}
		if(flag)
		{
			switch (key) {
			case "postedDate":
				startdate = new String(ch);
				break;
			case "lastSeenDate":
				endDate = new String(ch);
					break;
			case "longitude":
				longitude = new Float(new String(ch));
					break;
			case "jobType":
				jobType = new String(ch);
				break;
			case "latitude":
				latitude = new Float(new String(ch));
					break;
			default:
				break;
			}
			write("\"");
			//System.out.println(ch);
			super.characters(ch, start, length);
			write("\"");
		}
	}

	@Override
	public void endDocument() throws SAXException {
		write("\"id\":\""+id+"\","
				+"\"postDuration\":\""+dateDiff(startdate, endDate)+"\","
				+"\"isSouth\":\""+isSouthAmerica(latitude, longitude)+"\","
				+"\"Region\":\""+getRegion(latitude, longitude)+"\","
				+"\"score\":\""+Deduplication.getScore(jobType, latitude,longitude)+"\""
				+ "}");
		super.endDocument();
	}
	
	public long dateDiff(String date, String date2){
		if(date == null || date2 == null || date.length() < 7 || date2.length() <7)
			return 0;
		try {
			Date d1 = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH).parse(date);
			Date d2 = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH).parse(date2);
			return Math.abs(d1.getTime()-d2.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return 0;
	}
	public boolean isSouthAmerica(float lat, float lon){
		if(lat <= 12.457778 && lat >= -53.933333 && lon >= -81.326389 && lon<= -34.796894 ){
			return true;
		}
		//up 12.457778, left -81.326389
		//down -53.933333, right -34.796894
		return false;
	}
	public String getRegion(float lat, float lon){
		String val = "NA";
		if(isSouthAmerica(lat, lon)){
			if(lat <= 12.457778 && lat >= -20.933333 && lon >= -81.326389 && lon<= -57.796894 ){
				val = "NorthEast";
			}else if(lat <= 12.457778 && lat >= -20.933333 && lon >= -57.796894 && lon<= -34.796894 ){
				val = "NorthWest";
			}else if(lat <= -20.933333  && lat >= -53.933333 && lon >= -81.326389 && lon<= -57.796894 ){
				val = "SouthEast";
			}else if(lat <= -20.933333  && lat >= -53.933333 && lon >= -57.796894 && lon<= -34.796894 ){
				val = "SouthWest";
			}
		}else{
			if(lat <= 83.666667 && lat >= 45.518889 && lon >= -179.15 && lon<= -95.489444 ){
				val = "NorthWest";
			}else if(lat <= 83.666667 && lat >= 45.518889 && lon >= -95.489444 && lon<= -11.489444 ){
				val = "NorthWest";
			}else if(lat <= 45.518889 && lat >= 5.518889 && lon >=  -179.15 && lon<= -95.489444 ){
				val = "SouthEast";
			}else if(lat <= 45.518889 && lat >= 5.518889 && lon >= -95.489444 && lon<= -11.489444 ){
				val = "SouthWest";
			}
		}
		//up 83.666667, left  -179.15 //// north america 
		//down 5.518889, right -11.489444
		
		//up 12.457778, left -81.326389 /// south america
				//down -53.933333, right -34.796894
		return val;
	}
}
