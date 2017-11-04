package com.irproject2.www;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class Reader1 {

	public static void main(String[] args) throws IOException {
		Reader1 r1 = new Reader1();
		try{
		r1.getPostings();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
}
	public void getPostings() throws IOException
	{
		/*
		 * there are two places where we can store the index... either RAM or
		 * Disk can be RAMDirectory or FSDirectory
		 */
		// create the analyzer object

		// index directory for storing our index
		// since we already have our index we need to add it to our directory
		File indexDir = new File("index/");
		Path p = indexDir.toPath();
		System.out.println(p);
		FSDirectory fsdirectory = FSDirectory.open(p);
		IndexReader reader = DirectoryReader.open(fsdirectory);
		Document d = new Document();
		HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<String, LinkedList<Integer>>();
		Fields fields = MultiFields.getFields(reader);
		LinkedList<Integer> docIdValues;
		PrintWriter pw = new PrintWriter("temp-inverted-index-1.txt","UTF-8");
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
				System.out.println(t+",fields------------" + f);
				//get the terms from each fields
				Terms terms = fields.terms(f);
				//iterator for each term
				TermsEnum termsIterator = terms.iterator();
				//initializing the bytesref
				BytesRef termText;
				int counter = 0;
				//looping through all the terms in a field
				//while (termsIterator.next() != null && counter++ < reader.numDocs())
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
						//pw.println(tempTermText+","+docID);
						
						//temp_dictionary.put(termText.utf8ToString(), docID);
					}
					//pw.println("field - "+f+",term - "+termText.utf8ToString()+",docID : "+docID);
					//System.out.println("doc ID : " +docID );

					//pw.println(termText.utf8ToString()+","+ docID);
				}
				System.out.println(temp_dictionary.size());
			}
		}
		reader.close();
		System.out.println("ok");
		System.out.println(invertedIndex.size());
		//System.out.println(invertedIndex);
		//pw.println(temp_dictionary);
		pw.println(invertedIndex);
		pw.close();
		String test1 = "тройк";
		String test2 = "emprunt";
		String test3 = "打ち出す";
		String test4= "merogi";
		String test5 = "horni";
		String test6 = "триллион";
		System.out.println(invertedIndex.get(test1));
		System.out.println(invertedIndex.get(test2));
		System.out.println(invertedIndex.get(test3));
		System.out.println(invertedIndex.get(test4));
		System.out.println(invertedIndex.get(test5));
		System.out.println(invertedIndex.get(test6));
	}
}
