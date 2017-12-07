package MCTS;

/**
 * The methods to be implemented by any game used for the Monte Carlo tree search.
 * @author      Jared Prince
 * @version     1.0
 * @since       1.0
 */

public abstract class MCGame {
	
	/**
	 * Finds all possible actions from a given state.
	 * 
	 * @param  state The state before the move is selected.
	 * @return Integer array representing all possible moves from the given state.
	 */
	public abstract int[] getActions(GameState state);
	
	/**
	 * Gets the successor of a given state.
	 * <p>
	 * This is the function called by the MCNode when a new node is created. Because the states in all
	 * MCNodes are cast to the superclass (GameState), it is the job of the MCGame to downcast to the subclass,
	 * get the successor, and cast back to the superclass before returning (if a subclass is used).
	 * 
	 * @param  state The state from which a move is made.
	 * @param  action An integer representing which move is made.
	 * @return The state after the move is made.
	 */
	public abstract GameState getSuccessorState(GameState state, int action);
	
	/**
	 * Checks whether the given game is compatible with this one.
	 * <p>
	 * This method should return true if two player using the given game and this game
	 * can play against each other without causing either logical or runtime errors.
	 * The two games need not have all the same variable values as long as the differences
	 * do not cause such errors.
	 * <p>
	 * For instance, two games with different board sizes would be incompatible because one
	 * player could make moves which are not part of the other's game. Two players with different
	 * strategies, however, would be compatible.
	 * 
	 * @param game The game with which to compare.
	 * @return True if the games are compatible, false otherwise.
	 */
	public abstract boolean compatible(MCGame game);
	
	/**
	 * Checks if the given game is equivalent to this one.
	 * <p>
	 * This method should return true only if the given game could be substituted for this one with
	 * no change in the program. In other words, the given game should share all variable values with
	 * this one, excluding ones that have no effect on the operation of the game, the tree or the search.
	 * 
	 * @param game The game with which to compare.
	 * @return True if the games are equal, false otherwise.
	 */
	public abstract boolean equals(MCGame game);
	
	/**
	 * Checks if the given state is terminal (a sate which ends the game).
	 * @param state The state to be checked.
	 * @return True if terminal, false otherwise.
	 */
	public abstract boolean isTerminal(GameState state);
	
	/**
	 * Gives a default action from among the possible actions for the given state.
	 * @param state The state from which to choose an action.
	 * @return The action as an integer.
	 */
	public abstract int defaultAction(GameState state);
	
	/**
	 * Gives the length (number of moves) of the game or an average, for games which
	 * do not have a defined length.
	 * 
	 * @return The game length.
	 */
	public abstract int gameLength();
}
