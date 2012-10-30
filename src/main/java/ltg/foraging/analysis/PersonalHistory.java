package ltg.foraging.analysis;

import java.util.ArrayList;
import java.util.List;

public class PersonalHistory {
	
	public String id = null;
	private List<Action> actions = new ArrayList<Action>();
	public int[] patchTimes = {0,0,0,0,0,0};
	
	
	public PersonalHistory(String id) {
		this.id = id;
	}
	
	
	public void addAction(int ts, int action, String patch) {
		actions.add(new Action(ts, action, patch));
	}
	
	
	public void computePatchTimes() {
		if (!checkActionsSequence())
			System.exit(-1);
		int lastTs= actions.get(0).ts;
	}


	private boolean checkActionsSequence() {
		if (! (actions.get(0).patch.equals("fg-den") && actions.get(0).action==-1) ) {
			System.err.println("First action is not leaving the den!");
			return false;
		}
		int lastAction = 1;
		int counter = 0;
		for (Action a: actions) {
			if(lastAction==1) {
				if (a.action==1) {
					System.err.println("Action sequence is corrupted: " + id + " : " + counter );
					return false;
				}
				lastAction = -1;
			} else {
				if (a.action==-1) {
					System.err.println("Action sequence is corrupted: " + id + " : " + counter );
					return false;
				}
				lastAction = 1;
			}
			counter ++;
		}
		return true;
	}
	

}
