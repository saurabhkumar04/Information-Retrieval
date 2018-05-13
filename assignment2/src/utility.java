package IR.searchEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Saurabh Kumar 
//Code 4
//Date: 10/24/2017

public class utility {
	

	public List<String> buildQueryList(String path, String startTag, String endTag) throws IOException {
		List<String> queryList = new ArrayList<String>();

		
		StringBuffer fileText = new StringBuffer(readTextFile(path));

		
		int iStartIndex = fileText.indexOf(startTag) + startTag.length();
		int iEndIndex = fileText.indexOf(endTag, iStartIndex);

		while (iEndIndex > 0 && iStartIndex > 0) {

			String Query = fileText.substring(iStartIndex, iEndIndex).trim().replaceAll("\n", " ");
			
			queryList.add(Query);

			fileText = new StringBuffer(fileText.substring(iEndIndex));

			iStartIndex = fileText.indexOf(startTag) + startTag.length();
			iEndIndex = fileText.indexOf(endTag, iStartIndex);

		}

		return (queryList);
	}

	public String readTextFile(String file) throws IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader buffreader = null;
		try {
			FileReader freader = new FileReader(new File(file));
			buffreader = new BufferedReader(freader);
			String ls = System.getProperty("line.separator");

			String line = null;
			while ((line = buffreader.readLine()) != null) {
				sb.append(line.replaceAll(" +", " ")); 
				sb.append(ls);
			}

			buffreader.close();

		} catch (IOException e) {
			
			e.printStackTrace();
			if (buffreader != null)
				buffreader.close();

		}

		return sb.toString();

	}
	// Function to write file
	public void writeFile(String outFileName,StringBuilder allResults) throws IOException{
		File file = new File(outFileName);
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter(new FileWriter(file));
		    writer.write(allResults.toString());
		} finally {
		    if (writer != null) writer.close();
		}
	}

}