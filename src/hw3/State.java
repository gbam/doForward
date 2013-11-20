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
	public List<Transition> getTransitions() {
		return transitions;
	}
	public int getIndex() {
		return index;
	}
	public State(String name, int index) {
		super();
		this.name = name;
		this.transitions = new ArrayList<Transition>();
		this.emissions = new ArrayList<Emission>();
		this.index = index;
	}
	private List<Emission> emissions;
	private String name;
	public void addEmission(Emission emission) {
		this.emissions.add(emission);
	}
	public void addTransition(Transition transition) {
		this.transitions.add(transition);
	}
	private List<Transition> transitions;
	private int index;
}
