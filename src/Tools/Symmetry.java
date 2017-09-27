package Tools;

import java.math.BigInteger;

/**
 * This class uses multithreading to count all the non-symmetrcal states for a given board size of dots and boxes.
 * @author Jared
 *
 */

public class Symmetry {

	static int size;

	static boolean threadSwitching = true;

	static BigInteger nextIndex = new BigInteger("0");
	static BigInteger threadSize = new BigInteger("0");

	static BigInteger increment = new BigInteger("1");

	static BigInteger terminalState;
	static BigInteger startState;
	static boolean[] savedStates;
	static int activeThreads = 0;

	static int edges;
	static BigInteger[] savedStatesEdges;

	static int threads = 10;

	static BigInteger[][] threadSSE;
	static SymmetrySearch[] searchThreads; 
	
	public static int[][] rotationMap = {
		{2,0,3,1},
		{4,9,1,6,11,3,8,0,5,10,2,7},
		{6,13,20,2,9,16,23,5,12,19,1,8,15,22,4,11,18,0,7,14,21,3,10,17},
		{8,17,26,35,3,12,21,30,39,7,16,23,34,2,11,20,29,38,6,15,24,33,1,10,19,28,37,5,14,23,32,0,9,18,27,36,4,13,22,31},
		{10,21,32,43,54,4,15,26,37,48,59,9,20,31,42,53,3,14,25,36,47,58,8,19,30,41,52,2,13,24,35,46,57,7,18,29,40,51,1,12,23,34,45,56,6,17,28,39,50,0,11,22,33,44,55,5,16,27,38,49},
		{12,25,38,51,64,77,5,18,31,44,57,70,83,11,24,37,50,63,76,4,17,30,43,56,69,82,10,23,36,49,62,75,3,16,29,42,55,68,81,9,22,35,48,61,74,2,15,28,41,54,67,80,8,21,34,47,60,73,1,14,27,40,53,66,79,7,20,33,46,59,72,0,13,26,39,52,65,78,6,19,32,45,58,71},
		{14,29,44,59,74,89,104,6,21,36,51,66,81,96,111,13,28,43,58,73,88,103,5,20,35,50,65,80,95,110,12,27,42,57,72,87,102,4,19,34,49,64,79,94,109,11,26,41,56,71,86,101,3,18,33,48,63,78,93,108,10,25,40,55,70,85,100,2,17,32,47,62,77,92,107,9,24,39,54,69,84,99,1,16,31,46,61,76,91,106,8,23,38,53,68,83,98,0,15,30,45,60,75,90,105,7,22,37,52,67,82,97}
	};
	
	public static int[][] reflectionMap = {
		{3,2,1,0},
		{10,11,7,8,9,5,6,2,3,4,0,1},
		{21,22,23,17,18,19,20,14,15,16,10,11,12,13,7,8,9,3,4,5,6,0,1,2},
		{39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0},
		{4,3,2,1,0,10,9,8,7,6,5,15,14,13,12,11,21,20,19,18,17,16,26,25,24,23,22,32,31,30,29,28,27,37,36,35,34,33,43,42,41,40,39,38,48,47,46,45,44,54,53,52,51,50,49,59,58,57,56,55},
		{5,4,3,2,1,0,12,11,10,9,8,7,6,18,17,16,15,14,13,25,24,23,22,21,20,19,31,30,29,28,27,26,38,37,36,35,34,33,32,44,43,42,41,40,39,51,50,49,48,47,46,45,57,56,55,54,53,52,64,63,62,61,60,59,58,70,69,68,67,66,65,77,76,75,74,73,72,71,83,82,81,80,79,78},
		{6,5,4,3,2,1,0,14,13,12,11,10,9,8,7,21,20,19,18,17,16,15,29,28,27,26,25,24,23,22,36,35,34,33,32,31,30,44,43,42,41,40,39,38,37,51,50,49,48,47,46,45,59,58,57,56,55,54,53,52,66,65,64,63,62,61,60,74,73,72,71,70,69,68,67,81,80,79,78,77,76,75,89,88,87,86,85,84,83,82,96,95,94,93,92,91,90,104,103,102,101,100,99,98,97,111,110,109,108,107,106,105}
	};
	
	public static void main(String args[]) throws InterruptedException{

		size = Integer.parseInt(args[0]);
		edges = 2 * ((size + 1) * size);
		terminalState = new BigInteger("0").flipBit(edges);
		savedStatesEdges = new BigInteger[edges + 1];
		startState = new BigInteger("0");
		
		
		if(args.length > 2){
			startState = new BigInteger(args[2]);
			nextIndex = startState;
		}
		
		if(args.length > 3){
			terminalState = new BigInteger(args[3]);
		}
		
		System.out.println("Total States: " + terminalState);
		
		long start = System.currentTimeMillis();

		for(int i = 0; i < savedStatesEdges.length; i++){
			savedStatesEdges[i] = new BigInteger("0");
		}
		
		if(size < 3){
			savedStates = new boolean[terminalState.intValue()];

			for(BigInteger i = new BigInteger("0"); i.compareTo(terminalState) < 0; i = i.add(increment)){
				if(!savedStates[i.intValue()]){
					int index = i.toString(2).replaceAll("0", "").length();
					savedStatesEdges[index] = savedStatesEdges[index].add(increment);
					removeSymmetries(i);
				}
			}
		} 
		
		else {
			threads = Integer.parseInt(args[1]);
			searchThreads = new SymmetrySearch[threads];
			
			BigInteger searchSize = terminalState.subtract(startState);

			threadSSE = new BigInteger[threads][edges + 1];

			for(int i = 0; i < threads; i++){
				for(int j = 0; j < savedStatesEdges.length; j++){
					threadSSE[i][j] = new BigInteger("0");
				}
			}

			threadSize = threadSize.add(searchSize.divide(new BigInteger(Integer.toString(threads))));

			for(int i = 0; i < searchThreads.length; i++){
				if(i == searchThreads.length - 1){
					searchThreads[i] = new SymmetrySearch(nextIndex, terminalState, i);
				} else {
					searchThreads[i] = new SymmetrySearch(nextIndex, nextIndex.add(threadSize), i);
					nextIndex = nextIndex.add(threadSize);
				}

				activeThreads++;
			}

			while(activeThreads > 0){
				Thread.sleep(100);
			}

			for(int i = 0; i < threads; i++){
				for(int j = 0; j < savedStatesEdges.length; j++){
					savedStatesEdges[j] = savedStatesEdges[j].add(threadSSE[i][j]);
				}
			}
		}
		
		BigInteger states = new BigInteger("0");
		
		for(int i = 0; i < savedStatesEdges.length; i++){
			System.out.println("(" + i + ", " + savedStatesEdges[i] + ")");
			states = states.add(savedStatesEdges[i]);
		}
		
		long end = System.currentTimeMillis();

		System.out.println("Seconds: " + ((double) end - start) / 1000);
		System.out.println("Non-Symmetrical States: " + states);
		System.out.println("Ratio: " + states.divide(terminalState));
		System.out.println();
	}

	public static BigInteger getCannon(BigInteger state){
		
		String stateString = state.toString(2);
		
		// add extra leading zeros
		int b = stateString.length();
		for (int index = 0; index < (edges - b); index++) {
			stateString = "0" + stateString;
		}
		
		String returnState = stateString;
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);

			if(first(stateString, returnState)){
				returnState = stateString;
				return new BigInteger(returnState, 2);
			}
		}
		
		stateString = flip(stateString);
		
		if(first(stateString, returnState)){
			returnState = stateString;
			return new BigInteger(returnState, 2);
		}
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			
			if(first(stateString, returnState)){
				returnState = stateString;
				return new BigInteger(returnState, 2);
			}
		}
		
		return new BigInteger(returnState, 2);
	}
	
	public static boolean first(String state1, String state2){
		int length = state1.length();
		char c1;
		char c2;
		
		for(int i = 0; i < length; i++){
			c1 = state1.charAt(i);
			c2 = state2.charAt(i);
			
			if(c1 != c2){
				return c1 > c2 ? true : false;
			}
		}
		
		return false;
	}
	
	public static boolean isSymmetrical(BigInteger state){
		return false;
	}
	
	public static void removeSymmetries(BigInteger state){
		
		String stateString = state.toString(2);
		
		// add extra leading zeros
		int b = stateString.length();
		for (int index = 0; index < (edges - b); index++) {
			stateString = "0" + stateString;
		}
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			savedStates[Integer.parseInt(stateString, 2)] = true;
		}
		
		stateString = flip(stateString);
		savedStates[Integer.parseInt(stateString, 2)] = true;
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			savedStates[Integer.parseInt(stateString, 2)] = true;
		}
	}
	
	public static String rotate(String state){
		
		String newState = "";
		
		for(int i = 0; i < state.length(); i++){
			newState = newState + state.charAt(rotationMap[size - 1][i]);
		}
		
		return newState;
	}
	
	public static String flip(String state){

		String newState = "";
		
		for(int i = 0; i < state.length(); i++){
			newState = newState + state.charAt(reflectionMap[size - 1][i]);
		}
		
		return newState;
	}
}

class SymmetrySearch implements Runnable {

	BigInteger current;
	BigInteger chunkEnd;

	int id;

	BigInteger count = new BigInteger("0");

	private Thread t;

	SymmetrySearch(BigInteger start, BigInteger end, int id){
		this.current = start;
		this.chunkEnd = end;
		this.id = id;

		t = new Thread(this);
		t.start();
	}

	public void run(){
		
		while(current.compareTo(chunkEnd) < 0){
			BigInteger cannon = Symmetry.getCannon(current);

			if(current.equals(cannon)){
				int index = current.toString(2).replaceAll("0", "").length();
				Symmetry.threadSSE[id][index] = Symmetry.threadSSE[id][index].add(Symmetry.increment);
			}
			
			current = current.add(Symmetry.increment);
		}

		Symmetry.activeThreads--;
	}
}
