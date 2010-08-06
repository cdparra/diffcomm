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
import dk.eobjects.metamodel.schema.Table;

import jsc.correlation.*;
import jsc.datastructures.PairedData;

public class DiffCommAlgorithm {

	public static String path = "data/ICWE_ICPS2/";
	
	public static void main(String[] args) {

		// Recupero i Voters
		HashMap<String, Voter> hmap = new HashMap<String, Voter>();
		readVoters(hmap);

		Set<String> keySet_v = hmap.keySet();
		for (String key : keySet_v) {
			Voter v = (Voter) hmap.get(key);
			System.out.println(v.id + " " + v.name + " " + v.community + " "
					+ v.number_of_pubblications);
		}

		// Recupero le opinioni divise per comunitˆ
		HashMap<String, Community> community_op = new HashMap<String, Community>();
		readOpinions(hmap, community_op);

		Set<String> keySet_c = community_op.keySet();
		for (String key : keySet_c) {
			Community c = community_op.get(key);
			System.out.println(c.name + " " + c.community_opinion.size());
			for (int i = 0; i < c.community_opinion.size(); i++) {
				System.out.println("     --> "
						+ c.getCommunity_opinion().get(i).voter.id + ", "
						+ c.getCommunity_opinion().get(i).candidate + ", "
						+ c.getCommunity_opinion().get(i).vote);
			}

		}

		ArrayList<CommunityOpinion> communityOpinions = new ArrayList<CommunityOpinion>();
		int k = getCandidatesNumber();

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
						System.out.println(o.voter.id);
//						Voter v = hmap.get(Integer.toString(o.voter.id));
//						int parziale_f = hmap.get(Integer.toString(o.voter.id)).number_of_pubblications;
						
						int idx = hmap.get(Integer.toString(o.voter.id)).getCommunity().indexOf(c.name);
						int parziale_f = hmap.get(Integer.toString(o.voter.id)).getNumber_of_pubblications().get(idx);
						int parziale_media = Integer.parseInt(o.vote)
								* parziale_f;
						media = media + parziale_media;
						f = f + parziale_f;
					}
				}

				opinions[i] = media / f;
				System.out.println("¥ " + c.name + ", cadidate: " + i
						+ ", opinion: " + opinions[i]);
			}

			CommunityOpinion coop = new CommunityOpinion(c.name, opinions);
			communityOpinions.add(coop);
		}

		Collections.sort(communityOpinions);

		System.out.println("° " + communityOpinions.size());
		for (CommunityOpinion o : communityOpinions) {
			double[] v = o.getVotes();
			for (int i = 0; i < k; i++) {
				int n = i + 1;
				System.out
						.println("---> " + o.community + " " + n + " " + v[i]);
			}
		}

		System.out.println("____________");
		
		writeToFile(path + "Result.csv",communityOpinions);
	}
	
	// Recupera i vari voters dal file Voters.csv e li inserisce in una HashMap
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

				System.out.println(voter + " " + name);

				Voter v = new Voter(Integer.parseInt(voter), name);
				setVoter(v);
				x.put(voter, v);

				stringRead = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Impossibile trovare Voter.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("----- FINE readVoters() -----");
	}

	// Setta la community e il numero di pubblicazioni per il voter preso in input
	public static void setVoter(Voter v) {
		File file = new File(path + "VoterMembership.csv");
		DataContext dataContext = DataContextFactory.createCsvDataContext(file,true);

		Table table = dataContext.getDefaultSchema().getTables()[0];

		Column ticketColumn = table.getColumnByName("VOTER");

		// Will print: "VOTER column nullable: false"
		System.out.println("VOTER column nullable: "
				+ ticketColumn.isNullable());

		OperatorType o = OperatorType.convertOperatorType("=");

		// SELECT * FROM VoterMembership WHERE voter=v.id
		Query q = new Query().select(table.getColumns()).from(table).where(
				ticketColumn, o, v.id);
		// Query q = new
		// Query().select(table.getColumns()).from(table).orderBy(ticketColumn);

		DataSet dataSet = dataContext.executeQuery(q);
		ArrayList<String> communi = new ArrayList<String>();
		ArrayList<Integer> numpubbl = new ArrayList<Integer>();

		while (dataSet.next()) {
			Row row = dataSet.getRow();

			// Extract values or do something similar with the row
			System.out.println("row: " + row);

			String comm = (String) row.getValue(table.getColumnByName("COMMUNITY"));
			System.out.println(comm);
			communi.add(comm);
			//v.setCommunity(comm);

			int pubbl = (Integer) row.getValue(table.getColumnByName("NUMBERS_OF_PUBBL"));
			System.out.println(pubbl);
			numpubbl.add(pubbl);
			//v.setNumber_of_pubblications(pubbl);
		}
		
		v.setCommunity(communi);
		v.setNumber_of_pubblications(numpubbl);
	}

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

				System.out.println(voter + " " + candidate + " " + vote);

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

//				if (community_op.containsKey(v.community)) {
//					Community c = community_op.get(v.community);
//					c.aggiungiElemento(op);
//				} else {
//					ArrayList<Opinion> list_op = new ArrayList<Opinion>();
//					list_op.add(op);
//					Community c = new Community(v.community, list_op);
//					community_op.put(v.community, c);
//				}

				stringRead = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Impossibile trovare opinions.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("----- FINE readOpinions() -----");
	}

	public static int getCandidatesNumber() {
		int candidates = 0;
		try {
			FileReader fr = new FileReader(path + "Candidates.csv");

			BufferedReader br = new BufferedReader(fr);

			String stringRead = br.readLine();
			stringRead = br.readLine();
			while (stringRead != null) {
				StringTokenizer st = new StringTokenizer(stringRead, ",");
				String candidate = st.nextToken();
				String name = st.nextToken();

				System.out.println(candidate + " " + name);

				candidates++;

				stringRead = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Impossibile trovare Candidates.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("candidates: " + candidates);
		return candidates;
	}

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

				System.out.println(communities + " " + name);

				x.put(communities, name);

				stringRead = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Impossibile trovare Communities.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("----- FINE readCommunities() -----");
	}
	
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
						correlation = "Nan";
					} else {
						double[] x = communityOpinions.get(i).getVotes();
						double[] y = communityOpinions.get(j).getVotes();
						PairedData pd = new PairedData(x, y);

						KendallCorrelation kt = new KendallCorrelation(pd);
						//correlation = String.format("%.2f", kt.getR());
						
						String n = Double.toString(kt.getR());
						
						if (n.equals("NaN")){
							correlation = "NaN";
						} else {
							BigDecimal bg = new BigDecimal(kt.getR());
							bg = bg.setScale(2, BigDecimal.ROUND_HALF_UP);
							
							BigDecimal b = new BigDecimal((bg.doubleValue()+1)*50);
							b = b.setScale(2, BigDecimal.ROUND_HALF_UP);
							correlation = Double.toString(b.doubleValue());
						}
					}
					
					System.out.println(w.get("\"" + communityOpinions.get(i).community + "\"") + "-"
							+ w.get("\"" + communityOpinions.get(j).community + "\"") + " = "
							+ correlation);
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
