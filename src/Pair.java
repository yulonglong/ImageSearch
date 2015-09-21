
public class Pair implements Comparable<Pair> {
	String first;
	String second;

	Pair(String _first, String _second) {
		first = _first;
		second = _second;
	}

	@Override
	public int compareTo(Pair other) {
		if (first.equals(other.first)) {
			return second.compareTo(other.second);
		}
		return first.compareTo(other.first);
	}

	public boolean equals(Pair other) {
		return (first.equals(other.first) && second.equals(other.second));
	}
}
