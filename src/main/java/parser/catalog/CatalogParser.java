package parser.catalog;

import catalogData.httpCourseDownloader;
import com.mongodb.*;
import org.json.JSONArray;
import org.json.JSONException;
import parser.instructor.InstructorParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class CatalogParser {
	
	public static int index;								  // Index for the current element within the section (0-18)
	public static final int MAX_INDEX_BEFORE_INSERTION = 19;  // 19 elements (between '#' symbols) per section
	public static List<Section> sections = new ArrayList<>(); // A list of sections (one per full section parsed)
	public static Section curSection = new Section();         // The current section to insert into (added to sections list when done)

    public static void main(String[] args) throws UnknownHostException, JSONException {

        String fileName = System.getProperty("user.dir") + "/ParseableFall2014Catalog.txt";
        parseAndUpdateDatabase(fileName);
    }

    public static void parseAndUpdateDatabase(String fileName) throws JSONException {
        index = 0;
        File file = new File(fileName);
        Map professorMap = InstructorParser.getInstructorMap();
        try {

            BufferedReader reader = new BufferedReader(new FileReader(file));
            boolean firstLine = true;
            String line = "";
            String temp;
            while ((temp = reader.readLine()) != null) { // If the line doesn't have "#" in it, append the next line

                if (firstLine)
                    firstLine = false;
                else
                    line += "\n";
                line += temp;
                while (line.contains("#")) {

                    insertElementIntoSection(line.substring(0, line.indexOf("#")));
                    line = line.substring(line.indexOf("#") + 1);
                }
            }
            reader.close();
        }
        catch (IOException e) {

            System.out.println("Error: FILE WAS NOT FOUND");
            e.printStackTrace();
        }

        Map<String, String> professorCodes = InstructorParser.getInstructorMap();
        for (Section s: sections) {

             if (professorCodes.containsKey(s.professor)){
                s.rateMyProfId = professorCodes.get(s.professor);
                System.out.println(s.professor + " MATCH " + s.rateMyProfId);
             }
             else{
                 s.rateMyProfId = "";
             }

        }


        System.out.println("Success in parsing!");
        System.out.println("Total Sections Parsed: " + sections.size() + "\n");

        // Insert the courses into the database
        if (sections.size() > 0) {

            DB db = getDB();

            // Add the departments for displaying a list
            addDepartments(db);

            // Drop the course collection (so I can keep testing)
            dropCourseCollection(db);

            // Insert new courses into the db
            insertCoursesIntoDB(db);

            // Immediately after inserting all courses, try printing them out FROM the db
            //printCoursesFromDB(db);
        }
        else
            System.out.println("No Sections to insert!");
    }

    /**
     * Add all of the unique departments from this list into the departments list
     * @param db
     */
    public static void addDepartments(DB db) {
    	
/*    	System.out.println("\nAdding the unique departments now");
    	// Fill a list with all the unique departments from the new sections
    	List<String> departmentList = new ArrayList<>();
    	String[] deptArray = httpCourseDownloader.getDepartments();
    	for (int i = 0; i < deptArray.length; i++)
    		departmentList.add(deptArray[i].replaceAll("[+]", " "));
    	
    	System.out.print("There were a total of " + departmentList.size() + " departments found (");
    	// Remove departments that are already in the database
    	DBCollection departmentCollection = db.getCollection("department");
    	for (int i = 0; i < departmentList.size(); i++) {
    		
            BasicDBObject query = new BasicDBObject("name", departmentList.get(i));
    		
            // If this department was found
            if (departmentCollection.find(query).count() > 0) {
            	
            	departmentList.remove(i);
            	i--;
            }
    	}
    	System.out.println(departmentList.size() + " unique)\n");
    	
    	// If there are new departments, add them to the DB
    	if (departmentList.size() > 0) {
    		
    		System.out.println("Inserting departments now..");
        	// Change the array to a list to determine
        	BasicDBObject[] departmentArray = new BasicDBObject[departmentList.size()];
        	for (int i = 0; i < departmentList.size(); i++) {
        		
        		departmentArray[i] = new BasicDBObject();
        		departmentArray[i].put("name", departmentList.get(i));
        	}
        	departmentCollection.insert(departmentArray);
        	System.out.println("Done!\n");
    	}
*/
    }
    
    /**
     * Insert all of the sections into given courses in the course collection of the database
     * STRUCTURE: Course document holds a list of Sections, which hold a list of TimePlaces
     * @param db Database to insert the courses into
     */
    public static void insertCoursesIntoDB(DB db) throws JSONException {
    	
    	// Map of course IDs that point to a list of section DATABASE OBJECTS that have that course ID
    	Map<String, List<BasicDBObject>> courseMap = new HashMap<>();
    	
    	// Map of course IDs that point to a Course object (holding more details for the course) 
    	Map<String, Course> courseInfoMap = new HashMap<>();
    	
    	// Fill these two maps by iterating through the list of sections
    	for (int i = 0; i < sections.size(); i++) {
    		
    		if (courseMap.containsKey(sections.get(i).courseID))
    			courseMap.get(sections.get(i).courseID).add(sections.get(i).getDBObject());
    		else {


                Section s = sections.get(i);
                String outcomes_str = httpCourseDownloader.getCourseOutcomes(s.courseID, s.newTitleCode);
                List<String> outcomes = new ArrayList<String>();

                JSONArray jsonArray = new JSONArray(outcomes_str);
                for(int j = 0; j < jsonArray.length(); j++){
                    System.out.println(" JOSN--" + jsonArray.get(j).toString());
                     outcomes.add(jsonArray.get(j).toString());
                }

    			courseMap.put(s.courseID, new ArrayList<BasicDBObject>(Arrays.asList(s.getDBObject())));
    			courseInfoMap.put(s.courseID, new Course(s.courseID, s.courseName, outcomes, s.newTitleCode, s.department, s.registrationType, s.courseNumber));
    		}
    	}
    	
    	// Create an ArrayList of BasicDBObjects (one for each course) called courseObjects
    	List<BasicDBObject> courseObjects = new ArrayList<>();
    	for (Map.Entry<String, List<BasicDBObject>> curCourse : courseMap.entrySet()) {
    		
    		Course c = courseInfoMap.get(curCourse.getKey());
    		BasicDBObject newCourse = new BasicDBObject();
    		
    		newCourse.put("courseID", curCourse.getKey());
    		newCourse.put("courseName", c.courseName);
            newCourse.put("outcomes", c.outcomes);
    		newCourse.put("newTitleCode", c.newTitleCode);
    		newCourse.put("department", c.department);
    		newCourse.put("registrationType", c.registrationType);
    		newCourse.put("courseNumber", c.courseNumber);
    		newCourse.put("sections", curCourse.getValue());
    		
    		courseObjects.add(newCourse);
    	}
    	
    	// Change format from ArrayList to an Array (for inserting)
    	BasicDBObject[] courseArray = new BasicDBObject[courseObjects.size()];
    	for (int i = 0; i < courseObjects.size(); i++)
    		courseArray[i] = courseObjects.get(i);
    		
    	// Insert into the "course" collection in our DB
    	
		DBCollection courseCollection = db.getCollection("course");
		System.out.println("Start inserting into DB");
		
		long startTime = System.currentTimeMillis();
//		courseCollection.insert(courseArray);       //TODO UNCOMMENT TO UPDATE DATABASE
		long endTime = System.currentTimeMillis();
		
		System.out.println("Done inserting, took " + ((endTime - startTime)/1000.0) + " seconds\n");
    }
    
    /**
     *  Print all of the documents in the "course" collection (from the DB database parameter)
     *  They're printed in order of courseID
     */
    public static void printCoursesFromDB(DB db) {
    	
    	// Print the count in sections
    	DBCollection courseCollection = db.getCollection("course");
    	
    	// Sort condition is passed to the query
    	BasicDBObject orderBy = new BasicDBObject("courseID", 0);
    	
    	// find() all courses and sort() by the sort condition (courseID)
    	DBCursor docs = courseCollection.find().sort(orderBy);
    	while (docs.hasNext()) {
    		
    		DBObject doc = docs.next();
    		System.out.println("Course ID: " + doc.get("courseID") + "\tName: " + doc.get("courseName") + 
    				/*"\tNTC: " + doc.get("newTitleCode") + */"\tdept: " + doc.get("department") 
    				/*+ "\tregType: " + doc.get("registrationType")*/);
    	}
    	System.out.println("\nYour DB has a total of " + (courseCollection.find().count()) + " courses.");
    }
    
    /**
     * Drop the entire "course" collection from our DB (used for testing)
     * @param db The Database to get the course collection from and drop it
     */
    public static void dropCourseCollection(DB db) {
    	
    	System.out.println("\nDropping the course collection now..");
    	
    	long startTime = System.currentTimeMillis();
    	db.getCollection("course").drop();
    	long endTime = System.currentTimeMillis();
    	
    	System.out.println("Done dropping!  This took " + ((endTime - startTime)/1000.0) + " seconds");
    }
    
    /**
     * For every element, place it into the current section
     * If index reaches MAX_INDEX_BEFORE_INSERTION, finish off a section by adding it to the section-list and start a new section
     * @param nextElement Next item to be placed into the current Section object
     */
    public static void insertElementIntoSection(String nextElement) {
    	
    	// Set the element using the current index before incrementing the index
    	curSection.setElement(index, nextElement);
    	index++;
    	
    	// If index equals this max index variable, this means the parser has finished one section
    	// and will move onto the next.  We'll finish off this section and start a new one
    	if (index == MAX_INDEX_BEFORE_INSERTION) {
    	    		
    		sections.add(curSection);
    		curSection = new Section();
    		index = 0;
    	}
    	
    	// This shouldn't be reached unless the code is modified incorrectly
    	if (index > MAX_INDEX_BEFORE_INSERTION)
    		System.out.println("Error: Index shouldn't be > " + MAX_INDEX_BEFORE_INSERTION + " (it is currently " + index + ")");
    }

    public static DB getDB() {
    	
        String dbUser = "admin";
        String dbPassword = "ad428min";

        // Connect to our database
        MongoClientURI uri = new MongoClientURI("mongodb://" + dbUser + ":" + dbPassword +
                "@mongo.andyetitcopmiles.com:27017/classreg");
        MongoClient client;
		try {
			client = new MongoClient(uri);
	        DB db = client.getDB(uri.getDatabase());
	        return db;
		} catch (UnknownHostException e) {

			System.out.println("COULDN\'T GET THE DB");
			e.printStackTrace();
		}
		return null;
    }
}