package com.irproject2.www;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

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

public class Daat {

	public static int daatAndComparisons = 0;
	public static int daatOrComparisons = 0;

	public static void main(String[] args) throws IOException {
		File indexDir = new File("index/");
		Path p = indexDir.toPath();
		System.out.println(p);
		FSDirectory fsdirectory = FSDirectory.open(p);
		IndexReader reader = DirectoryReader.open(fsdirectory);
		Document d = new Document();
		HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<String, LinkedList<Integer>>();
		Fields fields = MultiFields.getFields(reader);
		LinkedList<Integer> docIdValues;
		// PrintWriter pw = new PrintWriter("temp-taat-1.txt", "UTF-8");
		Map<String, Integer> temp_dictionary = new HashMap<String, Integer>();
		int t = 0;
		// iterating thro each field.. includes text_fr, text_de
		for (String f : fields) {
			// checking if the field contains text_ field
			if (f.contains("text_")) {
				t++;
				// creating a new linked list for storing the Doc IDs
				// docIDs = new LinkedList<Integer>();
				// print the fields
				System.out.println(t + ",fields------------" + f);
				// get the terms from each fields
				Terms terms = fields.terms(f);
				// iterator for each term
				TermsEnum termsIterator = terms.iterator();
				// initializing the bytesref
				BytesRef termText;
				int counter = 0;
				// looping through all the terms in a field
				// while (termsIterator.next() != null && counter++ <
				// reader.numDocs())
				while (termsIterator.next() != null) {
					// storing each term in termText
					termText = termsIterator.term();
					// System.out.println("Field : " + f + " ,terms : " +
					// termText.utf8ToString());
					// invertedIndex.put(termText.utf8ToString(),new
					// LinkedList<Integer>());
					PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader, f, termText);
					// debug
					// System.out.println("postingsenum --"+postingsEnum);
					// System.out.println(postingsEnum.toString());
					docIdValues = new LinkedList<Integer>();
					while (postingsEnum.nextDoc() != PostingsEnum.NO_MORE_DOCS) {
						String tempTermText = termText.utf8ToString();
						int docID = postingsEnum.docID();
						if (invertedIndex.containsKey(tempTermText)) {
							// retrieve the linked list value from this key
							docIdValues = invertedIndex.get(tempTermText);
							// append the doc ID of this key to the linked list
							docIdValues.add(docID);
							// sort it in ascending order
							docIdValues.sort(null);
							// append the string, linked list back to the
							// hashmap
							invertedIndex.put(tempTermText, docIdValues);
						} else {
							// the term is the first of it or only one in the
							// index
							LinkedList<Integer> tempDocIdValues = new LinkedList<Integer>();
							tempDocIdValues.add(docID);
							invertedIndex.put(tempTermText, tempDocIdValues);
						}
						// pw.println(tempTermText+","+docID);

						// temp_dictionary.put(termText.utf8ToString(), docID);
					}
					// pw.println("field - "+f+",term -
					// "+termText.utf8ToString()+",docID : "+docID);
					// System.out.println("doc ID : " +docID );

					// pw.println(termText.utf8ToString()+","+ docID);
				}
				System.out.println(temp_dictionary.size());
			}
		}
		reader.close();
		System.out.println("ok");
		System.out.println(invertedIndex.size());
		// System.out.println(invertedIndex);
		// pw.println(temp_dictionary);
		// pw.println(invertedIndex);
		// pw.close();
		Daat daatObject = new Daat();
		// no match
		String[] queryTerms = { "тройк", "emprunt", "打ち出す", "merogi", "horni", "триллион" };
		// 1 match
		String[] queryTerms1 = { "chanda", "magdalena", "slova", "maleva" };
		// 1 match
		String[] queryTerms2 = { "銘", "ファンダメンタルズ", "深セン", "参照" };
		// no match
		String[] queryTerms3 = { "銘", "ファンダメンタルズ", "深セン", "参照", "прореагирова", "шум" };
		// daatObject.getDaatAnd1(queryTerms3, invertedIndex);
		Daat.getDaatOr(queryTerms3, invertedIndex);
	}

	public static LinkedList<Integer> getDaatAnd(String[] terms, HashMap<String, LinkedList<Integer>> invertedIndex) {
		ArrayList<LinkedList<Integer>> myTermsList = new ArrayList<LinkedList<Integer>>();
		LinkedList<Integer> referenceLinkedList = new LinkedList<Integer>();
		LinkedList<Integer> innerLinkedList = new LinkedList<Integer>();
		ArrayList<Integer> pointerList = new ArrayList<Integer>();
		LinkedList<Integer> finalLinkedList = new LinkedList<Integer>();
		int numberOfTerms = terms.length;
		// get the linked lists of all the terms
		for (int i = 0; i < numberOfTerms; i++) {
			myTermsList.add(invertedIndex.get(terms[i]));
			pointerList.add(0);
		}
		// put the list with the smallest term into the reference linked list
		int min_length = 10000;
		int indexShortestList = 0;
		for (int j = 0; j < myTermsList.size(); j++) {
			if (myTermsList.get(j).size() < min_length) {
				indexShortestList = j;
				min_length = myTermsList.get(j).size();
			}
			// min_length = myTermsList.get(j).size();
		}
		System.out.println("shortest linked list size " + indexShortestList);
		System.out.println(myTermsList.get(indexShortestList));
		referenceLinkedList = myTermsList.get(indexShortestList);
		myTermsList.remove(indexShortestList);
		myTermsList.add(0, referenceLinkedList);
		int referenceLinkedListValue = 0;
		int match = 1;
		int innerLinkedListValue = 0;
		int pointerValue = 0;
		// int l = 0;
		if (referenceLinkedList.size() > 0) {
			for (int k = 0; k < referenceLinkedList.size(); k++) {
				referenceLinkedListValue = referenceLinkedList.get(k);
				match = 1;
				if (myTermsList.size() == 0)
					break;
				for (int l = 1; l < myTermsList.size(); l++) {
					// daatAndComparisons++;
					// get the linkedlist we are working with right now
					innerLinkedList = myTermsList.get(l);
					System.out.println("inner LL ---->" + innerLinkedList);
					// get the current pointer value of the inner list we are
					// working with right now
					pointerValue = pointerList.get(l);
					// get the current inner linked list pointed value
					innerLinkedListValue = innerLinkedList.get(pointerList.get(l));
					// case 1
					if (innerLinkedListValue == referenceLinkedListValue) {
						match++;
						if (match == numberOfTerms) {
							// add to the final array list
							finalLinkedList.add(innerLinkedListValue);
						}
					}
					// case 2
					while (innerLinkedListValue < referenceLinkedListValue) {
						if (innerLinkedListValue < referenceLinkedListValue) {
							// check if there are next elements
							if (pointerValue == innerLinkedList.size() - 1) {
								break;
							}
							// 3 cases
							// can be in the last position
							// can be in the safer inner indices
							// can be in the last before position
							if (pointerValue + 1 < innerLinkedList.size()) {
								daatAndComparisons++;
								pointerValue++;
							}

							innerLinkedListValue = innerLinkedList.get(pointerValue);
							// if (pointerValue < (innerLinkedList.size()-1)) {
							// pointerValue++;
							// innerLinkedListValue =
							// innerLinkedList.get(pointerValue);
							// }
							// if(pointerValue == (innerLinkedList.size()-1))
							// {
							// innerLinkedListValue =
							// innerLinkedList.get(pointerValue);
							// }
							// innerLinkedListValue =
							// innerLinkedList.get(pointerValue);
						}
						if (innerLinkedListValue == referenceLinkedListValue) {
							match++;
							if (match == numberOfTerms) {
								// add to the final array list
								finalLinkedList.add(innerLinkedListValue);
							}
						}
						innerLinkedListValue = innerLinkedList.get(pointerValue);
					}
					// case 3
					// if(innerLinkedListValue > referenceLinkedListValue)
					// {
					// //do nothing
					//
					// }
					pointerList.remove(l);
					pointerList.add(l, pointerValue);
					System.out.println(pointerList);
				}
				pointerList.remove(0);
				pointerList.add(0, k);
			}
		}
		System.out.println(finalLinkedList);
		return finalLinkedList;
	}

	public static LinkedList<Integer> getDaatOr(String[] terms, HashMap<String, LinkedList<Integer>> invertedIndex) {
		ArrayList<LinkedList<Integer>> myTermsList = new ArrayList<LinkedList<Integer>>();
		LinkedList<Integer> referenceLinkedList = new LinkedList<Integer>();
		LinkedList<Integer> innerLinkedList = new LinkedList<Integer>();
		ArrayList<Integer> pointerList = new ArrayList<Integer>();
		LinkedList<Integer> finalLinkedList = new LinkedList<Integer>();
		ArrayList<Integer> tempFinalArrayList = new ArrayList<Integer>();
		int numberOfTerms = terms.length;
		// get the linked lists of all the terms
		for (int i = 0; i < numberOfTerms; i++) {
			myTermsList.add(invertedIndex.get(terms[i]));
			pointerList.add(0);
		}
		// put the list with the smallest term into the reference linked list
		int max_length = 0;
		int indexLargestList = 0;
		for (int j = 0; j < myTermsList.size(); j++) {
			if (myTermsList.get(j).size() > max_length) {
				indexLargestList = j;
				max_length = myTermsList.get(j).size();
			}
		}
		System.out.println("shortest linked list size " + indexLargestList);
		System.out.println(myTermsList.get(indexLargestList));
		System.out.println(myTermsList.get(indexLargestList).size());
		referenceLinkedList = myTermsList.get(indexLargestList);
		// remove the shortest list from the arraylist
		myTermsList.remove(indexLargestList);
		myTermsList.add(0, referenceLinkedList);
		if (referenceLinkedList.size() > 0) {
		for (int i = 0; i < referenceLinkedList.size(); i++) {
			// add the element pointed by the current list
			tempFinalArrayList.add(referenceLinkedList.get(i));
			// just start with the largest list
			if(myTermsList.size()==0)
				break;
			for (int l = 1; l < myTermsList.size(); l++) {
				
				// get the linkedlist we are working with right now
				innerLinkedList = myTermsList.get(l);
				// System.out.println("inner LL ---->" + innerLinkedList);
				if (i < innerLinkedList.size()) {
					tempFinalArrayList.add(innerLinkedList.get(i));
				} else {
					// do nothing
					// inner array has been fully added
				}
			}

		}
	}
		// now eliminate the duplicates from the tempfinallist
		tempFinalArrayList.sort(null);
		// put it into a set to eliminate duplicates for verification
		Set<Integer> newSet = new HashSet<Integer>();
		newSet.addAll(tempFinalArrayList);
		System.out.println("set size---------" + newSet.size());
		int prevElement = 0;
		for (int j = 0; j < tempFinalArrayList.size(); j++) {
			daatOrComparisons++;
			if (j == 0)
				prevElement = 0;
			// current element retrieved
			int currentElement = tempFinalArrayList.get(j);
			if (currentElement == prevElement) {
				// skip - this is a duplicate element
			} else {
				finalLinkedList.add(currentElement);
			}
			// assign the curr to be the next prev element
			prevElement = currentElement;

		}
		// System.out.println("my duplicate removal
		// system----"+finalLinkedList.size());
		return finalLinkedList;

	}

	public static int getDaatAndComparisons() {
		return daatAndComparisons;
	}

	public static int getDaatOrComparisons() {
		return daatOrComparisons;
	}

}
