package input.util;

import gnu.trove.strategy.HashingStrategy;

public class CharArrHashingStrategy {

	static public HashingStrategy<char[]> create() {
		return new HashingStrategy<char[]>() {

			private static final long serialVersionUID = 1L;

			public int computeHashCode(char[] c) {
				int h = 31; // seed chosen at random
				for (int i = 0; i < c.length; i++) {
					h = h ^ ((h << 5) + (h >> 2) + c[i]);
				}
				return h;
			}

			public boolean equals(char[] c1, char[] c2) {
				if (c1.length != c2.length) {
					return false;
				}
				for (int i = 0, len = c1.length; i < len; i++) {
					if (c1[i] != c2[i]) {
						return false;
					}
				}
				return true;
			}
		};
	}
}
