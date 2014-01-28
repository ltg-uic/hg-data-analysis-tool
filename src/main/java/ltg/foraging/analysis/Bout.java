package ltg.foraging.analysis;

import java.util.ArrayList;
import java.util.List;

public class Bout {
	
	public enum BoutType {
		GAMEON, PREDAT
	}
	
	// id
	public String run_id;
	public BoutType habitat_configuration;
	public String bout_id;
	// start/stop
	public long bout_start;
	public long bout_stop;
	public int bout_lenght_in_sec;
	// Raw list of actions
	private List<Event> rawLog = new ArrayList<>();
	
	public Bout(String run_id, String habitat_configuration, String bout_id, long bout_start, long bout_stop, List<Event> rawLog) {
		this.run_id = run_id;
		this.habitat_configuration = habitat_configuration.equalsIgnoreCase(BoutType.GAMEON.name()) ? BoutType.GAMEON : BoutType.PREDAT;
		this.bout_id = bout_id;
		this.bout_start = bout_start;
		this.bout_stop = bout_stop;
		bout_lenght_in_sec = (int) ((bout_stop - bout_start));
		this.rawLog = rawLog;
		System.out.println("Created new bout " + toString());
	}
	
	@Override
	public String toString() {
		return "[" + run_id + " " + bout_id + " " + habitat_configuration + "] Duration (sec): " + bout_lenght_in_sec + " Events: " + rawLog.size();
	}

	public void analyzeData() {
		// TODO Auto-generated method stub
		
	}

}
