import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

// Class for Indexing
class Index
{
	private String corpusPath;
	public void setCorpusPath(String path)
	{
		corpusPath = path;
	}
	
	//Function to extract sting between tags
	public String extract(StringBuilder buf, String startTag, String endTag)
	{
		String stringBetweenTags = new String();
		int k1 = buf.indexOf(startTag);
		while(k1 > 0)    
		{
		   k1 += startTag.length();
		   int k2 = buf.indexOf(endTag,k1);
		      
		   if (k2>=0)
		   {
			   stringBetweenTags +=(" " + buf.substring(k1,k2).trim());  
		   }
		   
		   k1 = buf.indexOf(startTag, k2);
		}
		return stringBetweenTags;	  
	}
	
	//Function to read each file in corpus
	private String readFile(String file) throws IOException 
	{
		file = corpusPath+"\\"+file;
		FileReader fileReader = new FileReader (file);
		BufferedReader reader = new BufferedReader(fileReader);
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while((line = reader.readLine()) != null ) 
	    {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }

	    return stringBuilder.toString();
	}
	
	// Class constructor for Index Comparison using 4 types of analyzers
	public void indexComparision( String indexPath, File docDir,int type, PrintWriter out) throws CorruptIndexException,LockObtainFailedException,
	IOException
	{
		Analyzer analyzer = null;
		switch(type)
		{
		case 1: 
			analyzer = new StandardAnalyzer();
			break;
		case 2:
			analyzer = new StopAnalyzer();
			break;
		case 3:
			analyzer = new SimpleAnalyzer();
			break;
		case 4:
			analyzer = new KeywordAnalyzer();
		};
		
		File indexDir = new File(indexPath+"//"+analyzer.toString());
		final String INDEX_DIRECTORY = "G:/Sem3/Search/assignment 1/corpus/corpus";
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		assert(analyzer != null);
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter indexWriter = new IndexWriter(directory, iwc);
		
		for (File f: docDir.listFiles())
		{
			String fileName = f.getName();
			String readBuffer = readFile(fileName);
			StringBuilder builder = new StringBuilder(readBuffer);
			
			String startDocTag = "<DOC>";
			String endDocTag = "</DOC>";
			int docStart = builder.indexOf(startDocTag);
			while(docStart != -1)    
			{
			   docStart += startDocTag.length();
			   int docEnd = builder.indexOf(endDocTag,docStart);
			   
			   if(docEnd > 0)
			   {
				   StringBuilder document = new StringBuilder(builder.substring(docStart,docEnd).trim());
				   
				   Document doc = new Document();
				   String text = extract(document,"<TEXT>", "</TEXT>");
				   doc.add(new TextField("TEXT",text, Field.Store.YES));
				   indexWriter.addDocument(doc); 
			   }
			   
			   docStart = builder.indexOf(startDocTag, docEnd);
			}	   
		}

		indexWriter.forceMerge(1);
		indexWriter.commit();
		indexWriter.close();

		//////////////*Analysis*////////////////////////////////////////////////////////////
		
		
		//System.out.println("For " +analyzer.toString()+ "Analyser");
		out.println("For " +analyzer.toString()+ "Analyser");

		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));

		//System.out.println("Number of occurences of\"the(example for stopword)\" in the field\"TEXT\": "+indexReader.totalTermFreq(new Term("TEXT","the")));
		out.println("Number of occurences of\"the(example for stopword)\" in the field\"TEXT\": "+indexReader.totalTermFreq(new Term("TEXT","the")));
		

		Terms vocabulary = MultiFields.getTerms(indexReader,"TEXT");
		//System.out.println("Size of the vocabulary for TEXT field:"+vocabulary.size());
		out.println("Size of the vocabulary for TEXT field:"+vocabulary.size());
		
		//System.out.println("Number of documents that have at least one term for TEXT field: " +vocabulary.getDocCount());
		out.println("Number of documents that have at least one term for TEXT field: " +vocabulary.getDocCount());
		
		//System.out.println("Number of tokens for TEXT field:"+vocabulary.getSumTotalTermFreq());
		out.println("Number of tokens for TEXT field:"+vocabulary.getSumTotalTermFreq());
		
		//System.out.println("Number of postings for TEXT field:"+vocabulary.getSumDocFreq());
		out.println("Number of postings for TEXT field:"+vocabulary.getSumDocFreq());

		TermsEnum iterator = vocabulary.iterator();
		BytesRef byteRef = null;
		
		FileWriter outputFile = new FileWriter(indexPath+"//"+analyzer.toString()+"vocabulary");

		while((byteRef = iterator.next()) !=null) 
		{
			String term = byteRef.utf8ToString();
			outputFile.write(term+"\n");
		}
		
		out.println("\n");
		indexReader.close();
	}	
	
	
}

public class indexComparison
{
	public static void main(String args[]) throws IOException
	{
		File docDir = new File("G:/Sem3/Search/assignment 1/corpus/corpus2");
		
		assert(docDir.exists());
		
		Index index = new Index();
		index.setCorpusPath("G:/Sem3/Search/assignment 1/corpus/corpus2");
		PrintWriter out = new PrintWriter(new FileWriter("G:/Sem3/Search/assignment 1/output/indexComparisonOutput.txt"));
		for(int i=1; i<=4;++i)
		{
			
			index.indexComparision("G:/Sem3/Search/assignment 1/index", docDir, i, out);
			
		}
		out.close();
		System.out.println("End");
	}
}