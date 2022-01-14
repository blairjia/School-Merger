//package ClosestSchools;
/*
* Author: 
* Implements the closest pair of points recursive algorithm
* on locations of K-12 schools in Vermont obtained from http://geodata.vermont.gov/datasets/vt-school-locations-k-12

*/

import java.io.File;
import java.util.Scanner;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.io.File;


public class Main {


	public static void main(String[] args) throws IOException{

		//Creates an ArrayList containing School objects from the .csv file
		// Based on https://stackoverflow.com/questions/49599194/reading-csv-file-into-an-arrayliststudent-java
		String line = null;
		ArrayList<School> schoolList = new ArrayList <School>();
		// You may have to adjust the file address in the following line to your computer
		BufferedReader br = new BufferedReader(new FileReader("\\VT_School_Locations__K12.csv"));//YOU WILL HAVE TO EDIT THIS LINE TO WHEREVER YOUR .CSV FILE IS STORED
		if ((line=br.readLine())==null){
			return;
		}
		while ((line = br.readLine())!=null) {
			String[] temp = line.split(",");
			schoolList.add(new School(temp[4],Double.parseDouble(temp[0]),Double.parseDouble(temp[1])));
		}


		//Preprocess the data to create two sorted arrayLists (one by X-coordinate and one by Y-coordinate):
		ArrayList<School> Xsorted = new ArrayList <School>();
		ArrayList<School> Ysorted = new ArrayList <School>();
		Collections.sort(schoolList, new SortbyX());
		Xsorted.addAll(schoolList);
		Collections.sort(schoolList, new SortbyY());
		Ysorted.addAll(schoolList);

		//Run the Recursive Algorithm
		School[] cp = new School[2];
		cp = ClosestPoints(Xsorted,Ysorted);
		if(cp[0]!=null)
			System.out.println("The two closest schools are "+ cp[0].name + " and " + cp[1].name +".");

	}

	public static School[] ClosestPoints(ArrayList<School> sLx, ArrayList<School> sLy){
		// Recursive divide and conquer algorithm for closest points
		// sLx should be sorted by x coordinate and sLy should be sorted by y coordinate
		// Returns an array containing the two closest School objects

		School[] closestPair = new School[2];

		//base case/brute force
		if (sLx.size() <= 3) {
			
			ArrayList<School> potentialPair = new ArrayList <School>();//create to put potential pairs in
			potentialPair.add(sLx.get(0));//add something in there, so we can remove
			potentialPair.add(sLx.get(1));//add something in there, so we can remove
			double minD = 9999999;//big number to start
			for(int i=0; i<sLx.size()-1; i++) {
				for(int j=i+1; j<sLx.size(); j++) {
					double dist = schoolDistance(sLx.get(i),sLx.get(j));
					if ( dist < minD ) {
						minD = dist;//update minD with the smaller distance
						potentialPair.remove(0);//remove the old data
						potentialPair.remove(0);
						potentialPair.add(sLx.get(i));//add the new schools
						potentialPair.add(sLx.get(j));
					}
				}
			}
			closestPair[0] = potentialPair.get(0);//add the schools to closestPair array and return.
			closestPair[1] = potentialPair.get(1);

			return closestPair;
		}

		else {
		
		int midindex = sLx.size()/2; //finding the middle index
		double midline = sLx.get(midindex).getX();//the x coordinate of the middle index of sLx.

		List<School> leftHalfXSorted = sLx.subList(0, midindex);//takes left half of the list of schools
		ArrayList<School> leftHalfXSortedArL = new ArrayList<>(leftHalfXSorted.size());//turn into arraylist
		leftHalfXSortedArL.addAll(leftHalfXSorted);

		List<School> rightHalfXSorted = sLx.subList(midindex,sLx.size());//takes right half of the list of schools
		ArrayList<School> rightHalfXSortedArL = new ArrayList<>(rightHalfXSorted.size());//turn into arraylist
		rightHalfXSortedArL.addAll(rightHalfXSorted);
				
		ArrayList<School> leftHalfYSorted = new ArrayList<School>();
		ArrayList<School> rightHalfYSorted = new ArrayList<School>();
		for(int l =0;l<sLy.size();l++) {
			if (leftHalfXSorted.contains(sLy.get(l))) {
				
				leftHalfYSorted.add(sLy.get(l));
			}
			else {
				rightHalfYSorted.add(sLy.get(l));				
			}
				
		}


		School[] leftSchools = ClosestPoints(leftHalfXSortedArL,leftHalfYSorted );//recursion run for left half
		School[] rightSchools = ClosestPoints(rightHalfXSortedArL,rightHalfYSorted );//recursion run for right half


		double delta = Math.min(schoolDistance(leftSchools[0],leftSchools[1]),schoolDistance(rightSchools[0],rightSchools[1]) );//smallest distance between schools of right half or left half
		

		if (schoolDistance(leftSchools[0],leftSchools[1]) > schoolDistance(rightSchools[0],rightSchools[1])) {//depending on the min distance, either add the 2 schools in the left half or the 2 schools in the right half.
			closestPair[0]=rightSchools[0];
			closestPair[1]=rightSchools[1];
		}
		else {
			closestPair[0]=leftSchools[0];
			closestPair[1]=leftSchools[1];
		}
				
		double midminusdelta = midline - delta;//the maximum place we have to search on the left half
		double midadddelta = midline + delta;// the maximum place we have to search on the right half.

		ArrayList<School> XWithinDelta = new ArrayList <School>();//new arraylist of all the schools within the delta area	
		for(int i=0;i<sLx.size();i++) {
			if(sLy.get(i).getX() > midminusdelta && sLy.get(i).getX() < midadddelta) {
				XWithinDelta.add(sLy.get(i));//add all the schools within the area into this arraylist	
			}

		}
		
		if(XWithinDelta.size() <2)//if there are less than 2 schools in this area, return closestPair
			return closestPair;
		
		ArrayList<School> potentialPair = new ArrayList <School>();
		potentialPair.add(XWithinDelta.get(0));//add something in there, so we can remove later
		potentialPair.add(XWithinDelta.get(1));//add something in there, so we can remove later
		
		int max = 0;
		if (XWithinDelta.size()<7) //if there are less than 7 other points to check, check however many remaining points are left
			max = XWithinDelta.size();
		else 
			max = 7;//otherwise, check with the next 7 points
		
		double deltaPrime = 999999;//big number
		for(int j=0;j<XWithinDelta.size();j++) {
			for(int k=j+1; k<max;k++) {//check each point with the next however many points, at most 7. 
				double curr = schoolDistance(XWithinDelta.get(j), XWithinDelta.get(k));
				if (curr<deltaPrime ) {//if the distance is smaller than deltaPrime
					deltaPrime = curr;// update deltaprime
					potentialPair.remove(0);//update the potential pairs by removing the former 2 closest schools with the new schools
					potentialPair.remove(0);
					potentialPair.add(XWithinDelta.get(j));
					potentialPair.add(XWithinDelta.get(k));

				}
			}
		}
		
		if (deltaPrime < delta) {//check which distance is smaller
			closestPair[0] = potentialPair.get(0);//change to the closest pair 
			closestPair[1] = potentialPair.get(1);

		}

	}
		return closestPair;	//return the closest pair
	}

	
	private static double schoolDistance(School s1, School s2) {//equation to find distance between two points

		return Math.sqrt( (s1.getX()-s2.getX()) * (s1.getX()-s2.getX()) 
				+ (s1.getY()-s2.getY()) * (s1.getY()-s2.getY()) );	
	}
}