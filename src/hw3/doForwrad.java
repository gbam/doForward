package hw3;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class doForwrad {
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
		String sequence = args[6];

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
						State s = new State(fromName, stateCounter);
						stateCounter++; //Need to increment state counter to move position in array
						statesList.put(s.getName(), s);
					}
					if(!statesList.containsKey(toName)){
						State s = new State(toName, stateCounter);
						stateCounter++; //Need to increment state counter to move position in array
						statesList.put(s.getName(), s);
					}
					State fromState = statesList.get(fromName);
					State toState = statesList.get(toName); 
					Transition t = new Transition(fromState, toState, probability);
					fromState.addTransition(t); //Adds the transition to the from state as this is the forward alg
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
State endState = statesList.get(startStateName);
calc(startState, 0, 1.0, endState, sequence);


		
	}
	
	private static Double calc(State s, int time, Double prob, State endState, String sequence){
		if(sequence.length() > time){
			time++; //Increase time
			for(Transition t: s.getTransitions()){
			System.out.println("Alpha for state " + s.getName().toString() + " at time " + time + " - " + prob);
			State nextState = t.toState();
			if(nextState.getName() == endState.getName()){
				
				prob = prob*t.getTransProb()*(s.getEmission().get(sequence.charAt(time)).getEmissionProb()); //Probability of current * transition * emission
			}
			
		}
		
		}
		return prob;
	}

}
