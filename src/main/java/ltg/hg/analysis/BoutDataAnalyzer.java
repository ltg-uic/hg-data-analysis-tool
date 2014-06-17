package ltg.hg.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ltg.hg.analysis.Bout.BoutType;
import ltg.hg.model.Patch;
import ltg.hg.model.Tag;

public class BoutDataAnalyzer {

	// Current state
	private Map<String, Tag> tags = new HashMap<>();
	private LinkedHashMap<String, Patch> patches = new LinkedHashMap<>();

	// Results
	private BoutDataAnalysisResults results = new BoutDataAnalysisResults();

	public BoutDataAnalyzer() {
		patches.put("patch-a", new Patch("patch-a", 10.0d, 0.025d));
		patches.put("patch-b", new Patch("patch-b", 10.0d, 0.01d));
		patches.put("patch-c", new Patch("patch-c", 15.0d, 0.025d));
		patches.put("patch-d", new Patch("patch-d", 15.0d, 0.01d));
		patches.put("patch-e", new Patch("patch-e", 20.0d, 0.025d));
		patches.put("patch-f", new Patch("patch-f", 20.0d, 0.01d));
	}

	// Replays the bout and calculates the aggregates values that we need
	public BoutDataAnalysisResults replayLogAndCalculateResults(long bout_start, long bout_stop, BoutType habitat_configuration, List<Event> rawLog) {
		List<Event> consumableEvents = new ArrayList<>(rawLog);
		for (long ts = bout_start; ts<bout_stop; ts++) {
			List<Event> eventsAtTS = findEventsAtTS(ts, consumableEvents);
			updateStats();
			updateModel(habitat_configuration, eventsAtTS);
			updateResults(ts);
		}
		composeResults();
		validateResults();
		return results;
	}

	// Similarly to what is performed by the real time update thread
	private void updateStats() {
		for (Tag tag: tags.values()) {
			tag.updateTimeAtPatch();
			tag.updateTotalHarvest();
		}
	}

	// Similarly to what is done through calls on the model
	private void updateModel(BoutType habitat_configuration, List<Event> eventsAtTS) {
		//System.out.println(eventsAtTS);
		for (Event e: eventsAtTS) 
			switch (e.action) {
			case MOVE:
				if (tags.get(e.id)==null)
					tags.put(e.id, new Tag(e.id));
				patches.get(e.arrival).peopleAtPatch += 1.0d;
				if (e.departure!=null)
					patches.get(e.departure).peopleAtPatch -= 1.0d;
				tags.get(e.id).setCurrentLocation(patches.get(e.arrival));
				break;
			case KILL:
				tags.get(e.id).is_alive = false;
                tags.get(e.id).updateKillings();
				break;
			case REVIVE:
				tags.get(e.id).is_alive = true;
				break;
			}
	}


	private void updateResults(long ts) {
		List<Integer> pap = new ArrayList<>();
		for (Patch p: patches.values())
			pap.add((int) p.peopleAtPatch);
		results.peopleAtPatchPerTimestamp.put(ts, pap);
		results.bout_length_in_seconds++;
	}

	private void composeResults() {
		List<Tag> tagsList = new ArrayList<>(tags.values());
		Collections.sort(tagsList, new Comparator<Tag>() {
			@Override
			public int compare(Tag t1, Tag t2) {
				return t1.id.compareTo(t2.id);
			}
		});
		for (Tag t: tagsList) {
			List<Integer>patchTimes = t.getPatchTimes(patches.keySet());
			results.cumulativeTimeAtPatchPerTag.put(t.id, patchTimes);
			List<Double> patchHarvest = t.getPatchHarvests(patches.keySet());
			results.cumulativeHarvestAtPatchperTag.put(t.id, patchHarvest);
            results.totalDeathsPerTag.put(t.id, t.total_deaths);
		}
	}
	
	private void validateResults() {
		//System.out.println("Results time!");
	}


	// Finds all the events that happened at a certain timestamp
	private List<Event> findEventsAtTS(long ts, List<Event> events) {
		List<Event> eats = new ArrayList<>();
		Iterator<Event> i = events.iterator();
		while (i.hasNext()) {
			Event e = i.next();
			if (e.ts==ts) {
				eats.add(e);
				i.remove();
			}
			if (e.ts < ts)
				break;
		}
		return eats;
	}

}
