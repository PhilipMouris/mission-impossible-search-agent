package code.mission;

import code.generic.NodeComparator;
import code.generic.SearchTreeNode;

public class TotalCostManhattanNodeComparator extends NodeComparator {
	public int compare(SearchTreeNode a, SearchTreeNode b) {
		int deathsA = getTotalCost(a)[0];
		int deathsB = getTotalCost(b)[0];
		int damageA = getTotalCost(a)[1];
		int damageB = getTotalCost(b)[1];
		// priority in comparison is given to number of deaths
		if (deathsA != deathsB)
			return deathsA - deathsB;
		// if deaths are equal, damage is compared
		if (damageA != damageB)
			return damageA - damageB;
		// if both deaths and damage are equal, insertion order is preserved by comparing id's 
		return a.getId() - b.getId();
	}
	
	// total cost is calculated by adding deaths in the path cost to deaths estimated by the manhattan heuristic,
	// and adding damage in the path cost to damage estimated by the manhattan heuristic
	public int[] getTotalCost(SearchTreeNode node) {
		String pathCost = node.getPathCost();
		int[] heuristicCost = new ManhattanHeuristicNodeComparator().getCostToNearestGoal(node);
		int newDeaths = heuristicCost[0] + Integer.parseInt(pathCost.split(",")[1]);
		int newDamage = heuristicCost[1] + Integer.parseInt(pathCost.split(",")[0]);
		return new int[] {newDeaths, newDamage};
	}
}
