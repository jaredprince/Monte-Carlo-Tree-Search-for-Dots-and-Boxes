package MCTS;

import java.math.BigInteger;
import java.util.Random;
//import mpi.*;

/**
 * This class runs games using the Monte Carlo tree search.
 * 
 * The amount of game-specific data in this class should be minimal.
 * Only the data that is absolutely necessary (to keep MCTree and MCNode clear of game-specific data)
 * should be used here. Wherever possible, such data should be located in the MCGame subclass and
 * used by the public methods of MCGame (getActons and getSuccessorState).
 * 
 * @author Jared Prince
 * @version 1.0
 * @since 1.0
 */

public class MonteCarloTreeSearch {

	/**
	 * Used to randomly pick actions.
	 */
	static Random r = new Random();

	/**
	 * The width (in boxes) of the board.
	 */
	static int width;

	/**
	 * The height (in boxes) of the board.
	 */
	static int height;

	/**
	 * The number of edges on the board.
	 */
	static int edges;

	/**
	 * The uncertainty constant.
	 */
	static double c;

	/**
	 * The game to use for player one.
	 */
	static DotsAndBoxes game = new DotsAndBoxes(2, 2, false, false);

	/**
	 * The game to use for player two.
	 */
	static DotsAndBoxes game2;

	/**
	 * The tree of player one.
	 */
	static MCTree tree;

	/**
	 * The tree of player two.
	 */
	static MCTree tree2;

	/**
	 * A 2D array representing the times taken for each move made by player one.
	 * Index i is an array of the total time taken by player one during turn i
	 * (in milliseconds) and the total number of times player one took turn i.
	 */
	static long times[][];
	
	/*
	 * The following constants define the behavior of the search. Variations in the MCTS algorithm are
	 * selected using these constants. All options related to the MCTS algorithm should be defined here.
	 */
	
	/**
	 * Defines the behavior in which node creation is dependent upon NODE_CREATION_COUNT.
	 */
	public static final int BEHAVIOR_EXPANSION_STANDARD = 0;
	
	/**
	 * Defines the behavior in which a node is created (if the node does not exist already), regardless of NODE_CREATION_COUNT.
	 */
	public static final int BEHAVIOR_EXPANSION_ALWAYS = 1;
	
	/**
	 * Defines the behavior in which a node is not created, regardless of NODE_CREATION_COUNT.
	 */
	public static final int BEHAVIOR_EXPANSION_NEVER = 2;
	
	/**
	 * Defines the behavior in which only a single node in a new branch is created when expanding the tree.
	 * (Unimplemented)
	 */
	public static final int BEHAVIOR_EXPANSION_SINGLE = 0;
	
	/**
	 * Defines the behavior in which multiple nodes in a new branch are created when expanding the tree.
	 * (Unimplemented)
	 */
	public static final int BEHAVIOR_EXPANSION_MULTIPLE = 1;
	
	/**
	 * Defines the behavior in which all nodes of a new branch are created when expanding the tree.
	 * (Unimplemented)
	 */
	public static final int BEHAVIOR_EXPANSION_FULL = 2;
	
	/**
	 * Defines the behavior in which unexplored nodes are selected in the order they are tested, and all are selected before 
	 * any node is selected a second time.
	 * (Unimplemented)
	 */
	public static final int BEHAVIOR_UNEXPLORED_STANDARD = 0;
	
	/**
	 * Defines the behavior in which unexplored nodes are selected using first play urgency (FPU). FPU gives unexplored nodes
	 * a constant reward value. This value can be tuned to encourage exploitation in the early game.
	 * (Unimplemented)
	 */
	public static final int BEHAVIOR_UNEXPLORED_FIRST_PLAY_URGENCY = 1;
	
	/**
	 * Defines the behaviors to be used during this search.
	 */
	static int[] behaviors = {BEHAVIOR_EXPANSION_STANDARD, BEHAVIOR_UNEXPLORED_STANDARD};
	
	/*------------------Parallel MCTS-----------------------*/
	/**
	 * The number of simulations made before sharing data between two parallel trees.
	 */
	static int shareInfoEvery;
	
	static int rank;
	static final boolean TESTPRINT= false;
	static int maxTasks=2;
	/*------------------------------------------------------*/

	/**
	 * @param args
	 *            width, height, c, matches, sims1, scored1,
	 *            sym1, opponent (1 for MCTS player, 2 for
	 *            default), parallel
	 *            
	 *            If opponent == 1:
	 *            	scored2, sym2, (sims2)
	 *            
	 *            If parallel:
	 *            	shareInfoEvery, tasks
	 */
	public static void main(String[] args) /*throws MPIException*/ {

		long s = System.currentTimeMillis();
		
		int matches = 0, sims1 = 0, sims2 = 0, opponent = 0;
		boolean scored1 = false, scored2 = false, sym1 = false, sym2 = false, parallel = false;
		
		boolean[] params = new boolean[14];
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			int index = arg.indexOf("=") + 1;

			switch (arg.substring(0, index - 1)) {

			case "width":
				width = Integer.parseInt(arg.substring(index));
				params[0] = true;
				break;
			case "height":
				height = Integer.parseInt(arg.substring(index));
				params[1] = true;
				break;
			case "c":
				c = Double.parseDouble(arg.substring(index));
				params[2] = true;
				break;
			case "matches":
				matches = Integer.parseInt(arg.substring(index));
				params[3] = true;
				break;
			case "sims1":
				sims1 = Integer.parseInt(arg.substring(index));
				params[4] = true;
				break;
			case "sims2":
				sims2 = Integer.parseInt(arg.substring(index));
				params[5] = true;
				break;
			case "scored1":
				scored1 = Boolean.parseBoolean(arg.substring(index));
				params[6] = true;
				break;
			case "scored2":
				scored2 = Boolean.parseBoolean(arg.substring(index));
				params[7] = true;
				break;
			case "opponent":
				opponent = Integer.parseInt(arg.substring(index));
				params[8] = true;
				break;
			case "sym1":
				sym1 = Boolean.parseBoolean(arg.substring(index));
				params[9] = true;
				break;
			case "sym2":
				sym2 = Boolean.parseBoolean(arg.substring(index));
				params[10] = true;
				break;
			case "parallel":
				parallel = Boolean.parseBoolean(arg.substring(index));
				params[11] = true;
				break;
			case "shareInfoEvery":
				shareInfoEvery = Integer.parseInt(arg.substring(index));
				params[12] = true;
				break;
			
			case "tasks":
				maxTasks = Integer.parseInt(arg.substring(index));
				params[13] = true;
				break;
			}
		}
		
		boolean missingParams = false;

		if (!params[0]) {
			System.out.println("Missing Parameter: height");
			missingParams = true;
		}
		if (!params[1]) {
			System.out.println("Missing Parameter: width");
			missingParams = true;
		}
		if (!params[2]) {
			System.out.println("Missing Parameter: c");
			missingParams = true;
		}
		if (!params[3]) {
			System.out.println("Missing Parameter: matches");
			missingParams = true;
		}
		if (!params[4]) {
			System.out.println("Missing Parameter: sims1");
			missingParams = true;
		}
		if (!params[5]) {
			sims2 = sims1;
		}
		
		if (!params[6]) {
			System.out.println("Missing Parameter: scored1");
			missingParams = true;
		}
		
		if (!params[9]) {
			System.out.println("Missing Parameter: sym1");
			missingParams = true;
		}
		
		if(!params[8]){
			System.out.println("Missing Parameter: opponent");
			missingParams = true;
		} else if(opponent == 1) {
			if(!params[7]){
				System.out.println("Missing Parameter: scored2");
				missingParams = true;
			}
			if(!params[10]){
				System.out.println("Missing Parameter: sym2");
				missingParams = true;
			}
		} else if(opponent != 2){
			System.out.println("Invalid Parameter: opponent");
			missingParams = true;
		}
		
		if(!params[11]){
			System.out.println("Missing Parameter: parallel");
			missingParams = true;
		} else {
			if(parallel){
				if(!params[12]){
					System.out.println("Missing Parameter: shareInfoEvery");
				}
				if(!params[13]){
					System.out.println("Missing Parameter: tasks");
				}
			}
		}
		
		if(missingParams){
			return;
		}
		
		/* All parameters present and valid - Game can begin */
		
		edges = (height * (width + 1)) + (width * (height + 1));
		times = new long[edges][2];
		game = new DotsAndBoxes(height, width, scored1, sym1);
		
		if(parallel){
//			game2 = new DotsAndBoxes(height, width, scored1, sym1);
//			
//			if(maxTasks>1){
//				MPI.Init(args);
//				rank = MPI.COMM_WORLD.getRank();
//				competitionParallel(tree, game, tree2, game2, sims1/maxTasks, sims2/maxTasks, matches);
//				MPI.Finalize();
//			}
//			else{
//				rank=-1;
//				competitionParallel(tree, game, tree2, game2, sims1, sims2, matches);
//			}
		} else {
			if(opponent == 1){
				game2 = new DotsAndBoxes(height, width, scored2, sym2);
				
				tree = game.scored ? new MCTree(game, new GameStateScored(0, 0)) : new MCTree(game, new GameState(0));
				tree2 = game2.scored ? new MCTree(game2, new GameStateScored(0, 0)) : new MCTree(game2, new GameState(0));
				
				MCPlayer p1 = new MCPlayer(game, tree);
				MCPlayer p2 = new MCPlayer(game2, tree2);
				
				competition(p1, p2, sims1, sims2, matches);
			} else {
//				competition(tree, game, null, null, sims1, sims2, matches);
			}
		}

		System.out.println(System.currentTimeMillis() - s);

	}
	
	/**
	 * 
	 * @param state The current state of the game. 
	 * @param controllerNetScore The net score for the player currently in control.
	 * @return True if the player in control wins, false otherwise.
	 */
	public boolean endgame(GameState state, int controllerNetScore){
		
		
		
		return true;
	}

	/**
	 * Plays a number of games between two MCTS players.
	 * 
	 * @param tree
	 *            The tree for player one.
	 * @param game
	 *            The game for player one.
	 * @param tree2
	 *            The tree for player two.
	 * @param game2
	 *            The game for player two.
	 * @param simulationsPerTurn1
	 *            The number of simulations given to player one.
	 * @param simulationsPerTurn2
	 *            The number of simulations given to player two.
	 * @param matches
	 *            The number of games to be played.
	 */
	public static void competition(MCPlayer p1, MCPlayer p2,
			int simulationsPerTurn1, int simulationsPerTurn2, int matches) /*throws MPIException*/ {
		int wins = 0;
		int losses = 0;
		int draws = 0;

		double totalAveDepth = 0;
		long totalNodes = 0;

		/* plays a match */
		for (int i = matches; i > 0; i--) {
			double[] results = match(p1, p2, simulationsPerTurn1, simulationsPerTurn2, false);
			int result = (int) results[0];
			totalAveDepth += results[1];
			totalNodes += results[2];

			if (result == 1)
				wins++;
			else if (result == 0) {
				draws++;
			} else {
				losses++;
			}
		}

		/* Results */
		System.out.println(height + "x" + width + " c=" + c + " matches=" + matches + " sims=" + simulationsPerTurn1
				+ "," + simulationsPerTurn2 + " p1=" + (game.scored ? "sc+" : "nsc+")
				+ (game.asymmetrical ? "s" : "ns") + " p2=" + (game2.scored ? "sc+" : "nsc+")
				+ (game2.asymmetrical ? "s" : "ns") + " w=" + wins + " l=" + losses + " d=" + draws);
		System.out.println("Average nodes: " + totalNodes / matches);
		System.out.println("average depth: " + (totalAveDepth / matches) + "\nAverage Time: ");

		for (int i = 0; i < times.length; i++) {
			if (times[i][1] == 0) {
				continue;
			}

			System.out.println("Move " + i + ": " + times[i][0] / times[i][1]);
		}
	}

	/**
	 * Plays a single game between two MCTS players.
	 * 
	 * @param simulationsPerTurn1
	 *            The number of simulations given to player one.
	 * @param simulationsPerTurn2
	 *            The number of simulations given to player two.
	 * @param parallel True if the tree is parallelized.
	 * @return An array of the form {result, average depth of the final tree for
	 *         player one, number of nodes in the final tree for player one}.
	 */
	public static double[] match(MCPlayer p1, MCPlayer p2, int simulationsPerTurn1, int simulationsPerTurn2, boolean parallel) /*throws MPIException*/ {

		int result = -10;

		/*
		 * This is used as a backup to resolve flawed tests caused by
		 * ArrayIndexOutOfBounds or NullPointer errors during the game. When
		 * these errors occur, they return a result of -10, and the game is
		 * restarted.
		 */
		while (result == -10) {
			if(parallel){
//				result = testGameParallel(tree, game, tree2, game2, simulationsPerTurn1, simulationsPerTurn2);
			}
			else
				result = testGame(p1, p2, simulationsPerTurn1, simulationsPerTurn2);
		}

		double results[] = new double[3];
		results[0] = result;
		results[1] = (double) p1.tree.totalDepth / p1.tree.numNodes;
		results[2] = p1.tree.numNodes;

		return results;
	}

	/**
	 * Plays a single game between two MCTS players.
	 * 
	 * @param simulationsPerTurn1
	 *            The number of simulations given to player one.
	 * @param simulationsPerTurn2
	 *            The number of simulations given to player two.
	 * @return An integer representing the result for player one.
	 */
	public static int testGame(MCPlayer p1, MCPlayer p2,
			int simulationsPerTurn1, int simulationsPerTurn2) {

		// the game variables
		int action = 0;
		
		boolean playerOneTurn = true;
		int p1Score = 0;
		int p2Score = 0;
		
		//the number of boxes that are completed or have two edges
		int twoOrFour = 0;
		
		//board[i] is the number of taken edges for box i
		int[] board = new int[width * height];
		
		//a clone to pass to the simulate method
		int[] boardClone = new int[width * height];
		
		// for every turn
		while (!p1.isTerminal()) {
			
			if (p1Score > (width * width) / 2 || p2Score > (width * width) / 2) {
				break;
			}

			int sims = playerOneTurn ? simulationsPerTurn1 : simulationsPerTurn2;

			// get the action based on the current player
			if (playerOneTurn) {
				
				long start = System.currentTimeMillis();

				// perform the simulations for this move
				while (sims > 0) {
					// give player one's game, tree, node, and score
					simulate(p1, p1Score - p2Score, boardClone, twoOrFour);
					sims--;
				}

				long end = System.currentTimeMillis();
				times[p1.currentNode.depth][1]++;
				times[p1.currentNode.depth][0] = times[p1.currentNode.depth][0] + (end - start);
				
				action = p1.getAction();
			} else {
				
				// perform the simulations for this move
				while (sims > 0) {
					// give player two's game, tree, node, and score
					simulate(p2, p2Score - p1Score, boardClone, twoOrFour);
					sims--;
				}

				action = p2.getAction();
			}
			
			// get the points for this move
			int taken = 0;
			
			// increment the edges for each box which adjoins action
			for(int i = 0; i < game.edgeBoxes[action].length; i++){
				board[game.edgeBoxes[action][i]]++;
				boardClone[game.edgeBoxes[action][i]]++;
				
				if(board[game.edgeBoxes[action][i]] == 4){
					taken++;
					twoOrFour++;
				} else if (board[game.edgeBoxes[action][i]] == 2){
					twoOrFour++;
				}
			}
			
			//if both players are symmetrical or both are asymmetrical, the same moves are possible for each
			if(game.asymmetrical == game2.asymmetrical){
				//each player should play the given action
				p1.play(action, BEHAVIOR_EXPANSION_ALWAYS);
				p2.play(action, BEHAVIOR_EXPANSION_ALWAYS);
			}
			
//			else if(playerOneTurn){
//				if(!game.asymmetrical){
//					currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
//					currentNode2 = currentNode2.getNode(game.removeSymmetries(currentNode.state), BEHAVIOR_EXPANSION_ALWAYS);
//				}
//				
//				else {
//					//get the transformation from player one to player two
//					int rotation = game2.getRotation(currentNode2.state, currentNode.state);
//					int newAction = 0;
//					
//					//get the action which will make player one's (canon) board match player two's
//					switch(rotation){
//						case 1: newAction = game2.getTransformedAction(action, 1, false); break;
//						case 2: newAction = game2.getTransformedAction(action, 2, false); break;
//						case 3: newAction = game2.getTransformedAction(action, 3, false); break;
//						case 4: newAction = game2.getTransformedAction(action, 0, true); break;
//						case 5: newAction = game2.getTransformedAction(action, 1, true); break;
//						case 6: newAction = game2.getTransformedAction(action, 2, true); break;
//						case 7: newAction = game2.getTransformedAction(action, 3, true); break;
//						default: newAction = action;
//					}
//					
//					currentNode2 = currentNode2.getNode(newAction, BEHAVIOR_EXPANSION_ALWAYS);
//					currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
//				}
//			}
//			
//			else {
//				if(!game2.asymmetrical){
//					currentNode2 = currentNode2.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
//					currentNode = currentNode.getNode(game2.removeSymmetries(currentNode2.state), BEHAVIOR_EXPANSION_ALWAYS);
//				}
//				
//				else {
//					//get the transformation from player one to player two
//					int rotation = game.getRotation(currentNode.state, currentNode2.state);
//					int newAction = 0;
//					
//					//get the action which will make player one's (canon) board match player two's
//					switch(rotation){
//						case 1: newAction = game.getTransformedAction(action, 1, false); break;
//						case 2: newAction = game.getTransformedAction(action, 2, false); break;
//						case 3: newAction = game.getTransformedAction(action, 3, false); break;
//						case 4: newAction = game.getTransformedAction(action, 0, true); break;
//						case 5: newAction = game.getTransformedAction(action, 1, true); break;
//						case 6: newAction = game.getTransformedAction(action, 2, true); break;
//						case 7: newAction = game.getTransformedAction(action, 3, true); break;
//						default: newAction = action;
//					}
//					
//					currentNode = currentNode.getNode(newAction, BEHAVIOR_EXPANSION_ALWAYS);
//					currentNode2 = currentNode2.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
//				}
//			}

			if (playerOneTurn) {
				p1Score += taken;
			} else {
				p2Score += taken;
			}

			playerOneTurn = taken > 0 ? playerOneTurn : !playerOneTurn;
		}

		int p1Net = p1Score - p2Score;

		return p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;
	}

	/**
	 * Updates the nodes played in a game. This is the backpropogation stage of
	 * the simulation.
	 * 
	 * @param nodes
	 *            An array of all nodes traversed during the game.
	 * @param player
	 *            An array with turns played by player one represented as true
	 *            and turns played by player two represented as false.
	 * @param actions
	 *            An array of all the actions played during the selection
	 *            portion of the game.
	 * @param result
	 *            An integer representing the result for player one (-1 for a
	 *            loss, 0 for a tie, and 1 for a win).
	 */
	public static void backup(MCNode[] nodes, boolean[] player, int[] actions, int result) {
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] == null) {
				break;
			}

			/* switch result if this was player two's move */
			if (!player[i]) {
				result = -result;
			}

			/* add a win, loss, or tie, to the node given the action taken */
			nodes[i].addValue(actions[i], result, c);

			if (!player[i]) {
				result = -result;
			}
		}
	}

	/**
	 * Plays the game from a given point off the tree with a random default
	 * policy. This is the playout stage of simulation.
	 * 
	 * @param playerOne
	 *            True if player one is to move, false otherwise.
	 * @param p1Net
	 *            The starting net score for player one.
	 * @return An integer representing the result for player one (-1 for a loss,
	 *         0 for a tie, and 1 for a win).
	 */
	public static int simulateDefault(MCPlayer player, boolean playerOne, int p1Net) {

		/* play until the terminalState */
		while (!player.isTerminalTemp()) {

			//get and play a default action
			int action = player.playDefaultAction();
			
			int taken = game.completedBoxesForEdge(action, player.state);

			if (taken > 0) {
				p1Net += playerOne ? taken : -taken;
			}

			else {
				playerOne = !playerOne;
			}
		}

		p1Net = p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;

		return p1Net;
	}

	/**
	 * Runs a single simulation and updates the tree accordingly. The majority
	 * of this method constitutes the selection stage of simulation.
	 * @param player 
	 * 
	 * @param p1Net
	 *            The starting net score for player one.
	 * @param terminalState
	 *            The state at which simulation will cease.
	 * @param board An array representing the number of edges taken for each box.
	 * @param twoOrFour The number of boxes which have either 2 or 4 edges.
	 */
	public static void simulate(MCPlayer player, int p1Net, int[] board, int twoOrFour) {
		boolean playerOne = true;

		int action = 0;
		boolean[] turns = new boolean[edges];
		int[] actionsTaken = new int[edges + 1];

		//change nodes
		player.tempNode = player.currentNode;
		
		/* keep track of the traversed nodes */
		MCNode[] playedNodes = new MCNode[edges];

		playedNodes[0] = player.tempNode;

		/* plays each move until game over or off the tree */
		for (int i = 0; (!player.isOffTreeTemp() && !player.isTerminalTemp()); i++) {

			turns[i] = playerOne ? true : false;

			/* make a move */
			action = player.getActionTemp();
			player.playTemp(action, BEHAVIOR_EXPANSION_ALWAYS);

			actionsTaken[i] = action;

			/* if someone has more than half the squares, quit early */
			if (p1Net > (height * width) / 2 || p1Net < (-height * width) / 2) {
				break;
			}

			int taken = 0;
			
			// increment the edges for each box which adjoins action
			for(int b = 0; b < game.edgeBoxes[action].length; b++){
				board[game.edgeBoxes[action][b]]++;
				
				if(board[game.edgeBoxes[action][b]] == 4){
					taken++;
					twoOrFour++;
				} else if(board[game.edgeBoxes[action][b]] == 2){
					twoOrFour++;
				}
			}

			/* doesn't add the terminal node */
			if (!player.isTerminalTemp()) {
				playedNodes[i + 1] = player.tempNode;
			}

			if (taken > 0) {
				p1Net += playerOne ? taken : -taken;
			}

			else {
				playerOne = !playerOne;
			}

		}

		int z; /* the result */

		/* playout if not at terminal state */
		if (!player.isTerminalTemp() && player.isOffTreeTemp()) {
			z = simulateDefault(player, playerOne, p1Net);
		}

		else {
			z = p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;
		}

		/* backup the nodes */
		backup(playedNodes, turns, actionsTaken, z);
	}

	/**
	 * Gets a random action from a given state.
	 * 
	 * @param state
	 *            The state from which to select an action.
	 * @return An integer representing the action selected.
	 */
	public static int randomPolicy(GameState state) {
		int[] actions = DotsAndBoxes.getAllActions(state, edges);

		int next = r.nextInt(actions.length);

		return actions[next];
	}

	/**
	 * Plays a single game using the tree developed for player one.
	 * 
	 * @param random
	 *            True if random moves should be made during player two's turn.
	 *            False if both players should make moves from the same tree.
	 * @return True if player one wins the game, false otherwise.
	 */
	public static boolean testPolicy(boolean random) {
		int p1Net = 0;
		GameState state = new GameState(0);
		int action = 0;
		boolean playerOne = true;

		MCNode currentNode = tree.root;

		/* for every move in the game */
		for (int i = 0; i < edges; i++) {

			/* for a random player or when off the tree */
			if ((random && !playerOne) || currentNode == null) {

				action = randomPolicy(state);

				if (currentNode != null) {
					currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_STANDARD);
				}
			}

			else {
				/* get the next node, given c */
				action = currentNode.getNextAction(c);
				currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_STANDARD);
			}

			if (currentNode != null) {
				state = currentNode.state;
			}

			else {
				state = game.getSuccessorState(state, action);
			}

			int taken = game.completedBoxesForEdge(action, state);

			if (taken > 0) {
				p1Net += playerOne ? taken : -taken;
			}

			else {
				playerOne = !playerOne;
			}
		}

		if (p1Net > 0) {
			return true;
		}

		return false;
	}
	
	/*-----------------------------------Parallel MCTS----------------------------------------------*/
//	
//	public static MCNode doStuff(MCNode currNode) throws MPIException {
//		int currNodeNumActions= DotsAndBoxes.getAllActions(currNode.state, edges).length;
//		MCNode toReturn = currNode;
//		
//		//arrays for each compute node
//		int[] nCompute;
//		double[] rCompute;
//		double[] rToRecieve;
//		int[] nToRecieve;
//		//double[] wCompute;
//		//arrays for the master node
//		double[] rToBcast;
//		int[] nToGather;
//		double [] rToGather;
//		
//		double[] rSum;
//		int[] nSum;
//		
//		//arrays for each compute node
//		nCompute= currNode.getTimesActionChosen();
//		rCompute= currNode.getRewards();
//		//wCompute= new double[currNodeNumActions];
//		rToRecieve= new double[currNodeNumActions];
//		nToRecieve= new int[currNodeNumActions];
//		//arrays for the master node
//		
//		//long arrays to store info from each compute node
//		nToGather= new int[(currNodeNumActions*maxTasks)];//(currNodeNumActions *maxTasks)];
//		rToGather = new double[(currNodeNumActions*maxTasks)];//(currNodeNumActions * maxTasks)];
//		
//		//these will be broadcasted by the master node to the compute nodes
//		rSum= new double[currNodeNumActions];
//		nSum= new int[currNodeNumActions];
//		
//		//qToBcast= fillWithDummy(qToBcast, -99);
//		//nToGather= fillWithDummy(nToGather, -99);
//		//wToGather= fillWithDummy(wToGather, -99);
//		//wSum= fillWithDummy(wSum, -99);
//		//nSum= fillWithDummy(nSum, -99);
//		
//		
//		
//		if(TESTPRINT){
//			if(rank==0){
//				System.out.println("number of actions to be gathered "+nToGather.length);
//			}
//
//			//print information from each non-master node
//			//	if(rank!=0){
//			System.out.println("rank "+rank+" BEFORE GATHER");
//			System.out.println("rank " + rank + " number of possible actions "+currNodeNumActions);
//			//System.out.print ("rank " + rank + " times action chosen BEFORE ");
//			printArr("rank " + rank + " times action chosen BEFORE ", nCompute);
//			//System.out.print ("rank " + rank + " BEFORE child averages ");
//			printArr("rank " + rank + " BEFORE rewards ",rCompute);
//		}
//		
//	//	}
//		
//		
////		try{
//			MPI.COMM_WORLD.gather(nCompute, currNodeNumActions, MPI.INT, nToGather, currNodeNumActions, MPI.INT, 0);
//			
//			MPI.COMM_WORLD.barrier();
//			if(TESTPRINT){
//				System.out.println (" after barrier rank = " + rank);
//			}
////		}
////		catch(Exception e){
//			// display the rank, the exception message (if any) and the
//			// stacktrace
////			System.out.println("crashed on first gather");
////			System.out.println(" rank " + rank + " " + e.getMessage());
////			e.printStackTrace();
//
//		// abort processing
////			MPI.COMM_WORLD.abort(1);
////		}
////		try{
//			MPI.COMM_WORLD.gather(rCompute, currNodeNumActions, MPI.DOUBLE, rToGather, currNodeNumActions, MPI.DOUBLE, 0);
////		}
////		catch(Exception e){
////			System.out.println("crashed on second gather");
////			System.out.println(" rank " + rank + " " + e.getMessage());
////			e.printStackTrace();
//			
////			MPI.COMM_WORLD.abort(1);
////		}
//		if(rank==0 ){
//			nSum= sumWithin(nToGather, currNodeNumActions);
//			rSum= sumWithin(rToGather, currNodeNumActions);
//			
//			if(TESTPRINT){
//				System.out.println("rank "+rank+" AFTER GATHER BEFORE BCAST ");
//				//System.out.print("rank "+rank+" nSum AFTER ");
//				printArr("rank "+rank+" nSum AFTER ", nSum);
//				//System.out.print("rank "+rank+" wSum  AFTER ");
//				printArr("rank "+rank+" wSum  AFTER ",rSum);
//			}
//			
//			MPI.COMM_WORLD.bcast(rSum, currNodeNumActions, MPI.DOUBLE, 0);
//			MPI.COMM_WORLD.bcast(nSum, currNodeNumActions, MPI.INT, 0);
//		}
//		if(rank!= 0){
//			
//			MPI.COMM_WORLD.bcast(rToRecieve, currNodeNumActions, MPI.DOUBLE, 0);
//			MPI.COMM_WORLD.bcast(nToRecieve, currNodeNumActions, MPI.INT, 0);
//			toReturn.setRewards(rToRecieve);
//			toReturn.setTimesActionChosen(nToRecieve);
//			
//			if(TESTPRINT){
//				System.out.print("rank "+rank+" AFTER BCAST");
//				//System.out.print("rank "+rank+" what q was recieved: AFTER");
//				printArr("rank "+rank+" what q was recieved: AFTER",rToRecieve);
//				//System.out.print("rank "+rank+" what n was received: AFTER");
//				printArr("rank "+rank+" what n was received: AFTER",nToRecieve);
//				//System.out.print("rank "+rank+" current node's child averages:AFTER ");
//				printArr("rank "+rank+" current node's rewards:AFTER ", toReturn.getRewards());
//				//System.out.print("rank "+rank+" current node's TAC: ");
//				printArr("rank "+rank+" current node's TAC: ",toReturn.getTimesActionChosen());
//			}
//		}
//		return toReturn;
//		
//	}
//	
//	//prints the contents of the array
//	public static void printArr(String res,int[] arr){
//		for(int i=0; i<arr.length; i++){
//			res= res+ arr[i]+" ";
//		}
//		System.out.println(res);
//	}
//	public static void printArr(String res, double[] arr){
//		for(int i=0; i<arr.length; i++){
//			res= res+ arr[i]+" ";
//			//System.out.print(arr[i]+" ");
//		}
//		System.out.println(res);
//	}
//	//arrays must be the same length, multipies them together
//	public static double[] arrayMultiply(int[] arr1, double[] arr2){
//		double[] product = new double[arr1.length];
//		for(int i=0; i<arr1.length; i++){
//			product[i]=(arr1[i]*arr2[i]);
//		}
//		return product;
//	}
//
//	//kind of sums an array
//	public static double[] sumWithin(double[] arr, int num){
//		double[] sum= new double[num];
//		for(int i=0; i< arr.length; i++ ){
//			sum[i%num] += arr[i];
//		}
//
//		return sum;
//	}
//
//	public static int[] sumWithin(int[] arr, int num){
//		int[] sum= new int[num];
//		for(int i=0; i< arr.length; i++ ){
//			sum[i%num] += arr[i];
//		}
//
//		return sum;
//	}
//
//	//divides two arrays by index
//	public static double[] arrayDivide(int[] arr1, double[] arr2){
//		double[] result = new double[arr1.length];
//		for(int i=0; i<arr1.length; i++){
//			result[i]=(arr2[i]/arr1[i]);
//		}
//		return result;
//	}
//
//	public static void printAveTime(String res, long[][] arr){
//		String printStr="";
//		printStr+=res;
//		
//		for(int i = 0; i < arr.length; i++){
//			if(arr[i][1] == 0){
//				continue;
//			}
//			
//			printStr+=" Move " + i + ": " + arr[i][0] / (double)arr[i][1];
//		}
//		System.out.println(printStr);
//	}
//	
//	public static void printNumTime(String res, long[][] arr){
//		String printStr="";
//		
//		printStr+=res;
//		for(int i=0; i<times.length; i++){
//			printStr+=" Move "+i+": "+arr[i][1];
//		}
//		System.out.println(printStr);
//	}
//		
//	
//	/**
//	 * Plays a single game between two MCTS players.
//	 * 
//	 * @param  tree The tree for player one.
//	 * @param  game The game for player one.
//	 * @param  tree2 The tree for player two.
//	 * @param  game2 The game for player two.
//	 * @param  simulationsPerTurn1 The number of simulations given to player one.
//	 * @param  simulationsPerTurn2 The number of simulations given to player two.
//	 * @return An integer representing the result for player one.
//	 */
//	public static int testGameParallel(MCTree tree, DotsAndBoxes game, MCTree tree2, DotsAndBoxes game2, int simulationsPerTurn1, int simulationsPerTurn2) throws MPIException {
//		
//		GameState terminalState = null;
//		
//		if(edges > 60){
//			terminalState = new GameState(new BigInteger("2").pow(edges).subtract(new BigInteger("1")));
//		} 
//		else{
//			terminalState = new GameState((long) Math.pow(2, edges) - 1);
//		}
//		
//		//the current node of each tree
//		MCNode currentNode = tree.root;
//		MCNode currentNode2 = tree2.root;
//		
//		//the game variables
//		int action = 0;
//		boolean playerOneTurn = true;
//		int p1Score = 0;
//		int p2Score = 0;
//
//		//the number of boxes that are completed or have two edges
//		int twoOrFour = 0;
//		
//		//board[i] is the number of taken edges for box i
//		int[] board = new int[width * height];
//		
//		//a clone to pass to the simulate method
//		int[] boardClone = new int[width * height];
//		
//		//for every turn
//		while(!currentNode.state.equals(terminalState)){
//			int i=0;
//			if(p1Score > (width*width) / 2 || p2Score > (width*width) / 2){
//				break;
//			}
//			
//			int sims = playerOneTurn ? simulationsPerTurn1 : simulationsPerTurn2;
//			
//			//get the action based on the current player
//			if(playerOneTurn){
//				long start = System.nanoTime();
//				
//				//perform the simulations for this move
//				while(sims > 0){
//					//give player one's game, tree, node, and score
//					simulate(currentNode.state, p1Score - p2Score, currentNode, terminalState, tree, game, boardClone, twoOrFour);
//					if(maxTasks>1){
//						if(sims< simulationsPerTurn1 && sims%shareInfoEvery ==0 ){
//							try{
//								doStuff(currentNode);
//							}
//							catch(Exception e){
//								// display the rank, the exception message (if any) and the
//								// stacktrace
//
//								System.out.println(" rank " + rank + " crashed " + e.getMessage());
//								e.printStackTrace();
//
//								// abort processing
//								//MPI.COMM_WORLD.abort(1);
//							}
//
//						}	
//					}
//
//					
//					
//					sims--;
//				}
//				
//				long end = System.nanoTime();
//				
//				try{
//					times[currentNode.depth][1]++;
//					times[currentNode.depth][0] = times[currentNode.depth][0] + (end - start);
//				} catch (ArrayIndexOutOfBoundsException e) {
//					System.out.println("Array Index Error");
//					return -10;
//				}
//				
//				action = currentNode.getNextAction(0);
//			} else {
//				//perform the simulations for this move
//				while(sims > 0){
//					//give player two's game, tree, node, and score
//					simulate(currentNode2.state, p2Score - p1Score, currentNode2, terminalState, tree2, game2, boardClone, twoOrFour);
//					sims--;
//				}
//				
//				action = currentNode2.getNextAction(0);
//			}
//
//			// get the points for this move
//			int taken = 0;
//			
//			// increment the edges for each box which adjoins action
//			for(int b = 0; b < game.edgeBoxes[action].length; b++){
//				board[game.edgeBoxes[action][b]]++;
//				boardClone[game.edgeBoxes[action][b]]++;
//				
//				if(board[game.edgeBoxes[action][b]] == 4){
//					taken++;
//					twoOrFour++;
//				} else if (board[game.edgeBoxes[action][b]] == 2){
//					twoOrFour++;
//				}
//			}
//			
//			if(maxTasks>1){
//				if(rank==0){
//
//					int[] tempAction= {action};
//					MPI.COMM_WORLD.bcast(tempAction, 1, MPI.INT, 0);
//				}
//				if(rank!=0){
//					//this is overriding the action for each compute node with the action selected by the master node
//					int[] tempActionCompute= new int[1];
//					MPI.COMM_WORLD.bcast(tempActionCompute, 1, MPI.INT, 0);
//					action= tempActionCompute[0];
//				}
//			}
//			
//			
//			if(TESTPRINT){
//				//get the point for this move
//				System.out.println ("rank " + rank + " about to determine score with action state " + action + " " + currentNode.state.longState);
//			}
//			
//			if(TESTPRINT){
//				System.out.println ("rank " + rank + " after " + i + " moves taken = "  + taken);
//			}
//			if(TESTPRINT){
//				System.out.println("rank "+rank+" action: "+action);
//				System.out.println("rank "+rank+" prev-state "+currentNode.state.longState);
//			}
//			//update the currentNodes
//			currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
//			currentNode2 = currentNode2.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
//			
//			/*possibly circumvent the null pointer*/
//			if(currentNode == null || currentNode2 == null){
//				System.out.println("Null Error: " + currentNode == null ? "Player 1" : "Player 2");
//				return -10;
//			}
//			
//			if(playerOneTurn){
//				p1Score += taken;
//			} else {
//				p2Score += taken;
//			}
//			if(TESTPRINT){
//				System.out.println("rank "+rank+" post-state "+currentNode.state.longState);
//				System.out.println (" rank = " + rank + " score after " + i + "moves  is " + p1Score);
//			}
//			playerOneTurn = taken > 0 ? playerOneTurn : !playerOneTurn;
//			i++;
//		}
//		
//		int p1Net = p1Score - p2Score;
//		
//		if(TESTPRINT){
//			System.out.println (" rank = " + rank + " is done after all moves  and netscore " + p1Net);
//		}
//		return p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;
//	}
//	
//	
//	public static void competitionParallel(MCTree tree, DotsAndBoxes game, MCTree tree2, DotsAndBoxes game2, int simulationsPerTurn1, int simulationsPerTurn2, int matches) throws MPIException{
//		
//		int wins = 0;
//		int losses = 0;
//		int draws = 0;
//		
//		double totalAveDepth = 0;
//		long totalNodes = 0;
//		
//		
//		/* plays a match */
//		for(int i = matches; i > 0; i--){
//			double[] results = match(tree, game, tree2, game2, simulationsPerTurn1, simulationsPerTurn2, true);
//			int result = (int) results[0];
//			totalAveDepth += results[1];
//			totalNodes += results[2];
//			
//			if(result == 1)
//				wins++;
//			else if(result == 0){
//				draws++;
//			} else {
//				losses++;
//			}
//		}
//		
//		/* Results */
//		System.out.println(height + "x" + width + " c=" + c + " matches=" + matches + " sims=" + simulationsPerTurn1 + "," + simulationsPerTurn2 + " p1=" + (game.scored ? "sc+" : "nsc+") + (game.asymmetrical ? "s" : "ns") + " p2=" + (game2.scored ? "sc+" : "nsc+") + (game2.asymmetrical ? "s" : "ns") + " w=" + wins + " l=" + losses + " d=" + draws);
//		System.out.println("nodes: " + totalNodes / matches);
//		System.out.println("average depth: " + (totalAveDepth / matches));
//		
//		printAveTime("Average Times RANK "+rank, times);
//		printNumTime("Number of Times Chosen "+rank, times);
//	}
	
	/*----------------------------------------------------------------------------------------------*/
}