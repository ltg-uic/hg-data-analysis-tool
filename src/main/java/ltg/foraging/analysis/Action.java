package ltg.foraging.analysis;

public class Action {
	
	public enum ActionTypes {
		MOVE, KILL, REVIVE
	}
	
	public long ts = -1;
	public String id;
	public ActionTypes action;
	public String departure = null;
	public String arrival = null;
	
	
	public Action(long ts, String id, String depart, String arrive) {
		this.ts = ts;
		this.id = id;
		this.action = ActionTypes.MOVE;
		this.departure = depart;
		this.arrival = arrive;
	}
	
	public Action(long ts, String id, ActionTypes action) {
		this.ts = ts;
		this.id = id;
		this.action = action;
	}
	
	
	@Override
	public String toString() {
		if (action==ActionTypes.MOVE)
			return ts + " : " + id + " " + departure + " => " + arrival;
		else
			return ts + " : " + action + " " +id;
	}

}
