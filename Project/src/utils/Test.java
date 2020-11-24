package utils;

public class Test {

	public static void main(String[] args) {
		String s = "Hi, *how are you*?";
		String bold = "*";

		for (int i = 1; i < s.length() + 1; i++) {
			if (s.substring(i - 1, i).equals(bold)) {
				s.replace(s.substring(i - 1, i), "<b>");
			}

		}

		System.out.println(s);
	}
}
