package assignment3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.collections15.Transformer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldableNode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.Hypergraph;

public class AuthorRankwithQuery {

	public static void main(String args[]) throws IOException, ParseException {

		//Initializing Number of top others and number of top docs
		int numTopAuthors = 10;
		int numTopDocs = 300;
		
		//Creating utility object for authorid to authorname mapping
		utility utObj = new utility();
		Hypergraph<Integer, Integer> graphData = utObj.getGraphData();
		Transformer<Integer, String> vertexToAuthorMapping = utObj.getAuthorMapping();
		Map<Integer, String> authorNameMapping = utObj.getAuthorNameMapping();

		//Invoking getPageranksByQuery for both queries
		getPageranksByQuery(graphData, vertexToAuthorMapping, authorNameMapping, "data mining", numTopAuthors, numTopDocs);
		getPageranksByQuery(graphData, vertexToAuthorMapping, authorNameMapping, "information retrieval", numTopAuthors, numTopDocs);

	}

	public static void getPageranksByQuery(Hypergraph<Integer, Integer> graphData, Transformer<Integer, String> tf,
			Map<Integer, String> authorNameMapping, String query, int nTopAuthors, int nTopResults)
			throws IOException, ParseException {

		System.out.println("Query: " + query);
		DecimalFormat df = new DecimalFormat(".#######");

		//Converting vertex-author mapping to author-vertex mapping
		Map<Integer, Integer> authorToVertexMapping = new HashMap<Integer, Integer>();

		for (int i = 0; i < graphData.getVertexCount(); i++) {
			authorToVertexMapping.put(Integer.valueOf(tf.transform(i).trim()), i);
		}

		int authorsCount = graphData.getVertexCount();
		Map<Integer, Double> topAuthorsPriors = getPriors(query, nTopAuthors, nTopResults);
		List<Double> priorsList = new ArrayList<Double>();

		//Initialize priors list with 0
		for (int i = 0; i < authorsCount; i++) {
			priorsList.add(0.0);
		}

		//Updating priors list
		for (Entry<Integer, Double> e : topAuthorsPriors.entrySet()) {
			priorsList.add(authorToVertexMapping.get(e.getKey()), e.getValue());
		}

		//Page rank with priors
		Transformer<Integer, Double> prior_transform = new Transformer<Integer, Double>() {
			@Override
			public Double transform(Integer v) {
				return (Double) priorsList.get(v);
			}
		};

		//Calculate pagerank with priors
		Map<Integer, Double> pageranks = calcPageRank(graphData, prior_transform);

		int index = 1;
		
		// Print Result: author name, vertexid and pagerank
		System.out.printf("%-40s %-8s %s\n", "Author", "VertexId", "Pagerank");
		for (Entry<Integer, Double> v : pageranks.entrySet()) {
			
			System.out.printf("%-40s %-8s %s\n",
					authorNameMapping.get(Integer.valueOf(tf.transform(v.getKey()).trim())), v.getKey(),
					df.format(v.getValue()));

			index = index + 1;
			if (index > 10) {
				break;
			}
		}

	}

	public static Map<Integer, Double> getPriors(String queryString, int nTopAuthors, int nTopResults)
			throws IOException, ParseException {

		String indexPath = "author_index/author_index/";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Document d = reader.document(0);
		List<IndexableField> fields = d.getFields();
		Iterator<IndexableField> fieldsItr = fields.iterator();
		Analyzer analyzer = new StandardAnalyzer();
		searcher.setSimilarity(new BM25Similarity());
		QueryParser parser = new QueryParser("content", analyzer);
		StringBuilder allResults = new StringBuilder();
		
		Query query = parser.parse(QueryParser.escape(queryString));
		
		//Calculating top docs
		TopDocs results = searcher.search(query, nTopResults);

		//Number of hits
		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		// Print retrieved results
		ScoreDoc[] hits = results.scoreDocs;
		Map<Integer, Double> topAuthors = new HashMap<Integer, Double>();
		int rank = 1;
		double totalPrior = 0;
		
		
		for (int i = 0; i < hits.length; i++) {
		
			
			Document doc = searcher.doc(hits[i].doc);
			int authorId = Integer.valueOf(doc.get("authorid"));
			String result = authorId + " " + rank + " " + hits[i].score + " ";

			if (topAuthors.containsKey(authorId)) {
				Double currentProb = topAuthors.get(authorId);
				currentProb += hits[i].score;
				topAuthors.put(authorId, currentProb);
			} else {
				topAuthors.put(authorId, (double) hits[i].score);
			}

			totalPrior += hits[i].score;

			allResults.append(result);
			allResults.append("\n");
			rank++;
		}

		//Normalizing  priors
		for (Entry<Integer, Double> entry : topAuthors.entrySet()) {
			topAuthors.put(entry.getKey(), entry.getValue() / totalPrior);
		}

		System.out.println("Unique authors: " + topAuthors.size());

		reader.close();

		return topAuthors;
	}

	public static Map<Integer, Double> calcPageRank(Hypergraph<Integer, Integer> graph,
			Transformer<Integer, Double> priors) {

		PageRankWithPriors<Integer, Integer> alg = new PageRankWithPriors<Integer, Integer>(graph, priors, 0.1);
		
		//Damping factor = 0.85
		alg.setTolerance(0.85);
		
		//Page rank calculation
		alg.evaluate();

		Map<Integer, Double> pageranksMap = new HashMap<Integer, Double>();

		// Map vertex with page rank scores
		for (Integer v : graph.getVertices()) {
			pageranksMap.put(v, alg.getVertexScore(v));
		}

		//Sort map by pagerank score
		IntegerComparator bvc = new IntegerComparator(pageranksMap);
		TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);

		sorted_map.putAll(pageranksMap);

		return sorted_map;

	}

}