package ltg.hg.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		results =  analyzer.replayLogAndCalculateResults(bout_start, bout_stop, habitat_configuration, rawLog, run_id, bout_id);
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

    public List<Event> getRawLogBetween(long start, long stop) {
        List<Event> l = new ArrayList<>();
        for (Event e : rawLog)
            if (e.ts >= start && e.ts <= stop)
                l.add(e);
        return l;
    }


    public List<Event> getRawLogBetweenAndFix(long start, long stop) {
        Map<String, String> tagLocation = new HashMap<>();
        List<Event> buff = new ArrayList<>();
        for (Event e : rawLog)
            if (e.ts < start) {
                if (e.action.equals(Event.ActionTypes.MOVE))
                    tagLocation.put(e.id, e.arrival);
            } else if (e.ts >= start && e.ts <= stop)
                buff.add(e);
        List<Event> l = new ArrayList<>();
        for (String s: tagLocation.keySet())
            l.add(new Event(start,s, null, tagLocation.get(s)));
        l.addAll(buff);
        return l;
    }


	@Override
	public String toString() {
		return "[" + run_id + " " + bout_id + " " + habitat_configuration + "] Duration (sec): " + bout_length_in_sec + " Events: " + rawLog.size();
	}

}
