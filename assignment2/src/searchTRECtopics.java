package IR.searchEngine;

//Saurabh Kumar 
//Code 2
//Date: 10/24/2017

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.lucene.queryparser.classic.ParseException;


public class searchTRECtopics {
	public static void main(String[] args) throws IOException, ParseException {

		String indexPath = "G:/Sem3/Search/assignment 2/index/index";
		
		utility utObj = new utility();
		
		// Short Query List
		String sstartTag = "<title> Topic:";
		String sendTag = "<desc>";

		List<String> shortQueriesList = utObj.buildQueryList(
				"G:/Sem3/Search/assignment 2/topics.51-100", sstartTag, sendTag);

		System.out.println("Number of short queries: " + shortQueriesList.size());

		String estartTag = "<desc> Description:";
		String eendTag = "<smry>";

		List<String> longQueriesList = utObj.buildQueryList(
				"G:/Sem3/Search/assignment 2/topics.51-100", estartTag, eendTag);
		System.out.println("Number of long queries: " + longQueriesList.size());
		
		// Finding Top results
		findTopResults(shortQueriesList,indexPath,1000,"run-1","StigshortQueries");
		findTopResults(longQueriesList,indexPath,1000,"run-2","StiglongQueries");

	}
	
	public static void findTopResults(List<String> queriesList,String indexPath,int numOfResults,String runID,String outFileName) throws IOException, ParseException{
		int queryId = 51;
		StringBuilder results = new StringBuilder();
		easySearch eSearchObj = new easySearch();
		for(String query : queriesList){
			Map<String,Double> rankMap = eSearchObj.calcRank(indexPath,query);
			
			
			Iterator it = rankMap.entrySet().iterator();
			int rank = 1;
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
				String result = queryId + " " + "0" + " " + pair.getKey() + " "+ rank + " " + pair.getValue() + " " + runID;
				System.out.println(result);
				results.append(result);
				results.append("\n");
				
				// To avoid Exception
		        it.remove(); 
		        rank++;
		        if(rank >= numOfResults){
		        	break;
		        }
		    }
		    queryId++;
		}
		
		utility utObj = new utility();
		utObj.writeFile(outFileName, results);
		
	}
}
	