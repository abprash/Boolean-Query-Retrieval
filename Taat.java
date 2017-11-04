package com.irproject2.www;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class Taat {
	public static int taatAndComparisons = 0;
	public static int taatOrComparisons = 0;
	public static void main(String[] args) throws IOException
	{
		File indexDir = new File("index/");
		Path p = indexDir.toPath();
		System.out.println(p);
		FSDirectory fsdirectory = FSDirectory.open(p);
		IndexReader reader = DirectoryReader.open(fsdirectory);
		HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<String, LinkedList<Integer>>();
		Fields fields = MultiFields.getFields(reader);
		LinkedList<Integer> docIdValues;
		PrintWriter pw = new PrintWriter("temp-taat-1.txt","UTF-8");
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
		Taat taatObject = new Taat();
		//no match
		//String[] queryTerms = {"тройк","emprunt","打ち出す", "merogi","horni","триллион"};
		//1 match
		String[] queryTerms1 ={"chanda","magdalena","slova","maleva"};
		getTaatOr(queryTerms1,invertedIndex);
	}
	
	public static LinkedList<Integer> getTaatAnd(String[] terms, HashMap<String,LinkedList<Integer>> invertedIndex)
	{
		LinkedList<Integer> tempNewIntermediateList = new LinkedList<Integer>();
		if(terms.length>=2)
		{
			
			
			//get the input query terms
			for(int i = 0; i < terms.length; i++)
			{
				System.out.println(i);
				LinkedList<Integer> termList = invertedIndex.get(terms[i]);
				
				System.out.println(terms[i]);
				if(i == 0)
				{
					//this is the first term
					tempNewIntermediateList = termList;
					//System.out.println("1--"+tempNewIntermediateList);
					//intermediateList 
					continue;
				}
				else
				{
					if(termList.size()==0)
						break;
					LinkedList<Integer> newIntermediateList = new LinkedList<Integer>();
					//we now have 2 lists to compare
					for(int element1 : termList )
					{
						
						//System.out.println(i+"---"+termList);
						//iterate through the new intermediate list if it has element
						for(int element2 : tempNewIntermediateList)
						{
							taatAndComparisons++;
							//System.out.println(i+"---"+tempNewIntermediateList);
							if(element1 == element2)
							{
								if (newIntermediateList.indexOf(element2) == -1)
								{
									newIntermediateList.add(element2);
									System.out.println("new one --"+newIntermediateList);
								}
								else
								{
									//about to add a duplicate element
									(newIntermediateList = new LinkedList<Integer>()).add(element2);
									System.out.println("new one --"+newIntermediateList);
								}
							}
						}
					}
					tempNewIntermediateList = newIntermediateList;
				}
				
			}
			System.out.println(tempNewIntermediateList);
		}
		else
		{
			System.out.println("Query terms too few for boolean processing");
		}
		return tempNewIntermediateList;
	}
	
	
	
	public static LinkedList<Integer> getTaatOr(String[] terms, HashMap<String,LinkedList<Integer>> invertedIndex)
	{
		LinkedList<Integer> finalList = new LinkedList<Integer>();
		LinkedList<Integer> termList = new LinkedList<Integer>();
		LinkedList<Integer> tempNewIntermediateList = new LinkedList<Integer>();
		LinkedList<Integer> x =  new LinkedList<Integer>();
		if(terms.length>=2)
		{
			tempNewIntermediateList = new LinkedList<Integer>();
			
			//get the input query terms
			for(int i = 0; i < terms.length; i++)
			{
				System.out.println(i);
				termList = invertedIndex.get(terms[i]);
				System.out.println(terms[i]);
				if(i == 0)
				{
					//this is the first term
					tempNewIntermediateList = termList;
					//System.out.println("1--"+tempNewIntermediateList);
					//intermediateList 
					continue;
				}
				else
				{
					if(termList.size()>=1 && tempNewIntermediateList.size()>=1)
					{}	//proceed
					else
						break;
					//we now have 2 lists to compare
					
					if(finalList.size()==0)
					{
						tempNewIntermediateList.addAll(termList);
						finalList = tempNewIntermediateList;
					}
					else
						finalList.addAll(termList);
					finalList.sort(null);

					//System.out.println("temp has---"+x);
					
				}
				
			}
			//finalList = tempNewIntermediateList;
			//finalList.sort(null);
			//System.out.println("final size--"+x.size());
			int prevElement = 0;
			for(int j =0;j<finalList.size();j++)
			{
				taatOrComparisons++;
				if(j==0)
					prevElement = 0;
				//current element retrieved
				int currentElement = tempNewIntermediateList.get(j);
				if(currentElement == prevElement)
				{
					//skip - this is a duplicate element
				}
				else
				{
					x.add(currentElement);
				}
				//assign the curr to be the next prev element
				prevElement = currentElement;	
			}
			System.out.println("final --"+x);
			System.out.println("final size--"+x.size());
		}
		else
		{
			System.out.println("Query terms too few for boolean processing");
		}
		return x;
	}
	
	public static int getTaatAndComparisons()
	{
		return taatAndComparisons;
	}
	
	public static int getTaatOrComparisons()
	{
		return taatOrComparisons;
	}

}
