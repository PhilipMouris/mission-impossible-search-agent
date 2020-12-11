package code.generic;

public class SearchTreeNode {
	private State state;
	private SearchTreeNode parent;
	private Operator operator;
	private int depth;
	private String pathCost;
	private int id;

	public SearchTreeNode(State state, SearchTreeNode parent, Operator operator, int depth, String pathCost, int id) {
		this.state = state;
		this.parent = parent;
		this.operator = operator;
		this.depth = depth;
		this.pathCost = pathCost;
		this.id = id;
	}
	
	public State getState() {
		return state;
	}
	public SearchTreeNode getParent() {
		return parent;
	}
	public Operator getOperator() {
		return operator;
	}
	public int getDepth() {
		return depth;
	}
	public String getPathCost() {
		return pathCost;
	}
	public void setPathCost(String pathCost) {
		this.pathCost = pathCost;
	}
	public int getId() {
		return id;
	}
}
