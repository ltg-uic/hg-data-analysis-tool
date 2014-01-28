package ltg.foraging.analysis;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ltg.foraging.analysis.Action.ActionTypes;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataAnalysis {

	private static String DATA_FOLDER = "data/hg_fall_13/";
	private static String OUTPUT_FOLDER = DATA_FOLDER + "out/";

	// Dirty data
	private Map<String, List<ObjectNode>> jsonLogFiles = new HashMap<String, List<ObjectNode>>();
	private List<ObjectNode> aggregates;
	// Clean data
	private List<Bout> bouts = new ArrayList<>();

	/**
	 * Calls the various functions to analyze data
	 * @param args
	 */
	public static void main(String[] args) {
		DataAnalysis da = new DataAnalysis();
		da.importData();
		da.cleanData();
		//da.doAnalysis();
		da.dumpToXLSX();
		//da.printResults();
	}


	public void importData() {
		jsonLogFiles.put("5ag", parseFile(DATA_FOLDER+"5ag_log.json"));
		jsonLogFiles.put("5at", parseFile(DATA_FOLDER+"5at_log.json"));
		jsonLogFiles.put("5bj", parseFile(DATA_FOLDER+"5bj_log.json"));
		aggregates = parseFile(DATA_FOLDER+"stats.json");
	}

	public void cleanData() {
		for (String run_id: jsonLogFiles.keySet())
			extractBouts(run_id);
	}


	private void extractBouts(String run_id) {
		long bout_start_ts = -1;
		long bout_stop_ts = -1;
		String bout_id = null;
		String habitat_configuration = null;
		List<Action> bout_log = null;
		for (ObjectNode item : jsonLogFiles.get(run_id)) {
			switch (item.get("event").textValue()) {
			case "start_bout":
				bout_start_ts =  getTs(item);
				bout_id = item.get("payload").get("bout_id").textValue();
				bout_id = item.get("payload").get("bout_id").textValue();
				bout_log = new ArrayList<>();
				break;
			case "stop_bout":
				bout_stop_ts = getTs(item);
				break;
			case "rfid_update":
				bout_log.add(new Action(	getTs(item), 
											item.get("payload").get("id").textValue(), 
											item.get("payload").get("departure").textValue(), 
											item.get("payload").get("arrival").textValue() 
											));
				break;
			case "kill_tag":
				bout_log.add(new Action(	getTs(item), 
											item.get("payload").get("id").textValue(), 
											ActionTypes.KILL
						));
				break;
			case "resurrect_tag":
				bout_log.add(new Action(	getTs(item), 
											item.get("payload").get("id").textValue(), 
											ActionTypes.REVIVE
						));
				break;
			default:
				System.err.println("Unknown message type... what! Terminating");
				System.exit(-1);
				break;
			}
			new Bout(run_id, habitat_configuration, bout_id, bout_start_ts, bout_stop_ts, bout_log);
		}
	}

	
	public void doAnalysis() {

	}


	public void dumpToXLSX() {
		dumpAggregateStatsToXLSX(aggregates);
	}

	private void dumpAggregateStatsToXLSX(List<ObjectNode> bouts) {
		// Create output folder and workbook
		new File(OUTPUT_FOLDER).mkdir();
		Workbook wb = new XSSFWorkbook();
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(OUTPUT_FOLDER + "aggregates.xlsx");
		} catch (FileNotFoundException e) {
			System.out.println("Can't create the XLSX file, terminating");
			System.exit(-1);
		}
		// One Worksheet per bout
		for (ObjectNode bout: bouts)
			wb = dumpBoutAggregateStatsToXLSX(wb, bout);
		// Write and close file
		try {
			wb.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			System.out.println("Can't write/close the XLSX file, terminating");
			System.exit(-1);
		}
	}

	private Workbook dumpBoutAggregateStatsToXLSX(Workbook wb, ObjectNode bout) {
		String sheetName = bout.get("run_id").textValue() + "_bout" + bout.get("bout_id").textValue();
		Sheet sheet = wb.createSheet(sheetName);
		int i = 0;
		Row row = sheet.createRow(i);
		row.createCell(0).setCellValue("name");
		row.createCell(1).setCellValue("harvest");
		row.createCell(2).setCellValue("avg_quality");
		row.createCell(3).setCellValue("avg_competition");
		row.createCell(4).setCellValue("total_moves");
		row.createCell(5).setCellValue("arbitrage");
		row.createCell(6).setCellValue("avg_risk");
		for (JsonNode user: (ArrayNode) bout.get("user_stats")) {
			Row r = sheet.createRow(++i);
			r.createCell(0).setCellValue(user.get("name").textValue());
			r.createCell(1).setCellValue(user.get("harvest").asDouble());
			r.createCell(2).setCellValue(user.get("avg_quality").asDouble());
			r.createCell(3).setCellValue(user.get("avg_competition").asDouble());
			r.createCell(4).setCellValue(user.get("total_moves").asDouble());
			r.createCell(5).setCellValue(user.get("arbitrage").asDouble());
			r.createCell(6).setCellValue(user.get("avg_risk").asDouble());
		}
		return wb;
	}




	public String removeNull(JsonNode jsonNode) {
		if (jsonNode.asText().equals("null"))
			return "";
		else
			return jsonNode.asText();
	}


	// Path relative to project root (e.g. "data/helio_sp_13/ben_log.json")
	public static List<ObjectNode> parseFile(String file) {
		List<ObjectNode> jsonData = new ArrayList<ObjectNode>();
		ObjectMapper jsonParser = new ObjectMapper();
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(file);		
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
		return jsonData;
	}

	public static long getTs(ObjectNode o) {
		return Integer.parseInt(o.get("_id").get("$oid").asText().substring(0, 8), 16);
	}

	//////////////////////////
	// Legacy export to CSV //
	//////////////////////////

	//public void dumpToCSV() {
	//	dumpAggregateStatsToCSV(jsonFiles.get(3));
	//}

	//private void dumpAggregateStatsToCSV(List<ObjectNode> bouts) {
	//// Create output folder
	//new File(OUTPUT_FOLDER).mkdir();
	//// One file per bout
	//for (ObjectNode bout: bouts)
	//dumpBoutAggregateStatsToCSV(bout);
	//}
	//
	//private void dumpBoutAggregateStatsToCSV(ObjectNode bout) {
	//String outputFileName = OUTPUT_FOLDER + bout.get("run_id").textValue() + "_bout" + bout.get("bout_id").textValue() + "_" + bout.get("habitat_configuration").textValue() + "_aggregate";
	//CSVWriter writer = null;
	//try {
	//writer = new CSVWriter(new FileWriter(outputFileName));
	//for (JsonNode user: (ArrayNode) bout.get("user_stats"))
	//writer.writeNext(userStatsToStringArray((ObjectNode) user));
	//writer.close();
	//} catch (IOException e) {
	//System.out.println("Can't create the CSV file, terminating");
	//System.exit(-1);
	//}
	//}
	//
	//private String[] userStatsToStringArray(ObjectNode user_stats) {
	//String[] entries = new String[7];
	//entries[0] = user_stats.get("name").textValue();
	//entries[1] = user_stats.get("harvest").asText();
	//entries[2] = user_stats.get("avg_quality").asText();
	//entries[3] = user_stats.get("avg_competition").asText();
	//entries[4] = user_stats.get("total_moves").asText();
	//entries[5] = user_stats.get("arbitrage").asText();
	//entries[6] = user_stats.get("avg_risk").asText();
	//return entries;
	//}

	//	private String[] entryToArray(ObjectNode n) {
	//		String[] entries = new String[6];	
	//		Date d = new Date(1000*getTs(n));
	//		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy kk:mm:ss");
	//		df.setTimeZone(TimeZone.getTimeZone("America/Toronto"));
	//		entries[0] = df.format(d);
	//		Long.toString(getTs(n));
	//		entries[1] = removeNull(n.get("origin"));
	//		entries[2] = removeNull(n.get("event"));
	//		entries[3] = removeNull(n.get("payload").get("anchor"));
	//		entries[4] = removeNull(n.get("payload").get("color"));
	//		entries[5] = removeNull(n.get("payload").get("reason"));
	//		return entries;
	//	}

}
