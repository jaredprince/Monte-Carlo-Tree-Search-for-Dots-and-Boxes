package Tools;
import java.io.FileWriter;
import java.io.IOException;

//used to create a variety of bash files for testing
public class CreateTestFiles {
	
	public static void main(String args[]) throws IOException {
		fileSet1();
		fileSet2();
		fileSet3();
	}
	
	public static void fileSet2() throws IOException{
		int[] sims = {0,1000,5000,10000,20000,50000, 100000, 150000};
		int[] sims2 = {1000,5000};
		int[] tests = {200,500,1000,2000};
		
		double c = .75;
		
		for(int i = 2; i <= 5; i++){
			int test = tests[i - 2];
			for(int q = 0; q < sims.length; q++){
				for(int o = 0; o < sims2.length; o++){
					for(int b = 0; b < 2; b++){
//						for(int r = 0; r < test; r++){
							
							String constellation = b == 0 ? "YN" : "NN";
							String constargs = b == 0 ? "true false 1 true false" : "false false 1 false false";
							
//							String extra = test <= 1 ? "" : "_" + (r+1);
							String extra = "";
							
							FileWriter newFile = new FileWriter("C_Test-" + i + "-" + sims[q] + "-" + sims2[o] + "-" + constellation + extra + ".sh");
							
							newFile.write("#!/bin/bash\n");
							newFile.write("#PBS -N C" + i + "-" + sims[q] + "-" + constellation + extra + "\n");
							newFile.write("#PBS -o Comparison" + i + "-" + sims[q] + "-" + sims2[o] + "-" + constellation + extra + ".out\n");
							newFile.write("#PBS -e Comparison" + i + "-" + sims[q] + "-" + sims2[o] + "-" + constellation + extra + ".err\n");
							
							newFile.write("\ncd $PBS_O_HOME/dots/MCTS\n");
							newFile.write("java MCTS " + i + " " + i + " " + c + " " + test + " " + sims[q] + " " + constargs + " " + sims2[o]);
							
							newFile.close();
//						}
					}
				}
			}
		}
	}
	
	public static void fileSet1() throws IOException{
		int[] sims = {0,1000,5000,10000,20000,50000, 100000, 150000};
		int[] tests = {200,500,1000,2000};
		double c = .75;
		
		for(int i = 2; i <= 5; i++){
			int test = tests[i - 2];
			for(int q = 0; q < sims.length; q++){
				for(int b = 0; b < 2; b++){
//					for(int r = 0; r < test; r++){
						String constellation = b == 0 ? "YNNN" : "NNNN";
						String constargs = b == 0 ? "true false 1 false false" : "false false 1 false false";
//						String extra = test == 1 ? "" : "_" + (r+1);
						String extra = "";
						FileWriter newFile = new FileWriter("Test-" + i + "-" + sims[q] + "-" + constellation + extra +".sh");
						
						newFile.write("#!/bin/bash\n");
						newFile.write("#PBS -N " + i + "-" + sims[q] + "-" + constellation + extra + "\n");
						newFile.write("#PBS -o " + i + "-" + sims[q] + "-" + constellation + extra + ".out\n");
						newFile.write("#PBS -e " + i + "-" + sims[q] + "-" + constellation + extra + ".err\n");
						
						newFile.write("\ncd $PBS_O_HOME/dots/MCTS\n");
						newFile.write("java MCTS " + i + " " + i + " " + c + " " + test + " " + sims[q] + " " + constargs);
						
						newFile.close();
//					}
				}
			}
		}
	}
	
	public static void fileSet3() throws IOException{
		int[] sims = {0,1000,5000,10000,20000,50000,100000,150000};
		int[] tests = {200,500,1000,2000};
		double c = .75;
		
		for(int i = 2; i <= 5; i++){
			int test = tests[i - 2];
			for(int q = 0; q < sims.length; q++){
				for(int b = 1; b < 2; b++){
//					for(int r = 0; r < test; r++){
					String constellation = b == 0 ? "YNNN" : "NYNY";
					String constargs = b == 0 ? "true false 1 false false" : "false true 1 false true";
					
//					String extra = test == 1 ? "" : "_"+(r+1);
					String extra = "";
					
					FileWriter newFile = new FileWriter("S_Test-" + i + "-" + sims[q] + "-" + constellation + extra + ".sh");
					
					newFile.write("#!/bin/bash\n");
					newFile.write("#PBS -N S_" + i + "-" + sims[q] + "-" + constellation + extra + "\n");
					newFile.write("#PBS -o S_" + i + "-" + sims[q] + "-" + constellation + extra + ".out\n");
					newFile.write("#PBS -e S_" + i + "-" + sims[q] + "-" + constellation + extra + ".err\n");
					
					newFile.write("\ncd $PBS_O_HOME/dots/MCTS\n");
					newFile.write("java MCTS " + i + " " + i + " " + c + " " + test + " " + sims[q] + " " + constargs);
					
					newFile.close();
//					}
				}
			}
		}
	}
}
