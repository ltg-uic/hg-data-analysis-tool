package ltg.foraging.analysis;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.exceptions.JsonParseException;
import com.github.jsonj.tools.JsonParser;


public class DataAnalysis {
	
	private int gameBeginTime = -1;
	private int gameEndTime = -1;
	private Histories h = new Histories();
	

	public void importData() {
		JsonParser parser = new JsonParser();
		List<JsonObject> jsonData = new ArrayList<JsonObject>();
		// Read from file into jsonData
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream("/Users/tebemis/Desktop/Dropbox/Foraging_Data_Analysis/foraging_pilot_oct12_log_1.json");
			//fstream = new FileInputStream("/Users/tebemis/Desktop/Dropbox/Foraging_Data_Analysis/foraging_pilot_oct12_log_3.json");
			//fstream = new FileInputStream("/Users/tebemis/Desktop/Dropbox/Foraging_Data_Analysis/foraging_pilot_oct12_log_3.json");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			JsonObject json = null;
			JsonElement jsone = null;
			try {
				while ((strLine = br.readLine()) != null)   {
					try {
						jsone = parser.parse(strLine);
						if (!jsone.isObject()) {
							System.err.println("This is not a JSON object!!!");
						}
						json = jsone.asObject();
						jsonData.add(json);
					} catch (JsonParseException e) {
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
		// Parse JSON objects into data structure
		for (JsonObject o: jsonData) {
			if (o.getString("event").equals("game_reset")) {
				gameBeginTime =  getTs(o);
				continue;
			}
			if (o.getString("event").equals("game_stop")) {
				gameEndTime = getTs(o);
				continue;
			}
			if (o.getString("event").equals("rfid_update")) {
				String dest = o.getString("destination");
				JsonArray a = o.getArray("payload", "arrivals");
				JsonArray d = o.getArray("payload", "departures");
				if (a.isEmpty())
					h.addAction(d.get(0).toString(), getTs(o), -1, dest);  	// It's a departure
				else
					h.addAction(a.get(0).toString(), getTs(o),  1, dest); 	// It's an arrival
				continue;
			}
			//System.err.println("Unknown message type... what!");
		}
	}
	
	
	
	public void doAnalysis() {
		
	}
	
	
	public void printResults() {
		System.out.println("Run time: "+(gameEndTime-gameBeginTime)+"s");
	}

	

	private int getTs(JsonObject o) {
		return Integer.parseInt(o.getString("_id", "$oid").substring(0, 8), 16);
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataAnalysis da = new DataAnalysis();
		da.importData();
		da.doAnalysis();
		da.printResults();
	}

}
