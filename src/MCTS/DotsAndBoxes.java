package MCTS;

import java.math.BigInteger;

/**
 * A Dots and Boxes MCTSGame.
 * @author      Jared Prince
 * @version     1.0
 * @since       1.0
 */

public class DotsAndBoxes extends MCTSGame{
	
	/** The height (in boxes) of the board.
	 */
	public int height;
	
	/** The width (in boxes) of the board.
	 */
	public int width;
	
	/** The number of edges on the board.
	 */
	public int edges;
	
	/** A boolean which is true if the game uses scored states and false otherwise.
	 */
	public boolean scored;
	
	/** A boolean which is true if the game uses non-symmetrical states and false otherwise.
	 */
	public boolean nonsymmetrical;
	
	/*
	public int[] fourthEdges = null;
	public int[] twoOrThreeEdges = null;
	*/
	
	/** A 2D array which lists the box(es) which adjoin a given edge.
	 *  Position i contains an array of 1 or two integers representing the boxes to which edge i belongs
	 */
	public int[][] edgeBoxes;
	
	/** A 2D array which lists the edges which form a given box.
	 *  Position i contains an array of 4 integers representing the edges which form box i.
	 */
	public int[][] boxEdges;
	
	/** A 2D array which maps each edge (in square boards of size 1 - 7) to its position after a single rotation of the board.
	 *  Position i contains the map for a square board of size i + 1.
	 *  Position j in i contains an integer representing the edge which edge j will become after rotation.
	 */
	public static int[][] rotationMap = {
		{2,0,3,1},
		{4,9,1,6,11,3,8,0,5,10,2,7},
		{6,13,20,2,9,16,23,5,12,19,1,8,15,22,4,11,18,0,7,14,21,3,10,17},
		{8,17,26,35,3,12,21,30,39,7,16,23,34,2,11,20,29,38,6,15,24,33,1,10,19,28,37,5,14,23,32,0,9,18,27,36,4,13,22,31},
		{10,21,32,43,54,4,15,26,37,48,59,9,20,31,42,53,3,14,25,36,47,58,8,19,30,41,52,2,13,24,35,46,57,7,18,29,40,51,1,12,23,34,45,56,6,17,28,39,50,0,11,22,33,44,55,5,16,27,38,49},
		{12,25,38,51,64,77,5,18,31,44,57,70,83,11,24,37,50,63,76,4,17,30,43,56,69,82,10,23,36,49,62,75,3,16,29,42,55,68,81,9,22,35,48,61,74,2,15,28,41,54,67,80,8,21,34,47,60,73,1,14,27,40,53,66,79,7,20,33,46,59,72,0,13,26,39,52,65,78,6,19,32,45,58,71},
		{14,29,44,59,74,89,104,6,21,36,51,66,81,96,111,13,28,43,58,73,88,103,5,20,35,50,65,80,95,110,12,27,42,57,72,87,102,4,19,34,49,64,79,94,109,11,26,41,56,71,86,101,3,18,33,48,63,78,93,108,10,25,40,55,70,85,100,2,17,32,47,62,77,92,107,9,24,39,54,69,84,99,1,16,31,46,61,76,91,106,8,23,38,53,68,83,98,0,15,30,45,60,75,90,105,7,22,37,52,67,82,97}
	};
	
	/** A 2D array which maps each edge (in square boards of size 1 - 7) to its position after a reflection of the board.
	 *  Position i contains the map for a square board of size i + 1.
	 *  Position j in i contains an integer representing the edge which edge j will become after reflection.
	 */
	public static int[][] reflectionMap = {
		{3,2,1,0},
		{10,11,7,8,9,5,6,2,3,4,0,1},
		{21,22,23,17,18,19,20,14,15,16,10,11,12,13,7,8,9,3,4,5,6,0,1,2},
		{39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0},
		{4,3,2,1,0,10,9,8,7,6,5,15,14,13,12,11,21,20,19,18,17,16,26,25,24,23,22,32,31,30,29,28,27,37,36,35,34,33,43,42,41,40,39,38,48,47,46,45,44,54,53,52,51,50,49,59,58,57,56,55},
		{5,4,3,2,1,0,12,11,10,9,8,7,6,18,17,16,15,14,13,25,24,23,22,21,20,19,31,30,29,28,27,26,38,37,36,35,34,33,32,44,43,42,41,40,39,51,50,49,48,47,46,45,57,56,55,54,53,52,64,63,62,61,60,59,58,70,69,68,67,66,65,77,76,75,74,73,72,71,83,82,81,80,79,78},
		{6,5,4,3,2,1,0,14,13,12,11,10,9,8,7,21,20,19,18,17,16,15,29,28,27,26,25,24,23,22,36,35,34,33,32,31,30,44,43,42,41,40,39,38,37,51,50,49,48,47,46,45,59,58,57,56,55,54,53,52,66,65,64,63,62,61,60,74,73,72,71,70,69,68,67,81,80,79,78,77,76,75,89,88,87,86,85,84,83,82,96,95,94,93,92,91,90,104,103,102,101,100,99,98,97,111,110,109,108,107,106,105}
	};
	
	/**
	 * Constructor for the game.
	 * 
	 * @param  height The height (in boxes) of the board.
	 * @param  width The width (in boxes) of the board.
	 * @param  scored True if the game uses scored states and false otherwise.
	 * @param  nonsymmetrical True if the game uses nonsymmetrical states and false otherwise.
	 */
	public DotsAndBoxes(int height, int width, boolean scored, boolean nonsymmetrical){
		this.height = height;
		this.width = width;
		this.scored = scored;
		this.nonsymmetrical = nonsymmetrical;
		this.scored = scored;
		
		if(height != width && nonsymmetrical){
			System.out.println("Cannot remove symmetries on a rectangular board.");
			nonsymmetrical = false;
		}
		
		edges = (height * (width + 1)) + (width * (height + 1));
		initializeEdgeToBoxMaps();
	}
	
	/**
	 * Initializes the edgeBoxes and boxEdges arrays.
	 */
	private void initializeEdgeToBoxMaps(){
		edgeBoxes = new int[edges][2];
		boxEdges = new int[height * width][4];
		
		for(int i = 0; i < edgeBoxes.length; i++){
			edgeBoxes[i][0] = -1;
			edgeBoxes[i][1] = -1;
		}
		
		/* sets the edges for each square and the squares for each edge */
		for(int i = 0; i < boxEdges.length; i++){
			
			/* these are the formulas for each edge of a box*/
			int first = (((i / width) * ((2 * width) + 1)) + (i % width));
			int second = first + width;
			int third = second + 1;
			int fourth = third + width;
			
			
			int[] square = {first, second, third, fourth};
			boxEdges[i] = square;
			
			if(edgeBoxes[first][0] == -1){
				edgeBoxes[first][0] = i;
			}
			else{
				edgeBoxes[first][1] = i;
			}
			
			if(edgeBoxes[second][0] == -1){
				edgeBoxes[second][0] = i;
			}
			else{
				edgeBoxes[second][1] = i;
			}
			
			if(edgeBoxes[third][0] == -1){
				edgeBoxes[third][0] = i;
			}
			else{
				edgeBoxes[third][1] = i;
			}
			
			if(edgeBoxes[fourth][0] == -1){
				edgeBoxes[fourth][0] = i;
			}
			else{
				edgeBoxes[fourth][1] = i;
			}
		}
		
		/* remove second array position if the edge has only one box */
		for(int i = 0; i < edgeBoxes.length; i++){
			if(edgeBoxes[i][1] == -1){
				int[] box = {edgeBoxes[i][0]};
				edgeBoxes[i] = box;
			}
		}
	}
	
	/**
	 * Finds the position of an edge after transformation.
	 * 
	 * @param  edge The starting edge.
	 * @param  rotation The number of rotations to perform.
	 * @param  reflection True if the board is to be reflected.
	 * @return The starting edge after transformation.
	 */
	public int getTransformedAction(int edge, int rotation, boolean reflection){
		for(int i = 0; i < rotation; i++){
			edge = rotationMap[height - 1][edge];
		}
		
		if(reflection){
			edge = reflectionMap[height - 1][edge];
		}
		
		return edge;
	}

	/**
	 * Finds the number of completed boxes connected to an edge (assuming the edge is taken).
	 * 
	 * @param  edge The edge to check.
	 * @param  state The state of the board.
	 * @return The number of completed boxes connected to edge (0 - 2)
	 */
	public int completedBoxesForEdge(int edge, GameState state){
		
		int taken = 0;
		String s = state.getBinaryString();
		
		/* check each box attached to the edge */
		for(int i = 0; i < edgeBoxes[edge].length; i++){			
			int index = edgeBoxes[edge][i];
			taken++;
			
			/* check each edge of that box */
			for(int b = 0; b < boxEdges[index].length; b++){
				
				/* the given edge is assumed to be taken */
				if(boxEdges[index][b] == edge){
					continue;
				}
				
				if(s.length() < edges - boxEdges[index][b]){
					taken--;
					break;
				}
				
				if(s.charAt(boxEdges[index][b] - (edges - s.length())) == '0'){
					taken--;
					break;
				}
			}
		}
		
		return taken;
	}
	
	/*// updates the array of fourth edges for almost completed squares
	public void updateFourthEdges(int edge, GameState state){
		fourthEdges = new int[edges];
		
		String s = state.getBinaryString();
		
		int index = 0;
		
		for(int i = 0; i < edgeSquares[edge].length; i++){
			int count = 0;
			int b;
			int missingEdgeIndex = 0;
			int j = edgeSquares[edge][i];
			
			
			for(b = 0; b < boxEdges[j].length; b++){
				if(s.length() < edges - boxEdges[j][b]){
					count++;
				
					missingEdgeIndex = b;
					
					if(count > 1){
						break;
					}
				}
				
				else if(s.charAt(boxEdges[j][b] - (edges - s.length())) == '0'){
					count++;
					
					missingEdgeIndex = b;
					
					if(count > 1){
						break;
					}
				}
			}
			
			if(count == 1){
				fourthEdges[index] = boxEdges[j][missingEdgeIndex];
				index++;
			}
		}
		
		if(index == 0){
			fourthEdges = null;
			return;
		}
		
		int[] temp = new int[index];
		
		for(int i = 0; i < temp.length; i++){
			temp[i] = fourthEdges[i];
		}
		
		fourthEdges = temp;
	}
	*/
	
	/**
	 * Gets the possible actions for the game from a given state. Each free edge is a possible action. If the game uses nonsymmetrical states, this method returns only nonsymmetrical actions.
	 * 
	 * @param  state The state before the move is selected.
	 * @return An integer array representing all possible moves from the given state.
	 */
	public int[] getActions(GameState state) {
		if(nonsymmetrical){
			return getActionsSymmetrical(state);
		} else {
			return getAllActions(state, edges);
		}
	}
	
	/**
	 * Gets all the possible actions from the given state. Each free edge is a possible action.
	 * 
	 * @param  state The state before the move is selected.
	 * @param  edges The total number of edges on the board.
	 * @return An integer array representing all possible moves from the given state.
	 */
	public static int[] getAllActions(GameState state, int edges){
		int[] temp = new int[edges];
		int index = 0;
		
		/* all zeros are possible actions*/
		
		if(state.bigState != null){
			for(int i = 0; i < edges; i++){
				if(!state.bigState.testBit(edges - i - 1)){
					temp[index] = i;
					index++;
				}
			}
		}
		
		else {
			String binary = state.getBinaryString();
	
			/* all leading zeros are possible moves */
			
			int b = binary.length();
			for (int i = 0; i < (edges - b); i++) {
				temp[index] = i;
				index++;
			}
	
			/* check every digit */
			for (int i = 0; i < b; i++) {
				
				/* add all zero indexes to temp*/
				if (binary.charAt(i) == '0') {
					temp[index] = i + (edges - b);
					index++;
				}
			}
		}

		/* resize the array */
		
		int[] actions = new int[index];

		for (int i = 0; i < index; i++) {
			actions[i] = temp[i];
		}
		
		return actions;
	}
	
	/**
	 * Gets all the possible nonsymmetrical actions from the given state. Each free edge is a possible action. Each nonsymmetrical action leads to a nonsymmetrical state.
	 * 
	 * @param  state The state before the move is selected.
	 * @return An integer array representing all possible moves from the given state.
	 */
	public int[] getActionsSymmetrical(GameState state){
		int[] temp = new int[edges];
		GameState[] tempStates = new GameState[edges];
		GameState tempState;
		int tempStateIndex = 0;
		int index = 0;
		
		if(state.bigState != null){
			for(int i = 0; i < edges; i++){
				if(!state.bigState.testBit(edges - i - 1)){
					
					tempState = getSuccessorState(state, i);
					boolean used = false;
					
					for(int b = 0; b < tempStateIndex; b++){
						if(tempStates[b].equals(tempState)){
							used = true;
							break;
						}
					}
					
					if(!used){
						tempStates[tempStateIndex] = tempState;
						temp[index] = i;
						tempStateIndex++;
						index++;
					}
				}
			}
		}
		
		else {
			String binary = state.getBinaryString();
	
			// add extra leading zeros
			int b = binary.length();
			for (int i = 0; i < (edges - b); i++) {
				binary = "0" + binary;
			}
			
			// for every character
			for (int i = 0; i < edges; i++) {
				// if it is zero, add index to temp
				if (binary.charAt(i) == '0') {
					
					tempState = getSuccessorState(state, i);
					boolean used = false;
					
					for(int j = 0; j < tempStateIndex; j++){
						if(tempStates[j].equals(tempState)){
							used = true;
							break;
						}
					}
					
					if(!used){
						tempStates[tempStateIndex] = tempState;
						temp[index] = i;
						tempStateIndex++;
						index++;
					}
				}
			}
		}

		int[] actions = new int[index];

		// resize the array
		for (int i = 0; i < index; i++) {
			actions[i] = temp[i];
		}
		
		return actions;
	}
	
	/**
	 * Gets the successor of a given state.
	 * 
	 * @param  state The state from which a move is made.
	 * @param  action An integer representing which move is made.
	 * @return The state after the move is made.
	 */
	public GameState getSuccessorState(GameState state, int action) {

		GameState returnState = getSimpleSuccessorState(state, action);
		
		if(nonsymmetrical){
			returnState = removeSymmetries(returnState);
		}
		
		return returnState;
	}

	/**
	 * Gets the simple successor of a given state.
	 * 
	 * @param  state The state from which a move is made.
	 * @param  action An integer representing which move is made.
	 * @return The state after the move is made.
	 */
	public GameState getSimpleSuccessorState(GameState state, int action){
		GameState returnState = null;
		
		if(state.bigState != null){
			BigInteger newState = new BigInteger(state.bigState.toString());
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameState(newState);
		}
		
		else if(edges - action - 2 > 62){
			BigInteger newState = new BigInteger(Long.toString(state.longState));
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameState(newState);
		}
		
		else{
			long newState = (long) (state.longState + ((long) Math.pow(2, edges - action - 1)));
			returnState = new GameState(newState);
		}
		
		return returnState;
	}
	
	/**
	 * Gets the nonsymmetrical canonical representation of a given state.
	 * 
	 * @param  state The state to transform.
	 * @return The canonical representation of state.
	 */
	public GameState removeSymmetries(GameState state){
		
		String stateString = state.getBinaryString();
		
		/* add extra leading zeros */
		int b = stateString.length();
		for (int index = 0; index < (edges - b); index++) {
			stateString = "0" + stateString;
		}
		
		String returnState = stateString;
		
		/* three rotations */
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);

			if(first(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		/* one reflection */
		
		stateString = reflect(stateString);
		
		if(first(stateString, returnState)){
			returnState = stateString;
		}

		/* three more rotations */
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			
			if(first(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		return new GameState(returnState, true);
	}
	
	/**
	 * Gets the nonsymmetrical canonical representation of a given state.
	 * 
	 * @param  state The state to transform.
	 * @return The canonical representation of state.
	 */
	public GameStateScored removeSymmetries(GameStateScored state){
		
		String stateString = state.getBinaryString();
		
		/* add extra leading zeros */
		int b = stateString.length();
		for (int index = 0; index < (edges - b); index++) {
			stateString = "0" + stateString;
		}
		
		String returnState = stateString;
		
		/* three rotations */
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);

			if(first(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		/* one reflection */
		
		stateString = reflect(stateString);
		
		if(first(stateString, returnState)){
			returnState = stateString;
		}

		/* three more rotations */
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			
			if(first(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		return new GameStateScored(returnState, state.playerNetScore, true);
	}
	
	/**
	 * Checks which binary string is larger.
	 * 
	 * @param  firstState The first state to compare.
	 * @param  secondState The second state to compare.
	 * @return True if the first string is larger, false otherwise.
	 */
	public boolean first(String firstState, String secondState){
		int length = firstState.length();
		char c1;
		char c2;
		
		for(int i = 0; i < length; i++){
			c1 = firstState.charAt(i);
			c2 = secondState.charAt(i);
			
			if(c1 != c2){
				return c1 > c2 ? true : false;
			}
		}
		
		return false;
	}
	
	/**
	 * Transforms the binary representation of a state with a rotation.
	 * 
	 * @param  state The state to be transformed (as a String).
	 * @return The string representation of state after the rotation.
	 */
	public String rotate(String state){
		System.out.println(state);
		String newState = "";
		
		for(int i = 0; i < state.length(); i++){
			newState = newState + state.charAt(rotationMap[height - 1][i]);
		}
		
		return newState;
	}
	
	/**
	 * Transforms the binary representation of a state with a reflection.
	 * 
	 * @param  state The state to be transformed (as a String).
	 * @return The string representation of state after the reflection.
	 */
	public String reflect(String state){

		String newState = "";
		
		for(int i = 0; i < state.length(); i++){
			newState = newState + state.charAt(reflectionMap[height - 1][i]);
		}
		
		return newState;
	}
	
	/**
	 * Gets the successor of a given scored state.
	 * 
	 * @param  state The state from which a move is made.
	 * @param  action An integer representing which move is made.
	 * @return The state after the move is made.
	 */
	public GameStateScored getSuccessorState(GameStateScored state, int action){
		GameStateScored returnState = null;
		int z = completedBoxesForEdge(action, state);
		int score = state.playerNetScore;
		
		if(z > 0){
			score = score + z;
		} else {
			score = -score;
		}
		
		if(state.bigState != null){
			BigInteger newState = new BigInteger(state.bigState.toString());
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameStateScored(newState, score);
		}
		
		else if(edges - action - 2 > 62){
			BigInteger newState = new BigInteger(Long.toString(state.longState));
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameStateScored(newState, score);
		}
		
		else{
			long newState = (long) (state.longState + ((long) Math.pow(2, edges - action - 1)));
			returnState = new GameStateScored(newState, score);
		}
		
		if(nonsymmetrical){
			returnState = removeSymmetries(returnState);
		}
		
		return returnState;
	}
}
