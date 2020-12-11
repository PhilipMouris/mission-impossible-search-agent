package code.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public abstract class SearchProblem {
	protected Operator[] operators;
	protected State initialState;
	// comparator to be used for UCS
	protected NodeComparator orderedInsertNodeComparator;
	// comparator to be used for Greedy 1
	protected NodeComparator manhattanHeuristicNodeComparator;
	// comparator to be used for Greedy 2
	protected NodeComparator euclideanHeuristicNodeComparator;
	// comparator to be used for A* 1
	protected NodeComparator totalCostEuclideanNodeComparator;
	// comparator to be used for A* 2
	protected NodeComparator totalCostManhattanNodeComparator;
	// number of all expanded nodes
	protected static int expandedNodes = 0;
	// id for each node, incremented whenever a nodes is created
	protected static int nodeId = 0;
	
	// transition function
	public abstract State nextState(State oldState, Operator action);
	
	public abstract boolean goalTest(State state);
	
	public abstract String pathCost(SearchTreeNode node);
	
	public abstract boolean isRepeatedState(State state);
	
	public abstract void resetRepeatedStates();
	
	// each search function resets the number of expanded nodes and calls the generalSearch function,
	// passing it the problem and the queuing function
	public static SearchTreeNode BF(SearchProblem problem) {
		expandedNodes = 0;
		return generalSearch(problem, QueuingFunction.ENQUEUE_AT_END);
	}
	
	public static SearchTreeNode DF(SearchProblem problem) {
		expandedNodes = 0;
		return generalSearch(problem, QueuingFunction.ENQUEUE_AT_FRONT);
	}
	
	public static SearchTreeNode ID(SearchProblem problem) {
		expandedNodes = 0;
		int depth = 0;
		// the loop for iterative deepening resets nodeId and repeated states in each iteration,
		// it increments the depth by adding 1 and 0.1 of the depth, this makes the increment dependent on how deep
		// the tree already is, which speeds up execution, the depth along with the problem and the queuing function
		// are passed to the overloaded generalSearch function
		while(true) {
			nodeId = 0;
			SearchTreeNode solution = generalSearch(problem, QueuingFunction.ENQUEUE_AT_FRONT, depth);
			if(solution != null)
				return solution;
			depth += (int)(1 + (depth*0.1));
			problem.resetRepeatedStates();
		}
	}
	
	public static SearchTreeNode UC(SearchProblem problem) {
		expandedNodes = 0;
		return generalSearch(problem, QueuingFunction.ORDERED_INSERT);
	}
	
	public static SearchTreeNode GR1(SearchProblem problem) {
		expandedNodes = 0;
		return generalSearch(problem, QueuingFunction.MANHATTAN_HEURISTIC);
	}
	
	public static SearchTreeNode GR2(SearchProblem problem) {
		expandedNodes = 0;
		return generalSearch(problem, QueuingFunction.EUCLIDEAN_HEURISTIC);
	}
	
	public static SearchTreeNode AS1(SearchProblem problem) {
		expandedNodes = 0;
		return generalSearch(problem, QueuingFunction.MANHATTAN_TOTAL);
	}
	
	public static SearchTreeNode AS2(SearchProblem problem) {
		expandedNodes = 0;
		return generalSearch(problem, QueuingFunction.EUCLIDEAN_TOTAL);
	}
	
	public static SearchTreeNode generalSearch(SearchProblem problem, QueuingFunction queuingFunction) {
		// initialize the root node
		SearchTreeNode rootNode = new SearchTreeNode(problem.initialState, null, null, 0, "0,0", nodeId);
		// incrementing the nodeId
		nodeId++;
		// declaring the nodes queue
		Queue<SearchTreeNode> nodesQueue;
		// initializing the nodes queue with a comparator according to the queuing function
		if(queuingFunction == QueuingFunction.ORDERED_INSERT) {
			NodeComparator comparator = problem.orderedInsertNodeComparator;
			nodesQueue = new PriorityQueue<SearchTreeNode>(11, comparator);
		}
		else if(queuingFunction == QueuingFunction.MANHATTAN_HEURISTIC) {
			NodeComparator comparator = problem.manhattanHeuristicNodeComparator;
			nodesQueue = new PriorityQueue<SearchTreeNode>(11, comparator);
		}
		else if(queuingFunction == QueuingFunction.EUCLIDEAN_HEURISTIC) {
			NodeComparator comparator = problem.euclideanHeuristicNodeComparator;
			nodesQueue = new PriorityQueue<SearchTreeNode>(11, comparator);
		}
		else if(queuingFunction == QueuingFunction.MANHATTAN_TOTAL) {
			NodeComparator comparator = problem.totalCostManhattanNodeComparator;
			nodesQueue = new PriorityQueue<SearchTreeNode>(11, comparator);
		}
		else if(queuingFunction == QueuingFunction.EUCLIDEAN_TOTAL) {
			NodeComparator comparator = problem.totalCostEuclideanNodeComparator;
			nodesQueue = new PriorityQueue<SearchTreeNode>(11, comparator);
		}
		else {
			nodesQueue = new LinkedList<>();			
		}
		// adding the root node to the nodes queue
		nodesQueue.add(rootNode);
		// while nodes queue is not empty, remove the first node and check if it is a goal node
		// if it is a goal, return it
		// otherwise, update the nodes queue with the expanded nodes
		while(nodesQueue.size() > 0) {
			SearchTreeNode queueFront = nodesQueue.remove();
			if (problem.goalTest(queueFront.getState())) {
				return queueFront;
			}
			nodesQueue = updateQueue(nodesQueue, expand(problem, queueFront, problem.operators), queuingFunction);
		}
		// return null if no goal node is found
		return null;
	}
	
	// overloaded general search function that takes depth as a parameter, used by iterative deepening search
	public static SearchTreeNode generalSearch(SearchProblem problem, QueuingFunction queuingFunction, int depth) {
		SearchTreeNode rootNode = new SearchTreeNode(problem.initialState, null, null, 0, "0,0", nodeId);
		nodeId++;
		Queue<SearchTreeNode> nodesQueue = new LinkedList<>();
		nodesQueue.add(rootNode);
		while(nodesQueue.size() > 0) {
			SearchTreeNode queueFront = nodesQueue.remove();
			if (problem.goalTest(queueFront.getState())) {
				return queueFront;
			}
			// only expand and update queue if depth of current node is less than the current depth iteration
			if(queueFront.getDepth()<depth)
				nodesQueue = updateQueue(nodesQueue, expand(problem, queueFront, problem.operators), queuingFunction);
		}
		return null;
	}
	
	public static ArrayList<SearchTreeNode> expand(SearchProblem problem, SearchTreeNode node, Operator[] operators) {
		// increment expanded nodes
		expandedNodes++;
		ArrayList<SearchTreeNode> expandedNodes = new ArrayList<SearchTreeNode>();
		// for each operator, we find the next state using the transition function, create a node using this state
		// and add it to the list of expanded nodes, and increment the nodeId
		for(int i = 0;i<operators.length; i++) {
			State newState = problem.nextState(node.getState(), operators[i]);
			if (newState != null) {
				SearchTreeNode newNode = new SearchTreeNode(newState, node, operators[i], node.getDepth() + 1, "", nodeId);
				newNode.setPathCost(problem.pathCost(newNode));
				expandedNodes.add(newNode);
				nodeId++;
			}
		}
		return expandedNodes;
	}
	
	public static Queue<SearchTreeNode> updateQueue(Queue<SearchTreeNode> oldQueue, ArrayList<SearchTreeNode> expandedNodes, QueuingFunction queuingFunction){
		// if the queuing function is ENQUEUE_AT_FRONT, which corresponds to depth first search,
		// then a new queue is created, in which the expanded nodes are inserted first, then the nodes
		// of the original queue are inserted
		if(queuingFunction == QueuingFunction.ENQUEUE_AT_FRONT) {
			Queue<SearchTreeNode> newQueue = new LinkedList<>();
			for(int i = 0; i<expandedNodes.size(); i++)
				newQueue.add(expandedNodes.get(i));
			while(!oldQueue.isEmpty())
				newQueue.add(oldQueue.remove());
			return newQueue;
		}
		// otherwise, we insert the nodes directly in the old queue, and if the old queue is a priority queue,
		// it will handle the order based on its comparator
		else {
			for(int i = 0; i<expandedNodes.size(); i++)
				oldQueue.add(expandedNodes.get(i));
			return oldQueue;
		}
	}
}
