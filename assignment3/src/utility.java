package assignment3;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;

//Utility class for file load
public class utility {
	
	
	private Transformer<Integer, String> tf;
	private Hypergraph<Integer,Integer> graphData;
	private Map<Integer,String> authorNameMapping;
	
	public utility() throws IOException {
		
		readGraphData();
		authorNameMapping();
		
	}

	public void readGraphData() throws IOException 
	{
		PajekNetReader pnr = new PajekNetReader(FactoryUtils.instantiateFactory(Object.class));
		Graph<Integer,Integer> g = new SparseGraph<Integer,Integer>();
	    pnr.load(System.getProperty("user.dir") + "/author_index/author.net", g);
		graphData = g;

		tf = pnr.getVertexLabeller();
	}
	
	public Hypergraph<Integer,Integer> getGraphData(){
		return graphData;
	}
	
	public void authorNameMapping() throws IOException{
		String indexPath = "author_index/author_index/";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));

		authorNameMapping = new HashMap<Integer,String>();
		
		for(int i=0;i<reader.maxDoc();i++){
			Document d = reader.document(i);
			authorNameMapping.put(Integer.valueOf(d.get("authorid")), d.get("authorName"));
		}
	}
	
	public Map<Integer,String> getAuthorNameMapping(){
		return authorNameMapping;
	}
	
	public Transformer<Integer, String> getAuthorMapping(){
		return tf;
	}

}