package cs5625.deferred.datastruct;

public class EdgePair{
	public int getE0ID() {
		return e0ID;
	}


	private int e0ID, e1ID;
	
	public EdgePair(int e0, int e1){
		this.e0ID = e0;
		this.setE1ID(e1);
	}

	public int getE1ID() {
		return e1ID;
	}

	public void setE1ID(int e1ID) {
		this.e1ID = e1ID;
	}
}