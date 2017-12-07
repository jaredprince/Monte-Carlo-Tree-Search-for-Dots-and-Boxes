package MCTS;

import java.util.ArrayList;
import java.util.Enumeration;
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
	 * The number of nodes that have been removed from the tree.
	 */
	public int deletedNodes = 0;

	/**
	 * The combined depth of all nodes in the tree.
	 */
	public long totalDepth = 0;

	/**
	 * The total number of leaves (nodes with no children) on the tree.
	 */
	public int leaves = 1;

	/**
	 * The depth of the deepest node in the tree.
	 */
	public int maximumDepth = 0;

	/**
	 * The number of times an action must be selected from a node before a
	 * successor is created for that action (if EXPANSION_STANDARD behavior is used).
	 */
	public int nodeCreationCount = 2;

	/**
	 * The MCTSGame to be used by this tree.
	 * This is only used by the MCNodes which belong in this tree.
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

		//if the node doesn't exist in the tree
		if (p == null) {
			p = node;
			nodeTable.put(p.state.getString(), p);
			numNodes++;
			leaves++;
			totalDepth += node.depth;

			if (p.depth > maximumDepth) {
				maximumDepth = p.depth;
			}
		} else {
			//add to the parents of p because this method is used when creating a child of a node
			p.parents++;
		}

		return p;
	}

	/**
	 * Deletes the node on the tree equivalent to the given node.
	 * 
	 * @param node
	 *            The node to be deleted.
	 * 
	 * @return The node that was deleted or null.
	 */
	public MCNode deleteNode(MCNode node) {
		//delete the node
		node = nodeTable.remove(node.state.getString());

		if (node != null) {
			numNodes--;
			node.delinkChildren();
		}

		return node;
	}

	/**
	 * Deletes the node equivalent to the given node and recursively deletes its
	 * children. The children are only deleted if they do not have other parent
	 * nodes. It is necessary to recursively delete the children from the node table
	 * in order for the garbage collector to collect them.
	 * 
	 * @param node
	 *            The node to be deleted.
	 * @return The number of nodes deleted.
	 */
	public int deleteBranch(MCNode node) {
		int deleted = 0;
		
		//delete this node
		node = nodeTable.remove(node.state.getString());

		if (node != null) {
			deleted++;
			numNodes--;
			totalDepth -= node.depth;

			node.delinkChildren();

			if (node.isLeaf) {
				leaves--;
			}

			MCNode child;
			for (int i = 0; i < node.links.length; i++) {
				child = node.links[i].child;

				if (child.parents == 0) {
					deleted += deleteBranch(child);
				}
			}
		}

		deletedNodes += deleted;

		return deleted;
	}

	/**
	 * Merges this tree with another.
	 * 
	 * <strong> DO NOT merge trees which do not form the same game tree. The roots
	 * and games of each tree must be equivalent. </strong>
	 * 
	 * @param tree
	 *            The tree with which to merge.
	 */
	public void merge(MCTree tree) {

		//only merge trees with equivalent roots (no subtrees allowed)
		if (tree.root.equals(root)) {
			merge(tree, root);
		}
		
		//update the data for the tree
		updateTreeData();
	}

	/**
	 * Recursively merges nodes of this tree with equivalent nodes of the given
	 * tree. This assumes a topological ordering of the nodes. Nodes lower in the
	 * tree are merged first.
	 * 
	 * @param tree
	 *            The tree with which to merge.
	 * @param node
	 *            The node (from this tree) currently being merged.
	 */
	private void merge(MCTree tree, MCNode node) {
		//TODO: fix this so that nodes which exist on the new tree but not this one are added
		
		MCNode child;
		
		//for each child of node
		for(int i = 0; i < node.links.length; i++){
			child = node.links[i].child;
			
			if(child != null){
				//recursively merge the child with the equivalent node in this tree
				child.mergeNode(tree.findNode(child));
			}
		}
		
		//merge this node with the eqiuvalent node in this tree
		node.mergeNode(tree.findNode(node));
	}

	/**
	 * Gets the nodes along the path currently favored by the tree. Equivalent
	 * to the player using this tree playing against itself for a game with no
	 * additional simulations.
	 * 
	 * @return The array of nodes along the path.
	 */
	public MCNode[] currentPath() {
		ArrayList<MCNode> path = new ArrayList<MCNode>();

		MCNode currentNode = root;
		
		//until the path moves off the tree or reaches the end
		while(currentNode != null && !game.isTerminal(currentNode.state)){
			//add the node to the path
			path.add(currentNode);
			
			//move down the tree, but do not expand
			currentNode = currentNode.getNode(currentNode.getNextAction(0), MonteCarloTreeSearch.BEHAVIOR_EXPANSION_NEVER);
		}
		
		return (MCNode[]) path.toArray();
	}

	/**
	 * Updates the data for the tree by iterating through all nodes.
	 */
	public void updateTreeData() {
		Enumeration<MCNode> nodes = nodeTable.elements();
		MCNode node;
		int depth;

		//for every element
		while (nodes.hasMoreElements()) {
			node = (MCNode) nodes.nextElement();
			numNodes++;

			depth = node.depth;
			totalDepth += depth;

			if (maximumDepth < depth) {
				maximumDepth = depth;
			}

			if (node.isLeaf) {
				leaves++;
			}
		}
	}
}