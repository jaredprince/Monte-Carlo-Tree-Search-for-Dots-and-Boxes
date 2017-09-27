package Tools;
/* Buzzard, Kevin, and Michael Ciere. "Playing Simple Loony Dots and Boxes Endgames Optimally." 
 * Integers 14 (2014). spiral.imperial.ac.uk:8443/bitstream/10044/1/31035/2/buzzciere8.pdf.
 *
 * This is the first algorithm given, which determines the net value for the player who just moved, given optimal play
 * 
 * Written by Jared Prince 2/20/2017
 */

public class FindWinner {

	// this is algorithm #1 (4.1)
	// returns the value of a board
	public static int getValue(int[] chains, int[] loops) {
		int minChain = chains[0];

		int cv = getCV(chains, loops);

		if (cv >= 2) {
			return cv;
		}

		// use Theorem 10
		else if (loops.length == 0) {
			return theorem10(cv, chains);
		}

		// use Corollary 22
		else if (minChain > 3) {
			return corollary22(chains, loops);
		}

		// use Corollary 24
		else if (minChain == 3 && chains.length == 1 && loops.length > 0) {
			return corollary24(chains, loops);
		}

		// use Corollary 27
		else if (minChain == 3 && chains[1] > 3) {
			return corollary27(chains, loops);
		}

		// use corollary 30
		else {
			return corollary30(chains, loops);
		}
	}

	public static int getFCV(int[] chains, int[] loops) {
		int totalChains = 0;
		for (int c : chains) {
			totalChains += c;
		}

		int totalLoops = 0;
		for (int l : loops) {
			totalLoops += l;
		}

		return (totalChains - (4 * chains.length)) + (totalLoops - (8 * loops.length));
	}

	// returns the controlled value of a board
	public static int getCV(int[] chains, int[] loops) {

		int fcv = getFCV(chains, loops);
		int tb = 0;

		// set the terminal bonus
		if (loops.length == 0 || chains[chains.length - 1] > 3) {
			tb = 4;
		} else if (chains.length == 0) {
			tb = 8;
		} else {
			tb = 6;
		}

		// get the controlled value (sacrificing on all but the last move)
		return fcv + tb;
	}

	public static int theorem10(int c, int[] chains) {
		// part a
		if (chains[0] >= 4) {
			return c;
		}

		// part b
		else {
			if (c >= 1) {
				return c;
			}

			else {
				return c % 2;
			}
		}
	}

	public static int theorem12(int[] loops) {
		int maxLoop = loops[loops.length - 1];

		int[] tempChain = { 0 };

		int c = getCV(tempChain, loops);

		int f = 0;
		for (int l : loops) {
			if (l == 4)
				f++;
		}

		// part a
		if (maxLoop >= 8) {
			return c;
		}

		// part b
		else if (c >= 2) {
			return c;
		}

		// part c
		else if (c <= 0 && f == 0) {
			return c % 4;
		}

		// part d
		else {
			int[] k = new int[loops.length - f];
			int skip = 0;
			for (int i = 0; i < loops.length; i++) {
				if (loops[i] != 4) {
					k[i - skip] = loops[i];
				} else {
					skip++;
				}
			}

			int vK = theorem12(k);

			if (vK == 2) {
				return 2;
			}

			else {
				return vK + ((4 * f) % 8);
			}
		}
	}

	public static int corollary22(int[] chains, int[] loops) {
		int c = getCV(chains, loops);

		int f = 0;
		for (int l : loops) {
			if (l == 4)
				f++;
		}

		// part a
		if (c >= 2) {
			return c;
		}

		// part b
		else if (c <= 1 && c % 2 == 1 && f == 0) {
			return 3;
		}

		// part c
		else if (c <= 1 && c % 2 == 0 && f == 0) {
			return c % 4;
		}

		// part d
		else {
			int[] k = new int[loops.length - f];
			int skip = 0;
			for (int i = 0; i < loops.length; i++) {
				if (loops[i] != 4) {
					k[i - skip] = loops[i];
				} else {
					skip++;
				}
			}

			int vK = corollary22(chains, k);

			int d = vK % 8;

			if (f % 2 == 1) {
				return Math.abs(4 - d);
			}

			else {
				return 4 - Math.abs(4 - d);
			}
		}
	}

	public static int corollary24(int[] chains, int[] loops) {
		int c = getCV(chains, loops);

		int f = 0;
		for (int l : loops) {
			if (l == 4)
				f++;
		}

		// part a
		if (c <= 2) {
			return c;
		}

		int vH = theorem12(loops);

		// part b
		if (vH == 2) {
			return 1;
		}

		// part c
		else if (f == 0) {
			return 3;
		}

		// part d
		else {
			int[] m = new int[loops.length - f];
			int skip = 0;
			for (int i = 0; i < loops.length; i++) {
				if (loops[i] != 4) {
					m[i - skip] = loops[i];
				} else {
					skip++;
				}
			}

			int vM = corollary22(chains, m);

			int d = vM % 8;

			if (f % 2 == 1) {
				return Math.abs(4 - d);
			}

			else {
				return 4 - Math.abs(4 - d);
			}
		}
	}

	public static int corollary27(int[] chains, int[] loops) {
		int c = getCV(chains, loops);

		int f = 0;
		for (int l : loops) {
			if (l == 4)
				f++;
		}

		// part a
		if (c >= 2) {
			return c;
		}

		// part b
		else if (c % 2 == 0) {
			if (f > 0) {
				int[] k = new int[loops.length - f];
				int skip = 0;
				for (int i = 0; i < loops.length; i++) {
					if (loops[i] != 4) {
						k[i - skip] = loops[i];
					} else {
						skip++;
					}
				}

				int tempCV = getCV(chains, k);

				if (tempCV == 4) {
					return 0;
				}

				return 2;
			}

			return 2;
		}

		// part c
		else {
			int index = 0;
			for (int i = 0; i < chains.length; i++) {
				if (chains[i] > 3) {
					index = i;
				}
			}

			int[] k = new int[chains.length - index];

			for (int i = 0; i < k.length; i++) {
				k[i] = chains[i + index];
			}

			int vGSans3 = corollary22(chains, k);

			if (vGSans3 == 2) {
				return 1;
			}

			else if (f == 0) {
				return 3;
			}

			else {
				int[] q = new int[loops.length - f];
				int skip = 0;
				for (int i = 0; i < loops.length; i++) {
					if (loops[i] != 4) {
						q[i - skip] = loops[i];
					} else {
						skip++;
					}
				}

				int vQ = corollary27(chains, q);

				int d = vQ % 8;

				if (f % 2 == 1) {
					return Math.abs(4 - d);
				}

				else {
					return 4 - Math.abs(4 - d);
				}
			}
		}
	}

	public static int corollary30(int[] chains, int[] loops) {
		int cv = getCV(chains, loops);

		int f = 0;
		for (int l : loops) {
			if (l == 4)
				f++;
		}

		// part a
		if (cv >= 2) {
			return cv;
		}

		else {
			// part b
			if (cv % 2 == 0) {
				int[] k = new int[loops.length - f];
				int skip = 0;
				for (int i = 0; i < loops.length; i++) {
					if (loops[i] != 4) {
						k[i - skip] = loops[i];
					} else {
						skip++;
					}
				}

				int tempCV = getCV(chains, k);

				if (tempCV == 4) {
					return 0;
				}

				return 2;
			}

			// part c
			else {
				return 1;
			}
		}
	}
}