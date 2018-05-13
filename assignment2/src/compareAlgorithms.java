package IR.searchEngine;

//Saurabh Kumar 
//Code 3
//Date: 10/24/2017

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {

	public static void main(String args[]) {

		try {

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
			
			
			compareAlgorithms cAlgoObj = new compareAlgorithms();

			// defining objects for different Algorithms
			cAlgoObj.findQuery(new ClassicSimilarity(),shortQueriesList,"run-1","ClassicshortQuery");
			cAlgoObj.findQuery(new ClassicSimilarity(),longQueriesList,"run-1","ClassiclongQuery");
			
			cAlgoObj.findQuery(new BM25Similarity(),shortQueriesList,"run-1","BM25shortQuery");
			cAlgoObj.findQuery(new BM25Similarity(),longQueriesList,"run-1","BM25longQuery");
			
			cAlgoObj.findQuery(new LMDirichletSimilarity(),shortQueriesList,"run-1","LMDshortQuery");
			cAlgoObj.findQuery(new LMDirichletSimilarity(),longQueriesList,"run-1","LMDlongQuery");
			
			cAlgoObj.findQuery(new LMJelinekMercerSimilarity(0.7f),shortQueriesList,"run-1","LMJMshortQuery");
			cAlgoObj.findQuery(new LMJelinekMercerSimilarity(0.7f),longQueriesList,"run-1","LMJMlongQuery");

		} catch (IOException e1) {
			
			e1.printStackTrace();
		} catch (ParseException e) {
			
			e.printStackTrace();
		}


		
	}
	// Searching Query
	public void findQuery(Similarity sAlgo, List<String> queryList,String runID,String outFileName) throws IOException, ParseException {
		
		System.out.println("Finding results for " + outFileName);
		String index = "G:/Sem3/Search/assignment 2/index/index";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		
		Analyzer analyzer = new StandardAnalyzer();
		searcher.setSimilarity(sAlgo);
		QueryParser parser = new QueryParser("TEXT", analyzer);


		StringBuilder allResults = new StringBuilder();
		int queryId = 51;
		for (String queryString : queryList) {
		

			
			Query query = parser.parse(QueryParser.escape(queryString));
			
			TopDocs results = searcher.search(query, 1000);

			int numTotalHits = results.totalHits;
			
			ScoreDoc[] hits = results.scoreDocs;
			int rank = 1;
			
			for (int i = 0; i < hits.length; i++) {
			
				Document doc = searcher.doc(hits[i].doc);
				String result = queryId + " " + "0" + " " +doc.get("DOCNO") + " "+ rank + " " + hits[i].score + " " + runID;
				allResults.append(result);
				allResults.append("\n");

				rank++;
			}
					
			queryId++;
		}
		
		reader.close();
		
		System.out.println("Writing results into "+ outFileName);
		
		utility utObj = new utility();
		utObj.writeFile(outFileName, allResults);

	}

}