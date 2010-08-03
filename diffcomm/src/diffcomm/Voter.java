package diffcomm;

import java.util.ArrayList;

public class Voter {
	
	public int id;
	public String name;
//	public String community;
//	public int number_of_pubblications;
	public ArrayList<String> community;
	public ArrayList<Integer> number_of_pubblications;
	

	public Voter(){
	}
	
	public Voter(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getCommunity() {
		return community;
	}

	public void setCommunity(ArrayList<String> community) {
		this.community = community;
	}

	public ArrayList<Integer> getNumber_of_pubblications() {
		return number_of_pubblications;
	}

	public void setNumber_of_pubblications(
			ArrayList<Integer> number_of_pubblications) {
		this.number_of_pubblications = number_of_pubblications;
	}



//	public String getCommunity() {
//		return community;
//	}
//
//	public void setCommunity(String community) {
//		this.community = community;
//	}
//
//	public int getNumber_of_pubblications() {
//		return number_of_pubblications;
//	}
//
//	public void setNumber_of_pubblications(int number_of_pubblications) {
//		this.number_of_pubblications = number_of_pubblications;
//	}
	
}
