package my.amppercent.types;

public class Couple<T, Z> {
	private T first;
	private Z second;;

	public Couple(T x, Z y) {
		this.first = x;
		this.second = y;
	}

	public T fst() {
		return this.first;
	}

	public Z snd() {
		return this.second;
	}
}
