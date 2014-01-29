package ltg.hg.analysis;

import java.util.ArrayList;
import java.util.List;

public class Bout {

	public enum BoutType {
		GAMEON, PREDAT
	}

	// id data
	public String run_id;
	public BoutType habitat_configuration;
	public String bout_id;
	// start/stop data
	public long bout_start;
	public long bout_stop;
	public int bout_length_in_sec;
	// Raw list of actions
	private List<Event> rawLog = new ArrayList<>();
	// Analysis results
	public BoutDataAnalysisResults results = null;

	public Bout(String run_id, String habitat_configuration, String bout_id, long bout_start, long bout_stop, List<Event> rawLog) {
		this.run_id = run_id;
		this.habitat_configuration = habitat_configuration.equalsIgnoreCase(BoutType.GAMEON.name()) ? BoutType.GAMEON : BoutType.PREDAT;
		this.bout_id = bout_id;
		this.bout_start = bout_start;
		this.bout_stop = bout_stop;
		bout_length_in_sec = (int) ((bout_stop - bout_start));
		this.rawLog = rawLog;
		verifyLog(rawLog);
	}

	
	// Performs thre data analysis
	public void analyzeData() {
		BoutDataAnalyzer analyzer = new BoutDataAnalyzer();
		results =  analyzer.replayLogAndCalculateResults(bout_start, bout_stop, habitat_configuration, rawLog);
	}


	// Simply verifies that the log is sorted properly
	private void verifyLog(List<Event> rawLog) {
		long prev_ts = 0;
		for (Event e: rawLog) {
			if (prev_ts > e.ts) {
				System.err.println("Log is not ordered. Terminating...");
				System.exit(-1);
			}
			prev_ts = e.ts;
		}
	}

	@Override
	public String toString() {
		return "[" + run_id + " " + bout_id + " " + habitat_configuration + "] Duration (sec): " + bout_length_in_sec + " Events: " + rawLog.size();
	}

}
