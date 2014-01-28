package ltg.foraging.analysis;

import java.util.ArrayList;
import java.util.List;

public class Bout {
	// id
	private String run_id;
	private String habitat_configuration;
	private String bout_id;
	// start/stop
	private long bout_start;
	private long bout_stop;
	// Raw list of actions
	private List<Action> rawLog = new ArrayList<>();
	
	public Bout(String run_id, String habitat_configuration, String bout_id, long bout_start, long bout_stop, List<Action> rawLog) {
		this.run_id = run_id;
		this.habitat_configuration = habitat_configuration;
		this.bout_id = bout_id;
		this.bout_start = bout_start;
		this.bout_stop = bout_stop;
		this.rawLog = rawLog;
	}
	
	

}
