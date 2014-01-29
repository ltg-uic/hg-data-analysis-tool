package ltg.hg.analysis;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BoutDataAnalysisResults {

	//////////////////////////
	// !! NOTE VERY WELL !! //
	//////////////////////////
	// In the following data structures we ALWAYS assume that the index 
	// identifies the patch in the lists.
	// e.g. Patch A => index 0; Patch B => index 1; ...


	// People at patch at every timestamp
	//				| # @ Patch A	| # @ Patch B	| ...
	//  1383596795 	|		0		|		0		|
	// 	1383596796 	|		1		|		0		|
	// ....
	public Map<Long, List<Integer>> peopleAtPatchPerTimestamp = new LinkedHashMap<>();

	// Cumulative time at patch per tag
	//			| # @ Patch A	| # @ Patch B	| ...
	//  tuz 	|		15		|		38		|
	// 	boz 	|		0		|		11		|
	// 	....
	public Map<String, List<Integer>> cumulativeTimeAtPatchPerTag = new LinkedHashMap<>();

	// Cumulative harvest at patch per tag
	//			| # @ Patch A	| # @ Patch B	| ...
	//  tuz 	|		15		|		38		|
	// 	boz 	|		0		|		11		|
	// 	....
	public Map<String, List<Double>> cumulativeHarvestAtPatchperTag = new LinkedHashMap<>();

	// Bout lenght in seconds
	public double bout_length_in_seconds = 0.0d;

}
