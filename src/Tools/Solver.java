package Tools;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import MCTS.DotsAndBoxes;
import MCTS.GameState;

public class Solver {
	
	static int width = 3;
	static int height = 3;
	static int edges = (height * (width + 1)) + (width * (height + 1));
	static DotsAndBoxes game = new DotsAndBoxes(width, height, false, false);
	static long games = 0;
	static long states = 0;
	
	static int[][] memos = new int[(int) Math.pow(2, edges)][3]; //{initialized, max score for current player from this state, optimal move}
	
	public static void main(String args[]) throws IOException{
		Random r = new Random();
		memos[(int) Math.pow(2, edges) - 1][0] = 1;
		memos[(int) Math.pow(2, edges) - 1][1] = 0;
		
		long start = System.currentTimeMillis();
		System.out.println("Forcable net score for player 1: " + memomax(new GameState(0), 0));
		System.out.println("Time for search: " + (System.currentTimeMillis() - start) + " miliseconds");
		System.out.println("Paths explored: " + games);
		System.out.println("States reached: " + states);
		
		FileWriter newFile = new FileWriter("2x2 Scored Optimal Moves");
		
		for(int i = 0; i < 10000; i++){
			
			int e = r.nextInt(16777200);
			String s = Integer.toBinaryString(e);
			
//			String s = Integer.toBinaryString(i);
			
			// add extra leading zeros
			int b = s.length();
			for (int j = 0; j < (edges - b); j++) {
				s = "0" + s;
			}
			
			int score = 0;
			
			for(int j = 0; j < game.boxEdges.length; j++){
				boolean taken = true;
				
				for(int k = 0; k < game.boxEdges[j].length; k++){
					if(s.charAt(game.boxEdges[j][k]) == '0'){
						taken = false;
						break;
					}
				}
				
				if(taken)
					score++;
			}
			
			int sc = score;
			
			for(int k = 0; k <= sc; k++){
				
				newFile.write("0,");
				
				for(int j = 0; j < edges - s.length(); j++){
					newFile.write("0,0,");
				}
				
				for(int j = 0; j < s.length(); j++){
					newFile.write(s.charAt(j) + ",0,");
				}
				
				int winner = (k+memos[i][1] > (width*height)/2 ? 0 : 1);
				newFile.write(score + "," + winner + "," + memos[i][2] + "\n");
				
				score -= 2;
			}
			
		}
		
		newFile.close();
	}
	
	public static int[][] getMoves(){
		memomax(new GameState(0), 0);
		return memos;
	}
	
	//gets the max possible score for this player from this state
	public static int memomax(GameState state, int depth){
		
		if(memos[(int) state.longState][0] == 1){
			//terminal state
			if(depth == edges){
				games++;
			}
			
			return memos[(int) state.longState][1];
		}
		
		else {
			states++;
			
			int[] actions = game.getActions(state);
	
			//the max score for this player
			int bestScore = -(width * height) - 1;
			
			int bestAction = -200;
			
			//for every possible action
			for(int i = 0; i < actions.length; i++){
				
				//the points you get from making this move
				int z = game.completedBoxesForEdge(actions[i], state);
								
				int val;
			
				int futureVal = memomax(game.getSuccessorState(state, actions[i]), depth + 1);
				
				if(z != 0){
					val = z + futureVal;
				} else {
					val = -futureVal;
				}
				
				if(val > bestScore){
					bestScore = val;
					bestAction = actions[i];
				}
			}
			
			memos[(int) state.longState][0] = 1;
			memos[(int) state.longState][1] = bestScore;
			memos[(int) state.longState][2] = bestAction;
			
			return bestScore;
		}
	}
	
//	//gets the max possible score for this player from this state
//	public static int alphabeta(GameState state, int depth, int alpha, int beta, boolean playerOne){
//		return 1;
//	}
}