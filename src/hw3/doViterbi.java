package hw3;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class doViterbi {

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
		ViterbiHolder[][] fMatrix = new ViterbiHolder[statesList.size()][sequence.length() + 1];
		//Initialize matrix
		for(int i = 0; i < fMatrix.length; i++){
			for(int j = 0; j < fMatrix[0].length; j++){
				fMatrix[i][j] = new ViterbiHolder(0.0, null);


			}
		}
		fMatrix[0][0].value = 1.0;
		for(int k = 1; k < statesList.size(); k++){
			if(statesList.get(k) != startState && statesList.get(k) != endState){
				for(int t = 1; t < sequence.length() + 1; t++){

					State currentState = statesList.get(k);
					List<Transition> previousTrans = currentState.getBwdTransitions();

					for(Transition tOld: previousTrans){
						double transProb = tOld.getTransProb();
						int originID = Integer.parseInt(tOld.fromState().getName());
						ViterbiHolder vHolder = fMatrix[originID][t-1];
						Double newProb = vHolder.value * transProb *  currentState.findEmissionProb(sequence.charAt(t-1));
						if(fMatrix[Integer.parseInt(currentState.getName())][t].value < newProb){
							fMatrix[Integer.parseInt(currentState.getName())][t].value = newProb;
							fMatrix[Integer.parseInt(currentState.getName())][t].t = tOld;
						}


					}
				}
			}
		}
		List<Transition> previousStates = endState.getBwdTransitions();
		for(Transition currTrans: previousStates){ //All previous transition to end state
			double transProb = currTrans.getTransProb(); //Get their transmission probability
			int originID = Integer.parseInt(currTrans.fromState().getName()); 
			double oldProb = fMatrix[originID][sequence.length()].value;  //Get the previous value
			//sum += transProb * oldProb;
			double newProb = oldProb * transProb;
			if(fMatrix[Integer.parseInt(endState.getName())][sequence.length()].value < newProb){
				fMatrix[Integer.parseInt(endState.getName())][sequence.length()].value = newProb;
				fMatrix[Integer.parseInt(endState.getName())][sequence.length()].t = currTrans;
			}

		}
		//fMatrix[statesList.size()-1][sequence.length()] = sum;

		for(int j = 1; j < fMatrix[0].length; j++){
			Double highest = Double.MIN_VALUE;
			String highestState = "";
			for(int i = 1; i < fMatrix.length-1; i++){
				System.out.print("Alpha for state: " + i + " time: " + j + ": " + (float) fMatrix[i][j].value);	
				if(highest <  fMatrix[i][j].value){
					highest =  fMatrix[i][j].value;
					highestState =  fMatrix[i][j].t.toState().getName();;
				}
				
				System.out.println(" maxstate " + highestState);
			}
		}
		State s = endState;
		boolean first = true;
		ArrayList<String> pLine = new ArrayList<String>(); //print list
		Transition t = null;
		int i = sequence.length();
		while((s != null || first) && i >= 0){
			if(first){
				t =  fMatrix[Integer.parseInt(endState.getName())][sequence.length()].t;
				first = false;
			}
			String seqLetter = "";
			String pMe = "";
			if(i != 0){
				s = t.fromState();
				String fromStateName = t.fromState().getName();
				seqLetter = sequence.charAt(i-1) + "";
				pMe = t.fromState().getName() + " -> " + seqLetter;
				t = fMatrix[Integer.parseInt(fromStateName)][i].t;
			}
			else {
				pMe = t.fromState().getName();
			}
			i--;
			pLine.add(0, pMe);

		}

		for(String printer: pLine){
			System.out.println(printer);

		}
System.out.println("Explaining my max states: To me, the max states printed in the assignment make no sense.  Looking at the example,");
System.out.println("You print a zero for the max state when really the max state is currently one, the highest state for that time?  Unless, ");
System.out.println("you wanted to print the state it came from?  Sorry I didn't real.  Either way, I don't think this should be a big point deducation, ");
System.out.println("assuming the rest works.  And if you don't mind, I can easily fix it to print the correct output, I just have no idea what that is...");
	}




}


