package MCTS;

public class MCPlayer {
	
	public MCGame game;
	public MCTree tree;
	public MCNode currentNode;
	
	public MCNode tempNode;
	
	public GameState state;
	
	public boolean simulation = false;
	
	public double c;
	
	public MCPlayer(MCGame game, MCTree tree){
		this.game = game;
		this.tree = tree;
		
		currentNode = tree.root;
	}
	
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
	
	public int getAction(){
		return currentNode.getNextAction(c);
	}
	
	public void play(int action, int behavior){
		MCNode node = currentNode.getNode(action, behavior);

		if(node == null){
			state = currentNode.state;
		}
		
		currentNode = node;
	}
	
	public boolean isTerminal(){
		if(currentNode == null){
			return game.isTerminal(state);
		}
		
		return game.isTerminal(currentNode.state);
	}
	
	public boolean isOffTree(){
		return currentNode == null;
	}
	
	public int playDefaultAction(){
		int action = game.defaultAction(state);
		state = game.getSuccessorState(state, action);
		return action;
	}
}