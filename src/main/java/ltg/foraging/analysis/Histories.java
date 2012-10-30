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

	
	public void computePatchTimes(int gameEndTime) {
		for (PersonalHistory ph: th)
			ph.computePatchTimes(gameEndTime);
	}
	

	public void printResults() {
		for (PersonalHistory ph: th) {
			System.out.format("%s || " +
					"%3d | %3d | %3d | %3d | %3d | %3d | %3d || %3d ||" +
					"%4d | %4d | %4d | %4d | %4d | %4d || %3d" +
					"%n", 
					ph.id, 
					ph.patchTimes[0], ph.patchTimes[1], ph.patchTimes[2], ph.patchTimes[3], ph.patchTimes[4], ph.patchTimes[5],ph.patchTimes[6], ph.totalGameTime, 
					ph.patchHarvests[0], ph.patchHarvests[1], ph.patchHarvests[2], ph.patchHarvests[3], ph.patchHarvests[4], ph.patchHarvests[5], 
					ph.totalPatchEntries);
		}
	}

}
