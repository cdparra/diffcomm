package diffcomm;

// Contiene il nome della comunitˆ scientifica e l'array con i voti
// ricavati facendo la media ponderata delle diverse opinioni dei 
// voters appartenenti a questa comunitˆ

// Contains the name of the scientific community and the array with the votes
// obtained by the weighted average of the opinions of various
// voters belonging to this community

public class CommunityOpinion implements Comparable<CommunityOpinion>{
	
	public String community;
	public double[] votes;
	
	public CommunityOpinion(){
	}
	
	public CommunityOpinion(String voter, double[] vote){
		this.community = voter;
		this.votes = vote;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public double[] getVotes() {
		return votes;
	}

	public void setVotes(double[] votes) {
		this.votes = votes;
	}

	public int compareTo(CommunityOpinion o) {
		return this.getCommunity().compareTo(o.getCommunity());
	}

}
