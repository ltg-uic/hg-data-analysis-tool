package ltg.foraging.analysis;

import java.util.ArrayList;
import java.util.List;

public class Histories {
	
	private List<PersonalHistory> th = new ArrayList<PersonalHistory>();
	
	
	public void addAction(String id, int ts, int action, String patch) {
		if (isThereHistory(id))
			addActionTo(id, ts, action, patch);
		else
			addActionNew(id, ts, action, patch);
		
	}
	
	private void addActionNew(String id, int ts, int action, String patch) {
		PersonalHistory ph = new PersonalHistory(id);
		ph.addAction(ts, action, patch);
		th.add(ph);
	}

	
	private void addActionTo(String id, int ts, int action, String patch) {
		for (PersonalHistory ph: th) {
			if (ph.id.equals(id))
				ph.addAction(ts, action, patch);
		}
	}

	
	public boolean isThereHistory(String id) {
		for (PersonalHistory ph: th) {
			if (ph.id.equals(id))
				return true;
		}
		return false;
	}

}
