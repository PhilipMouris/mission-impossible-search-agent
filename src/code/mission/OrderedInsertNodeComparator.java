package code.mission;

import code.generic.NodeComparator;
import code.generic.SearchTreeNode;

public class OrderedInsertNodeComparator extends NodeComparator {
	public int compare(SearchTreeNode a, SearchTreeNode b) {
		int deathsA = Integer.parseInt(a.getPathCost().split(",")[1]);
		int deathsB = Integer.parseInt(b.getPathCost().split(",")[1]);
		int damageA = Integer.parseInt(a.getPathCost().split(",")[0]);
		int damageB = Integer.parseInt(b.getPathCost().split(",")[0]);
		// priority in comparison is given to number of deaths
		if (deathsA != deathsB)
			return deathsA - deathsB;
		// if deaths are equal, damage is compared
		if (damageA != damageB)
			return damageA - damageB;
		// if both deaths and damage are equal, insertion order is preserved by comparing id's 
		return a.getId() - b.getId();
	}
}
