package MCTS;

public class MCPlayer {
	
	public MCGame game;
	public MCTree tree;
	public MCNode currentNode;
	
	public MCNode tempNode;
	
	public GameState state;
	
	public int score = 0;
	
	public double c;
	
	public MCPlayer(MCGame game, MCTree tree){
		this.game = game;
		this.tree = tree;
		
		currentNode = tree.root;
	}
	
	public int getAction(){
		return currentNode.getNextAction(c);
	}
	
	public int getActionTemp(){
		return tempNode.getNextAction(c);
	}
	
	public void play(int action, int behavior){
		MCNode node = currentNode.getNode(action, behavior);

		if(node == null){
			state = currentNode.state;
		}
		
		currentNode = node;
	}
	
	public void playTemp(int action, int behavior){
		MCNode node = tempNode.getNode(action, behavior);

		if(node == null){
			state = tempNode.state;
		}
		
		tempNode = node;
	}
	
	public boolean isTerminal(){
		if(currentNode == null){
			return game.isTerminal(state);
		}
		
		return game.isTerminal(currentNode.state);
	}
	
	public boolean isTerminalTemp(){
		if(tempNode == null){
			return game.isTerminal(state);
		}
		
		return game.isTerminal(tempNode.state);
	}
	
	public boolean isOffTree(){
		return currentNode == null;
	}
	
	public boolean isOffTreeTemp(){
		return tempNode == null;
	}
	
	public int playDefaultAction(){
		int action = game.defaultAction(state);
		state = game.getSuccessorState(state, action);
		return action;
	}
}