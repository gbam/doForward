package hw3;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class doForwrad {
	private static String sequence;
	private static State endState;
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
		endState = statesList.get(startStateName);
		startState.probabilityList.add(1.0);
		for(Transition t: startState.getFwdTransitions()){
			fwdAlg(1.0, t, 0);
		}
		List<State> myStateList = printMap(statesList);
		for(int i = 0; i < sequence.length(); i++){
			for(State s: myStateList){

				System.out.println("Alpha for state " + s.getName() + " time: " + i + ": " + s.probabilityList.get(i));
			}
		}

		Double[][] fMatrix = new Double[statesList.size()][sequence.length() + 1];
		fMatrix[0][0] = 1.0;
		

		
	}

	private static List<State> printMap(HashMap<String, State> mp) {
		List<State> allStates = new ArrayList<State>();
		Iterator<Entry<String, State>> it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, State> pairs = (Map.Entry)it.next();
			allStates.add(pairs.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}
		return allStates;
	}


	private static void fwdAlg(Double prevProb, Transition trans, int t){
		if(t < sequence.length()){
			State next = trans.toState();
			if(next != endState || (next == endState && t == sequence.length())){
				List<Emission> emiss = next.getEmission(); //Possible list of emissions
				Double emitProb = null;
				for(Emission e: emiss){ //Finds the next emission probablity
					if(e.emission().charAt(0) == sequence.charAt(t)){
						emitProb = e.getEmissionProb();
					}
				}
				if(next == endState)emitProb = 1.0;
				if(emitProb == null){
					System.out.println("Something went wrong and the emission probablity wasn't found");
					System.exit(-1);
				}
				while(next.probabilityList.size() <= t){
					next.probabilityList.add(0.0);
				}
				Double currentProbability = trans.fromState().probabilityList.get(t); //In case this space doesn't exist
				currentProbability = currentProbability * emitProb * trans.getTransProb();
				for(Transition tFwd: next.getFwdTransitions()){
					fwdAlg(currentProbability, tFwd, t+1);
				}
			}
		}
	}

}


