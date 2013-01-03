package ltg.foraging.analysis;

import java.util.ArrayList;
import java.util.List;

public class PersonalHistory {
	
	public  static final int patchRichness[] = {10, 10, 15, 15, 20, 20, 0};

	public List<Action> actions = new ArrayList<Action>();
	public String id = null;
	public int[] patchTimes = {0,0,0,0,0,0,0};
	public int[] patchHarvests = {0,0,0,0,0,0};
	public int totalGameTime = -1;
	public int totalPatchEntries = -1;


	public PersonalHistory(String id) {
		this.id = id;
	}


	public void addAction(int ts, int action, String patch) {
		actions.add(new Action(ts, action, patch));
	}


	public void computePatchTimes(int gameEndTime) {
		// Compute pairs
		for (int i=1; i<actions.size()-2; i+=2) {
			if (actions.get(i).patch.equals(actions.get(i+1).patch)) {
				if(actions.get(i).patch.equals("fg-den")) {
					// Den
					patchTimes[6] += (actions.get(i+1).ts - actions.get(i).ts);
				} else {
					// Patches
					String p = actions.get(i).patch;
					int pi = Integer.parseInt(p.substring(p.length()-1, p.length())) - 1;
					patchTimes[pi] += (actions.get(i+1).ts - actions.get(i).ts);
				}
			} else {
				System.err.println("Patch mismatch!");
			}
		}
		// Compute time at last patch
		String p = actions.get(actions.size()-1).patch;
		int pi = Integer.parseInt(p.substring(p.length()-1, p.length())) -1;
		patchTimes[pi] += (gameEndTime-actions.get(actions.size()-1).ts);
		// Compute total game time per kid
		for (int i=0; i<patchTimes.length; i++) {
			totalGameTime += patchTimes[i];
		}
		// Compute total patch entries
		for (Action a: actions) {
			if (a.action==1) {
				totalPatchEntries++;
			}
		}
	}


	public boolean checkActionsSequence() {
		// First action is leaving the den
		if (! (actions.get(0).patch.equals("fg-den") && actions.get(0).action==-1) ) {
			System.err.println("First action is not leaving the den!");
			return false;
		}
		// There is strict interleaving between arrivals and departures
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
		// Sequence length is EVEN
		if (!(actions.size()%2==0)) {
			System.err.println("Action sequence is corrupted: sequence length is odd");
			return false; 
		}
		return true;
	}



	public void computeHarvest(int gBt, int gEt, int[][] kidsAtPatch) {
		// Compute pairs
		for (int i=1; i<actions.size()-2; i+=2) {
			if (actions.get(i).patch.equals(actions.get(i+1).patch)) {
				if(actions.get(i).patch.equals("fg-den")) {
					// Den doesn't give you points!
					continue;
				}
				// Patches
				String p = actions.get(i).patch;
				int pi = Integer.parseInt(p.substring(p.length()-1, p.length())) - 1;
				for (int ts=actions.get(i).ts; ts<actions.get(i+1).ts; ts++) {
					patchHarvests[pi] += patchRichness[pi]/kidsAtPatch[ts-gBt][pi];
				}
			} else {
				System.err.println("Patch mismatch!");
			}
		}
		// Compute time at last patch
		String p = actions.get(actions.size()-1).patch;
		int pi = Integer.parseInt(p.substring(p.length()-1, p.length())) - 1;
		for (int ts=actions.get(actions.size()-1).ts; ts<gEt; ts++) {
			patchHarvests[pi] += patchRichness[pi]/kidsAtPatch[ts-gBt][pi];
		}
	}
	
	
	public float getCurrentRate(int ts, int patch, int gBt, int[][] kidsAtPatch) {
		return ((float) patchRichness[patch]) / ((float) kidsAtPatch[ts-gBt][patch]);
	}
	
	
	public float getJoinRate(int ts, int patch, int gBt, int[][] kidsAtPatch) {
		return ((float) patchRichness[patch]) / ((float) kidsAtPatch[ts-gBt][patch]+1);
	}
	

	public void computeQualityOfPatchSwitchedIndex(int gBt, int gEt, int[][] patchKidsDist) {
		for (int i=0; i<actions.size(); i+=2) {
			
		}
	}
	
	
	@Override
	public String toString() {
		return id + " " + actions.toString();
	}


}
