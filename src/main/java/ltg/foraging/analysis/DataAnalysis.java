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
import java.util.List;

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

	private List<List<ObjectNode>> jsonFiles = new ArrayList<>();
	private static String DATA_FOLDER = "data/hg_fall_13/";
	private static String OUTPUT_FOLDER = DATA_FOLDER + "out/";

	/**
	 * Calls all the functions necessary to analyze data
	 * @param args
	 */
	public static void main(String[] args) {
		DataAnalysis da = new DataAnalysis();
		da.importData();
		//da.doAnalysis();
		da.dumpToXLSX();
		//da.printResults();
	}


	public void importData() {
		jsonFiles.add(parseFile(DATA_FOLDER+"5ag_log.json"));
		jsonFiles.add(parseFile(DATA_FOLDER+"5at_log.json"));
		jsonFiles.add(parseFile(DATA_FOLDER+"5bj_log.json"));
		jsonFiles.add(parseFile(DATA_FOLDER+"stats.json"));
	}


	public void doAnalysis() {

	}


	public void dumpToXLSX() {
		dumpAggregateStatsToXLSX(jsonFiles.get(3));
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
