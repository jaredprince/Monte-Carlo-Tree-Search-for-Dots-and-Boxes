package MCTS;

import java.math.BigInteger;
import java.util.Random;

/**
 * This class runs games using the Monte Carlo tree search.
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

	/**
	 * @param args
	 *            width, height, c, matches, simulations, p1_scored,
	 *            p1_nonsymmetrical, opponent (1 for MCTS player, 2 for
	 *            default), p2_scored, p2_nonsymmetrical, p2_simulations
	 *            (Optional)
	 */
	public static void main(String[] args) {

		long s = System.currentTimeMillis();

		width = Integer.parseInt(args[0]);
		height = Integer.parseInt(args[1]);

		edges = (height * (width + 1)) + (width * (height + 1));

		times = new long[edges][2];

		int sims1 = Integer.parseInt(args[4]);
		int sims2;

		if (args.length == 11) {
			sims2 = Integer.parseInt(args[10]);
		} else {
			sims2 = sims1;
		}

		c = Double.parseDouble(args[2]);
		int matches = Integer.parseInt(args[3]);

		game = new DotsAndBoxes(height, width, Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]));

		if (Integer.parseInt(args[7]) == 1) {
			game2 = new DotsAndBoxes(height, width, Boolean.parseBoolean(args[8]), Boolean.parseBoolean(args[9]));
			competition(tree, game, tree2, game2, sims1, sims2, matches);
		} else {
			competition(tree, game, null, null, sims1, sims2, matches);
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
			double[] results = match(tree, game, tree2, game2, simulationsPerTurn1, simulationsPerTurn2);
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
			int simulationsPerTurn1, int simulationsPerTurn2) {

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

			// function to rotate unsym board to canon and return rotation and
			// reversal

			// update the currentNodes
			currentNode = currentNode.getNode(action, MCNode.BEHAVIOR_CREATE);
			currentNode2 = currentNode2.getNode(action, MCNode.BEHAVIOR_CREATE);

			/* possibly circumvent the null pointer */
			if (currentNode == null || currentNode2 == null) {
				System.out.println("Null Error: " + currentNode == null ? "Player 1" : "Player 2");
				return -10;
			}

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
			currentNode = currentNode.getNode(action, MCNode.BEHAVIOR_STANDARD);

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

	/*
	 * // returns a state by taking a square if possible public static int
	 * boxTakingPolicy(GameState state, DotsAndBoxes game){ int[] actions =
	 * game.fourthEdges;
	 * 
	 * if(actions == null){ return randomPolicy(state); }
	 * 
	 * int next = r.nextInt(actions.length);
	 * 
	 * return actions[next]; }
	 */

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
					currentNode = currentNode.getNode(action, MCNode.BEHAVIOR_STANDARD);
				}
			}

			else {
				/* get the next node, given c */
				action = currentNode.getNextAction(c);
				currentNode = currentNode.getNode(action, MCNode.BEHAVIOR_STANDARD);
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
}
