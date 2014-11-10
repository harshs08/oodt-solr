package com.solr.queries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.*;

public class App {

	// connection parameters
	static HttpURLConnection connectionObj;
	private final static String REQUEST_METHOD = "GET";
	private final static int CONNECTION_TIMEOUT = 5000;
	private final static int READ_TIMEOUT = 10000;

	// list of locations sorted using our score
	String[] rankedLocations;
	// unsorted list of locations
	String[] unrankedLocations;

	HashMap<String, LocationJobType> query1;
	String startDate, endDate;

	DecimalFormat df;

	public App() {
		// TODO Auto-generated constructor stub
		// For formatting the display of doubles
		df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
	}

	public static void main(String args[]) {
		URL givenUrl = null;
		BufferedReader br = null;
		StringBuilder sb = null;
		String line = null;

		App q = new App();

		if (args[1].trim().equals("*"))
			q.startDate = "the beginning";
		else
			q.startDate = args[1].trim();

		if (args[2].trim().equals("*"))
			q.endDate = "the end";
		else
			q.endDate = args[2].trim();

		q.query1 = new HashMap<String, LocationJobType>();

		try {
			// all the queries to be executed
			ArrayList<String> queries = new ArrayList<String>();
			// 1. generate ranked list of locations
			queries.add(args[0]
					+ "select?&q={!boost%20b=score}*:*&group=true&group.field=location1&wt=json&indent=true&group.limit=0&sort=score%20asc&fl=score&rows=100");

			// 2. query1
			queries.add(args[0]
					+ "select?&q=postedDate:["
					+ args[1].trim()
					+ "%20TO%20"
					+ args[2].trim()
					+ "]&fl=id&facet=true&facet.pivot=jobType,location1&facet.sort=count&wt=json&indent=true&rows=0");

			// 3. query2:Comparison for NorthEast South America
			queries.add(args[0]
					+ "select?&q=Region:NorthEast%20AND%20isSouth:true&stats=true&stats.field=postDuration&wt=json&indent=true&rows=0");
			// 4. query2:Comparison for NorthWest South America
			queries.add(args[0]
					+ "select?&q=Region:NorthWest%20AND%20isSouth:true&stats=true&stats.field=postDuration&wt=json&indent=true&rows=0");
			// 5. query2:Comparison for SouthEast South America
			queries.add(args[0]
					+ "select?&q=Region:SouthEast%20AND%20isSouth:true&stats=true&stats.field=postDuration&wt=json&indent=true&rows=0");
			// 6. query2:Comparison for SouthWest South America
			queries.add(args[0]
					+ "select?&q=Region:SouthWest%20AND%20isSouth:true&stats=true&stats.field=postDuration&wt=json&indent=true&rows=0");
			// 7. query2:Comparison for other regions (Not South America)
			queries.add(args[0]
					+ "select?&q=isSouth:false&stats=true&stats.field=postDuration&wt=json&indent=true&rows=0");

			// 8. query3:List of locations that contain a Commercial zone
			queries.add(args[0]
					+ "select?&q=title:comercial%20OR%20comerciales%20OR%20coordinador%20OR%20empresa%20OR%20mantenimiento%20OR%20vendedor%20OR%20vendedores%20OR%20ventas&fl=title&facet=true&facet.field=location1&wt=json&indent=true&facet.sort=count&rows=0");
			// 9. query3:List of locations that contain a Shopping zone
			queries.add(args[0]
					+ "select?&q=title:ayudante%20OR%20cliente%20OR%20compras%20OR%20venta%20OR%20bodega&fl=title&facet=true&facet.field=location1&wt=json&indent=true&facet.sort=count&rows=0");
			// 10. query3:List of locations that contain a Industrial Zone
			queries.add(args[0]
					+ "select?&q=title:auxiliar%20OR%20industrial%20OR%20mecnico%20OR%20produccin%20OR%20operario&fl=title&facet=true&facet.field=location1&wt=json&indent=true&facet.sort=count&rows=0");
			// 11. query3:List of locations that contain a Residential zone
			queries.add(args[0]
					+ "select?&q=title:cocina%20OR%20estudiante%20OR%20conductor&fl=title&facet=true&facet.field=location1&wt=json&indent=true&facet.sort=count&rows=0");
			// 12. query3:List of locations that contain a Business zone
			queries.add(args[0]
					+ "select?&q=title:administrativa%20OR%20administrativo%20OR%20asesor%20OR%20consultor%20OR%20contable%20OR%20cuentas%20OR%20ejecutivo%20OR%20encargado%20OR%20gerente%20OR%20recepcionista%20OR%20analista&fl=title&facet=true&facet.field=location1&wt=json&indent=true&facet.sort=count&rows=0");
			// 13. query3:List of locations that contain a Medical zone
			queries.add(args[0]
					+ "select?&q=title:salud%20OR%20limpieza&fl=title&facet=true&facet.field=location1&wt=json&indent=true&facet.sort=count&rows=0");
			// 14. query3:List of locations that contain a Silicon Valley
			queries.add(args[0]
					+ "select?&q=title:desarrollador%20OR%20ingeniero%20OR%20java%20OR%20programador%20OR%20sistemas%20OR%20soporte%20OR%20tcnico%20OR%20web%20OR%20tecnico&fl=title&facet=true&facet.field=location1&wt=json&indent=true&facet.sort=count&rows=0");

			// 15. query4:Content based geographical distribution for given time
			// period
			queries.add(args[0]
					+ "select?&q=isSouth:true%20AND%20postedDate:["
					+ args[1].trim()
					+ "%20TO%20"
					+ args[2].trim()
					+ "]&facet=true&facet.pivot=jobType,Region&wt=json&indent=true&fl=score&facet.sort=count%20desc&rows=0");
			// 16. query4:Link based geographical distribution for given time
			// period
			queries.add(args[0]
					+ "select?&q={!boost%20b=score}jobType:Completo%20AND%20postedDate:["
					+ args[1].trim()
					+ "%20TO%20"
					+ args[2].trim()
					+ "]&group=true&group.field=Region&sort=score%20asc&wt=json&indent=true&fl=score");
			// 17. query4:Link based geographical distribution for given time
			// period
			queries.add(args[0]
					+ "select?&q={!boost%20b=score}jobType:Medio%20AND%20postedDate:["
					+ args[1].trim()
					+ "%20TO%20"
					+ args[2].trim()
					+ "]&group=true&group.field=Region&sort=score%20asc&wt=json&indent=true&fl=score");

			// Stores the query results
			JSONObject[] objs = new JSONObject[queries.size()];

			// execute all the queries
			for (int i = 0; i < queries.size(); i++) {

				// establish connection
				givenUrl = new URL(queries.get(i));
				connectionObj = (HttpURLConnection) givenUrl.openConnection();
				connectionObj.setRequestMethod(REQUEST_METHOD);
				connectionObj.setConnectTimeout(CONNECTION_TIMEOUT);
				connectionObj.setReadTimeout(READ_TIMEOUT);
				connectionObj.connect();

				// read the resulting json data
				br = new BufferedReader(new InputStreamReader(
						connectionObj.getInputStream()));
				sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					if (!line.trim().equals("")) {
						sb.append(line + "\n");
					}
				}
				br.close();

				// store the result
				objs[i] = new JSONObject(sb.toString());
			}

			// generate ranked list of locations
			q.getRankedLocations(objs);

			// parse and interpret results for query1
			q.query1ContentBased(objs);
			q.query1LinkBased(objs);

			// parse and interpret results for query2
			q.query2ContentBased(objs);

			// parse and interpret results for query3
			q.query3ContentBased(objs);

			// parse and interpret results for query4
			q.query4ContentBased(objs);
			q.query4LinkBased(objs);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// group by location and perform inter-group sorting based on our score;
	// retrieve the locations in the resulting order
	void getRankedLocations(JSONObject[] jsonArr) {
		JSONArray arrQuery;
		try {
			arrQuery = jsonArr[0].getJSONObject("grouped")
					.getJSONObject("location1").getJSONArray("groups");

			rankedLocations = new String[arrQuery.length()];

			query1 = new HashMap<String, LocationJobType>();

			for (int i = 0; i < arrQuery.length(); i++) {
				rankedLocations[i] = arrQuery.getJSONObject(i).getString(
						"groupValue");
				query1.put(rankedLocations[i], new LocationJobType());
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// hierarchically facet the data based on jobtype then location; get the
	// count of part time and full time jobs per location; predict based on this
	// information. ex: data states that Bogota has had 3 full time jobs and 2
	// part time jobs in all, then we'll predict Bogota will have full time job
	// posting in the future with a confidence of 60%
	void query1ContentBased(JSONObject[] jsonArr) {
		JSONArray arrQuery1;
		try {
			arrQuery1 = jsonArr[1].getJSONObject("facet_counts")
					.getJSONObject("facet_pivot")
					.getJSONArray("jobType,location1");

			JSONArray arrCompleto = null, arrMedio = null;
			for (int i = 0; i < arrQuery1.length(); i++) {
				if (arrQuery1.getJSONObject(i).getString("value")
						.equalsIgnoreCase("completo"))
					arrCompleto = arrQuery1.getJSONObject(i).getJSONArray(
							"pivot");
				if (arrQuery1.getJSONObject(i).getString("value")
						.equalsIgnoreCase("medio"))
					arrMedio = arrQuery1.getJSONObject(i).getJSONArray("pivot");
			}

			if (arrCompleto != null) {
				for (int i = 0; i < arrCompleto.length(); i++) {
					String locationName = arrCompleto.getJSONObject(i)
							.getString("value");
					if (query1.containsKey(locationName)) {
						LocationJobType ljObj = query1.get(locationName);
						ljObj.fullTime = arrCompleto.getJSONObject(i).getInt(
								"count");
						ljObj.location = locationName;
					} else {

						LocationJobType ljObj = new LocationJobType();
						ljObj.location = locationName;
						ljObj.fullTime = arrCompleto.getJSONObject(i).getInt(
								"count");
						query1.put(locationName, ljObj);
					}
				}
			}

			if (arrMedio != null) {
				for (int i = 0; i < arrMedio.length(); i++) {

					String locationName = arrMedio.getJSONObject(i).getString(
							"value");
					if (query1.containsKey(locationName)) {
						LocationJobType ljObj = query1.get(locationName);
						ljObj.partTime = arrMedio.getJSONObject(i).getInt(
								"count");
						ljObj.location = locationName;

					} else {
						LocationJobType ljObj = new LocationJobType();
						ljObj.location = locationName;
						ljObj.partTime = arrMedio.getJSONObject(i).getInt(
								"count");
						query1.put(locationName, ljObj);
					}
				}
			}
			unrankedLocations = new String[query1.size()];
			query1.keySet().toArray(unrankedLocations);
			System.out.println("Query1: Content Based Approach\n");
			System.out.println("Location Name:\t\t Job type (Confidence%)");
			for (int i = 0; i < 100; i++) {
				LocationJobType ljObj = query1.get(unrankedLocations[i]);
				int totJobs = ljObj.fullTime + ljObj.partTime;
				int fullProb = 0, partProb = 0;
				if (totJobs > 0) {
					fullProb = (ljObj.fullTime * 100) / totJobs;
					partProb = (ljObj.partTime * 100) / totJobs;
				}
				if (fullProb > partProb)
					System.out.println(ljObj.location + ":\t\t Full time ("
							+ fullProb + "%)");
				else
					System.out.println(ljObj.location + ":\t\t Part time ("
							+ partProb + "%)");
			}
			System.out
					.println("------------------------------------------------------------------------------------------------------------------");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// same prediction algorithm as content based is used; the only difference
	// is the order in which the places and their predictions are displayed; in
	// content based the order of display is arbitrary and doesn't mean
	// anything, but in link based, the prediction for the locations displays on
	// top have a higher chance being correct as compared to the lower jobs
	void query1LinkBased(JSONObject[] jsonArr) {
		System.out
				.println("\nQuery1: Link Based Approach (Sorted in the descending order of confidence)\n");
		System.out.println("Location Name:\t\t Job type (Confidence%)");
		for (int i = 0; i < rankedLocations.length; i++) {
			if(!rankedLocations[i].equals(""))
			{LocationJobType ljObj = query1.get(rankedLocations[i]);
			if (ljObj != null) {
				int totJobs = ljObj.fullTime + ljObj.partTime;
				int fullProb = 0, partProb = 0;
				if (totJobs > 0) {
					fullProb = (ljObj.fullTime * 100) / totJobs;
					partProb = (ljObj.partTime * 100) / totJobs;
				}
				if (fullProb > partProb)
					System.out.println(ljObj.location + ":\t\t Full time ("
							+ fullProb + "%)");
				else
					System.out.println(ljObj.location + ":\t\t Part time ("
							+ partProb + "%)");
			}}
		}
		System.out
				.println("------------------------------------------------------------------------------------------------------------------");
	}

	//Use self-defined fields - Region and postDuration; 
	//regionwise calculate the "stats" for the field postDuration;
	//compare min, max and avg stats of the different regions
	void query2ContentBased(JSONObject[] jsonArr) {
		System.out.println("Query2: Content Based Approach\n");
		JSONObject arrQuery2a = null, arrQuery2b, arrQuery2c, arrQuery2d, arrQuery2e;
		RegionJobCloseStats[] r = new RegionJobCloseStats[5];
		try {
			//for NorthEast South America
			arrQuery2a = jsonArr[2].getJSONObject("stats")
					.getJSONObject("stats_fields")
					.getJSONObject("postDuration");
			r[0] = new RegionJobCloseStats();
			r[0].name = "NorthEast South America";
			r[0].numOfJobs = arrQuery2a.getInt("count");
			r[0].setLeast(arrQuery2a.getInt("min"));
			r[0].setMost(arrQuery2a.getInt("max"));
			r[0].setAvg(arrQuery2a.getInt("mean"));
			r[0].setSd(arrQuery2a.getInt("stddev"));

		} catch (Exception e) {
			System.out.println("No jobs in NE South America");
		}

		//for NorthWest South America
		try {
			arrQuery2b = jsonArr[3].getJSONObject("stats")
					.getJSONObject("stats_fields")
					.getJSONObject("postDuration");
			r[1] = new RegionJobCloseStats();
			r[1].name = "NorthWest South America";
			r[1].numOfJobs = arrQuery2b.getInt("count");
			r[1].setLeast(arrQuery2b.getInt("min"));
			r[1].setMost(arrQuery2b.getInt("max"));
			r[1].setAvg(arrQuery2b.getInt("mean"));
			r[1].setSd(arrQuery2b.getInt("stddev"));
		} catch (Exception e) {
			System.out.println("No jobs in NW South America");
		}

		//for SouthEast South America
		try {

			arrQuery2c = jsonArr[4].getJSONObject("stats")
					.getJSONObject("stats_fields")
					.getJSONObject("postDuration");
			r[2] = new RegionJobCloseStats();
			r[2].name = "SouthEast South America";
			r[2].numOfJobs = arrQuery2c.getInt("count");
			r[2].setLeast(arrQuery2c.getInt("min"));
			r[2].setMost(arrQuery2c.getInt("max"));
			r[2].setAvg(arrQuery2c.getInt("mean"));
			r[2].setSd(arrQuery2c.getInt("stddev"));

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println("No jobs in SE South America");
		}

		//for SouthWest South America
		try {

			arrQuery2d = jsonArr[5].getJSONObject("stats")
					.getJSONObject("stats_fields")
					.getJSONObject("postDuration");
			r[3] = new RegionJobCloseStats();
			r[3].name = "SouthWest South America";
			r[3].numOfJobs = arrQuery2d.getInt("count");
			r[3].setLeast(arrQuery2d.getInt("min"));
			r[3].setMost(arrQuery2d.getInt("max"));
			r[3].setAvg(arrQuery2d.getInt("mean"));
			r[3].setSd(arrQuery2d.getInt("stddev"));
		} catch (Exception e) {
			System.out.println("No jobs in SW South America");
		}

		//for other regions
		try {

			arrQuery2e = jsonArr[6].getJSONObject("stats")
					.getJSONObject("stats_fields")
					.getJSONObject("postDuration");
			r[4] = new RegionJobCloseStats();
			r[4].name = "Other";
			r[4].numOfJobs = arrQuery2e.getInt("count");
			r[4].setLeast(arrQuery2e.getInt("min"));
			r[4].setMost(arrQuery2e.getInt("max"));
			r[4].setAvg(arrQuery2e.getInt("mean"));
			r[4].setSd(arrQuery2e.getInt("stddev"));
		} catch (Exception e) {
			System.out.println("No jobs in other regions");
		}

		System.out.println("The number of jobs in each region:");
		for (int i = 0; i < 5; i++) {
			if (r[i] != null)
				System.out.println("\t" + r[i].name + ": " + r[i].numOfJobs);
			else {
				r[i] = new RegionJobCloseStats();
				r[i].name = "";
				r[i].numOfJobs = 0;
				r[i].least = 0;
				r[i].most = 0;
				r[i].avg = 0;
				r[i].sd = 0;
			}
		}

		//sort regions according to the min time taken to close a job
		ArrayList<RegionJobCloseStats> leastTimeTaken = new ArrayList<RegionJobCloseStats>(
				5);
		leastTimeTaken.add(r[0]);
		for (int i = 1; i < 5; i++) {
			for (int j = 0; j < leastTimeTaken.size(); j++) {
				if (r[i].least > leastTimeTaken.get(j).least) {
					leastTimeTaken.add(i, r[i]);
					break;
				}
			}
			if (leastTimeTaken.size() < i + 1)
				leastTimeTaken.add(r[i]);
		}

		System.out
				.println("The order according to the minimum time taken to fill a job:");
		for (int i = 0; i < 5; i++) {
			if (!leastTimeTaken.get(i).name.equals(""))
				System.out.println("\t" + leastTimeTaken.get(i).name + " ("
						+ df.format(leastTimeTaken.get(i).least) + " days)");
		}

		//sort regions according to the max time taken to close a job
		ArrayList<RegionJobCloseStats> mostTimeTaken = new ArrayList<RegionJobCloseStats>(
				5);

		mostTimeTaken.add(r[0]);
		for (int i = 1; i < 5; i++) {
			for (int j = 0; j < mostTimeTaken.size(); j++) {
				if (r[i].least > mostTimeTaken.get(j).least) {
					mostTimeTaken.add(i, r[i]);
					break;
				}
			}
			if (mostTimeTaken.size() < i + 1)
				mostTimeTaken.add(r[i]);
		}

		System.out
				.println("The order according to maximum time taken to fill a job:");
		for (int i = 0; i < 5; i++) {
			if (!mostTimeTaken.get(i).name.equals(""))
				System.out.println("\t" + mostTimeTaken.get(i).name + " ("
						+ df.format(mostTimeTaken.get(i).most) + " days)");
		}

		//sort regions according to the avg time taken to close a job
		ArrayList<RegionJobCloseStats> averageTimeTaken = new ArrayList<RegionJobCloseStats>(
				5);

		averageTimeTaken.add(r[0]);
		for (int i = 1; i < 5; i++) {
			for (int j = 0; j < averageTimeTaken.size(); j++) {
				if (r[i].least > averageTimeTaken.get(j).least) {
					averageTimeTaken.add(i, r[i]);
					break;
				}
			}
			if (averageTimeTaken.size() < i + 1)
				averageTimeTaken.add(r[i]);
		}
		System.out
				.println("The order according to the average time taken to fill a job:");
		for (int i = 0; i < 5; i++) {
			if (!averageTimeTaken.get(i).name.equals(""))
				System.out.println("\t" + averageTimeTaken.get(i).name + " ("
						+ df.format(averageTimeTaken.get(i).avg)
						+ " days with standard deviation as "
						+ df.format(averageTimeTaken.get(i).sd) + " days)");
		}
		System.out
				.println("------------------------------------------------------------------------------------------------------------------");
	}

	void query3ContentBased(JSONObject[] jsonArr) {
System.out.println("Query3: Content Based Approach\n");
		try {
				String s = jsonArr[7].getJSONObject("facet_counts")
						.getJSONObject("facet_fields")
						.getJSONArray("location1").toString();
				String split[] = s.replaceAll("\"", " ")
						.substring(1, s.length() - 1).split(",");
				System.out.print("Commercial zone: ");
				for (int i = 1; i < 12; i += 2) {
					System.out.print(split[i - 1] + " ");
				}
				System.out.println("");
			
				 s = jsonArr[8].getJSONObject("facet_counts")
						.getJSONObject("facet_fields")
						.getJSONArray("location1").toString();
				 split = s.replaceAll("\"", " ")
						.substring(1, s.length() - 1).split(",");
				System.out.print("Shopping zone: ");
				for (int i = 1; i < 12; i += 2) {
					System.out.print(split[i - 1] + " ");
				}
				System.out.println("");
			
			s = jsonArr[9].getJSONObject("facet_counts")
						.getJSONObject("facet_fields")
						.getJSONArray("location1").toString();
				 split = s.replaceAll("\"", " ")
						.substring(1, s.length() - 1).split(",");
				System.out.print("Industrial zone: ");
				for (int i = 1; i < 12; i += 2) {
					System.out.print(split[i - 1] + " ");
				}
				System.out.println("");
			s = jsonArr[10].getJSONObject("facet_counts")
						.getJSONObject("facet_fields")
						.getJSONArray("location1").toString();
				 split = s.replaceAll("\"", " ")
						.substring(1, s.length() - 1).split(",");
				System.out.print("Residential zone: ");
				for (int i = 1; i < 12; i += 2) {
					System.out.print(split[i - 1] + " ");
				}
				System.out.println("");
			 s = jsonArr[11].getJSONObject("facet_counts")
						.getJSONObject("facet_fields")
						.getJSONArray("location1").toString();
				 split = s.replaceAll("\"", " ")
						.substring(1, s.length() - 1).split(",");
				System.out.print("Business zone: ");
				for (int i = 1; i < 12; i += 2) {
					System.out.print(split[i - 1] + " ");
				}
				System.out.println("");
			 s = jsonArr[12].getJSONObject("facet_counts")
						.getJSONObject("facet_fields")
						.getJSONArray("location1").toString();
				 split = s.replaceAll("\"", " ")
						.substring(1, s.length() - 1).split(",");
				System.out.print("Medical zone: ");
				for (int i = 1; i < 12; i += 2) {
					System.out.print(split[i - 1] + " ");
				}
				System.out.println("");
			s = jsonArr[13].getJSONObject("facet_counts")
						.getJSONObject("facet_fields")
						.getJSONArray("location1").toString();
				 split = s.replaceAll("\"", " ")
						.substring(1, s.length() - 1).split(",");
				System.out.print("Silicon Valley zone: ");
				for (int i = 1; i < 12; i += 2) {
					System.out.print(split[i - 1] + " ");				}
				System.out.println("");
			
			System.out
					.println("------------------------------------------------------------------------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void query4ContentBased(JSONObject[] jsonArr) {
		System.out.println("Query4: Content Based Approach\n");
		JSONArray arrQuery4;
		try {
			arrQuery4 = jsonArr[14].getJSONObject("facet_counts")
					.getJSONObject("facet_pivot")
					.getJSONArray("jobType,Region");

			JSONArray arrCompleto = null, arrMedio = null;
			for (int i = 0; i < arrQuery4.length(); i++) {
				if (arrQuery4.getJSONObject(i).getString("value")
						.equalsIgnoreCase("completo"))
					arrCompleto = arrQuery4.getJSONObject(i).getJSONArray(
							"pivot");
				if (arrQuery4.getJSONObject(i).getString("value")
						.equalsIgnoreCase("medio"))
					arrMedio = arrQuery4.getJSONObject(i).getJSONArray("pivot");
			}
			HashMap<String, LocationJobType> query4 = new HashMap<String, LocationJobType>();
			if (arrCompleto != null) {
				for (int i = 0; i < arrCompleto.length(); i++) {
					String locationName = arrCompleto.getJSONObject(i)
							.getString("value");
					if (query4.containsKey(locationName)) {
						LocationJobType ljObj = query4.get(locationName);
						ljObj.fullTime = arrCompleto.getJSONObject(i).getInt(
								"count");
						ljObj.location = locationName;
					} else {

						LocationJobType ljObj = new LocationJobType();
						ljObj.location = locationName;
						ljObj.fullTime = arrCompleto.getJSONObject(i).getInt(
								"count");
						query4.put(locationName, ljObj);
					}
				}
			}

			if (arrMedio != null) {
				for (int i = 0; i < arrMedio.length(); i++) {

					String locationName = arrMedio.getJSONObject(i).getString(
							"value");
					if (query4.containsKey(locationName)) {
						LocationJobType ljObj = query4.get(locationName);
						ljObj.partTime = arrMedio.getJSONObject(i).getInt(
								"count");
						ljObj.location = locationName;

					} else {
						LocationJobType ljObj = new LocationJobType();
						ljObj.location = locationName;
						ljObj.partTime = arrMedio.getJSONObject(i).getInt(
								"count");
						query4.put(locationName, ljObj);
					}
				}
			}
			String[] SARegions = new String[query4.size()];
			query4.keySet().toArray(SARegions);
			ArrayList<LocationJobType> fullTime = new ArrayList<LocationJobType>();
			ArrayList<LocationJobType> partTime = new ArrayList<LocationJobType>();

			int full = 0, part = 0;
			System.out
					.println("Trends in terms of Full time vs Part time jobs in South America observed from "
							+ startDate + " to " + endDate + ":");
			for (int i = 0; i < SARegions.length; i++) {
				LocationJobType ljObj = query4.get(SARegions[i]);
				full += ljObj.fullTime;
				part += ljObj.partTime;
				if (ljObj.fullTime > ljObj.partTime)
					fullTime.add(ljObj);
				else
					partTime.add(ljObj);
			}

			if (fullTime.size() > 0) {
				System.out
						.print("Geographical distribution of full time opportunities: ");
				for (int i = 0; i < fullTime.size(); i++) {
					if (i != fullTime.size() - 1)
						System.out.print(fullTime.get(i).location + " region ("
								+ (fullTime.get(i).fullTime * 100 / full)
								+ "%)" + ", ");
					else
						System.out.println(fullTime.get(i).location
								+ " region ("
								+ (fullTime.get(i).fullTime * 100 / full)
								+ "%)" + " ");
				}
			} else {
				System.out.println("No full time opportunities");
			}

			if (partTime.size() > 0) {
				System.out
						.print("Geographical distribution of part time opportunities: ");
				for (int i = 0; i < partTime.size(); i++) {
					if (i != partTime.size() - 1)
						System.out.print(partTime.get(i).location + " region ("
								+ (partTime.get(i).partTime * 100 / part)
								+ "%)" + ", ");
					else
						System.out.println(partTime.get(i).location
								+ " region ("
								+ (partTime.get(i).partTime * 100 / part)
								+ "%)" + " ");
				}
			} else {
				System.out.println("No part time opportunities");
			}
			System.out
					.println("------------------------------------------------------------------------------------------------------------------");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void query4LinkBased(JSONObject[] jsonArr) {
		System.out.println("Query4: Link Based Approach\n");
		JSONArray arrQuery4a, arrQuery4b;
		try {
			arrQuery4a = jsonArr[15].getJSONObject("grouped")
					.getJSONObject("Region").getJSONArray("groups");

			String fullTimeOrder = "";
			for (int i = 0; i < arrQuery4a.length(); i++) {
				if (!arrQuery4a.getJSONObject(i).getString("groupValue")
						.equalsIgnoreCase("na"))
					fullTimeOrder += arrQuery4a.getJSONObject(i).getString(
							"groupValue")
							+ "\n";
			}

			arrQuery4b = jsonArr[16].getJSONObject("grouped")
					.getJSONObject("Region").getJSONArray("groups");

			String partTimeOrder = "";
			for (int i = 0; i < arrQuery4b.length(); i++) {
				if (!arrQuery4b.getJSONObject(i).getString("groupValue")
						.equalsIgnoreCase("na"))
					partTimeOrder += arrQuery4b.getJSONObject(i).getString(
							"groupValue")
							+ "\n";
			}
			System.out
					.println("Trends in terms of Full time vs Part time jobs in South America observed from "
							+ startDate + " to " + endDate + ":");
			System.out
					.println("The order(best to worst) in which full time jobs are clustered:\n"
							+ fullTimeOrder);
			System.out
					.println("The order(best to worst) in which part time jobs are clustered:\n"
							+ partTimeOrder);

			System.out
					.println("------------------------------------------------------------------------------------------------------------------");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class LocationJobType {
	String location;
	int fullTime;
	int partTime;
}

class RegionJobCloseStats {

	String name;
	double least;
	double most;
	double avg;
	double sd;
	int numOfJobs;

	void setLeast(int s) {
		least = (double) s / (double) 86400000;
	}

	void setMost(int s) {
		most = (double) s / (double) 86400000;
	}

	void setAvg(int s) {
		avg = (double) s / (double) 86400000;
	}

	void setSd(int s) {
		sd = (double) s / (double) 86400000;
	}

}