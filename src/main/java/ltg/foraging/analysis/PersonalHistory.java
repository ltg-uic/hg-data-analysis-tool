package ltg.foraging.analysis;

import java.util.ArrayList;
import java.util.List;

public class PersonalHistory {
	
	public String id = null;
	private List<Action> actions = new ArrayList<Action>();
	
	
	public PersonalHistory(String id) {
		this.id = id;
	}
	
	
	public void addAction(int ts, int action, String patch) {
		actions.add(new Action(ts, action, patch));
	}
	

}
