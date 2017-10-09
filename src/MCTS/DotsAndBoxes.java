package MCTS;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * A Dots and Boxes MCTSGame.
 * @author      Jared Prince
 * @version     1.0
 * @since       1.0
 * 
 * 9/26/17:
 * Added methods to get a rotation or reflection map for a given board size.
 * Added a method to turn a state into a 2D array representing the state of each box in the board.
 * Added methods to measure chains, loops, and intersections and return arrays of loops and chains.
 * Added a method to give the number of taken edges for each box.
 */

public class DotsAndBoxes extends MCGame {
	
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
	public boolean asymmetrical;
	
	/** A 2D array which lists the box(es) which adjoin a given edge.
	 *  Position i contains an array of 1 or two integers representing the boxes to which edge i belongs
	 */
	public int[][] edgeBoxes;
	
	/** A 2D array which lists the edges which form a given box.
	 *  Position i contains an array of 4 integers representing the edges which form box i.
	 */
	public int[][] boxEdges;
	
	/** An array which maps each edge on the board to another edge after rotation.
	 *  Position i represents the number of the edge that edge i will move to after a 90 degree rotation.
	 */
	public static int[] rotationMap;
	
	
	/** An array which maps each edge on the board to another edge after reflection.
	 *  Position i represents the number of the edge that edge i will move to after a reflection across the y axis.
	 */
	public static int[] reflectionMap;
	
	/**
	 * Creates an array representing a map of edges to edges when rotating the board 90 degrees.
	 * This works only on square boards.
	 * 
	 * @param width The width of the board.
	 * @return The map.
	 */
	public static int[] getRotationMap(int width){
		int[] map = new int[width * (width+1) * 2];
		
		int gap = (2*width + 1);
	
		//set the edges on the top, 0 - (width - 1)
		for(int i = 0; i < width; i++){
			map[i] = (i+1) * (2 * width) + i;
		}
		
		//set the edges in every other (horizontal) row
		for(int i = 1; i <= width; i++){
			
			//set each edge in the row to the value of the above edge - 1
			for(int b = 0; b < width; b++){
				map[b + (gap * i)] = map[b + (gap * (i - 1))] - 1;
			}
		}
		
		//set the edges in the first interior row
		for(int i = width; i <= 2 * width; i++){
			map[i] = (width - 1) + (2*width + 1) * (i - width);
		}
		
		//set the edges in the other vertical rows
		for(int i = 1; i < width; i++){
			for(int b = 0; b <= width; b++){
				map[b + (width + (i * 2 * width) + i)] = map[b + (width + ((i - 1) * 2 * width) + (i - 1))] - 1;
			}
		}
		
		/*
		System.out.print("{");
		for(int i = 0; i < map.length; i++){
			System.out.print(map[i] + ",");
		}
		System.out.print("}");
		*/
		
		return map;
	}
	
	/**
	 * Creates an array representing a map of edges to edges when reflecting the board.
	 * This works only on square boards.
	 * 
	 * @param width The width of the board.
	 * @return The map.
	 */
	public static int[] getReflectionMap(int width){
		int[] map = new int[width * (width+1) * 2];
		
		int gap = (2*width + 1);
		
		for(int c = 0; c < width + 1; c++){
			int start = gap * c;
			for(int i = 0; i < width; i++){
				map[i + start] = start + (width - i - 1);
			}
		}
		
		for(int c = 0; c < width; c++){
			int start = (width + (c * 2 * width) + c);
			for(int i = 0; i < width + 1; i++){
				map[i + (width + (c * 2 * width) + c)] = start + (width - i);
			}
		}
		
		/*
		System.out.print("{");
		for(int i = 0; i < map.length; i++){
			System.out.print(map[i] + ",");
		}
		System.out.print("}");
		*/
		
		return map;
	}
	
	/**
	 * Constructor for the game.
	 * 
	 * @param  height The height (in boxes) of the board.
	 * @param  width The width (in boxes) of the board.
	 * @param  scored True if the game uses scored states and false otherwise.
	 * @param  asymmetrical True if the game uses asymmetrical states and false otherwise.
	 */
	public DotsAndBoxes(int height, int width, boolean scored, boolean asymmetrical){
		this.height = height;
		this.width = width;
		this.scored = scored;
		this.asymmetrical = asymmetrical;
		
		rotationMap = getRotationMap(width);
		reflectionMap = getReflectionMap(width);
		
		if(height != width && asymmetrical){
			System.out.println("Symmetries can only be removed on a square board.");
			asymmetrical = false;
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
			edge = rotationMap[edge];
		}
		
		if(reflection){
			edge = reflectionMap[edge];
		}
		
		return edge;
	}

	/**
	 * Finds the number of boxes connected to the given edge which are complete (assuming the edge is taken).
	 * 
	 * @param  edge The edge to check.
	 * @param  state The state of the board.
	 * @return The number of boxes connected to edge with n edges (0 - 2)
	 */
	public int completedBoxesForEdge(int edge, GameState state){
		int[] boxes = boxPerEdge(edge, state);
		
		if(boxes.length == 1){
			return boxes[0] == 4 ? 1 : 0;
		} else {
			return boxes[0] + boxes[1];
		}
	}
	
	/**
	 * Finds the number of edges in each box attached to the given edge (assuming the edge is taken).
	 * 
	 * @param edge The edge taken.
	 * @param state The state of the board.
	 * @return An array of integers representing the number of edges taken for each of the boxes.
	 */
	public int[] boxPerEdge(int edge, GameState state){
		int[] boxes = new int[edgeBoxes[edge].length];
		
		String s = state.getBinaryString();
		
		/* check each box attached to the edge */
		for(int i = 0; i < edgeBoxes[edge].length; i++){			
			int index = edgeBoxes[edge][i];
			
			/* check each edge of that box */
			for(int b = 0; b < boxEdges[index].length; b++){
				
				/* the given edge is assumed to be taken */
				if(boxEdges[index][b] == edge){
					boxes[i]++;
					continue;
				}
				
				//edge not found
				if(s.length() < edges - boxEdges[index][b]){
					break;
				}
				
				if(s.charAt(boxEdges[index][b] - (edges - s.length())) == '1'){
					boxes[i]++;
					break;
				}
			}
		}
		
		return boxes;
	}
	
	/**
	 * Creates a 2D array which represents the edges of each box for a given state.
	 * 
	 * @param state The state of the board.
	 * @return A 2D array representing the edges of each box.
	 */
	public int[][] stateToBoard(GameState state){		
		String str = state.getBinaryString();
		int[] boxes = new int[width * height];
		
		int totalEdges = edgeBoxes.length;
		int length = str.length();
		
		//add extra leading zeros
		for(int i = 0; i < totalEdges - length; i++){
			str = '0' + str;
		}
		
		//get the orientation of the box as an integer
		for(int i = 0; i < boxes.length; i++){
			int[] edges = boxEdges[i];
			
			boxes[i] = Integer.parseInt("" + str.charAt(edges[0]) + str.charAt(edges[1]) + str.charAt(edges[2]) + str.charAt(edges[3]), 2);
		}
		
		int[][] board = new int[width][height];
		int index = 0;
		
		//change boxes into a 2D array board
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				board[j][i] = boxes[index];
				index++;
			}
		}
		
		return board;
	}
	
	/**
	 * Finds the number and length of all loops and chains on the board.
	 * 
	 * @param state The state of the board.
	 * @param width The width of the board (in boxes).
	 * @param height The height of the board (in boxes).
	 * @return A 2D array or all the chains and loops on a board.
	 */
	public int[][] getChainsAndLoops(GameState state, int width, int height){
		int[][] chainsAndLoops = new int[2][];
		int[] chains = new int[(width * height) / 2];
		int[] loops = new int[(width * height) / 4];
		
		int board[][] = stateToBoard(state);
		
		int cIndex = 0;
		int lIndex = 0;
		
		boolean[][] visited = new boolean[width][height];
		int length = 0;
		
		//check every square for intersections
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				//if it's unvisited, measure the chain
				if(!visited[i][j]){
					int orientation = board[i][j];
					
					//this is an intersection
					if((orientation == 0 || orientation == 1 || orientation == 2 || orientation == 4 || orientation == 8)){
						int[] intersection = measureIntersection(board, visited, i, j, 0);
						
						Arrays.sort(intersection);
						
						for(int b = 0; b < intersection.length - 2; b++){
							chains[cIndex] = intersection[b];
							cIndex++;
						}
						
						//combine the last two chains
						chains[cIndex] = intersection[intersection.length - 1] + intersection[intersection.length - 2] + 1;
						cIndex++;
					}
				}
			}
		}
		
		//check every square
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				
				//if it's unvisited, measure the chain
				if(!visited[i][j]){
					
					length = measureChain(board, visited, i, j, 0, false);
					
					if(length < 0){
						loops[lIndex] = -length;
						lIndex++;
					} else {
						chains[cIndex] = length;
						cIndex++;
					}
				}
			}
		}
		
		chainsAndLoops[0] = chains;
		chainsAndLoops[1] = loops;
		
		return chainsAndLoops;
	}
	
	public int[] measureIntersection(int[][] board, boolean[][] visited, int i, int j, int depth){
		int[] result;
		int index = 0;
		
		int orientation = board[i][j];
		
		visited[i][j] = true;
		
		//this is a 3-way intersection
		if(orientation == 1 || orientation == 2 || orientation == 4 || orientation == 8){
			result = new int[3];
		} else {
			result = new int[4];
		}
		
		//left
		if((orientation == 0 || orientation == 1 || orientation == 2 || orientation == 8) && i > 0){
			result[index] = measureChain(board, visited, i - 1, j, 0, true);
			index++;
		}
		
		//top
		if((orientation == 0 || orientation == 1 || orientation == 2 || orientation == 4) && j > 0){
			result[index] = measureChain(board, visited, i, j - 1, 0, true);
			index++;
		}
		
		//right
		if((orientation == 0 || orientation == 1 || orientation == 4 || orientation == 8) && i < board.length - 1){
			result[index] = measureChain(board, visited, i + 1, j, 0, true);
			index++;
		}
		
		//bottom
		if((orientation == 0 || orientation == 2 || orientation == 4 || orientation == 8) && j < board[0].length - 1){
			result[index] = measureChain(board, visited, i, j + 1, 0, true);
			index++;
		}
		
		return result;
	}
	
	/**
	 * Measures the length of the chain or loop of which the given box is a part.
	 * Assumes the box is a part of a chain or loop.
	 * 
	 * @param board A 2D array representing the edges in each box on the board. @see stateToBoard
	 * @param visited A 2D array representing whether each box on the board has been counted already.
	 * @param i The column index of the box.
	 * @param j The row index of the box.
	 * @param depth The depth in the search. Should be called with '0'.
	 * @param intersection True if the given chain is part of an intersection.
	 * @return The length of the chain or loop of which the box is a part. Negative values signify the box is part of a loop.
	 */
	public int measureChain(int[][] board, boolean[][] visited, int i, int j, int depth, boolean intersection){
		
		//this prevents recounting
		if(visited[i][j]){
			return 0;
		}
		
		int orientation = board[i][j];
		
		//don't measure finished squares
		if(orientation == 15){
			return 0;
		}
		
		//stop if this is an intersection
		if((orientation == 0 || orientation == 1 || orientation == 2 || orientation == 4 || orientation == 8)){
			return 0;
		}
		
		visited[i][j] = true;
		
		int[] lengths = new int[2];
		int index = 0;
		
		//left
		if((orientation == 3 || orientation == 9 || orientation == 10) && i > 0){
			lengths[index] += measureChain(board, visited, i - 1, j, depth + 1, intersection);
			index++;
		}
		
		//top
		if((orientation == 3 || orientation == 5 || orientation == 6) && j > 0){
			lengths[index] += measureChain(board, visited, i, j - 1, depth + 1, intersection);
			index++;
		}
		
		//right
		if((orientation == 5 || orientation == 9 || orientation == 12) && i < board.length - 1){
			lengths[index] += measureChain(board, visited, i + 1, j, depth + 1, intersection);
			index++;
		}
		
		//bottom
		if((orientation == 6 || orientation == 10 || orientation == 12) && j < board[0].length - 1){
			lengths[index] += measureChain(board, visited, i, j + 1, depth + 1, intersection);
			index++;
		}
		
		//if this is the first level and two sides were checked and one of the
		//sides was already visited, return a negative to signify a loop
		//does not happen when this is an intersection chain
		if(depth == 0 && index == 2 && (lengths[1] == 0 || lengths[0] == 0) && !intersection){
			return -(lengths[0] + 1);
		}
		
		return lengths[0] + lengths[1] + 1;		
	}
	
	/**
	 * Gets the possible actions for the game from a given state. Each free edge is a possible action. If the game uses asymmetrical states, this method returns only asymmetrical actions.
	 * 
	 * @param  state The state before the move is selected.
	 * @return An integer array representing all possible moves from the given state.
	 */
	public int[] getActions(GameState state) {
		if(asymmetrical){
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
	 * Gets all the possible asymmetrical actions from the given state. Each free edge is a possible action. Each asymmetrical action leads to a asymmetrical state.
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

		GameState returnState;
		
		if(state instanceof GameStateScored){
			returnState = getScoredSuccessorState((GameStateScored) state, action);
		} else {
			returnState = getSimpleSuccessorState(state, action);
		}
		
		if(asymmetrical){
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
		
		else if(edges - action - 2 > 61){
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
	 * Gets the asymmetrical canonical representation of a given state.
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

			if(!binaryMax(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		/* one reflection */
		
		stateString = reflect(stateString);
		
		if(!binaryMax(stateString, returnState)){
			returnState = stateString;
		}

		/* three more rotations */
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			
			if(!binaryMax(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		return new GameState(returnState, true);
	}
	
	/**
	 * Gets the asymmetrical canonical representation of a given state.
	 * The canonical representation is the one whose integer value is smallest.
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

			if(!binaryMax(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		/* one reflection */
		
		stateString = reflect(stateString);
		
		if(!binaryMax(stateString, returnState)){
			returnState = stateString;
		}

		/* three more rotations */
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			
			if(!binaryMax(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		return new GameStateScored(returnState, state.playerNetScore, true);
	}
	
	/**
	 * Checks if the first binary string is larger than the second.
	 * 
	 * @param  firstState The first state to compare.
	 * @param  secondState The second state to compare.
	 * @return True if the first string is larger, false otherwise.
	 */
	public boolean binaryMax(String firstState, String secondState){
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
		String newState = "";
		
		for(int i = 0; i < state.length(); i++){
			newState = newState + state.charAt(rotationMap[i]);
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
			newState = newState + state.charAt(reflectionMap[i]);
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
	public GameStateScored getScoredSuccessorState(GameStateScored state, int action){
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
		
		else if(edges - action - 2 > 60){
			BigInteger newState = new BigInteger(Long.toString(state.longState));
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameStateScored(newState, score);
		}
		
		else{
			long newState = (long) (state.longState + ((long) Math.pow(2, edges - action - 1)));
			returnState = new GameStateScored(newState, score);
		}
		
		return returnState;
	}
	
	/**
	 * Returns a value representing the number of clockwise rotations needed to reach state2 from state1.
	 * If the states are equal, the value returned is 0.
	 * If the states are not symmetrical, the value returned is -1.
	 * Values 1-3 represent rotations only.
	 * Value 4 represents only a reflection.
	 * Values 5-7 represent a reflection and 1-3 rotations.
	 * 
	 * @param state1 The state to translate.
	 * @param state2 The goal state.
	 * @return The rotation value.
	 */
	public int getRotation(GameState state1, GameState state2){
		
		String s1 = state1.getBinaryString();
		String s2 = state2.getBinaryString();
		
		// add extra leading zeros for state 1
		int b = s1.length();
		for (int i = 0; i < (edges - b); i++) {
			s1 = "0" + s1;
		}
		
		// add extra leading zeros for state 2
		b = s2.length();
		for (int i = 0; i < (edges - b); i++) {
			s2 = "0" + s2;
		}
		
		//the states are equal
		if(s1.equals(s2)){
			return 0;
		}
		
		//rotate 3 times
		for(int i = 1; i < 4; i++){
			s1 = rotate(s1);
			
			if(s1.equals(s2)){
				return i;
			}
		}
		
		//reflect
		s1 = rotate(s1);
		s1 = reflect(s1);
		
		if(s1.equals(s2)){
			return 4;
		}
		
		//rotate 3 times
		for(int i = 5; i < 8; i++){
			s1 = rotate(s1);
			
			if(s1.equals(s2)){
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * Checks whether the given game is compatible with this one.
	 * Returns true if width, height, and asymmetrical values are
	 * equal. If symmetrical vs. asymmetrical games are ever fixed,
	 * the asymmetrical values will not need to be equal.
	 * 
	 * @param game The game with which to compare.
	 * @return True if the games are compatible, false otherwise.
	 * @see MCTS.MCGame#compatible(MCTS.MCGame)
	 */
	public boolean compatible(MCGame game) {
		if(game instanceof DotsAndBoxes){
			DotsAndBoxes game2 = (DotsAndBoxes) game;
			if(this.height == game2.height && this.width == game2.width && this.asymmetrical == game2.asymmetrical){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Checks if the given game is equivalent to this one.
	 * Returns true only if width, height, asymmetrical, and scored 
	 * values are equal for both games.
	 * 
	 * @param game The game with which to compare.
	 * @return True if the games are equal, false otherwise.
	 * @see MCTS.MCGame#equals(MCTS.MCGame)
	 */
	public boolean equals(MCGame game) {
		if(game instanceof DotsAndBoxes){
			DotsAndBoxes game2 = (DotsAndBoxes) game;
			if(this.height == game2.height && this.width == game2.width){
				if(this.asymmetrical == game2.asymmetrical && this.scored == game2.scored){
					return true;
				}
			}
		}
		
		return false;
	}
}
