package MCTS;

import java.math.BigInteger;

/**
 * A Dots and Boxes MCTSGame.
 * @author      Jared Prince
 * @version     1.0
 * @since       1.0
 */

public class DotsAndBoxes extends MCGame{
	
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
	
	/** A 2D array which maps each edge (in square boards of size 1 - 9) to its position after a single rotation of the board.
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
		{14,29,44,59,74,89,104,6,21,36,51,66,81,96,111,13,28,43,58,73,88,103,5,20,35,50,65,80,95,110,12,27,42,57,72,87,102,4,19,34,49,64,79,94,109,11,26,41,56,71,86,101,3,18,33,48,63,78,93,108,10,25,40,55,70,85,100,2,17,32,47,62,77,92,107,9,24,39,54,69,84,99,1,16,31,46,61,76,91,106,8,23,38,53,68,83,98,0,15,30,45,60,75,90,105,7,22,37,52,67,82,97},
		{16,33,50,67,84,101,118,135,7,24,41,58,75,92,109,126,143,15,32,49,66,83,100,117,134,6,23,40,57,74,91,108,125,142,14,31,48,65,82,99,116,133,5,22,39,56,73,90,107,124,141,13,30,47,64,81,98,115,132,4,21,38,55,72,89,106,123,140,12,29,46,63,80,97,114,131,3,20,37,54,71,88,105,122,139,11,28,45,62,79,96,113,130,2,19,36,53,70,87,104,121,138,10,27,44,61,78,95,112,129,1,18,35,52,69,86,103,120,137,9,26,43,60,77,94,111,128,0,17,34,51,68,85,102,119,136,8,25,42,59,76,93,110,127},
		{18,37,56,75,94,113,132,151,170,8,27,46,65,84,103,122,141,160,179,17,36,55,74,93,112,131,150,169,7,26,45,64,83,102,121,140,159,178,16,35,54,73,92,111,130,149,168,6,25,44,63,82,101,120,139,158,177,15,34,53,72,91,110,129,148,167,5,24,43,62,81,100,119,138,157,176,14,33,52,71,90,109,128,147,166,4,23,42,61,80,99,118,137,156,175,13,32,51,70,89,108,127,146,165,3,22,41,60,79,98,117,136,155,174,12,31,50,69,88,107,126,145,164,2,21,40,59,78,97,116,135,154,173,11,30,49,68,87,106,125,144,163,1,20,39,58,77,96,115,134,153,172,10,29,48,67,86,105,124,143,162,0,19,38,57,76,95,114,133,152,171,9,28,47,66,85,104,123,142,161,}
	};
	
	
	/** A 2D array which maps each edge (in square boards of size 1 - 9) to its position after a reflection of the board.
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
		{6,5,4,3,2,1,0,14,13,12,11,10,9,8,7,21,20,19,18,17,16,15,29,28,27,26,25,24,23,22,36,35,34,33,32,31,30,44,43,42,41,40,39,38,37,51,50,49,48,47,46,45,59,58,57,56,55,54,53,52,66,65,64,63,62,61,60,74,73,72,71,70,69,68,67,81,80,79,78,77,76,75,89,88,87,86,85,84,83,82,96,95,94,93,92,91,90,104,103,102,101,100,99,98,97,111,110,109,108,107,106,105},
		{7,6,5,4,3,2,1,0,16,15,14,13,12,11,10,9,8,24,23,22,21,20,19,18,17,33,32,31,30,29,28,27,26,25,41,40,39,38,37,36,35,34,50,49,48,47,46,45,44,43,42,58,57,56,55,54,53,52,51,67,66,65,64,63,62,61,60,59,75,74,73,72,71,70,69,68,84,83,82,81,80,79,78,77,76,92,91,90,89,88,87,86,85,101,100,99,98,97,96,95,94,93,109,108,107,106,105,104,103,102,118,117,116,115,114,113,112,111,110,126,125,124,123,122,121,120,119,135,134,133,132,131,130,129,128,127,143,142,141,140,139,138,137,136},
		{8,7,6,5,4,3,2,1,0,18,17,16,15,14,13,12,11,10,9,27,26,25,24,23,22,21,20,19,37,36,35,34,33,32,31,30,29,28,46,45,44,43,42,41,40,39,38,56,55,54,53,52,51,50,49,48,47,65,64,63,62,61,60,59,58,57,75,74,73,72,71,70,69,68,67,66,84,83,82,81,80,79,78,77,76,94,93,92,91,90,89,88,87,86,85,103,102,101,100,99,98,97,96,95,113,112,111,110,109,108,107,106,105,104,122,121,120,119,118,117,116,115,114,132,131,130,129,128,127,126,125,124,123,141,140,139,138,137,136,135,134,133,151,150,149,148,147,146,145,144,143,142,160,159,158,157,156,155,154,153,152,170,169,168,167,166,165,164,163,162,161,179,178,177,176,175,174,173,172,171}
	};
	
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
		this.scored = scored;
		
		if(height != width && asymmetrical){
			System.out.println("Cannot remove symmetries on a rectangular board.");
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
			edge = rotationMap[height - 1][edge];
		}
		
		if(reflection){
			edge = reflectionMap[height - 1][edge];
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
			return boxes[0] == 1 && boxes[1] == 1 ? 2 : boxes[0] == 1 || boxes[1] == 1 ? 1 : 0;
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
	
	public int[][] stateToBoard(GameState state){
		int[][] board = new int[width][height];
		
		for(int i = 0; i < board.length; i++){
			for(int b = 0; b < board[0].length; b++){
				
			}
		}
		
		return board;
	}
	
	//doesn't yet distinguish loops from chains
	public int[][] getChainsAndLoops(GameState state){
		int[][] chainsAndLoops = new int[2][];
		int[] chains = new int[(width * height) / 2];
		int[] loops = new int[(width * height) / 2];
		
		int board[][] = stateToBoard(state);
		
		int cIndex = 0;
		int lIndex = 0;
		
		boolean[][] visited = new boolean[width][height];
		
		//get chains
		for(int i = 0; i < board.length; i++){
			if(i == 0 || i == board.length - 1){
				for(int j = 0; j < board[0].length; j++){
					chains[cIndex] = measureChain(board, visited, i, j);
				}
			}
			
			else {
				chains[cIndex] = measureChain(board, visited, i, 0);
				
				if(chains[cIndex] != 0)
					cIndex++;
				
				chains[cIndex] = measureChain(board, visited, i, board[0].length - 1);
				
				if(chains[cIndex] != 0)
					cIndex++;
			}
		}
		
		for(int i = 1; i < board.length - 1; i++){
			for(int j = 1; j < board[0].length - 1; j++){
				loops[lIndex] = measureChain(board, visited, i, j);
			}
		}
		
		chainsAndLoops[0] = chains;
		chainsAndLoops[1] = loops;
		
		return chainsAndLoops;
	}
	
	public int measureChain(int[][] board, boolean[][] visited, int i, int j){
		/*orientation of free edges: 
		 * 1 = left, top
		 * 2 = left, right
		 * 3 = left, bottom
		 * 4 = top, right
		 * 5 = top, bottom
		 * 6 = right, bottom
		 */
		
		//this prevents recounting
		if(visited[i][j]){
			return 0;
		}
		
		//length starts at 1 because of this box
		int length = 1;
		visited[i][j] = true;
		int orientation = board[i][j];
		
		if((orientation == 1 || orientation == 2 || orientation == 3) && i > 0){
			length += measureChain(board, visited, i - 1, j);
		}
		
		if((orientation == 1 || orientation == 4 || orientation == 5) && j > 0){
			length += measureChain(board, visited, i, j - 1);
		}
		
		if((orientation == 2 || orientation == 4 || orientation == 6) && i < board.length - 1){
			length += measureChain(board, visited, i + 1, j);
		}
		
		if((orientation == 3 || orientation == 5 || orientation == 6) && j > board[0].length - 1){
			length += measureChain(board, visited, i, j + 1);
		}
		
		return length;		
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
	 * Gets the asymmetrical canonical representation of a given state.
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
		
		else if(edges - action - 2 > 62){
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
}
