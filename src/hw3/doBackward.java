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
		double[][] fMatrix = new double[statesList.size()][sequence.length() + 1]; //Stores probabilities
		// stores all probabilities of observing the rest of X given that we are in state k and after i characters
		fMatrix[fMatrix.length - 1][fMatrix[0].length - 1] = 1;
		double transProb = 0; 
		for(int i=0; i< Integer.parseInt(endStateName);i++){ // initialize matrix
			try {
				for(int j = 0; j < statesList.get(i).getFwdTransitions().size(); j++){
					Transition currentTransition = statesList.get(i).getFwdTransitions().get(j);
					if(currentTransition.toState().getName().compareTo(endState.getName()) == 0){
						transProb = currentTransition.getTransProb();
					}
				}
			} catch(Exception e) {
				transProb = 0.0;
			}
			fMatrix[i][sequence.length()] = transProb; 
		}

		backwardAlg(fMatrix, statesList);

		for(int c = fMatrix[0].length - 2; c > 0; c--) {
			for(int r = 1; r < fMatrix.length - 1; r++) {
				System.out.println("Beta for state " + r + " time " + c + ": " + (float) fMatrix[r][c]);

			}
		}


		double totalProb = 0, emissionProb = 0;;
		for(int i = 1; i < Integer.parseInt(endState.getName()); i++){
			try { // get probability of emission for first char 
				List<Emission> e =  statesList.get(i).getEmission();
				for(int z = 0; z < e.size(); z++){
					if(e.get(z).emission().charAt(0) == sequence.charAt(0)){
						emissionProb = e.get(z).getEmissionProb();
						break;
					}

				}
			} catch(Exception e) { // Got some errors, caught some errors, shouldn't be used.
				emissionProb = 0.0;
			}
			try { // getting transition probabilities
				List<Transition> tList = startState.getFwdTransitions();
				for(int p = 0; p < tList.size(); p++){
					if(tList.get(p).toState().getName() == statesList.get(i).getName()){
						transProb = tList.get(p).getTransProb();
						break;
					}
				}
			} catch(Exception e) { // just in case the transProb is set to 0
				transProb = 0.0;
			}
			totalProb += fMatrix[i][1]*emissionProb*transProb; //  total backwards probability
		}
		System.out.println("Backward probability: " + totalProb);
	}





	public static void backwardAlg(double[][] fMatrix, List<State> statesList){
		double emissionProb = 0;
		// compute every probability except the last one 
		for(int i = sequence.length()-1; i>=1; i--){
			for(int j = 1; j < Integer.parseInt(endState.getName()); j++){
				//				String xT = sequence[i];
				double sum1 = 0;			
				double sum2 = 0;
				emissionProb = 0;
				for(int a = 1; a < Integer.parseInt(endState.getName()); a++){

					//gets the emission for a given letter at a given state
					List<Emission> emissionList = statesList.get(a).getEmission();
					for(int q = 0; q < emissionList.size(); q++){
						if(emissionList.get(q).emission().charAt(0) == sequence.charAt(i)){
							emissionProb = emissionList.get(q).getEmissionProb();
							break;
						}
					}
					//End of getting emission letter

					//Gets the probability of a transition
					try {
						List<Transition> listT = statesList.get(j).getFwdTransitions();
						for(int q = 0; q < listT.size(); q++){
							if(listT.get(q).toState().getName() == statesList.get(a).getName()){
								sum2 = listT.get(q).getTransProb();
							}
						}
					} catch(Exception e) {
						sum2 = 0.0;
					}

					//End the probability of getting a transition

					sum1 += emissionProb*fMatrix[a][i+1]*sum2;
				}
				fMatrix[j][i] = sum1;
			}
		}
	}
}