package hw3;

import java.util.ArrayList;
import java.util.List;

public class State {

	public List<Emission> getEmission() {
		return emissions;
	}
	public String getName() {
		return name;
	}
	public List<Transition> getFwdTransitions() {
		return fwdTransitions;
	}
	public List<Transition> getBwdTransitions() {
		return bwdTransitions;
	}
	public State(String name) {
		this.name = name;
		this.fwdTransitions = new ArrayList<Transition>();
		this.bwdTransitions = new ArrayList<Transition>();
		this.emissions = new ArrayList<Emission>();
		this.probabilityList = new ArrayList<Double>();
		this.visited = new ArrayList<Boolean>();
	}
	private List<Emission> emissions;
	private String name;
	public void addEmission(Emission emission) {
		this.emissions.add(emission);
	}
	public void addFwdTransition(Transition transition) {
		this.fwdTransitions.add(transition);
	}
	public void addBwdTransition(Transition transition) {
		this.bwdTransitions.add(transition);
	}


	private List<Transition> fwdTransitions, bwdTransitions;
	public List<Double> probabilityList;
	public List<Boolean> visited;
	
	public Double findEmissionProb(char c) {
		for(Emission e: emissions){
			if(e.emission().charAt(0) == c){
				return e.getEmissionProb();
			}
		}
		return null;
		
	}
	

}
