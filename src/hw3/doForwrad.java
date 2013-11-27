package hw3;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

		State startState = statesList.get(Integer.parseInt(startStateName));
		endState = statesList.get(Integer.parseInt(endStateName));
		startState.probabilityList.add(1.0);

		double[][] fMatrix = new double[statesList.size()][sequence.length() + 1];
		fMatrix[0][0] = 1.0;
		for(int k = 1; k < statesList.size(); k++){
			if(statesList.get(k) != startState && statesList.get(k) != endState){
				for(int t = 1; t < sequence.length() + 1; t++){
					double sum = 0.0;
					State currentState = statesList.get(k);
					List<Transition> previousTrans = currentState.getBwdTransitions();
					for(Transition tOld: previousTrans){
						double transProb = tOld.getTransProb();
						int originID = Integer.parseInt(tOld.fromState().getName());
						double oldProb = fMatrix[originID][t-1];
						sum += transProb * oldProb;

					}
					sum = sum * currentState.findEmissionProb(sequence.charAt(t-1));
					fMatrix[k][t] = sum;
				}
			}
		}
		double sum = 0.0;
		List<Transition> previousStates = endState.getBwdTransitions();
		for(Transition currTrans: previousStates){
			double transProb = currTrans.getTransProb();
			int originID = Integer.parseInt(currTrans.fromState().getName());
			double oldProb = fMatrix[originID][sequence.length()];
			sum += transProb * oldProb;

		}
		fMatrix[statesList.size()-1][sequence.length()] = sum;

		for(int i = 1; i < fMatrix.length; i++){
			for(int j = 1; j < fMatrix[0].length; j++){
				System.out.println("Alpha for state: " + i + " time: " + j + ": " + (float) fMatrix[i][j]);		
			}
		}
	}

}