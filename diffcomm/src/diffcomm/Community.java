package diffcomm;

// Contiene il nome della comunitˆ scientifica e l'arrayList con 
// le opinioni dei voters che appartengono a questa comunitˆ

// Contains the name of the scientific community and the arrayList 
// with the opinions of various voters that belong to this community

import java.util.ArrayList;

public class Community {
	
	public String name;
	public ArrayList<Opinion> community_opinion;

	public Community(){
	}
	
	public Community(String name, ArrayList<Opinion> op){
		this.name = name;
		this.community_opinion = op;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Opinion> getCommunity_opinion() {
		return community_opinion;
	}

	public void setCommunity_opinion(ArrayList<Opinion> community_opinion) {
		this.community_opinion = community_opinion;
	}
	
	public void aggiungiElemento(Opinion o){
		this.community_opinion.add(o);
	}
}
