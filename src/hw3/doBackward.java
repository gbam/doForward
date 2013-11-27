package hw3;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class doBackward {
	private static String sequence;
	private static State endState;
	private static State startState;
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

		List<State> statesList = new ArrayList<State>();
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
					double probability = Double.parseDouble(temp[2]);
					//You have to check if both states exist then add them if not
					try{
						statesList.get(Integer.parseInt(fromName));
					}catch(IndexOutOfBoundsException e){
						State s = new State(fromName);
						statesList.add(s);
					}
					try{
						statesList.get(Integer.parseInt(toName));
					}catch(IndexOutOfBoundsException e){
						State s = new State(toName);
						statesList.add(s);
					}
					State fromState = statesList.get(Integer.parseInt(fromName));
					State toState = statesList.get(Integer.parseInt(toName)); 
					Transition fwdT = new Transition(fromState, toState, probability);
					Transition bwdT = new Transition(fromState, toState, probability);
					fromState.addFwdTransition(fwdT); //Adds the transition to the from state as this is the forward alg
					toState.addBwdTransition(bwdT); //Adds the backwards probability
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
				double prob = Double.parseDouble(temp[2]);
				Emission e = new Emission(prob, emission);
				statesList.get(Integer.parseInt(nodeName)).addEmission(e); //We are assuming all nodes exist so we can just add it to the list for that node.
				//	}
				//	else throw new Exception("Bad File Format");
			}
		}catch(Exception e){
			System.out.println("Failed to emissions file");
		}

		startState = statesList.get(Integer.parseInt(startStateName));
		endState = statesList.get(Integer.parseInt(endStateName));


		//Here's where the algo starts
		double[][] fMatrix = new double[statesList.size()][sequence.length() + 1]; // stores all probabilities of observing the rest of X given that we are in state k and after i characters
		fMatrix[fMatrix.length - 1][fMatrix[0].length - 1] = 1;
		
		for(int i=0; i< Integer.parseInt(endStateName);i++){ // initialize matrix
//			try {
//				tempProb = statesList.get(i).getTransitions();
//			} catch(Exception e) {
//				tempProb = 0.0;
//			}
//			fMatrix[i][x] = tempProb; 
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


}