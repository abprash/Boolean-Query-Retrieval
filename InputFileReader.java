package com.irproject2.www;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class InputFileReader {
	public static void main(String[] args)
	{
		try
		{
		BufferedReader breader = new BufferedReader(new FileReader(args[2]));
		PrintWriter pwriter = new PrintWriter(args[1],"UTF-8");
		//String line =breader.readLine();
		//get the entire postings list
		HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<String, LinkedList<Integer>>();
		invertedIndex = InputFileReader.getPostings(args[0]);
		//pwriter.println(invertedIndex);
		String line = new String();
		while((line =breader.readLine())!=null)
		{
			//split based on space
			String[] terms = line.split(" ");
			//invoke postings, taat and, taat or, daat and , daat or
			for(int i=0;i<terms.length;i++)
			{
				pwriter.println("GetPostings");
				//term
				pwriter.println(terms[i]);
				//postings list
				pwriter.println("Postings list: "+terms[i]);
			}
			//taat and
			pwriter.println("TaatAnd");
			for(int i=0;i<terms.length;i++)
			{
				pwriter.print(terms[i]+" ");
			}
			pwriter.println();
			LinkedList<Integer> taatand = Taat.getTaatAnd(terms, invertedIndex);
			//results
			pwriter.print("Results: ");//method invocation);
			for(int i : taatand)
				pwriter.print(i+" ");
			pwriter.println();
			pwriter.println("Number of documents in results: "+taatand.size());//ter
			pwriter.println("Number of comparisons: "+Taat.getTaatAndComparisons());
			//taat or
			pwriter.println("TaatOr");
			for(int i=0;i<terms.length;i++)
			{
				pwriter.print(terms[i]+" ");
			}
			pwriter.println();
			LinkedList<Integer> taator = Taat.getTaatOr(terms, invertedIndex);
			//results
			pwriter.print("Results: ");//method invocation);
			for(int i : taator)
				pwriter.print(i+" ");
			pwriter.println();
			pwriter.println("Number of documents in results: "+taator.size());
			pwriter.println("Number of comparisons: "+Taat.getTaatOrComparisons());
			//daat and
			pwriter.println("DaatAnd");
			for(int i=0;i<terms.length;i++)
			{
				pwriter.print(terms[i]+" ");
			}
			pwriter.println();
			LinkedList<Integer> daatand = Daat.getDaatAnd(terms, invertedIndex);
			//results
			pwriter.print("Results: ");//method invocation);
			for(int i : daatand)
				pwriter.print(i+" ");
			pwriter.println();
			pwriter.println("Number of documents in results: "+taator.size());
			pwriter.println("Number of comparisons: "+Daat.daatAndComparisons);
			//daat or
			pwriter.println("DaatOr");
			for(int i=0;i<terms.length;i++)
			{
				pwriter.print(terms[i]+" ");
			}
			pwriter.println();
			LinkedList<Integer> daator = Daat.getDaatOr(terms, invertedIndex);
			//results
			pwriter.print("Results: ");//method invocation);
			for(int i : daator)
				pwriter.print(i+" ");
			pwriter.println();
			pwriter.println("Number of documents in results: "+daator.size());
			pwriter.println("Number of comparisons: "+Daat.daatOrComparisons);
		}
		breader.close();
		pwriter.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	public static HashMap<String, LinkedList<Integer>>  getPostings(String path) throws IOException
	{
		/*
		 * there are two places where we can store the index... either RAM or
		 * Disk can be RAMDirectory or FSDirectory
		 */
		// create the analyzer object

		// since we already have our index we need to add it to our directory
		File indexDir = new File(path);
		Path p = indexDir.toPath();
		System.out.println(p);
		FSDirectory fsdirectory = FSDirectory.open(p);
		IndexReader reader = DirectoryReader.open(fsdirectory);

		HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<String, LinkedList<Integer>>();
		Fields fields = MultiFields.getFields(reader);
		LinkedList<Integer> docIdValues;
		Map<String,Integer> temp_dictionary = new HashMap<String,Integer>(); 
		int t = 0;
		//iterating thro each field.. includes text_fr, text_de
		for (String f : fields) {
			//checking if the field contains text_ field
			if (f.contains("text_")) {
				t++;
				//creating a new linked list for storing the Doc IDs
				//docIDs = new LinkedList<Integer>();
				//print the fields
				//System.out.println(t+",fields------------" + f);
				//get the terms from each fields
				Terms terms = fields.terms(f);
				//iterator for each term
				TermsEnum termsIterator = terms.iterator();
				//initializing the bytesref
				BytesRef termText;
				//int counter = 0;
				//looping through all the terms in a field
				while(termsIterator.next() != null)
				{
					//storing each term in termText
					termText = termsIterator.term();
					//System.out.println("Field : " + f + " ,terms : " + termText.utf8ToString());
					//invertedIndex.put(termText.utf8ToString(),new LinkedList<Integer>());
					PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader, f, termText);
					//debug
					//System.out.println("postingsenum --"+postingsEnum);
					// System.out.println(postingsEnum.toString());
					docIdValues = new LinkedList<Integer>();
					while(postingsEnum.nextDoc() != PostingsEnum.NO_MORE_DOCS)
					{
						String tempTermText = termText.utf8ToString();
						int docID =postingsEnum.docID();
						if(invertedIndex.containsKey(tempTermText))
						{
							//retrieve the linked list value from this key
							docIdValues = invertedIndex.get(tempTermText);
							//append the doc ID of this key to the linked list
							docIdValues.add(docID);
							//sort it in ascending order
							docIdValues.sort(null);
							//append the string, linked list back to the hashmap
							invertedIndex.put(tempTermText,docIdValues);
						}
						else
						{
							//the term is the first of it or only one in the index
							LinkedList<Integer> tempDocIdValues = new LinkedList<Integer>();
							tempDocIdValues.add(docID);
							invertedIndex.put(tempTermText, tempDocIdValues);
						}
						
					}
					
				}
				System.out.println(temp_dictionary.size());
			}
		}
		reader.close();
		System.out.println(invertedIndex.size());
		return invertedIndex;
	}

}
