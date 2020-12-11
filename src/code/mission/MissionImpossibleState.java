package code.mission;

import code.generic.State;

public class MissionImpossibleState extends State {
	private String ethanPosition;
	// total damage incurred to members
	private int totalDamage;
	// total number of dead members
	private int totalDeaths;
	// each member is represented as a string of the following format "row,column,health,flag", flag can be any value
	// from "r,c,d" representing "remaining,carried,dropped" respectively
	private String[] members;
	
	public MissionImpossibleState(String ethanPosition, int totalDamage, int totalDeaths, String[] members) {
		this.ethanPosition = ethanPosition;
		this.totalDamage = totalDamage;
		this.members = members;
		this.totalDeaths = totalDeaths;
	}
	
	public String getEthanPosition() {
		return ethanPosition;
	}
	public int getTotalDamage() {
		return totalDamage;
	}
	public void setEthanPosition(String ethanPosition) {
		this.ethanPosition = ethanPosition;
	}

	public void setTotalDamage(int totalDamage) {
		this.totalDamage = totalDamage;
	}

	public String[] getMembers() {
		return members;
	}

	public void setMembers(String[] members) {
		this.members = members;
	}

	public int getTotalDeaths() {
		return totalDeaths;
	}

	public void setTotalDeaths(int totalDeaths) {
		this.totalDeaths = totalDeaths;
	}
	
}
