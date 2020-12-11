package code.mission;

import java.util.ArrayList;

import code.generic.NodeComparator;
import code.generic.SearchTreeNode;

public class ManhattanHeuristicNodeComparator extends NodeComparator {
	public int compare(SearchTreeNode a, SearchTreeNode b) {
		int deathsA = getCostToNearestGoal(a)[0];
		int deathsB = getCostToNearestGoal(b)[0];
		int damageA = getCostToNearestGoal(a)[1];
		int damageB = getCostToNearestGoal(b)[1];
		// priority in comparison is given to number of deaths
		if (deathsA != deathsB)
			return deathsA - deathsB;
		// if deaths are equal, damage is compared
		if (damageA != damageB)
			return damageA - damageB;
		// if both deaths and damage are equal, insertion order is preserved by comparing id's 
		return a.getId() - b.getId();
	}
	
	// the heuristic initializes damage and deaths till the goal, iterates over each remaining member,
	// calculates the Manhattan distance between this member and ethan,
	// multiplies the distance by 2 to calculate its cost, and updates the total damage and deaths till the goal
	public int[] getCostToNearestGoal(SearchTreeNode node) {
		int damage = 0;
		int dead = 0;
		String[] members = ((MissionImpossibleState)node.getState()).getMembers();
		ArrayList<String> remainingMembers = new ArrayList<String>();
		for(int i = 0; i<members.length;i++) {
			if(members[i].split(",")[3].equals("r"))
				remainingMembers.add(members[i]);
		}
		for(int i = 0;i<remainingMembers.size();i++) {
			int distance = getManhattanDistance(remainingMembers.get(i), ((MissionImpossibleState)node.getState()).getEthanPosition());
			int memberDamage = distance * 2;
			int memberHealth = Integer.parseInt(remainingMembers.get(i).split(",")[2]);
			if(memberDamage+memberHealth < 100)
				damage += memberDamage;
			else {
				damage += (100-memberHealth);
			}
		}
		return new int[] {dead, damage};
	}
	
	// Manhattan distance = |ethanRow - memberRow| + |ethanColumn - memberColumn|
	public int getManhattanDistance(String member, String ethanPosition) {
		int memberRow = Integer.parseInt(member.split(",")[0]);
		int memberColumn = Integer.parseInt(member.split(",")[1]);
		int ethanRow = Integer.parseInt(ethanPosition.split(",")[0]);
		int ethanColumn = Integer.parseInt(ethanPosition.split(",")[1]);
		return Math.abs(ethanRow - memberRow) + Math.abs(ethanColumn - memberColumn);
	}
}
