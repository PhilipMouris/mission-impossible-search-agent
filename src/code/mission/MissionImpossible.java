package code.mission;

import code.generic.Operator;
import code.generic.SearchProblem;
import code.generic.SearchTreeNode;
import code.generic.State;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class MissionImpossible extends SearchProblem {
	
	int width;
	int height;
	String submarinePosition;
	int truckCapacity;
	int numberOfMembers;
	HashSet<String> statesHistory;
	
	public MissionImpossible(String grid) {
		// initialize all operators with their names, and set them to the operators instance variable
		Operator up = new Operator("up");
		Operator down = new Operator("down");
		Operator left = new Operator("left");
		Operator right = new Operator("right");
		Operator carry = new Operator("carry");
		Operator drop = new Operator("drop");
		this.operators = new Operator[] {left, right, up, down, carry, drop};
		// initialize width, height, truckCapacity and submarinePosition by extracting them from the input grid
		this.width = Integer.parseInt(grid.split(";")[0].split(",")[0]);
		this.height = Integer.parseInt(grid.split(";")[0].split(",")[1]);
		this.truckCapacity = Integer.parseInt(grid.split(";")[5]);
		this.submarinePosition = grid.split(";")[2];
		// extract member positions and healths from the input grid, initialize the members instance variable
		// each member is represented as a string of the following format "row,column,health,flag", flag can be any value
		// from "r,c,d" representing "remaining,carried,dropped" respectively
		String[] initialMembersPositions = grid.split(";")[3].split(",");
		String[] initialMembersHealths = grid.split(";")[4].split(",");
		this.numberOfMembers = initialMembersHealths.length;
		String[] members = new String[numberOfMembers];
		for(int i = 0; i<numberOfMembers; i++) {
			String position = initialMembersPositions[i*2] + "," + initialMembersPositions[(i*2)+1];
			int health = Integer.parseInt(initialMembersHealths[i]);
			members[i] = position + "," + health + "," + "r";
		}
		// construct initial state and assign it the corresponding instance variable
		MissionImpossibleState initialState = new MissionImpossibleState(grid.split(";")[1], 0, 0, members);
		this.initialState = initialState;
		// initialize statesHistory hashset and add the initial state to it, this is used to check for repeatedStates
		this.statesHistory = new HashSet<String>();
		this.statesHistory.add(initialState.getEthanPosition()+";"+membersCount(initialState.getMembers()));
		// initialize the different comparators used with different queuing functions
		this.orderedInsertNodeComparator = new OrderedInsertNodeComparator();
		this.manhattanHeuristicNodeComparator = new ManhattanHeuristicNodeComparator();
		this.euclideanHeuristicNodeComparator = new EuclideanHeuristicNodeComparator();
		this.totalCostEuclideanNodeComparator = new TotalCostEuclideanNodeComparator();
		this.totalCostManhattanNodeComparator = new TotalCostManhattanNodeComparator();
	}
	
	public static String genGrid() {
		Random rand = new Random();
		// generate random height and width for the grid
		int m = 5 + rand.nextInt(11);
		int n = 5 + rand.nextInt(11);
		// 2d array of flags indicating if each cell is filled (occupied) or not
		boolean[][] filledCells = new boolean[n][m];
		// generate random row and column for ethan
		int ex = rand.nextInt(n);
		int ey = rand.nextInt(m);
		filledCells[ex][ey] = true;
		// generate random row and column for the submarine
		int sx = rand.nextInt(n);
		int sy = rand.nextInt(m);
		// if generated position for submarine is filled, keep on generating, until an empty position is found
		while(filledCells[sx][sy]) {
			sx = rand.nextInt(n);
			sy = rand.nextInt(m);
		}
		filledCells[sx][sy] = true;
		// generate random number of members
		int membersCount = 5 + rand.nextInt(6);
		String grid = "";
		grid += m+","+n+";"+ex+","+ey+";"+sx+","+sy+";";
		for(int i = 0; i<membersCount; i++) {
			// generate random position for each member
			int x = rand.nextInt(n);
			int y = rand.nextInt(m);
			// if member position is filled keep on generating
			while(filledCells[x][y]) {
				x = rand.nextInt(n);
				y = rand.nextInt(m);
			}
			filledCells[x][y] = true;
			grid += x+","+y+",";
		}
		grid = grid.substring(0, grid.length()-1) + ";";
		// generate random health for each member from 1 to 99
		for(int i = 0; i<membersCount; i++) {
			int h = 1 + rand.nextInt(99);
			grid += h+",";
		}
		grid = grid.substring(0, grid.length()-1) + ";";
		// generate random capacity
		int c = rand.nextInt(11);
		grid += c;
		return grid;
	}
	
	public static String solve (String grid, String strategy, boolean visualize) {
		// initialize the problem using the grid, and initialize a goal node
		MissionImpossible missionImpossibleProblem = new MissionImpossible(grid);
		SearchTreeNode goalNode;
		// call the search function corresponding to the strategy argument passed
		switch(strategy) {
			case "BF":
				goalNode = BF(missionImpossibleProblem);
				break;
			case "DF":
				goalNode = DF(missionImpossibleProblem);
				break;
			case "ID":
				goalNode = ID(missionImpossibleProblem);
				break;
			case "UC":
				goalNode = UC(missionImpossibleProblem);
				break;
			case "GR1":
				goalNode = GR1(missionImpossibleProblem);
				break;
			case "GR2":
				goalNode = GR2(missionImpossibleProblem);
				break;
			case "AS1":
				goalNode = AS1(missionImpossibleProblem);
				break;
			case "AS2":
				goalNode = AS2(missionImpossibleProblem);
				break;
			default:
				goalNode = null;
		}
		// formulate the output solution using the goalNode returned
		String solution = missionImpossibleProblem.nodeToString(goalNode);
		// visualize the steps if visualize argument is passed as true and a goal node is returned
		if(visualize && goalNode != null)
			visualize(goalNode, missionImpossibleProblem);
		return solution;
	}
	
	public static void visualize(SearchTreeNode node, MissionImpossible problem) {
		ArrayList<String[][]> steps = new ArrayList<String[][]>();
		// iterate over all nodes on the path from root to goal, for each node, initialize a 2d array,
		// fill this 2d array with the remaining members along with their healths at the corresponding positions,
		// and fill it with ethan along with the number of carried members,
		// and fill it with the submarine along with the number of dropped members 
		while(node != null) {
			String[][] board = new String[problem.height][problem.width];
			int carried = 0;
			int dropped = 0;
			String[] members = ((MissionImpossibleState)node.getState()).getMembers();
			for(int i = 0;i<members.length;i++) {
				if(members[i].split(",")[3].equals("c"))
					carried++;
				if(members[i].split(",")[3].equals("d"))
					dropped++;
				if(members[i].split(",")[3].equals("r"))
					board[Integer.parseInt(members[i].split(",")[0])][Integer.parseInt(members[i].split(",")[1])] = "F(" + members[i].split(",")[2] + ")";
			}
			String ethanPosition = ((MissionImpossibleState)node.getState()).getEthanPosition();
			board[Integer.parseInt(problem.submarinePosition.split(",")[0])][Integer.parseInt(problem.submarinePosition.split(",")[1])] = "S(" + dropped + ")";
			board[Integer.parseInt(ethanPosition.split(",")[0])][Integer.parseInt(ethanPosition.split(",")[1])] = "E(" + carried + ")";
			steps.add(board);
			node = node.getParent();
		}
		// iterate over all grids corresponding to each step or node, and display this grid
		for(int i = steps.size()-1;i>=0;i--) {
			displayGrid(steps.get(i));
		}
	}
	
	public static void displayGrid(String[][] grid) {
		for(int i = 0;i<grid.length;i++) {
			// start each row with '|' character
			System.out.print("|");
			for(int j = 0;j<grid[i].length;j++) {
				// if cell is empty, display blank
				if(grid[i][j] == null)
					System.out.print("   -   ");
				// if cell is not empty, display it contents
				else
					System.out.print(" " + grid[i][j] + " ");
				// end each cell with '|' character
				System.out.print("|");
			}
			// print a new line after each row
			System.out.println();
			// fill this new line with '-' character
			for(int k = 0;k<8*grid[i].length;k++) {
				System.out.print("-");
			}
			System.out.println();
		}
		// print 2 new lines after each board
		System.out.println();
		System.out.println();
	}
	
	public boolean goalTest(State state) {
		// check if ethan's position and the submarine's position are the same
		if(!((MissionImpossibleState)state).getEthanPosition().equals(submarinePosition))
			return false;
		// check if all members are dropped members
		for(int i = 0;i<numberOfMembers;i++)
			if(!((MissionImpossibleState)state).getMembers()[i].split(",")[3].equals("d"))
				return false;
		// if both checks pass, state is goal state, if either does not pass, state is not goal state
		return true;
	}
	
	public String pathCost(SearchTreeNode node) {
		// pathCost of a node is the total number of deaths and total damage incurred in the state of the node
		MissionImpossibleState state = (MissionImpossibleState) node.getState();
		return state.getTotalDamage() + "," + state.getTotalDeaths();
	}
	
	public boolean isRepeatedState(State state) {
		// check if state is repeated state by looking for it in the statesHistory hashset
		// states are represented in statesHistory as "ethanRow,ethanColumn;member1Row,member1Column....,memberkRow,memberkColumn;carriedMembersCount"
		// where k is number of remaining members
		MissionImpossibleState checkedState = (MissionImpossibleState) state;
		if (statesHistory.contains(checkedState.getEthanPosition()+";"+membersCount(checkedState.getMembers())))
			return true;
		// if state is not repeated, ie, not in statesHistory, then add it to statesHistory
		statesHistory.add(checkedState.getEthanPosition()+";"+membersCount(checkedState.getMembers()));
		return false;
	}
	
	public void resetRepeatedStates() {
		// reset repeated states by clearing statesHistory hashset and adding the initial state to it
		statesHistory.clear();
		statesHistory.add(((MissionImpossibleState)initialState).getEthanPosition()+";"+membersCount(((MissionImpossibleState)initialState).getMembers()));
	}
	
	// transition function
	public State nextState(State oldState, Operator action) {
		// initialize new state with initial values
		MissionImpossibleState newState = new MissionImpossibleState("", 0, 0, null);
		// initialize a new array for updated members and a new variable for updated total damage
		String[] updatedMembers = new String[numberOfMembers];
		int updatedTotalDamage = ((MissionImpossibleState) oldState).getTotalDamage();
		for(int i = 0; i<numberOfMembers; i++) {
			updatedMembers[i] = ((MissionImpossibleState) oldState).getMembers()[i];
		}
		// for up action, update ethan position by decrementing row, unless row is 0
		if(action.getName().equals("up")) {
			int ethanRowNumber = Integer.parseInt(((MissionImpossibleState) oldState).getEthanPosition().split(",")[0]);
			if(ethanRowNumber == 0)
				return null;
			newState.setEthanPosition((ethanRowNumber-1) + "," + ((MissionImpossibleState) oldState).getEthanPosition().split(",")[1]);
		}
		// for down action, update ethan position by incrementing row, unless row is last row
		if(action.getName().equals("down")) {
			int ethanRowNumber = Integer.parseInt(((MissionImpossibleState) oldState).getEthanPosition().split(",")[0]);
			if(ethanRowNumber == height - 1)
				return null;
			newState.setEthanPosition((ethanRowNumber+1) + "," + ((MissionImpossibleState) oldState).getEthanPosition().split(",")[1]);
		}
		// for right action, update ethan position by incrementing column, unless column is last column
		if(action.getName().equals("right")) {
			int ethanColumnNumber = Integer.parseInt(((MissionImpossibleState) oldState).getEthanPosition().split(",")[1]);
			if(ethanColumnNumber == width - 1)
				return null;
			newState.setEthanPosition(((MissionImpossibleState) oldState).getEthanPosition().split(",")[0] + "," + (ethanColumnNumber+1));
		}
		// for left action, update ethan position by decrementing column, unless column is 0
		if(action.getName().equals("left")) {
			int ethanColumnNumber = Integer.parseInt(((MissionImpossibleState) oldState).getEthanPosition().split(",")[1]);
			if(ethanColumnNumber == 0)
				return null;
			newState.setEthanPosition(((MissionImpossibleState) oldState).getEthanPosition().split(",")[0] + "," + (ethanColumnNumber-1));
		}
		// for drop action
		if(action.getName().equals("drop")) {
			// if position of ethan is not same as submarine, return null
			if(!((MissionImpossibleState) oldState).getEthanPosition().equals(submarinePosition))
				return null;
			boolean isCarrying = false;
			// iterate over members, change flag of each carried member from 'c' to 'd'
			for(int i = 0; i<numberOfMembers; i++) {
				String[] memberAttributes = updatedMembers[i].split(",");
				if(memberAttributes[3].equals("c")) {
					memberAttributes[3] = "d";
					isCarrying = true;
				}
				updatedMembers[i] = String.join(",", memberAttributes);
			}
			// if isCarrying flag is still false, then no member is carried, return null
			if(!isCarrying)
				return null;
			newState.setEthanPosition(((MissionImpossibleState) oldState).getEthanPosition());
		}
		// for carry action
		if(action.getName().equals("carry")) {
			int carriedMembers = 0;
			// iterate over members to count number of carried members
			for(int i = 0; i<numberOfMembers; i++) {
				String[] memberAttributes = updatedMembers[i].split(",");
				if(memberAttributes[3].equals("c"))
					carriedMembers++;
			}
			// if number of carried members is greater than truck capacity, return null
			if(carriedMembers >= truckCapacity)
				return null;
			String ethanPosition = ((MissionImpossibleState) oldState).getEthanPosition();
			int i;
			// iterate over members to change flag of member at the same position of ethan from "r" to "c"
			for(i = 0; i<numberOfMembers; i++) {
				String[] memberAttributes = updatedMembers[i].split(",");
				if(ethanPosition.equals(memberAttributes[0]+","+memberAttributes[1]) && memberAttributes[3].equals("r")) {
					memberAttributes[3] = "c";
					updatedMembers[i] = String.join(",", memberAttributes);
					break;
				}
			}
			// if i is equal to number of members, then loop has not been broken, then no carried member is found, then return null
			if(i==numberOfMembers)
				return null;
			newState.setEthanPosition(((MissionImpossibleState) oldState).getEthanPosition());
		}
		// iterate over all members to update their healths by incrementing them by 2, unless health will exceed 100, then set it to 100
		for(int i = 0;i<numberOfMembers;i++) {
			String[] memberAttributes = updatedMembers[i].split(",");
			if(memberAttributes[3].equals("r")) {
				int health = Integer.parseInt(memberAttributes[2]);
				if(health < 99) {
					health += 2;
					updatedTotalDamage += 2;
				}
				else if(health == 99) {
					health++;
					updatedTotalDamage++;
				}
				memberAttributes[2] = health+"";
			}
			updatedMembers[i] = String.join(",", memberAttributes);
		}
		newState.setTotalDamage(updatedTotalDamage);
		newState.setMembers(updatedMembers);
		int updatedTotalDeaths = 0;
		// iterate over members to count members whose healths are 100, ie dead members
		for(int i = 0;i < numberOfMembers; i++) {
			if(Integer.parseInt(updatedMembers[i].split(",")[2]) == 100)
				updatedTotalDeaths++;
		}
		newState.setTotalDeaths(updatedTotalDeaths);
		// check if new state is repeated, then return null
		if(isRepeatedState(newState))
			return null;
		return newState;
	}
	
	// function to return string representation of solution
	public String nodeToString(SearchTreeNode node) {
		// if node is null then no solution
		if(node==null)
			return "FAILURE";
		String plan = "";
		SearchTreeNode goalNode = node;
		// iterate over all nodes from goal to root, and add the operator of each node to the plan
		while(goalNode != null) {
			if(goalNode.getOperator() != null)
				plan = goalNode.getOperator().getName() + "," + plan;
			goalNode = goalNode.getParent();
		}
		int deaths = 0;
		String healths = "";
		// iterate over all members to add their healths to the solution, and count the number of dead members and it too
		for(int i = 0;i<numberOfMembers;i++) {
			String health = ((MissionImpossibleState)node.getState()).getMembers()[i].split(",")[2];
			healths += health+",";
			if(health.equals("100"))
				deaths++;
		}
		// solution is "plan;deaths;healths;expandedNodes"
		return plan.substring(0, plan.length()-1)+";"+deaths+";"+healths.substring(0, healths.length()-1)+";"+expandedNodes;
		
	}
	
	// function to format members to be stored in statesHistory
	public String membersCount(String[] members) {
		int carriedMembers = 0;
		String remainingPositions = "";
		// iterate over all members to count carried members, and get positions of remaining members
		for(int i = 0;i<numberOfMembers;i++) {
			 if(members[i].split(",")[3].equals("c"))
				 carriedMembers++;
			 if(members[i].split(",")[3].equals("r"))
				 remainingPositions += members[i].split(",")[0] + "," + members[i].split(",")[1] + ";";
		}
		// return the following format "member1Row,member1Column;....memberkRow,memberkColumn;carriedMembersCount"
		return remainingPositions + ";" + carriedMembers;
	}
	
	public static void main(String[] args) {
		String solution = solve("9,9;8,7;5,0;0,8,2,6,5,6,1,7,5,5,8,3,2,2,2,5,0,7;11,13,75,50,56,44,26,77,18;2", "BF", true);
		System.out.println(solution);
		System.out.println(solution.split(";")[0].split(",").length);
	}
}
