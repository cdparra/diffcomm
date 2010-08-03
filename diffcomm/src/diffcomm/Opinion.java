package diffcomm;

public class Opinion {
	
	public Voter voter;
	public String candidate;
	public String vote;
	
	public Opinion(){
	}
	
	public Opinion(Voter voter, String candidate, String vote){
		this.voter = voter;
		this.candidate = candidate;
		this.vote = vote;
	}

	public Voter getVoter() {
		return voter;
	}

	public void setVoter(Voter voter) {
		this.voter = voter;
	}

	public String getCandidate() {
		return candidate;
	}

	public void setCandidate(String candidate) {
		this.candidate = candidate;
	}

	public String getVote() {
		return vote;
	}

	public void setVote(String vote) {
		this.vote = vote;
	}

}
