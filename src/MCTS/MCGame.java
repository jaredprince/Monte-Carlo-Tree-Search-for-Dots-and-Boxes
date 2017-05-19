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
	 * 
	 * @param  state The state from which a move is made.
	 * @param  action An integer representing which move is made.
	 * @return The state after the move is made.
	 */
	public abstract GameState getSuccessorState(GameState state, int action);
	
	/**
	 * Gets the successor of a given scored state.
	 * 
	 * @param  state The state from which a move is made.
	 * @param  action An integer representing which move is made.
	 * @return The state after the move is made.
	 */
	public abstract GameStateScored getSuccessorState(GameStateScored state, int action);
}
