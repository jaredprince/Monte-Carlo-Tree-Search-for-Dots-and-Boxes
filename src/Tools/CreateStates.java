package Tools;

import java.io.FileWriter;
import java.io.IOException;

import MCTS.DotsAndBoxes;
import MCTS.GameState;

public class CreateStates {

	public static void main(String[] args) throws IOException {
		
		int size = 3;
		int edges = (size * (size + 1)) * 2;
		
		DotsAndBoxes game = new DotsAndBoxes(size, size, false, false);
		
		FileWriter file = new FileWriter("2x2 Scored states.txt");
		
		
		for(int i = 0; i < Math.pow(2, edges); i++){
			GameState state = new GameState(i);
			String s = state.getBinaryString();
			
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
			
//			System.out.println(i + " " + score);
			
//			file.write(i + " " + score);
			
			int sc = score;
			
			for(int j = 0; j <= sc; j++){
				
				System.out.println(i + " " + score);
//				file.write(i + " " + score);
				score -= 2;
			}
		}
		
		file.close();

	}

}
