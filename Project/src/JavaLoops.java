
public class JavaLoops {
	
	public static void main(String[] args) {
		
		int[] evenNums = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		
		for (int num: evenNums) {
			
			if (num % 2 == 0) {
				System.out.println(num);
			}
		}
	}

}
