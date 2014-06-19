package ltg.hg.model;

import java.util.*;


public class Tag {

	// Assigned attributes
	public final String id;
	
	// Instantaneous attributes
	public String current_location = null;
	public boolean is_alive = true;	
	
	// Aggregates
	public Map<String, Double> time_at_patch = new HashMap<>();
	public Map<String, Double> harvest_at_patch = new HashMap<>();
    public int total_deaths;
	
	
	public Tag(String id) {
        this.id = id;
	}
	
	
	public void setCurrentLocation(Patch patch) {
		current_location = patch.id;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tag 
				&& ((Tag) obj).id.equals(id)
			)
			return true;
		return false;
	}


	public void updateTimeAtPatch() {
		if (is_alive) {
			time_at_patch.put(current_location, time_at_patch.get(current_location)==null ? 1.0d : time_at_patch.get(current_location) + 1.0d);
		}
	}


	public void updateTotalHarvest(LinkedHashMap<String, Patch> patches) {
		if (is_alive) {
            double current_yield = patches.get(current_location).richness / patches.get(current_location).peopleAtPatch;
			harvest_at_patch.put(current_location, harvest_at_patch.get(current_location)==null ? current_yield : harvest_at_patch.get(current_location) + current_yield);
		}
	}

    public void updateKillings() {
        total_deaths++;
    }

	public List<Integer> getPatchTimes(Set<String> patches) {
		List<Integer> list = new ArrayList<>();
		for (String s: patches) {
			int value =  time_at_patch.get(s)==null ? 0 : time_at_patch.get(s).intValue();
			list.add(value);
		}
		return list;
	}
	
	public List<Double> getPatchHarvests(Set<String> patches) {
		List<Double> list = new ArrayList<>();
		for (String s: patches) {
			double value =  harvest_at_patch.get(s)==null ? 0.0d : harvest_at_patch.get(s);
			list.add(value);
		}
		return list;
	}


    public Double getTotalHarvest(Set<String> strings) {
        double total_harvest = 0;
        for (Double h : getPatchHarvests(strings))
            total_harvest += h;
        return total_harvest;
    }


	public static List<Double> fromMapToList(Map<String, Double> map, Set<String> patches) {
		List<Double> list = new ArrayList<>();
		for (String s: patches) {
			double value =  map.get(s)==null ? 0.0d : map.get(s);
			list.add(value);
		}
		return list;
	}

}
