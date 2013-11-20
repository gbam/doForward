package hw3;

public class Transition {
	private State fromState, toState;
	private double transProb;
	public State fromState() {
		return fromState;
	}
	public State toState() {
		return toState;
	}
	public double getTransProb() {
		return transProb;
	}
	public Transition(State fromState, State toState, double transProb) {
		super();
		this.fromState = fromState;
		this.toState = toState;
		this.transProb = transProb;
	}

}
