package MCTS;

import java.util.Hashtable;

/**
 * Represents a Monte Carlo Tree.
 * 
 * @author Jared Prince
 * @version 1.0
 * @since 1.0
 */

public class PolicyNodeTree {

	/**
	 * The number of nodes in the tree.
	 */
	int numNodes = 1;

	/**
	 * The combined depth of all nodes in the tree.
	 */
	long totalDepth = 0;

	/**
	 * The number of times an action must be selected from a node before a
	 * successor is created for that action.
	 */
	public final static int NODE_CREATION_COUNT = 1;

	/**
	 * The MCTSGame to be used by this tree.
	 */
	public MCTSGame game;

	/**
	 * The root node of the tree.
	 */
	PolicyNode root;

	/**
	 * The Hashtable which contains all the nodes of the tree with the string
	 * representation of the state as the key.
	 */
	private Hashtable<String, PolicyNode> nodeTable = new Hashtable<String, PolicyNode>();

	/**
	 * Constructor for the PolicyNodeTree.
	 * 
	 * @param game
	 *            MCTSGame to be used by this tree.
	 * @param state
	 *            The state of the root node.
	 */
	public PolicyNodeTree(MCTSGame game, GameState state) {
		this.game = game;

		/* initialize the root */
		root = new PolicyNode(state, 0, game.getActions(state), this);
		nodeTable.put(root.state.getString(), root);
	}

	/**
	 * Finds a specific node in the tree.
	 * 
	 * @param node
	 *            PolicyNode equal to the one searched for.
	 * @return The PolicyNode search for (null if not found).
	 */
	public PolicyNode findNode(PolicyNode node) {
		return nodeTable.get(node.state.getString());
	}

	/**
	 * Adds a new node to the tree (if it does not already exist).
	 * 
	 * @param node
	 *            The node to be added.
	 * @return The node added or (if the node already exists in the tree) the
	 *         equivalent node in the tree.
	 */
	public PolicyNode addNode(PolicyNode node) {
		PolicyNode p = nodeTable.get(node.state.getString());

		if (p == null) {
			p = node;
			nodeTable.put(p.state.getString(), p);
			numNodes++;
			totalDepth += node.depth;
		}

		return p;
	}
}
