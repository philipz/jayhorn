package translation_tests;

public class TranslationTest01 {

//	public int noExceptions(int i) {
//		if (i == 0) {
//			return 1;
//		} else if (i == 2) {
//			return 2;
//		}
//		assert 2 == 1;
//		return 3;
//	}

	public void virtualCalls(int i) {
		Object o;
		if (i>0) {
			o = new TranslationTest01();
		} else {
			o = new String();
		}
		o.toString();
	}
	
	@Override
	public String toString() {
		return null;
	}
	
//	public void withException() {
//		try {
//			int a[] = new int[2];
//			System.out.println("Access element three :" + a[3]);
//		} catch (ArrayIndexOutOfBoundsException e) {
//			System.out.println("Exception thrown  :" + e);
//		}
//		System.out.println("Out of the block");
//	}
}