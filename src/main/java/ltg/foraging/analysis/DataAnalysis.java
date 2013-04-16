package ltg.foraging.analysis;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import au.com.bytecode.opencsv.CSVWriter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataAnalysis {

	private List<ObjectNode> jsonData = new ArrayList<ObjectNode>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataAnalysis da = new DataAnalysis();
		da.importData();
		da.dumpToCSV();
		//da.doAnalysis();
		//da.printResults();
	}


	public void dumpToCSV() {
		// Init CSV file
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new FileWriter("data/helio_sp_13/ben_log.csv"));
			//writer = new CSVWriter(new FileWriter("data/helio_sp_13/julia_log.csv"));
			Iterator<ObjectNode> i = jsonData.iterator();
			while (i.hasNext()) {
				ObjectNode n = i.next();
				if (n.get("event").asText().equals("init_helio") || 
						n.get("event").asText().equals("init_helio_diff")) {
					//i.remove();
				} else {
					writer.writeNext(entryToArray(n));
				}
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("Can't create the CSV file, terminating");
			System.exit(-1);
		}
	}


	private String[] entryToArray(ObjectNode n) {
		String[] entries = new String[6];	
		Date d = new Date(1000*getTs(n));
		SimpleDateFormat df = new SimpleDateFormat("mm/dd/yy HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("America/Toronto"));
		entries[0] = df.format(d);
		Long.toString(getTs(n));
		entries[1] = removeNull(n.get("origin"));
		entries[2] = removeNull(n.get("event"));
		entries[3] = removeNull(n.get("payload").get("anchor"));
		entries[4] = removeNull(n.get("payload").get("color"));
		entries[5] = removeNull(n.get("payload").get("reason"));
		return entries;
	}
	
	private String removeNull(JsonNode jsonNode) {
		if (jsonNode!=null)
			return jsonNode.asText();
		else
			return "";
	}


	public void importData() {
		ObjectMapper jsonParser = new ObjectMapper();

		// Read from file into jsonData list of json objects
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream("data/helio_sp_13/ben_log.json");
			//			fstream = new FileInputStream("data/helio_sp_13/julia_log.json");		
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			JsonNode jn = null;
			try {
				while ((strLine = br.readLine()) != null)   {
					try {
						jn = jsonParser.readTree(strLine);
						if (!jn.isObject()) {
							System.err.println("This is not a JSON object!!!");
							System.exit(-1);
						}
						jsonData.add((ObjectNode) jn);
					} catch (JsonParseException e) {
						System.err.println("Error parsing: this is not a JSON object!!!");
						System.exit(-1);
					}
				}
			} catch (IOException e) {
				System.err.println("Impossible to parse file line");
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println("Impossible to open file");
		} catch (IOException e) {
			System.err.println("Impossible to close file");
		}
		// Check sequence is ordered by timestamp
		long lastTs = -1;
		for (ObjectNode o: jsonData) {
			long currentTs = getTs(o);
			if (lastTs <= currentTs)
				lastTs = currentTs;
			else
				System.err.println("Sequence is not ordered properly... BAD!");
		}
	}



	public void doAnalysis() {

	}


	public void printResults() {

	}



	private long getTs(ObjectNode o) {
		return Integer.parseInt(o.get("_id").get("$oid").asText().substring(0, 8), 16);
	}

}
