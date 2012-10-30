package ltg.foraging.analysis;

public class Action {
	
	public int ts = -1;
	public int action = 0;   //-1 when leaves, +1 when comes
	public String patch = null;
	
	
	public Action(int ts, int action, String patch) {
		this.ts = ts;
		this.action = action;
		this.patch = patch;
	}
	
	
	@Override
	public String toString() {
		return action + " " + patch;
	}

}
