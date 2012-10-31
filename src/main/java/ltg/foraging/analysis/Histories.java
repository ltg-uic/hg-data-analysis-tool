package ltg.foraging.analysis;

import java.util.ArrayList;
import java.util.List;

public class Histories {

	private List<PersonalHistory> th = new ArrayList<PersonalHistory>();
	private int[] perPatchTotalTimes = {0,0,0,0,0,0,0};
	private int[][] patchKidsDist = null;


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
		for (PersonalHistory ph: th) {
			for (int i=0; i<perPatchTotalTimes.length; i++) {
				perPatchTotalTimes[i] += ph.patchTimes[i];
			}
		}
	}


	public void printResults() {
		// Print kids' distribution
		System.out.format("idx | 01 | 02 | 03 | 04 | 05 | 06 | dn%n");
		for (int i=0; i<patchKidsDist.length; i++ ) {
			System.out.format("%3d", i);
			for (int j=0; j<patchKidsDist[0].length; j++) {
				System.out.format(" | %2d", patchKidsDist[i][j]);
			}
			System.out.format("%n");
		}
		for (PersonalHistory ph: th)
			System.out.format("%s || " +
					"%3d | %3d | %3d | %3d | %3d | %3d | %3d || %3d ||" +
					"%4d | %4d | %4d | %4d | %4d | %4d || %3d" +
					"%n", 
					ph.id, 
					ph.patchTimes[0], ph.patchTimes[1], ph.patchTimes[2], ph.patchTimes[3], ph.patchTimes[4], ph.patchTimes[5],ph.patchTimes[6], ph.totalGameTime, 
					ph.patchHarvests[0], ph.patchHarvests[1], ph.patchHarvests[2], ph.patchHarvests[3], ph.patchHarvests[4], ph.patchHarvests[5], 
					ph.totalPatchEntries);
		System.out.format("        || %3d | %3d | %3d | %3d | %3d | %3d | %3d %n", 
				perPatchTotalTimes[0], perPatchTotalTimes[1], perPatchTotalTimes[2], perPatchTotalTimes[3], 
				perPatchTotalTimes[4], perPatchTotalTimes[5], perPatchTotalTimes[6]);
	}


	public void computePatchDistribution(int gameBeginTime, int gameEndTime) {
		// Init array
		int cols = 7;
		int rows = gameEndTime-gameBeginTime;
		patchKidsDist = new int[rows][cols];
		for (int i=0; i<rows; i++ ) 
			for (int j=0; j<cols; j++)
				patchKidsDist[i][j]=0;
		// Scroll personal stories and fill
		for (PersonalHistory ph: th) 
			addHistoryToDistribution(gameBeginTime, gameEndTime, rows, ph);
		// Check that at any given ts we have the same number of kids
		int kidsN = 0;
		for (int j=0; j<patchKidsDist[0].length; j++) {
			kidsN += patchKidsDist[0][j];
		}
		for (int i=0; i<patchKidsDist.length; i++) {
			int kn = 0;
			for (int j=0; j<patchKidsDist[0].length; j++) {
				kn += patchKidsDist[i][j];
			}
			if (kn!=kidsN){ 
				System.err.println("Kids are multiplying or shrinking... not good... terminating..,");
				System.exit(-1);
			}
		}
		System.out.println("There are always " + kidsN + " kids, hurray!");
	}


	private void addHistoryToDistribution(int gBt, int gEt, int rows, PersonalHistory ph) {
		int ts = gBt;
		// Initial den time
		int leaveDenTs = -1;
		if (ph.actions.get(0).action==-1 && ph.actions.get(0).patch.equals("fg-den"))
			leaveDenTs = ph.actions.get(0).ts;
		for (ts=gBt; ts<leaveDenTs; ts++) {
			patchKidsDist[ts-gBt][6]++;
		}
		// Arrivals and departures from patches
		for (int i=1; i<ph.actions.size()-1; i+=2) {
			for (ts=ph.actions.get(i).ts; ts<ph.actions.get(i+1).ts; ts++) {
				if(ph.actions.get(i).patch.equals("fg-den")) {
					patchKidsDist[ts-gBt][6]++; // Den
				} else {
					String p = ph.actions.get(i).patch; // Other patches
					int pi = Integer.parseInt(p.substring(p.length()-1, p.length()));
					patchKidsDist[ts-gBt][pi]++;
				}
			}
		}
		// Final patch time
		ph.actions.get(ph.actions.size()-1);
		for (ts=ph.actions.get(ph.actions.size()-1).ts; ts<gEt; ts++ ) {
			if(ph.actions.get(ph.actions.size()-1).patch.equals("fg-den")) {
				patchKidsDist[ts-gBt][6]++; // Den
			} else {
				String p = ph.actions.get(ph.actions.size()-1).patch; // Other patches
				int pi = Integer.parseInt(p.substring(p.length()-1, p.length()));
				patchKidsDist[ts-gBt][pi]++;
			}
		}
	}

}
