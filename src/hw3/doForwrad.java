package hw3;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class doForwrad {
	private static String sequence;
	private static State endState;
	private static PriorityQueue<pqHelper> pq;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 7){
			System.out.println("Not Correct # of Arguements");
			System.exit(0);
		}
		//Determines required information about inputs
		String transitionsFileName = args[0];
		String transitionsFileNameSyntax = args[1];
		int stateCounter = 0; //Keeps track of the number of the states - and indicies in grid.
		String emissionFileName = args[2];
		String emissionFileNameSyntax = args[3];
		String startStateName = args[4];
		String endStateName = args[5];
		sequence = args[6];

		HashMap<String, State> statesList = new HashMap<String, State>();
		//Transmission is read first to create all the nodes.
		try{
			File transitionsFile = new File(transitionsFileName + "." + transitionsFileNameSyntax);
			BufferedReader  reader = new BufferedReader(new FileReader(transitionsFile));
			while(reader.ready()){

				Pattern p = Pattern.compile("[0-9]+\\s[0-9]+\\s[0-9][.][0-9]+");
				Matcher m = p.matcher(reader.readLine());
				if(m.find()){
					String[] temp = m.group(0).split(" ");
					String fromName = temp[0];
					String toName = temp[1];
					Double probability = Double.parseDouble(temp[2]);
					//You have to check if both states exist then add them if not
					if(!statesList.containsKey(fromName)){
						State s = new State(fromName);
						statesList.put(s.getName(), s);
					}
					if(!statesList.containsKey(toName)){
						State s = new State(toName);
						statesList.put(s.getName(), s);
					}
					State fromState = statesList.get(fromName);
					State toState = statesList.get(toName); 
					Transition fwdT = new Transition(fromState, toState, probability);
					Transition bwdT = new Transition(toState, fromState, probability);
					fromState.addFwdTransition(fwdT); //Adds the transition to the from state as this is the forward alg
					fromState.addBwdTransition(bwdT); //Adds the backwards probability
				}
				else throw new Exception("Bad File Format");
			}
		}catch(Exception e){
			System.out.println("Failed to transitions file");
		}

		//Emission probabilities section
		try{
			File emissionFile = new File(emissionFileName + "." + emissionFileNameSyntax);
			BufferedReader  reader = new BufferedReader(new FileReader(emissionFile));
			while(reader.ready()){
				//Pattern p = Pattern.compile("[0-9]+[\\s][A-Za-z0-9]+[\\s][0-9][.][0-9]+");
				//	Matcher m = p.matcher(reader.readLine());
				//	if(m.find()){
				String[] temp = reader.readLine().split(" ");
				String nodeName = temp[0];
				String emission = temp[1];
				Double prob = Double.parseDouble(temp[2]);
				Emission e = new Emission(prob, emission);
				statesList.get(nodeName).addEmission(e); //We are assuming all nodes exist so we can just add it to the list for that node.
				//	}
				//	else throw new Exception("Bad File Format");
			}
		}catch(Exception e){
			System.out.println("Failed to emissions file");
		}

		State startState = statesList.get(startStateName);
		endState = statesList.get(endStateName);
		startState.probabilityList.add(1.0);
		pq = new PriorityQueue<pqHelper>();
		tTaken = new ArrayList<Transition>();
		for(Transition t: startState.getFwdTransitions()){
			fwdAlg(t, 1);
		}
		
		while(!pq.isEmpty()){
			pqHelper pqHelp = pq.poll();
			fwdAlg(pqHelp.t, pqHelp.priority);
		}
		List<State> myStateList = printMap(statesList);
		for(int i = 0; i < sequence.length()+1; i++){
			for(State s: myStateList){
				Double prob = 0.0;
				try{
					prob = s.probabilityList.get(i);
				}catch(Exception e){
				}
				if(prob != 0.0)	System.out.println("Alpha for state " + s.getName() + " time: " + i + ": " +  prob.floatValue());
			}
		}

		
	}

	private static void fwdAlg(Transition trans, int t){
		if(t <= sequence.length()){
			State next = trans.toState();
			if(next != endState){
				List<Emission> emiss = next.getEmission(); //Possible list of emissions
				Double emitProb = null;
				for(Emission e: emiss){ //Finds the next emission probablity
					if(e.emission().charAt(0) == sequence.charAt(t-1)){
						emitProb = e.getEmissionProb();
					}
				}
				if(emitProb == null){
					System.out.println("Something went wrong and the emission probablity wasn't found");
					System.exit(-1);
				}
				while(next.probabilityList.size() <= t){
					next.probabilityList.add(0.0);
				}
				Double previousProbability = trans.fromState().probabilityList.get(t-1); 
				Double currentProbability = 0.0;
				try{
					currentProbability = next.probabilityList.get(t);
				}catch(Exception e){}
				
				Double newProbability = previousProbability * emitProb * trans.getTransProb() + currentProbability;
				next.probabilityList.set(t, newProbability);
				for(Transition tFwd: next.getFwdTransitions()){		
					pqHelper pqHelps = new pqHelper();
					pqHelps.priority = t+1;
					pqHelps.t = tFwd;	
					while(tFwd.toState().visited.size() <= t+1){
						tFwd.toState().visited.add(false);
					}
					if(!tFwd.toState().visited.get(t+1)){
						pq.add(pqHelps);
						tFwd.toState().visited.set(t+1, true);
					}
					
				}
			}
			if(next == endState && t == sequence.length()){
				boolean transitionTaken = false;
//				for(Transition tr: tTaken){
//					if(tr.fromState() == trans.fromState() && tr.toState() == trans.toState()){
//						transitionTaken = true;
//						System.out.println("Taken!!!!!");
//					}
//				}
				
				if(!transitionTaken){
				while(next.probabilityList.size()-1 < t){
					next.probabilityList.add(0.0);
				}
				Double previousProbability = trans.fromState().probabilityList.get(t-1); //In case this space doesn't exist
				Double currentProbability = 0.0;
				try{
					currentProbability = next.probabilityList.get(t);
				}catch(Exception e){}
				System.out.println("-----------------------------------------------");
				System.out.println("Current State:" + trans.fromState().getName());
				System.out.println("Current Time: " + t);
				System.out.println("Previous Prob: " + previousProbability);
				System.out.println("Current Prob: " + currentProbability);
				System.out.println("trans Prob: " + trans.getTransProb());
				Double newProbability = previousProbability * trans.getTransProb() + currentProbability;
				next.probabilityList.set(t, newProbability);
				tTaken.add(trans);
				}
			}
		}
	}
	private static List<Transition> tTaken;
	private static List<State> printMap(HashMap<String, State> mp) {
		List<State> allStates = new ArrayList<State>();
		for(int i = 0; i < mp.values().size(); i++){
			allStates.add(null);
		}
		Iterator<Entry<String, State>> it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, State> pairs = (Map.Entry)it.next();
			allStates.set(Integer.parseInt(pairs.getKey()), pairs.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}
		return allStates;
	}

}


class pqHelper implements Comparable<pqHelper>{
	public int priority;
	public Transition t;
	public int compareTo(pqHelper arg0) {
		if(this.priority == arg0.priority) return 0;
		else if(this.priority > arg0.priority) return 1;
		else return -1;
		
		
	}
}