package ltg.hg.analysis;
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

import ltg.hg.analysis.Event.ActionTypes;

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
	private Map<String, List<ObjectNode>> jsonLogFiles = new HashMap<>();
	// Clean data
	private List<Bout> bouts = new ArrayList<>();
	// Aggregates results
	private List<ObjectNode> aggregates;

	/**
	 * Calls the various functions to analyze data
	 * @param args
	 */
	public static void main(String[] args) {
		DataAnalysis da = new DataAnalysis();
		da.importData();
		da.cleanData();
		da.doAnalysis();
		da.dumpToXLSX();
	}


	public void importData() {
		System.out.print("Importing data...");
		jsonLogFiles.put("5ag", parseFile(DATA_FOLDER+"5ag_log.json"));
		jsonLogFiles.put("5at", parseFile(DATA_FOLDER+"5at_log.json"));
		jsonLogFiles.put("5bj", parseFile(DATA_FOLDER+"5bj_log.json"));
		aggregates = parseFile(DATA_FOLDER+"stats.json");
		System.out.print(" DONE!\n");
	}

	public void cleanData() {
		System.out.print("Cleaning data...");
		for (String run_id: jsonLogFiles.keySet())
			extractBouts(run_id);
		System.out.print(" DONE!\n");
	}


	private void extractBouts(String run_id) {
		long bout_start_ts = -1;
		long bout_stop_ts = -1;
		String bout_id = null;
		String habitat_configuration = null;
		List<Event> bout_log = null;
		for (ObjectNode item : jsonLogFiles.get(run_id)) {
			switch (item.get("event").textValue()) {
			case "start_bout":
				bout_start_ts =  getTs(item);
				bout_id = item.get("payload").get("bout_id").textValue();
				habitat_configuration = item.get("payload").get("habitat_configuration_id").textValue();
				bout_log = new ArrayList<>();
				break;
			case "stop_bout":
				bout_stop_ts = getTs(item);
				bouts.add(new Bout(run_id, habitat_configuration, bout_id, bout_start_ts, bout_stop_ts, bout_log));
				break;
			case "reset_bout":
				break;
			case "rfid_update":
				bout_log.add(new Event(	getTs(item), 
						item.get("payload").get("id").textValue(), 
						item.get("payload").get("departure").textValue(), 
						item.get("payload").get("arrival").textValue() 
						));
				break;
			case "kill_tag":
				bout_log.add(new Event(	getTs(item), 
						item.get("payload").get("id").textValue(), 
						ActionTypes.KILL
						));
				break;
			case "resurrect_tag":
				bout_log.add(new Event(	getTs(item), 
						item.get("payload").get("id").textValue(), 
						ActionTypes.REVIVE
						));
				break;
			default:
				System.err.println("Unknown message type \"" + item.get("event").textValue() + "\"... what! Terminating");
				System.exit(-1);
				break;
			}
		}
	}


	public void doAnalysis() {
		System.out.print("Analyzing...");
		for (Bout b: bouts)
			b.analyzeData();
		System.out.print(" DONE!\n");
	}


	public void dumpToXLSX() {
		System.out.print("Dumping results...");
		// Create output folder and workbook
		new File(OUTPUT_FOLDER).mkdir();
		Workbook wb = new XSSFWorkbook();
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(OUTPUT_FOLDER + "hg_fall_13_per_student_data.xlsx");
		} catch (FileNotFoundException e) {
			System.out.println("Can't create the XLSX file, terminating");
			System.exit(-1);
		}
		// One Worksheet per bout
		for (Bout bout: bouts)
			wb = dumpBoutStatsToXLSX(wb, bout);
		// Write and close file
		try {
			wb.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			System.out.println("Can't write/close the XLSX file, terminating");
			System.exit(-1);
		}
		System.out.print(" DONE!\n");
	}

	private Workbook dumpBoutStatsToXLSX(Workbook wb, Bout bout) {
		String sheetName = bout.run_id + "_bout" + bout.bout_id;
		Sheet sheet = wb.createSheet(sheetName);
		sheet = addSheetHeader(sheet);
		int i=0;
		for (String tag_id : bout.results.cumulativeTimeAtPatchPerTag.keySet()) {
			Row row = sheet.createRow(++i);
			row.createCell(0).setCellValue(tag_id);
			row = addTimesToSheet(row, bout.results.cumulativeTimeAtPatchPerTag.get(tag_id));
			row = addHarvestsToSheet(row, bout.results.cumulativeHarvestAtPatchperTag.get(tag_id));
			row = addAggregatesToSheet(row, bout.run_id, bout.bout_id, tag_id);
		}
		return wb;
	}

	private Sheet addSheetHeader(Sheet sheet) {
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("name");
		row.createCell(1).setCellValue("time at A");
		row.createCell(2).setCellValue("time at B");
		row.createCell(3).setCellValue("time at C");
		row.createCell(4).setCellValue("time at D");
		row.createCell(5).setCellValue("time at E");
		row.createCell(6).setCellValue("time at F");
		row.createCell(7).setCellValue("time tot");
		row.createCell(8).setCellValue("harv at A");
		row.createCell(9).setCellValue("harv at B");
		row.createCell(10).setCellValue("harv at C");
		row.createCell(11).setCellValue("harv at D");
		row.createCell(12).setCellValue("harv at E");
		row.createCell(13).setCellValue("harv at F");
		row.createCell(14).setCellValue("harv tot");
		row.createCell(15).setCellValue("avg quality");
		row.createCell(16).setCellValue("avg competition");
		row.createCell(17).setCellValue("total moves");
		row.createCell(18).setCellValue("arbitrage");
		row.createCell(19).setCellValue("avg risk");
		return sheet;
	}

	private Row addTimesToSheet(Row row, List<Integer> times) {
		int i = 1;
		int total = 0;
		for (int t : times) {
			row.createCell(i++).setCellValue(t);
			total+=t;
		}
		row.createCell(i).setCellValue(total);
		return row;
	}

	private Row addHarvestsToSheet(Row row, List<Double> harvests) {
		int i = 8;
		int total = 0;
		for (double h : harvests) {
			row.createCell(i++).setCellValue(h);
			total+=h;
		}
		row.createCell(i).setCellValue(total);
		return row;
	}

	private Row addAggregatesToSheet(Row row, String run_id, String bout_id, String tag_id) {
		JsonNode user = null;
		for (ObjectNode bout : aggregates)
			if (bout.get("run_id").textValue().equals(run_id) && bout.get("bout_id").textValue().equals(bout_id))			
				for (JsonNode u: (ArrayNode) bout.get("user_stats"))
					if (u.get("name").textValue().equals(tag_id))
						user = u;
		row.createCell(15).setCellValue(user.get("avg_quality").asDouble());
		row.createCell(16).setCellValue(user.get("avg_competition").asDouble());
		row.createCell(17).setCellValue(user.get("total_moves").asDouble());
		row.createCell(18).setCellValue(user.get("arbitrage").asDouble());
		row.createCell(19).setCellValue(user.get("avg_risk").asDouble());
		return row;
	}


	//	public String removeNull(JsonNode jsonNode) {
	//		if (jsonNode.asText().equals("null"))
	//			return "";
	//		else
	//			return jsonNode.asText();
	//	}


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
