package MCTS;

public class MCPlayer {
	
	/**
	 * The game object used by this player.
	 */
	public MCGame game;
	
	/**
	 * The MCTree used by this player.
	 */
	public MCTree tree;
	
	/**
	 * The node representing the player's current state.
	 * If in simulation mode, currentNode is the node the representing the current state of the simulation.
	 */
	public MCNode currentNode;
	
	/**
	 * Saves the currentNode of the game while the player performs simulations using the currentNode variable.
	 */
	public MCNode tempNode;
	
	/**
	 * Represents the state of the game when the simulation is off the player's tree.
	 */
	public GameState state;
	
	/**
	 * The current mode.
	 * True is simulation mode.
	 * False is standard mode.
	 */
	public boolean simulation = false;
	
	/**
	 * The tuning constant used by this player for UCT.
	 */
	public double c;
	
	/**
	 * The index in the behaviors array representing selection behavior.
	 */
	public static final int SELECTION = 0;
	
	/**
	 * The index in the behaviors array representing expansion behavior.
	 */
	public static final int EXPANSION = 1;
	
	/**
	 * The index in the behaviors array representing expansion nodes behavior.
	 */
	public static final int EXPANSION_NODES = 2;
	
	/**
	 * An array of integers representing the behaviors used by this player.
	 */
	public int[] behaviors = {MonteCarloTreeSearch.BEHAVIOR_SELECTION_STANDARD, MonteCarloTreeSearch.BEHAVIOR_EXPANSION_STANDARD, MonteCarloTreeSearch.BEHAVIOR_EXPANSION_NODES_SINGLE};
	
	/**
	 * The number of nodes to expand is EXPANSION_NODES behavior is multiple.
	 */
	int nodesToExpand = 3;
	
	/**
	 * The number of nodes expanded during the current simulation
	 */
	int nodesExpanded = 0;
	
	/**
	 * Constructor for the MCPlayer.
	 * 
	 * @param game The game used by this player.
	 * @param tree The tree used by this player.
	 */
	public MCPlayer(MCGame game, MCTree tree){
		this.game = game;
		this.tree = tree;
		
		currentNode = tree.root;
	}
	
	/**
	 * Constructor for the MCPlayer.
	 * 
	 * @param game The game used by this player.
	 * @param tree The tree used by this player.
	 * @param behaviors An array of integers representing the behaviors to be used by this player.
	 */
	public MCPlayer(MCGame game, MCTree tree, int[] behaviors){
		this.game = game;
		this.tree = tree;
		this.behaviors = behaviors;
		
		currentNode = tree.root;
	}
	
	/**
	 * Sets the mode of the player as simulation or standard.
	 * The moves made in simulation mode do not carry over to standard mode.
	 * @param simulation True to set mode to simulations, false for standard.
	 * @return True if the mode was changed, false otherwise.
	 */
	public boolean setMode(boolean simulation){
		if(this.simulation == simulation){
			return false;
		}
		
		else if (simulation) {
			tempNode = currentNode;
		}
		
		else {
			currentNode = tempNode;
		}
		
		this.simulation = !this.simulation;
		
		return true;
	}
	
	/**
	 * Gets the next action for the player to take.
	 * This method uses the player's selection behavior.
	 * 
	 * @return The action as an integer.
	 */
	public int getAction(){
		
		if(behaviors[SELECTION] == MonteCarloTreeSearch.BEHAVIOR_SELECTION_STANDARD){
			return currentNode.getNextAction(c);
		}
		
		else {
			return currentNode.getRandomAction();
		}
	}
	
	/**
	 * This method plays the action given, including changing the current node to the successor
	 * of the given state/action and expanding the tree (if applicable).
	 * This method uses the player's expansion behavior.
	 * 
	 * @param action The action to be taken, as an integer.
	 */
	public void play(int action){
		MCNode node = null;
		
		//if not expanded yet, use EXPANSION behavior (which determines when to start expanding)
		if(nodesExpanded == 0){
			node = currentNode.getNode(action, behaviors[EXPANSION]);

		} else { //if already expanding, use EXPANSION_NODES behavior (which determines how much to expand)
			
			//if EXPANSION_NODES_SINGLE, do not expand again
			if(behaviors[EXPANSION_NODES] == MonteCarloTreeSearch.BEHAVIOR_EXPANSION_NODES_SINGLE){
				node = currentNode.getNode(action, MonteCarloTreeSearch.BEHAVIOR_EXPANSION_NEVER);
			} 
			
			//if EXPANSION_NODES_MULTIPLE and enough have been expanded, do not expand again
			else if (behaviors[EXPANSION_NODES] == MonteCarloTreeSearch.BEHAVIOR_EXPANSION_NODES_MULTIPLE && nodesExpanded >= nodesToExpand) {
				node = currentNode.getNode(action, MonteCarloTreeSearch.BEHAVIOR_EXPANSION_NEVER);
			} 
			
			//otherwise, expand again
			else {
				node = currentNode.getNode(action, MonteCarloTreeSearch.BEHAVIOR_EXPANSION_ALWAYS);
			}
		}

		if(node == null){
			//save the state if the node is null
			state = currentNode.state;
		} else {
			
			//updated nodesExpanded
			if(node.timesReached == 1) {
				nodesExpanded++;
			}
		}
		
		currentNode = node;
	}
	
	/**
	 * This method plays the action given, including changing the current node to the successor
	 * of the given state/action and expanding the tree (if applicable). This method is called in
	 * whenever a move is made in the actual game (not a simulation), and the behavior 
	 * is BEHAVIOR_EXPANSION_ALWAYS (since the player should always a node missing from the actual player moves).
	 * 
	 * @param action The action to be taken, as an integer.
	 * @param behavior The expansion behavior to be used.
	 */
	public void play(int action, int behavior){
		MCNode node = currentNode.getNode(action, behavior);

		if(node == null){
			state = currentNode.state;
		}
		
		currentNode = node;
		
		//since this method is called whenever a move in the actual game is played, it resets
		//the nodesExpanded for the simulation (because it is no longer in a simulation)
		nodesExpanded = 0;
	}
	
	/**
	 * Checks if the player is at a terminal state.
	 * @return True if the current state of the player is terminal, false otherwise.
	 */
	public boolean isTerminal(){
		if(currentNode == null){
			return game.isTerminal(state);
		}
		
		return game.isTerminal(currentNode.state);
	}
	
	/**
	 * Checks if the player has moved off the know portion of the tree.
	 * @return True if the player is off the tree and false otherwise.
	 */
	public boolean isOffTree(){
		return currentNode == null;
	}
	
	/**
	 * Plays an action based on the default policy of the game.
	 * @return An integer representing the action played.
	 */
	public int playDefaultAction(){
		int action = game.defaultAction(state);
		state = game.getSuccessorState(state, action);
		return action;
	}
}