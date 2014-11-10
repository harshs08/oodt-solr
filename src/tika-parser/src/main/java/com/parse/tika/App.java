package com.parse.tika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.xml.sax.SAXException;

import com.parse.deduplication.Deduplication;

public class App {

	static ArrayList<String> fileNames;
	
	public static long totalCount;

	public static void main(final String[] args) throws IOException,
			SAXException, TikaException {
		
		if (args.length > 1) {
			// base folder
			final File folder = new File(args[0]);
			fileNames = listFilesForFolder(folder);

			// deduplication: 0 - without; 1 - with
			int task = Integer.parseInt(args[1]);

			Deduplication dedup = new Deduplication();

			totalCount = 0;
			int withDedup = 0;
			StringBuffer output = new StringBuffer();

			DateFormat dateFormat = new SimpleDateFormat(
					"yyyy/MM/dd HH:mm:ss.SSS");
			Calendar cal = Calendar.getInstance();
			System.out.println("Parse start time: "
					+ dateFormat.format(cal.getTime()));
			switch (task) {
			case 0:
				withDedup = 0;
				break;

			case 1:
				withDedup = 1;
				break;

			case 2:
				withDedup = 2;
				break;

			default:
				System.out.println("Please enter a valid task type");
			}
			boolean flag  = true;
			for (int y = 0; y < 2; y++) {
			for (int i = 0; i < fileNames.size(); i++) {
				try {
					System.out.println(" Started file " + fileNames.get(i));
					CustomParser parser = new CustomParser();
					Metadata metadata = new Metadata();
					File f = new File(args[0] + "/" + fileNames.get(i));

					ToHTMLContentHandler h = new ToHTMLContentHandler();
					long count = parser.parseWithCount(new FileInputStream(f),
							h, metadata, new ParseContext(), dedup, true,flag);
					totalCount = totalCount + count;
					String out = " Job count for " + fileNames.get(i) + " = "
							+ count + "; total jobs count till now "
							+ totalCount;
					String dedupOut = ", total unique job count (deduplication) "
							+ dedup.getCount();
					if (withDedup==1) {
						System.out.println(out + dedupOut);
						output.append(out + dedupOut + "\n");
					} else if(withDedup==0) {
						System.out.println(out);
						output.append(out + "\n");
					}
					else if(withDedup==2){
						dedup.findNearlyDuplicates();
						System.out.println(out + ", total unique job count (nearly duplicates) " +  dedup.getNearlyDedupCount());
					}


				} catch (Exception e) {
					//e.printStackTrace();
				}
				//break;

			}
			flag = false;
			totalCount =0;
			}
			DateFormat dateFormat2 = new SimpleDateFormat(
					"yyyy/MM/dd HH:mm:ss.SSS");
			Calendar cal2 = Calendar.getInstance();
			System.out.println("Parse start time: "
					+ dateFormat2.format(cal2.getTime()));
			//dedup.findNearlyDuplicates();
			//dedup.getLL();

			System.out
					.println("Get Deduplicates" + dedup.getNearlyDedupCount());

			PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
			writer.println(output);
			writer.close();
		} else {
			System.out
					.println("2 arguments are required, 1st one is tsv input directory and 2nd one is the deduplication flag");
			System.exit(0);
		}
	}

	public static ArrayList<String> listFilesForFolder(final File folder) {
		fileNames = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			fileNames.add(fileEntry.getName());
		}
		return fileNames;
	}
}
