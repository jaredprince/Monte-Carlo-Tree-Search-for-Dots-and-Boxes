package Tools;

import java.math.BigInteger;


//finds the number of scored states for a given board size
public class NetScore {

	static int size;

	static boolean threadSwitching = true;

	static BigInteger nextIndex = new BigInteger("0");
	static BigInteger threadSize = new BigInteger("0");

	static BigInteger increment = new BigInteger("1");

	static BigInteger terminalState;
	static BigInteger startState;
	static int activeThreads = 0;
	
	static long netScores = 0;

	static int edges;

	static int threads = 10;
	
	public static int[][] edgeSquares; //edgeSquares[i] is an array containing the squares that belong to edge i
	public static int[][] squareEdges; //squareEdges[i] is an array containing all the edges of square i

	static long[] threadCount;
	static NetScoreSearch[] searchThreads; 
	
	public static int[][] rotationMap = {
		{2,0,3,1},
		{4,9,1,6,11,3,8,0,5,10,2,7},
		{6,13,20,2,9,16,23,5,12,19,1,8,15,22,4,11,18,0,7,14,21,3,10,17},
		{8,17,26,35,3,12,21,30,39,7,16,23,34,2,11,20,29,38,6,15,24,33,1,10,19,28,37,5,14,23,32,0,9,18,27,36,4,13,22,31},
		{10,21,32,43,54,4,15,26,37,48,59,9,20,31,42,53,3,14,25,36,47,58,8,19,30,41,52,2,13,24,35,46,57,7,18,29,40,51,1,12,23,34,45,56,6,17,28,39,50,0,11,22,33,44,55,5,16,27,38,49},
		{12,25,38,51,64,77,5,18,31,44,57,70,83,11,24,37,50,63,76,4,17,30,43,56,69,82,10,23,36,49,62,75,3,16,29,42,55,68,81,9,22,35,48,61,74,2,15,28,41,54,67,80,8,21,34,47,60,73,1,14,27,40,53,66,79,7,20,33,46,59,72,0,13,26,39,52,65,78,6,19,32,45,58,71},
		{14,29,44,59,74,89,104,6,21,36,51,66,81,96,111,13,28,43,58,73,88,103,5,20,35,50,65,80,95,110,12,27,42,57,72,87,102,4,19,34,49,64,79,94,109,11,26,41,56,71,86,101,3,18,33,48,63,78,93,108,10,25,40,55,70,85,100,2,17,32,47,62,77,92,107,9,24,39,54,69,84,99,1,16,31,46,61,76,91,106,8,23,38,53,68,83,98,0,15,30,45,60,75,90,105,7,22,37,52,67,82,97}
	};
	
	public static int[][] reflectionMap = {
		{3,2,1,0},
		{10,11,7,8,9,5,6,2,3,4,0,1},
		{21,22,23,17,18,19,20,14,15,16,10,11,12,13,7,8,9,3,4,5,6,0,1,2},
		{39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0},
		{4,3,2,1,0,10,9,8,7,6,5,15,14,13,12,11,21,20,19,18,17,16,26,25,24,23,22,32,31,30,29,28,27,37,36,35,34,33,43,42,41,40,39,38,48,47,46,45,44,54,53,52,51,50,49,59,58,57,56,55},
		{5,4,3,2,1,0,12,11,10,9,8,7,6,18,17,16,15,14,13,25,24,23,22,21,20,19,31,30,29,28,27,26,38,37,36,35,34,33,32,44,43,42,41,40,39,51,50,49,48,47,46,45,57,56,55,54,53,52,64,63,62,61,60,59,58,70,69,68,67,66,65,77,76,75,74,73,72,71,83,82,81,80,79,78},
		{6,5,4,3,2,1,0,14,13,12,11,10,9,8,7,21,20,19,18,17,16,15,29,28,27,26,25,24,23,22,36,35,34,33,32,31,30,44,43,42,41,40,39,38,37,51,50,49,48,47,46,45,59,58,57,56,55,54,53,52,66,65,64,63,62,61,60,74,73,72,71,70,69,68,67,81,80,79,78,77,76,75,89,88,87,86,85,84,83,82,96,95,94,93,92,91,90,104,103,102,101,100,99,98,97,111,110,109,108,107,106,105}
	};
	
	public static void main(String args[]) throws InterruptedException{

		size = Integer.parseInt(args[0]);
		edges = 2 * ((size + 1) * size);
		terminalState = new BigInteger("0").flipBit(edges);
		startState = new BigInteger("0");
		
		edgeSquares = new int[edges][2];
		squareEdges = new int[size * size][4];
		
		for(int i = 0; i < edgeSquares.length; i++){
			edgeSquares[i][0] = -1;
			edgeSquares[i][1] = -1;
		}
		
		//sets the edges for each square and the squares for each edge
		for(int i = 0; i < squareEdges.length; i++){
			int first = (((i / size) * ((2 * size) + 1)) + (i % size));
			int second = first + size;
			int third = second + 1;
			int fourth = third + size;
			
			
			int[] square = {first, second, third, fourth};
			squareEdges[i] = square;
			
			if(edgeSquares[first][0] == -1){
				edgeSquares[first][0] = i;
			}
			else{
				edgeSquares[first][1] = i;
			}
			
			if(edgeSquares[second][0] == -1){
				edgeSquares[second][0] = i;
			}
			else{
				edgeSquares[second][1] = i;
			}
			
			if(edgeSquares[third][0] == -1){
				edgeSquares[third][0] = i;
			}
			else{
				edgeSquares[third][1] = i;
			}
			
			if(edgeSquares[fourth][0] == -1){
				edgeSquares[fourth][0] = i;
			}
			else{
				edgeSquares[fourth][1] = i;
			}
		}
		
		//remove second array position if no square
		for(int i = 0; i < edgeSquares.length; i++){
			if(edgeSquares[i][1] == -1){
				int[] square = {edgeSquares[i][0]};
				edgeSquares[i] = square;
			}
		}
		
		if(args.length > 2){
			startState = new BigInteger(args[2]);
			nextIndex = startState;
		}
		
		if(args.length > 3){
			terminalState = new BigInteger(args[3]);
		}
		
		System.out.println("Total States: " + terminalState);
		
		long start = System.currentTimeMillis();

		if(size < 3){
			for(BigInteger i = new BigInteger("0"); i.compareTo(terminalState) < 0; i = i.add(increment)){
				netScores += boxesTaken(i) + 1;
			}
		} 
		
		else {
			threads = Integer.parseInt(args[1]);
			searchThreads = new NetScoreSearch[threads];
			
			BigInteger searchSize = terminalState.subtract(startState);

			threadCount = new long[threads];
			threadSize = threadSize.add(searchSize.divide(new BigInteger(Integer.toString(threads))));

			for(int i = 0; i < searchThreads.length; i++){
				if(i == searchThreads.length - 1){
					searchThreads[i] = new NetScoreSearch(nextIndex, terminalState, i);
				} else {
					searchThreads[i] = new NetScoreSearch(nextIndex, nextIndex.add(threadSize), i);
					nextIndex = nextIndex.add(threadSize);
				}

				activeThreads++;
			}

			while(activeThreads > 0){
				Thread.sleep(100);
			}

			for(int i = 0; i < threads; i++){
				netScores += threadCount[i];
			}
		}
		
		long end = System.currentTimeMillis();

		System.out.println("Seconds: " + ((double) end - start) / 1000);
		System.out.println("Scored States: " + netScores);
		System.out.println();
	}
	
	public static int boxesTaken(BigInteger number){
		
		String num = number.toString(2);
		int scores = 0;
		
		for(int i = 0; i < NetScore.squareEdges.length; i++){
			scores++;
			
			//for each edge of that square
			for(int b = 0; b < NetScore.squareEdges[i].length; b++){
				
				if(num.length() < NetScore.edges - NetScore.squareEdges[i][b]){
					scores--;
					break;
				}
				
				if(num.charAt(NetScore.squareEdges[i][b] - (NetScore.edges - num.length())) == '0'){
					scores--;
					break;
				}
			}
		}
		
		return scores;
	}
}

class NetScoreSearch implements Runnable {

	BigInteger current;
	BigInteger chunkEnd;

	int id;

	long count = 0;

	private Thread t;

	NetScoreSearch(BigInteger start, BigInteger end, int id){
		this.current = start;
		this.chunkEnd = end;
		this.id = id;

		t = new Thread(this);
		t.start();
	}

	public void run(){
		
		while(current.compareTo(chunkEnd) < 0){
			count += NetScore.boxesTaken(current) + 1;
			current = current.add(NetScore.increment);
		}

		NetScore.threadCount[id] = count;
		NetScore.activeThreads--;
	}
}