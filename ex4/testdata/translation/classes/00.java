class Main {
	public static void main(String[] a){
		System.out.println(12);
	}
}


//Pass the "this" to the method sqrt
//V Table contains two entries but there is only one method sqrt()
class B {
	
	int sqrt(int x) {
		int r;
		r = 0;
		if (x < 1) {
		} else {
			while (r*r < x) {
				r = r + 1;
			}
		}

		return r;
	}
	
}