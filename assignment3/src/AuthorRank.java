package assignment3;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.text.DecimalFormat;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;

public class AuthorRank {

	public static void main(String args[]) throws IOException {
		
		//Initializing Number of top others
		int numtopAuthors = 10;

		//Creating utility object for authorid to authorname mapping
		utility utObj = new utility();

		//Graph
		Hypergraph<Integer, Integer> graphData = utObj.getGraphData();
		Transformer<Integer, String> vertexToAuthorMapping = utObj.getAuthorMapping();
		Map<Integer, String> authorNameMapping = utObj.getAuthorNameMapping();

		DecimalFormat df = new DecimalFormat(".#######");
		
		//Calculating page rank without prior
		Map<Integer, Double> pageranks = calcPageRank(graphData);
		int index = 1;
		
		// Print Result: author name, vertexid and pagerank
		System.out.printf("%-40s %-8s %s\n", "Author", "VertexId", "Pagerank");
		for (Entry<Integer, Double> v : pageranks.entrySet()) {
			
			
			System.out.printf("%-40s %-8s %s\n",
					authorNameMapping.get(Integer.valueOf(vertexToAuthorMapping.transform(v.getKey()).trim())), v.getKey(),
					df.format(v.getValue()));

			index = index + 1;
			
			if (index > numtopAuthors) {
				break;
			}
		}
	}

	public static Map<Integer, Double> calcPageRank(Hypergraph<Integer, Integer> graph) {
		
		// Alpha = 0.1
		PageRank<Integer, Integer> alg = new PageRank<Integer, Integer>(graph, 0.1);
		
		//Damping factor 0.85
		alg.setTolerance(0.85);
		
		//Calculating pagerank for all vertexes
		alg.evaluate();

		Map<Integer, Double> pageranksMap = new HashMap<Integer, Double>();

		for (Integer v : graph.getVertices()) {
			pageranksMap.put(v, alg.getVertexScore(v));
		}

		//Sorting by pagerank value
		IntegerComparator bvc = new IntegerComparator(pageranksMap);
		TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);

		sorted_map.putAll(pageranksMap);

		return sorted_map;

	}

}

//Class for sorting map
class IntegerComparator implements Comparator<Integer> {
	Map<Integer, Double> map;

	public IntegerComparator(Map<Integer, Double> base) {
		this.map = base;
	}

	public int compare(Integer a, Integer b) {
		if (map.get(a) >= map.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}
}