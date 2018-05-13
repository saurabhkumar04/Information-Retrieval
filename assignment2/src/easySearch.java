package IR.searchEngine;

// Saurabh Kumar 
// Code 1
// Date: 10/24/2017

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.search.similarities.ClassicSimilarity;

public class easySearch {

	public static Map<String, Double> calcRank(String indexPath, String query_) throws IOException, ParseException {

		// Defining Parser
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(QueryParser.escape(query_));
		Set<Term> queryTerms = new LinkedHashSet<Term>();

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);

		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);

		int totalNumOfDocs = reader.maxDoc();

		// Default similarity function
		ClassicSimilarity cSim = new ClassicSimilarity();

		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();

		Map<String, Double> rankTable = new HashMap<String, Double>();

		for (Term t : queryTerms) {

			// Calculating IDF for each term
			int df = reader.docFreq(new Term("TEXT", t.text()));
			if(df <= 0){
				df = 1;
			}
			
			double idf = Math.log(1 + totalNumOfDocs / df);


			// Calculate TF for each term
			for (int i = 0; i < leafContexts.size(); i++) {
				
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = leafContext.docBase;
				int numberOfDoc = leafContext.reader().maxDoc();

				Map<String, Double> docLenghMap = new HashMap<String, Double>();

				for (int docId = 0; docId < numberOfDoc; docId++) {

					float normDocLeng = cSim.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
					// Get length of the document
					float docLeng = 1 / (normDocLeng * normDocLeng);
					String key = searcher.doc(docId + startDocNo).get("DOCNO");
					Double value = (double) docLeng;

					docLenghMap.put(key, value);

				}

				// Get term frequency
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(), "TEXT", new BytesRef(t.text()));

				int doc;
				if (de != null) {
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {

						double previoudtfidf = 0;
						String key = searcher.doc(de.docID() + startDocNo).get("DOCNO");
						if (rankTable.containsKey(key)) {
							previoudtfidf = rankTable.get(key);
						}

						// TF calculation
						int tf_num = de.freq();
						double tf_den = docLenghMap.get(key);
						double tf = tf_num / tf_den;
						double tfidf = tf * idf;

						rankTable.put(key, previoudtfidf + tfidf);
					}

				}

			}

		}

		valueComparator sortingComparator = new valueComparator(rankTable);

		TreeMap<String, Double> sortedMap = new TreeMap<String, Double>(sortingComparator);

		sortedMap.putAll(rankTable);

		return (sortedMap);
	}

}

class valueComparator implements Comparator<String> {
	Map<String, Double> base;

	public valueComparator(Map<String, Double> base) {
		this.base = base;
	}


	public int compare(String a, String b) {
		
		// Decreasing Order Sort
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}
}