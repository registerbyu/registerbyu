package parser;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;

public class Section {

	public String courseID;
	public String newTitleCode;
	public String department;
	public String registrationType;
	public String courseNumber;
	
	public String sectionNumber;
	public String sectionType;
	public String courseName;
	public String professor;
	public String creditHours;
	
	public List<String> days;
	public List<String> startTimes;
	public List<String> endTimes;
	public List<String> locations;
	public List<TimePlace> timePlaces;
	
	public List<String> notes;
	public String seatsAvailable;
	public String classSize;
	public String waitList;
	
	public void insert(int i, String element) {
		
    	switch (i) {
			case 0: courseID = element;
		    		break;
		    		
			case 1: newTitleCode = element;
					break;
					
			case 2: department = element;
					break;
					
			case 3: registrationType = element;
					break;
					
			case 4: courseNumber = element;
					break;
					
			case 5: /*if (element.length() > 0)
						System.out.println("BEFORE SEC NUMBER: " + element);*/
					break;
					
			case 6: sectionNumber = element;
					break;
					
			case 7: /*if (element.length() > 0)
						System.out.println("AFTER SEC NUMBER: " + element);*/
					break;
					
			case 8: sectionType = element;
					break;
					
			case 9: courseName = element;
					break;
					
			case 10: professor = element;
					 break;
					
			case 11: creditHours = element;
					 break;
					
			case 12: days = splitNewline(element);
					 break;
					
			case 13: startTimes = splitNewline(element);
					 break;
					
			case 14: endTimes = splitNewline(element);
					 break;
					
			case 15: locations = splitNewline(element);
					 fillRemainingFields();
					 makeTimePlaces();
					 break;
			
			case 16: notes = splitNewline(element);
					 break;
			
			case 17: seatsAvailable = element.substring(0, element.indexOf("/") - 1);
					  classSize = element.substring(element.indexOf("/") + 2);
					  break;
					 
			case 18: waitList = element;
					 break;
					
			default: System.out.println("ERROR: Your index isn't between 0 and x!");
					 break;
    	}
	}
	
	/*
	* Some courses have different numbers of days, times, and locations
	* (e.g. search for "Beecher, Mark", his course has 3 days and times, but only one "TBA" for location)
	* This method adds "NULL" to fields that are lacking (so we can create objects with them in the future if needed)
	*/
	public void fillRemainingFields() {
		
		int maxSize = 0;
		if (days.size() > maxSize)
			maxSize = days.size();
		if (startTimes.size() > maxSize)
			maxSize = startTimes.size();
		if (endTimes.size() > maxSize)
			maxSize = endTimes.size();
		if (locations.size() > maxSize)
			maxSize = locations.size();
		
		while (days.size() < maxSize)
			days.add("TBA");
		while (startTimes.size() < maxSize)
			startTimes.add("TBA");
		while (endTimes.size() < maxSize)
			endTimes.add("TBA");
		while (locations.size() < maxSize)
			locations.add("TBA");
	}
	
	// Split a string on newline chars
	public List<String> splitNewline(String element) {
		
		String[] textStr = element.split("\\r?\\n");
		List<String> tempList = new ArrayList<>();
		for (int i = 0; i < textStr.length; i++)
			if (textStr[i].length() > 0)
				tempList.add(textStr[i]);
		return tempList;
	}
	
	// Prints all of the class variables
	public void print() {
		
		System.out.println();
		System.out.println("CourseID:\t" + courseID);
		System.out.println("newTitleCode:\t" + newTitleCode);
		System.out.println("department:\t" + department);
		System.out.println("registrationType:\t" + registrationType);
		System.out.println("courseNumber:\t" + courseNumber);
		System.out.println("sectionNumber:\t" + sectionNumber);
		System.out.println("sectionType:\t" + sectionType);
		System.out.println("courseName:\t" + courseName);
		System.out.println("professor:\t" + professor);
		System.out.println("creditHours:\t" + creditHours);
		
		System.out.println("day/startTime/endTime/location:");
		for (int i = 0; i < timePlaces.size(); i++)
			timePlaces.get(i).print("\t");
			
		System.out.println("Notes:");
		for (int i = 0; i < notes.size(); i++)
			System.out.println("\t" + (i+1) + "- " + notes.get(i));
		
		System.out.println("Seats available: " + seatsAvailable + " (out of " + classSize + ")");
		System.out.println("waitList: " + waitList);
		System.out.println();
	}
	
	/* Create a BasicDBObject out of this Section Object and return it */
	public BasicDBObject getDBObject() {
		
		BasicDBObject sectionObject = new BasicDBObject();
		
		sectionObject.put("sectionID", sectionNumber);
		sectionObject.put("type", sectionType);
		sectionObject.put("courseName", courseName);
		sectionObject.put("professor", professor);
		sectionObject.put("creditHours", creditHours);
		sectionObject.put("seatsAvailable", seatsAvailable);
		sectionObject.put("seatsTotal", classSize);
		sectionObject.put("waitList", waitList);
		
		// Add the Notes
		List<BasicDBObject> notesObject = new ArrayList<>();
		for (int i = 0; i < notes.size(); i++) {
			
			BasicDBObject tempNote = new BasicDBObject();
			tempNote.put("note", notes.get(i));
			notesObject.add(tempNote);
		}
		sectionObject.put("notes", notesObject);
		
		// Add the TimePlaces
		List<BasicDBObject> timeplaces = new ArrayList<>();
		for (int i = 0; i < timePlaces.size(); i++) {
			
			timeplaces.add(timePlaces.get(i).getDBObject());
		}
		sectionObject.put("timeplaces", timeplaces);
		
		return sectionObject;
	}

	public void makeTimePlaces() {
		
		timePlaces = new ArrayList<>();
		for (int i = 0; i < days.size(); i++) {
			
			timePlaces.add(new TimePlace(days.get(i), startTimes.get(i), endTimes.get(i), locations.get(i)));
		}
	}
}