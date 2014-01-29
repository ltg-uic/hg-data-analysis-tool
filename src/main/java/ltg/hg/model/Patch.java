package ltg.hg.model;


public class Patch {
	
	// Assigned attributes
	public final String id;
	public final double richness;
	public final double risk;
	
	// Instantaneous attributes
	public double peopleAtPatch = 0;
	
	public Patch(String id, double richness, double risk) {
		this.id = id;
		this.richness = richness;
		this.risk = risk;
	}

}
