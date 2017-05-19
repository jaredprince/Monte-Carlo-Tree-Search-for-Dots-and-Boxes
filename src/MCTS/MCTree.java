package MCTS;

import java.util.Hashtable;

/**
 * Represents a Monte Carlo Tree.
 * 
 * @author Jared Prince
 * @version 1.0
 * @since 1.0
 */

public class MCTree {

	/**
	 * The number of nodes in the tree.
	 */
	public int numNodes = 1;

	/**
	 * The combined depth of all nodes in the tree.
	 */
	public long totalDepth = 0;

	/**
	 * The depth of the deepest node in the tree.
	 */
	public int maximumDepth = 0;

	/**
	 * The number of times an action must be selected from a node before a
	 * successor is created for that action.
	 */
	public final static int NODE_CREATION_COUNT = 1;

	/**
	 * The MCTSGame to be used by this tree.
	 */
	public MCGame game;

	/**
	 * The root node of the tree.
	 */
	public MCNode root;

	/**
	 * The Hashtable which contains all the nodes of the tree with the string
	 * representation of the state as the key.
	 */
	private Hashtable<String, MCNode> nodeTable = new Hashtable<String, MCNode>();

	/**
	 * Constructor for the MCTree.
	 * 
	 * @param game
	 *            MCTSGame to be used by this tree.
	 * @param state
	 *            The state of the root node.
	 */
	public MCTree(MCGame game, GameState state) {
		this.game = game;

		/* initialize the root */
		root = new MCNode(state, 0, game.getActions(state), this);
		nodeTable.put(root.state.getString(), root);
	}

	/**
	 * Finds a specific node in the tree.
	 * 
	 * @param node
	 *            MCNode equal to the one searched for.
	 * @return The MCNode searched for or null if not found.
	 */
	public MCNode findNode(MCNode node) {
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
	public MCNode addNode(MCNode node) {
		MCNode p = nodeTable.get(node.state.getString());

		if (p == null) {
			p = node;
			nodeTable.put(p.state.getString(), p);
			numNodes++;
			totalDepth += node.depth;

			if (p.depth > maximumDepth) {
				maximumDepth = p.depth;
			}
		} else {
			p.parents++;
		}

		return p;
	}

	/**
	 * Deletes the node on the tree equivalent to the given node.
	 * 
	 * @param node
	 *            The node to be deleted.
	 * @param recursive
	 *            True if the children of the node should be recursively
	 *            removed.
	 * @return The node that was deleted or null.
	 */
	public MCNode deleteNode(MCNode node, boolean recursive) {		
		if(recursive){
			deleteBranch(node);
			return nodeTable.get(node.state.getString());
		}
		
		node.delinkChildren();
		return nodeTable.remove(node.state.getString());
	}

	private void deleteBranch(MCNode node){
		
	}
}
