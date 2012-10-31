package ltg.foraging.analysis;

import java.util.ArrayList;
import java.util.List;

public class Histories {

	private List<PersonalHistory> th = new ArrayList<PersonalHistory>();
	private int[] perPatchTotalTimes = {0,0,0,0,0,0,0};
	private int[][] patchKidsDist = null;
	private int[][] timeWithKidsAtPatch = null;


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


	public void computeTotalPatchTimes() {
		for (PersonalHistory ph: th) {
			for (int i=0; i<perPatchTotalTimes.length; i++) {
				perPatchTotalTimes[i] += ph.patchTimes[i];
			}
		}
	}


	public void computeHarvest(int gameBeginTime, int gameEndTime) {
		computePatchDistribution(gameBeginTime, gameEndTime);
		for (PersonalHistory ph: th) 
			ph.computeHarvest(gameBeginTime, gameEndTime, patchKidsDist);
	}
	
	

	public void computeKidsAtPatch() {
		timeWithKidsAtPatch = new int[th.size()+1][patchKidsDist[0].length];
		for (int ts=0; ts<patchKidsDist.length; ts++) {
			for (int p=0; p<patchKidsDist[0].length; p++) {
				timeWithKidsAtPatch[patchKidsDist[ts][p]][p]++;
			}
		}
	}



	private void computePatchDistribution(int gameBeginTime, int gameEndTime) {
		// Init array with 0s
		int cols = 7;
		int rows = gameEndTime-gameBeginTime;
		patchKidsDist = new int[rows][cols];
		for (int i=0; i<rows; i++ ) 
			for (int j=0; j<cols; j++)
				patchKidsDist[i][j]=0;
		// Scroll personal stories and fill the distribution
		for (PersonalHistory ph: th) 
			addHistoryToDistribution(gameBeginTime, gameEndTime, ph);
		// DEBUG: print distribution
		//printKidsDistribution();
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
				System.err.println("Kids are multiplying or shrinking, not good! TS=" + i + " We fixed it but you should check");
				// Fix error by putting coping previous state into current
				for (int j=0; j<patchKidsDist[0].length; j++) {
					patchKidsDist[i][j] = patchKidsDist[i-1][j];
				}
			}
		}
	}


	private void addHistoryToDistribution(int gBt, int gEt, PersonalHistory ph) {
		int ts = gBt;
		// Initial den time
		int leaveDenTs = -1;
		if (ph.actions.get(0).action==-1 && ph.actions.get(0).patch.equals("fg-den"))
			leaveDenTs = ph.actions.get(0).ts;
		for (ts=gBt; ts<leaveDenTs; ts++) {
			patchKidsDist[ts-gBt][6]++;
		}
		// Arrivals and departures from patches
		for (int i=1; i<ph.actions.size()-2; i+=2) {
			for (ts=ph.actions.get(i).ts; ts<ph.actions.get(i+1).ts; ts++) {
				if (ph.actions.get(i).patch.equals(ph.actions.get(i+1).patch)) {
					if(ph.actions.get(i).patch.equals("fg-den")) {
						patchKidsDist[ts-gBt][6]++; // Den
					} else {
						String p = ph.actions.get(i).patch; // Other patches
						int pi = Integer.parseInt(p.substring(p.length()-1, p.length()))-1;
						patchKidsDist[ts-gBt][pi]++;
					}
				} else {
					System.err.println("Patch mismatch!");
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
				int pi = Integer.parseInt(p.substring(p.length()-1, p.length())) - 1;
				patchKidsDist[ts-gBt][pi]++;
			}
		}
	}


	public void printResults() {
		// Print time at patches, total game time, harvest at patches and number of patch entries
		System.out.format("tagId   || -1- | -2- | -3- | -4- | -5- | -6- | den || hv-1 | hv-2 | hv-3 | hv-4 | hv-5 | hv-6 || TPE%n");
		System.out.format("-----------------------------------------------------------------------------------------------------------%n");
		for (PersonalHistory ph: th)
			System.out.format("%s || " +
					"%3d | %3d | %3d | %3d | %3d | %3d | %3d ||" +
					" %4d | %4d | %4d | %4d | %4d | %4d || %3d" +
					"%n", 
					ph.id, 
					ph.patchTimes[0], ph.patchTimes[1], ph.patchTimes[2], ph.patchTimes[3], ph.patchTimes[4], ph.patchTimes[5],ph.patchTimes[6], 
					ph.patchHarvests[0], ph.patchHarvests[1], ph.patchHarvests[2], ph.patchHarvests[3], ph.patchHarvests[4], ph.patchHarvests[5], 
					ph.totalPatchEntries);
		// Print cumulative time spent at patches
		System.out.format("TOTAL   || %3d | %3d | %3d | %3d | %3d | %3d | %3d %n", 
				perPatchTotalTimes[0], perPatchTotalTimes[1], perPatchTotalTimes[2], perPatchTotalTimes[3], 
				perPatchTotalTimes[4], perPatchTotalTimes[5], perPatchTotalTimes[6]);
		// Print how many times there are kids at patches
		System.out.println();
		System.out.format("#kids || -1- | -2- | -3- | -4- | -5- | -6- | den %n");
		System.out.format("------------------------------------------------ %n");
		for (int i=0; i<timeWithKidsAtPatch.length; i++) {
			System.out.format("%3d   || %3d | %3d | %3d | %3d | %3d | %3d | %3d %n",
					i, timeWithKidsAtPatch[i][0], timeWithKidsAtPatch[i][1], timeWithKidsAtPatch[i][2],
					timeWithKidsAtPatch[i][3], timeWithKidsAtPatch[i][4], timeWithKidsAtPatch[i][5], timeWithKidsAtPatch[i][6]);
		}
	}
	
	
	public void printKidsDistribution() {
		// Print kids' distribution over patches
		System.out.format("idx | 01 | 02 | 03 | 04 | 05 | 06 | dn%n");
		for (int i=0; i<patchKidsDist.length; i++ ) {
			System.out.format("%3d", i);
			for (int j=0; j<patchKidsDist[0].length; j++) {
				System.out.format(" | %2d", patchKidsDist[i][j]);
			}
			System.out.format("%n");
		}

	}
}
