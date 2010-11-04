package diffcomm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

import jsc.correlation.*;
import jsc.datastructures.PairedData;

public class DiffCommAlgorithm {

	public static String folder = "ICWE_ICPS_BPM";
	
	public static String path = "data/" + folder + "/";
	
	public static void main(String[] args) {

		// Sets the voters
		HashMap<String, Voter> voters = new HashMap<String, Voter>();
		readVoters(voters);

//		// Print voters
//		Set<String> keySet_v = voters.keySet();
//		for (String key : keySet_v) {
//			Voter v = (Voter) voters.get(key);
//			System.out.println(v.id + " " + v.name + " " + v.community + " "+ v.number_of_pubblications);
//		}

		// Set the list of opinions for each community 
		HashMap<String, Community> community_op = new HashMap<String, Community>();
		readOpinions(voters, community_op);

//		// Print the list of opinions for each community 
//		Set<String> keySet_c = community_op.keySet();
//		for (String key : keySet_c) {
//			Community c = community_op.get(key);
//			System.out.println(c.name + " " + c.community_opinion.size());
//			for (int i = 0; i < c.community_opinion.size(); i++) {
//				System.out.println("     --> "
//						+ c.getCommunity_opinion().get(i).voter.id + ", "
//						+ c.getCommunity_opinion().get(i).candidate + ", "
//						+ c.getCommunity_opinion().get(i).vote);
//			}
//
//		}

		ArrayList<CommunityOpinion> communityOpinions = new ArrayList<CommunityOpinion>();
		int k = getCandidatesNumber();

		// Per ogni comunitˆ presente nell'HashMap faccio la media ponderata delle varie opinioni
		// e le inserisco in un array (double[] opinions)
		// For each community in the HashMap we do the weighted average of the various opinions
		// and insert these into an array (double [] opinions)
		Set<String> keySet_d = community_op.keySet();
		for (String key : keySet_d) {
			Community c = community_op.get(key);
			ArrayList<Opinion> op = c.getCommunity_opinion();

			double[] opinions = new double[k];

			for (int i = 0; i < k; i++) {
				int media = 0;
				int f = 0;
				for (Opinion o : op) {
					if (Integer.parseInt(o.candidate) == i + 1) {
						
						int idx = voters.get(Integer.toString(o.voter.id)).getCommunity().indexOf(c.name);
						int parziale_f = voters.get(Integer.toString(o.voter.id)).getNumber_of_pubblications().get(idx);
						int parziale_media = Integer.parseInt(o.vote)* parziale_f;
						
						if (parziale_media != 0){
							media = media + parziale_media;
							f = f + parziale_f;
							
//							System.out.println("parziale f:" + parziale_f + " - parziale media:" + parziale_media + " - media:" + media + " - f:" + f);
						}
						}
				}

				if (f == 0) {
					opinions[i] = 0;
				} else { 
					double divisione = (double) media/ (double) f;
					double div = divisione * 100;
					double div2 = Math.round(div);
					double div3 = div2/100;

					opinions[i] = div3;
				}
				
//				System.out.println("¥ " + c.name + ", cadidate: " + (i+1) + ", opinion: " + opinions[i]);
			}

			CommunityOpinion coop = new CommunityOpinion(c.name, opinions);
			communityOpinions.add(coop);
		}

		// Ordino l'array delle communities
		// Sort the array of the communities
		Collections.sort(communityOpinions);

//		// Print the weighted average vote of various community for each candidate
//		for (CommunityOpinion o : communityOpinions) {
//			double[] v = o.getVotes();
//			for (int i = 0; i < k; i++) {
//				int n = i + 1;
//				System.out.println("---> " + o.community + " " + n + " " + v[i]);
//			}
//		}
//		System.out.println("____________");
		
		
		writeToFile(path + "Result.csv", communityOpinions);
	}
	
	// Legge il file Voter.csv e inserisce i vari voters in una Hashmap	
	// Reads the file Voter.csv and places the various voters in a HashMap
	public static void readVoters(HashMap<String, Voter> x) {
		try {
			FileReader fr = new FileReader(path + "Voter.csv");

			BufferedReader br = new BufferedReader(fr);

			String stringRead = br.readLine();
			stringRead = br.readLine();
			while (stringRead != null) {
				StringTokenizer st = new StringTokenizer(stringRead, ",");
				String voter = st.nextToken();
				String name = st.nextToken();

				//System.out.println(voter + " " + name);

				Voter v = new Voter(Integer.parseInt(voter), name);
				setVoter(v);
				x.put(voter, v);

				stringRead = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Impossibile to find Voter.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("----- FINE readVoters() -----");
	}

	
	// Setta la community e il numero di pubblicazioni per il voter preso in input
	// Sets the community and the number of publications for the voter that was taken in input
	public static void setVoter(Voter v) {
		File file = new File(path + "VoterMembership.csv");
		
		DataContext dataContext = DataContextFactory.createCsvDataContext(file,true);
		Table table = dataContext.getDefaultSchema().getTables()[0];
		Column voterColumn = table.getColumnByName("VOTER");

		Column pubblColumn = table.getColumnByName("NUMBERS_OF_PUBBL");
		//System.out.println("NUMBERS_OF_PUBBL column type: " + pubblColumn.getType());
		pubblColumn = pubblColumn.setType(ColumnType.INTEGER);
		//System.out.println("NUMBERS_OF_PUBBL column type: " + pubblColumn.getType());
		
		OperatorType o = OperatorType.convertOperatorType("=");

		// SELECT * FROM VoterMembership WHERE voter=v.id
		Query q = new Query().select(table.getColumns()).from(table).where(voterColumn, o, v.id);

		DataSet dataSet = dataContext.executeQuery(q);
		ArrayList<String> communi = new ArrayList<String>();
		ArrayList<Integer> numpubbl = new ArrayList<Integer>();

		while (dataSet.next()) {
			// Extract values or do something similar with the row
			Row row = dataSet.getRow();

			String comm = (String) row.getValue(table.getColumnByName("COMMUNITY"));
			//System.out.println(comm);
			communi.add(comm);

			int pubbl = (Integer) row.getValue(table.getColumnByName("NUMBERS_OF_PUBBL"));
			//System.out.println(pubbl);
			numpubbl.add(pubbl);
		}
		
		v.setCommunity(communi);
		v.setNumber_of_pubblications(numpubbl);
	}

	
	// Legge il file Opinions.csv, inserisce le varie opinions nella lista della comunitˆ a cui
	// appartiene il voter e inserisce le varie comunitˆ in una Hashmap
	// Reads the file Opinions.csv, places the various opinions in the list of the community 
	// that belongs to the voter and places the various communities in a HashMap
	public static void readOpinions(HashMap<String, Voter> x, HashMap<String, Community> community_op) {
		try {
			FileReader fr = new FileReader(path + "Opinions.csv");

			BufferedReader br = new BufferedReader(fr);

			String stringRead = br.readLine();
			stringRead = br.readLine();
			while (stringRead != null) {
				StringTokenizer st = new StringTokenizer(stringRead, ",");
				String voter = st.nextToken();
				String candidate = st.nextToken();
				String vote = st.nextToken();

				//System.out.println(voter + " " + candidate + " " + vote);

				Voter v = x.get(voter);

				Opinion op = new Opinion(v, candidate, vote);
				
				ArrayList<String> communi = new ArrayList<String>();
				communi = v.getCommunity();
				
				for(String co : communi){
					if (community_op.containsKey(co)) {
						Community c = community_op.get(co);
						c.aggiungiElemento(op);
					} else {
						ArrayList<Opinion> list_op = new ArrayList<Opinion>();
						list_op.add(op);
						Community c = new Community(co, list_op);
						community_op.put(co, c);
					}
				}

				stringRead = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Impossibile to find Opinions.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	
	// Legge il file Candidates.csv e ritorna il numero di candidati
	// Reads the file Candidates.csv and returns the numbers of candidates
	public static int getCandidatesNumber() {
		int candidates = 0;
		try {
			FileReader fr = new FileReader(path + "Candidates.csv");

			BufferedReader br = new BufferedReader(fr);

			String stringRead = br.readLine();
			stringRead = br.readLine();
			while (stringRead != null) {
				//StringTokenizer st = new StringTokenizer(stringRead, ",");
				//String candidate = st.nextToken();
				//String name = st.nextToken();

				//System.out.println(candidate + " " + name);

				candidates++;

				stringRead = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Impossibile to find Candidates.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return candidates;
	}

	
	// Legge il file Communities.csv e inserisce le varie communities e i rispettivi nomi in una Hashmap
	// Reads the file Communities.csv and places the various communities and their names in a HashMap
	public static void readCommunities(HashMap<String, String> x) {
		try {
			FileReader fr = new FileReader(path + "Communities.csv");

			BufferedReader br = new BufferedReader(fr);

			String stringRead = br.readLine();
			stringRead = br.readLine();
			while (stringRead != null) {
				StringTokenizer st = new StringTokenizer(stringRead, ",");
				String communities = st.nextToken();
				String name = st.nextToken();

				//System.out.println(communities + " " + name);

				x.put(communities, name);

				stringRead = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Impossibile to find Communities.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("----- FINE readCommunities() -----");
	}
	
	
	// Calcola la correlazione fra le varie comunitˆ scientifiche e scrive il file Result.csv
	// Calculates the correlation between the various scientific communities and writes the file Result.csv
	public static void writeToFile(String filename, ArrayList<CommunityOpinion> communityOpinions) {
		
		HashMap<String, String> w = new HashMap<String, String>();
		readCommunities(w);

		BufferedWriter bufferedWriter = null;

		try {

			// Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(filename));
			
			bufferedWriter.write("\"COMMUNITY_1\",\"COMMUNITY2\",\"DIFFERENCE\"");
			
			int size = communityOpinions.size();
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					String correlation = null;
					if(j<i){
						correlation = "NaN";
					} else {
						
						// x e y contengono le medie dei voti della comunitˆ i e j
						double[] x = communityOpinions.get(i).getVotes();
						double[] y = communityOpinions.get(j).getVotes();
						
						PairedData pd = new PairedData(x, y);

						// Kendall Tau correlation between x and y
						KendallCorrelation kt = new KendallCorrelation(pd);
						
						String n = Double.toString(kt.getR());
						
						if (n.equals("NaN")){
							correlation = "NaN";
						} else {
							BigDecimal bg = new BigDecimal(kt.getR());
                            bg = bg.setScale(2, BigDecimal.ROUND_HALF_UP);
                            
                            // trasformation in /100
                            BigDecimal b = new BigDecimal((bg.doubleValue()+1)*50);
                            b = b.setScale(2, BigDecimal.ROUND_HALF_UP);
                            correlation = Double.toString(b.doubleValue());
						}
					}
					
					//System.out.println(w.get("\"" + communityOpinions.get(i).community + "\"") + "-"
					//		+ w.get("\"" + communityOpinions.get(j).community + "\"") + " = "
					//		+ correlation);
					bufferedWriter.newLine();
					bufferedWriter.write(w.get("\"" + communityOpinions.get(i).community + "\"")
							+ "," + w.get("\"" + communityOpinions.get(j).community + "\"") + ",\""
							+ correlation + "\"");
				}
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
