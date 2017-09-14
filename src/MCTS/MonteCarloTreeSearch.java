package MCTS;

import java.math.BigInteger;
import java.util.Random;
//import mpi.*;

/**
 * This class runs games using the Monte Carlo tree search.
 * 
 * The amount of game-specific data in this class should be minimal.
 * Only the data that is absolutely necessary (to keep MCTree and MCNode clear of game-specific data)
 * should be used here. Wherever possible, the such data should be located in the MCGame subclass and
 * used by the public methods of MCGame (getActons and getSuccessorState).
 * 
 * @author Jared Prince
 * @version 1.0
 * @since 1.0
 */

public class MonteCarloTreeSearch {

	static int nullErrors = 0;
	
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
	static final int MAXTASKS=2;
	/*------------------------------------------------------*/

	/**
	 * @param args
	 *            width, height, c, matches, simulations, p1_scored,
	 *            p1_nonsymmetrical, opponent (1 for MCTS player, 2 for
	 *            default), p2_scored, p2_nonsymmetrical, p2_simulations
	 *            (Optional)
	 */
	public static void main(String[] args) {

		
		long s = System.currentTimeMillis();
		
		int matches = 0, sims1 = 0, sims2 = 0, opponent = 0;
		boolean scored1 = false, scored2 = false, sym1 = false, sym2 = false, parallel = false;
		
		boolean[] params = new boolean[13];
		
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
			game2 = new DotsAndBoxes(height, width, scored1, sym1);
			
//			if(MAXTASKS>1){
//				MPI.Init(args);
//				rank = MPI.COMM_WORLD.getRank();
//				competition(tree, game, tree2, game2, sims1/MAXTASKS, sims2/MAXTASKS, matches);
//				MPI.Finalize();
//			}
//			else{
//				rank=-1;
//				competition(tree, game, tree2, game2, sims1, sims2, matches);
//			}
		} else {
			if(opponent == 1){
				game2 = new DotsAndBoxes(height, width, scored2, sym2);
				competition(tree, game, tree2, game2, sims1, sims2, matches);
			} else {
				competition(tree, game, null, null, sims1, sims2, matches);
			}
		}

		System.out.println(System.currentTimeMillis() - s);

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
	public static void competition(MCTree tree, DotsAndBoxes game, MCTree tree2, DotsAndBoxes game2,
			int simulationsPerTurn1, int simulationsPerTurn2, int matches) {

		int wins = 0;
		int losses = 0;
		int draws = 0;

		double totalAveDepth = 0;
		long totalNodes = 0;

		/* plays a match */
		for (int i = matches; i > 0; i--) {
			double[] results = match(tree, game, tree2, game2, simulationsPerTurn1, simulationsPerTurn2, false);
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
				+ (game.nonsymmetrical ? "s" : "ns") + " p2=" + (game2.scored ? "sc+" : "nsc+")
				+ (game2.nonsymmetrical ? "s" : "ns") + " w=" + wins + " l=" + losses + " d=" + draws);
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
	 * @return An array of the form {result, average depth of the final tree for
	 *         player one, number of nodes in the final tree for player one}.
	 */
	public static double[] match(MCTree tree, DotsAndBoxes game, MCTree tree2, DotsAndBoxes game2,
			int simulationsPerTurn1, int simulationsPerTurn2, boolean parallel) {

		tree = game.scored ? new MCTree(game, new GameStateScored(0, 0)) : new MCTree(game, new GameState(0));
		tree2 = game2.scored ? new MCTree(game2, new GameStateScored(0, 0)) : new MCTree(game2, new GameState(0));

		int result = -10;

		/*
		 * This is used as a backup to resolve flawed tests caused by
		 * ArrayIndexOutOfBounds or NullPointer errors during the game. When
		 * these errors occur, they return a result of -10, and the game is
		 * restarted.
		 */
		while (result == -10) {
			if(parallel);
//				result = testGameParallel(tree, game, tree2, game2, simulationsPerTurn1, simulationsPerTurn2);
			else
				result = testGame(tree, game, tree2, game2, simulationsPerTurn1, simulationsPerTurn2);
		}

		double results[] = new double[3];
		results[0] = result;
		results[1] = (double) tree.totalDepth / tree.numNodes;
		results[2] = tree.numNodes;

		return results;
	}

	/**
	 * Plays a single game between two MCTS players.
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
	 * @return An integer representing the result for player one.
	 */
	public static int testGame(MCTree tree, DotsAndBoxes game, MCTree tree2, DotsAndBoxes game2,
			int simulationsPerTurn1, int simulationsPerTurn2) {

		GameState terminalState = null;

		if (edges > 60) {
			terminalState = new GameState(new BigInteger("2").pow(edges).subtract(new BigInteger("1")));
		} else {
			terminalState = new GameState((long) Math.pow(2, edges) - 1);
		}

		// the current node of each tree
		MCNode currentNode = tree.root;
		MCNode currentNode2 = tree2.root;

		// the game variables
		int action = 0;
		boolean playerOneTurn = true;
		int p1Score = 0;
		int p2Score = 0;

		// for every turn
		while (!currentNode.state.equals(terminalState)) {

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
					simulate(currentNode.state, p1Score - p2Score, currentNode, terminalState, tree, game);
					sims--;
				}

				long end = System.currentTimeMillis();

				try {
					times[currentNode.depth][1]++;
					times[currentNode.depth][0] = times[currentNode.depth][0] + (end - start);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Array Index Error");
					return -10;
				}

				action = currentNode.getNextAction(0);
			} else {
				// perform the simulations for this move
				while (sims > 0) {
					// give player two's game, tree, node, and score
					simulate(currentNode2.state, p2Score - p1Score, currentNode2, terminalState, tree2, game2);
					sims--;
				}

				action = currentNode2.getNextAction(0);
			}

			// get the point for this move
			int taken = game.completedBoxesForEdge(action, currentNode.state);

			
			
			
			//if both players are symmetrical or both are nonsymmetrical, the same moves are possible for each
			if(game.nonsymmetrical == game2.nonsymmetrical){
				// update the currentNodes
				currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
				currentNode2 = currentNode2.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
			}
			
			//if the player in control is nonsymmetrical, translate
			else if (playerOneTurn && game.nonsymmetrical) {
				// update the currentNodes
				currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
				currentNode2 = currentNode2.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
			} else if (!playerOneTurn && game2.nonsymmetrical) {
				// update the currentNodes
				currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
				currentNode2 = currentNode2.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
			}
			
			//if the player in control is symmetrical, the moves must be translated to a symmetrical one
			else {
				
				//get the next node for the symmetrical player in control
				if(playerOneTurn) {
					currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
					currentNode2 = currentNode2.getNode(currentNode.state, BEHAVIOR_EXPANSION_ALWAYS);
				} else {
					currentNode2 = currentNode2.getNode(action, BEHAVIOR_EXPANSION_ALWAYS);
					currentNode = currentNode.getNode(currentNode2.state, BEHAVIOR_EXPANSION_ALWAYS);
				}
			}
			
			/* possibly circumvent the null pointer */
			if (currentNode == null || currentNode2 == null) {
				System.out.println("Null Error: " + (currentNode == null ? "Player 1" : "Player 2"));
				
				nullErrors++;
				
				return -10;
			}
			
			if(!game.removeSymmetries(currentNode.state).equals(game2.removeSymmetries(currentNode2.state))){
				System.out.println("Move Error: " + (playerOneTurn ? "Player 1" : "Player 2"));
				return -10;
			}
			
//			System.out.println(currentNode.state.getString() + " - " + currentNode.state.getBinaryString());
//			System.out.println(currentNode2.state.getString() + " - " + currentNode2.state.getBinaryString() + "\n");

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
	 * @param state
	 *            The starting state.
	 * @param playerOne
	 *            True if player one is to move, false otherwise.
	 * @param p1Net
	 *            The starting net score for player one.
	 * @param terminalState
	 *            The state at which simulation will cease.
	 * @return An integer representing the result for player one (-1 for a loss,
	 *         0 for a tie, and 1 for a win).
	 */
	public static int simulateDefault(GameState state, boolean playerOne, int p1Net, GameState terminalState) {

		/* play until the terminalState */

		for (int i = 0; i < edges; i++) {

			int action = randomPolicy(state);
			state = game.getSimpleSuccessorState(state, action);

			int taken = game.completedBoxesForEdge(action, state);

			if (taken > 0) {
				p1Net += playerOne ? taken : -taken;
			}

			else {
				playerOne = !playerOne;
			}

			if (state.equals(terminalState)) {
				break;
			}
		}

		p1Net = p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;

		return p1Net;
	}

	/**
	 * Runs a single simulation and updates the tree accordingly. The majority
	 * of this method constitutes the selection stage of simulation.
	 * 
	 * @param state
	 *            The starting state.
	 * @param p1Net
	 *            The starting net score for player one.
	 * @param pastNode
	 *            A node representing the current position on the tree.
	 * @param terminalState
	 *            The state at which simulation will cease.
	 * @param tree
	 *            The tree to be used and updated. This tree should belong to
	 *            the player running the simulation.
	 * @param game
	 *            The game to be used. This game should belong to the player
	 *            running the simulation.
	 */
	public static void simulate(GameState state, int p1Net, MCNode pastNode, GameState terminalState, MCTree tree,
			DotsAndBoxes game) {
		boolean playerOne = true;

		int action = 0;
		boolean[] turns = new boolean[edges];
		int[] actionsTaken = new int[edges + 1];

		/* keep track of the traversed nodes */
		MCNode[] playedNodes = new MCNode[edges];
		MCNode currentNode = pastNode;

		playedNodes[0] = currentNode;

		/* plays each move until game over or off the tree */
		for (int i = 0; !state.equals(terminalState); i++) {

			turns[i] = playerOne ? true : false;

			/* make a move */
			action = currentNode.getNextAction(c);
			currentNode = currentNode.getNode(action, BEHAVIOR_EXPANSION_STANDARD);

			actionsTaken[i] = action;

			/* if someone has more than half the squares, quit early */
			if (p1Net > (height * width) / 2 || p1Net < (-height * width) / 2) {
				state = terminalState;
				break;
			}

			int taken = game.completedBoxesForEdge(action, state);

			if (currentNode != null) {
				state = currentNode.state;
			}

			else {
				/*
				 * this turns a scored state to unscored, but since it just
				 * feeds into simulateDefault, it doesn't matter
				 */
				state = game.getSuccessorState(state, action);
			}

			/* doesn't add the terminal node */
			if (!state.equals(terminalState)) {
				playedNodes[i + 1] = currentNode;
			}

			if (taken > 0) {
				p1Net += playerOne ? taken : -taken;
			}

			else {
				playerOne = !playerOne;
			}

			if (currentNode == null) {
				break;
			}
		}

		int z; /* the result */

		/* playout if not at terminal state */
		if (!state.equals(terminalState)) {
			z = simulateDefault(state, playerOne, p1Net, terminalState);
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
	
//	public static MCNode doStuff(MCNode currNode) throws MPIException{
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
//		nToGather= new int[(currNodeNumActions*MAXTASKS)];//(currNodeNumActions *MAXTASKS)];
//		rToGather = new double[(currNodeNumActions*MAXTASKS)];//(currNodeNumActions * MAXTASKS)];
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
//	public static int testGameParallel(MCTree tree, DotsAndBoxes game, MCTree tree2, DotsAndBoxes game2, int simulationsPerTurn1, int simulationsPerTurn2) throws MPIException{
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
//					simulate(currentNode.state, p1Score - p2Score, currentNode, terminalState, tree, game);
//					if(MAXTASKS>1){
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
//					simulate(currentNode2.state, p2Score - p1Score, currentNode2, terminalState, tree2, game2);
//					sims--;
//				}
//				
//				action = currentNode2.getNextAction(0);
//			}
//			
//			if(MAXTASKS>1){
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
//			int taken = game.completedBoxesForEdge(action, currentNode.state);
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
//		System.out.println(height + "x" + width + " c=" + c + " matches=" + matches + " sims=" + simulationsPerTurn1 + "," + simulationsPerTurn2 + " p1=" + (game.scored ? "sc+" : "nsc+") + (game.nonsymmetrical ? "s" : "ns") + " p2=" + (game2.scored ? "sc+" : "nsc+") + (game2.nonsymmetrical ? "s" : "ns") + " w=" + wins + " l=" + losses + " d=" + draws);
//		System.out.println("nodes: " + totalNodes / matches);
//		System.out.println("average depth: " + (totalAveDepth / matches));
//		
//		printAveTime("Average Times RANK "+rank, times);
//		printNumTime("Number of Times Chosen "+rank, times);
//	}
	
	
	/*----------------------------------------------------------------------------------------------*/
}
