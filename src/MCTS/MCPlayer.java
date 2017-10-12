package MCTS;

public class MCPlayer {
	
	public MCGame game;
	public MCTree tree;
	public MCNode currentNode;
	public MCNode tempNode;
	
	public int score;
	
	public double c;
	
	public MCPlayer(MCGame game, MCTree tree){
		this.game = game;
		this.tree = tree;
		
		currentNode = tree.root;
	}
	
	public int getAction(){
		return currentNode.getNextAction(c);
	}
	
	public boolean play(int action, int behavior){
		currentNode = currentNode.getNode(action, behavior);
		
		if(game.isTerminal(currentNode.state)){
			return true;
		}
		
		return false;
	}

}