package MCTS;

import java.util.Random;

/**
 * A single node of a Monte Carlo tree.
 * 
 * @author Jared Prince
 * @version 1.0
 * @since 1.0
 */

public class MCNode {

	/**
	 * Random number generator used to get a random action during ties.
	 */
	public static Random r = new Random();

	/**
	 * The state represented by this node.
	 */
	public GameState state;

	/**
	 * The number of times this node has been reached - N(s)
	 */
	public int timesReached;

	/**
	 * The depth of this node in a full tree. Usually equivalent to the number
	 * of moves made to reach this state.
	 */
	public int depth;

	/**
	 * The number of nodes of which this node is a child. This is used to delete
	 * branches of the tree without removing nodes who are children of other
	 * nodes. It is necessary because the nodes are contained in a Hashtable, so
	 * just deleting all parents does not destroy the node.
	 */
	public int parents = 0;

	/**
	 * True if this node is a leaf (has no children).
	 */
	public boolean isLeaf = true;
	
	/**
	 * Defines the behavior in which a node creation is dependent upon NODE_CREATION_COUNT
	 */
	public static final int BEHAVIOR_STANDARD = 0;
	
	/**
	 * Defines the behavior in which a node is created (if the node does not exist already), regardless of NODE_CREATION_COUNT
	 */
	public static final int BEHAVIOR_CREATE = 1;
	
	/**
	 * Defines the behavior in which a node is not created, regardless of NODE_CREATION_COUNT
	 */
	public static final int BEHAVIOR_DO_NOT_CREATE = 2;

	/**
	 * An array representing the possible moves from this node.
	 */
	public ActionLink[] links;

	/**
	 * The tree to which this node belongs. Used to search for a node before an
	 * equivalent one is created. Also used to update tree statistics, including
	 * number of nodes and average depth.
	 */
	public MCTree tree;

	/**
	 * Constructor for the MCNode.
	 * 
	 * @param state
	 *            The state represented by this node.
	 * @param depth
	 *            The depth in the tree of this node.
	 * @param actions
	 *            The array of possible actions from this node.
	 * @param tree
	 *            The tree to which this node belongs.
	 */
	public MCNode(GameState state, int depth, int[] actions, MCTree tree) {
		this.tree = tree;

		this.state = state;
		this.depth = depth;

		timesReached = 1;

		links = new ActionLink[actions.length];

		for (int i = 0; i < links.length; i++) {
			links[i] = new ActionLink(actions[i], null);
		}
	}

	/**
	 * Gets the next action based on the average result Q(s,a) and the
	 * uncertainty bonus.
	 * 
	 * @param c
	 *            The uncertainty constant to be applied when calculating the
	 *            bonuses of each action.
	 * @return An integer representing the action selected.
	 */
	public int getNextAction(double c) {

		/* By default, the links are sorted in order by value + bonus */
		if (c > 0) {
			return links[0].action;
		}

		int action = -1;
		double max = -50;

		/* find the action with the largest average reward W(s,a) */
		for (int i = 0; i < links.length; i++) {

			double val = links[i].getValue(false);

			/*
			 * equal actions should be chosen semi-randomly apart from the first
			 * few times actions are chosen, two values should almost never be
			 * equal (the probability of more than two equal values is
			 * vanishingly small), so there are assumed to be only ties of two
			 */
			if (val > max || (val == max && r.nextDouble() < .5)) {
				max = val;
				action = links[i].action;
			}
		}

		return action;
	}

	/**
	 * Gets the successor of this node based on the given action.
	 * 
	 * @param action
	 *            An integer representing the action to be made.
	 * @param behavior
	 *            Defines under which conditions a node is created.
	 *            
	 * @return The successor or null.
	 */
	public MCNode getNode(int action, int behavior) {

		/* check every action to find the one specified */
		for (int i = 0; i < links.length; i++) {

			/* Get the corresponding child */
			if (links[i].action == action) {

				if (links[i].child != null) {
					return links[i].child;
				}

				/* Create a new node */
				else if (behavior == BEHAVIOR_CREATE || 
						(links[i].timesChosen == MCTree.NODE_CREATION_COUNT && behavior == BEHAVIOR_STANDARD)) {

					MCNode newNode = getNextNode(action);
					links[i].child = tree.addNode(newNode);

					if (!isLeaf) {
						isLeaf = true;
						tree.leaves--;
					}

					return links[i].child;
				}
			}
		}

		return null;
	}

	/**
	 * Checks if this node is equivalent to another. For the purpose of this
	 * method, two nodes are equal is thier states are equal.
	 * 
	 * @param p
	 *            The node to be compared to this one.
	 * @return True if the nodes are equivalent, false otherwise.
	 */
	public boolean equals(MCNode p) {
		return p.state.equals(this.state);
	}

	/**
	 * Adds the given reward to the total rewards for an action.
	 * 
	 * @param action
	 *            An integer representing the action selected.
	 * @param value
	 *            The reward to be added.
	 * @param c
	 *            The uncertainty constant to be applied to the updated bonus.
	 */
	public void addValue(int action, int value, double c) {
		timesReached++;

		/* find the index of the action */
		int index = -1;
		for (int i = 0; i < links.length; i++) {
			if (links[i].action == action) {
				index = i;
				break;
			}
		}

		links[index].update(value);

		/* update the bonuses and reorder the list */
		for (int i = 0; i < links.length; i++) {
			links[i].updateBonus(timesReached, c);

			int t = i;

			/*
			 * move link up the queue while it's value is greater than the link
			 * before it
			 */
			while (t > 0 && links[t].getValue(true) > links[t - 1].getValue(true)) {
				ActionLink tempLink = links[t];
				links[t] = links[t - 1];
				links[t - 1] = tempLink;

				t--;
			}

			/*
			 * move link down the queue while it's value is less than the link
			 * after it
			 */
			while (t < links.length - 1 && links[t].getValue(true) < links[t + 1].getValue(true)) {
				ActionLink tempLink = links[t];
				links[t] = links[t + 1];
				links[t + 1] = tempLink;

				t++;
			}
		}
	}

	/**
	 * Creates a new node which is the successor of this node given an action.
	 * 
	 * @param action
	 *            An integer representing the action selected.
	 * @return The newly created node.
	 */
	private MCNode getNextNode(int action) {
		GameState newState = tree.game.getSuccessorState(state, action);
		return new MCNode(newState, depth + 1, tree.game.getActions(newState), tree);
	}

	/**
	 * Decrements the count of parents for all children of this node.
	 */
	public void delinkChildren() {
		for (int i = 0; i < links.length; i++) {
			if (links[i].child != null) {
				links[i].child.parents--;
			}
		}
	}

	/**
	 * Merges this node with another.
	 * 
	 * Assumptions: The two nodes are equivalent (have equivalent states). When
	 * both nodes have pointers for a child, the pointers are to the same child,
	 * not just an equivalent one.
	 * 
	 * The primitive values of the given node are combined with this one.
	 * Objects that exist in both nodes are kept from this node. Objects which
	 * exist in the given node but not this one are added.
	 * 
	 * It is the duty of the tree to merge the nodes recursively (deepest nodes
	 * first) to ensure that all links point to the same children.
	 *
	 * @param node
	 *            The node with which to be merged.
	 */
	public void mergeNode(MCNode node) {
		timesReached += node.timesReached;
		isLeaf = (isLeaf && node.isLeaf);
		
		for(int i = 0; i < links.length; i++){
			links[i].merge(node.links[i]);
		}
	}

	/**
	 * Represents a single possible action from the parent node.
	 * 
	 * @author Jared Prince
	 * @version 1.0
	 * @since 1.0
	 */
	public class ActionLink {

		/**
		 * An integer representing the action.
		 */
		int action;

		/**
		 * The number of times this action was chosen.
		 */
		int timesChosen = 0;

		/**
		 * The total rewards resulting from selecting this action.
		 */
		double rewards;

		/**
		 * The bonus applied to the average u(s, a).
		 */
		double bonus = .9999;

		/**
		 * The successor node of the parent after this action is made.
		 */
		MCNode child;

		/**
		 * Constructor for the ActionLink.
		 * 
		 * @param action
		 *            An integer representing the action of this link.
		 * @param child
		 *            The successor node of the parent after this action is
		 *            made.
		 */
		public ActionLink(int action, MCNode child) {
			this.child = child;
			this.action = action;
		}

		/**
		 * Updates the node with a given reward.
		 * 
		 * @param reward
		 *            The reward to be added.
		 */
		public void update(int reward) {
			this.rewards += reward;
			timesChosen++;
		}

		/**
		 * Updates the bonus of this action.
		 * 
		 * @param timesReached
		 *            The number of times the parent node was reached.
		 * @param c
		 *            The uncertainty constant to be applied to the bonus.
		 */
		public void updateBonus(int timesReached, double c) {
			this.bonus = c * Math.sqrt(Math.log(timesReached) / timesChosen);
		}

		/**
		 * Gets the value of the action.
		 * 
		 * @param applyBonus
		 *            True if the uncertainty bonus should be applied, false
		 *            otherwise.
		 * @return The total value of this action.
		 */
		public double getValue(boolean applyBonus) {
			if (timesChosen == 0) {
				return bonus;
			}

			return (rewards / timesChosen) + (applyBonus ? bonus : 0);
		}
		
		/**
		 * Merges this link with another.
		 * 
		 * @param link The link with which to be merged.
		 */
		public void merge(ActionLink link){
			if(child == null && link.child != null){
				child = link.child;
			}
			
			timesChosen += link.timesChosen;
			rewards += link.rewards;
		}
	}
}