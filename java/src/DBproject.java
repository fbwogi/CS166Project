/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) {//1
	    String model;
	    int plane_ID;
	    String make;
	    int age;
	    int num_seats;
	    String query;  
		
		//plane_ID
	    do {
			System.out.print("Input plane ID number: ");
	       	try { // read the integer, parse it and break.
       			plane_ID = Integer.parseInt(in.readLine());
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

	    //model
	    do {
		System.out.print("Input plane model: ");
	       	try { // read the integer, parse it and break.
       			model = Integer.parseInt(in.readLine());
				if (model.length() <= 0 || model.length() > 64) {
			   		throw new RuntimeException("Invalid input. Plane model cannot be null or exceed 64 characters";
			 	}
					
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);
	    

	    //make
	    do {
			System.out.print("Input plane make: ");
	       	try { // read the integer, parse it and break.
       			make = Integer.parseInt(in.readLine());
				if (make.length() <= 0 || make.length() > 32) {
			    	throw new RuntimeException("Invalid input. Plane model cannot be null or exceed 64 characters");
				}
			}
    		break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

		//age
	    do {
			System.out.print("Input plane age: ");
	       	try { // read the integer, parse it and break.
       			model = Integer.parseInt(in.readLine());
				if(age < 0 || age > 30) {
			    	throw new RuntimeException("Age cannot be negative or greater than 30");
				}
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

	    //num_seats
	    do {
			System.out.print("Input number of seats on plane: ");
	       	try { // read the integer, parse it and break.
       			num_seats = Integer.parseInt(in.readLine());
				if(age < 0 || age > 500) {
			    	throw new RuntimeException("Number of plane seats cannot be negative or greater than 250");
				}
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

		//query
	    try {
			query = "INSERT INTO Plane (ID, make, model, age, num_seats) VALUES (" + plane_ID + ", \'" + make + "\', \'" + model + "\', \'" + age + ", " + num_seats + "):";
			esql.executeUpdate(query);
	    }catch (Exception e){
			System.err.println("Query failed: " + e.getMessage());
	    }

	}

	public static void AddPilot(DBproject esql) {//2
	    int pilot_ID;
	    String name;
	    String nationality;
	    String query;

		//pilot_ID
	    do {
			System.out.print("Input pilot ID number: ");
	       	try { // read the integer, parse it and break.
       			pilot_ID = Integer.parseInt(in.readLine());
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

	    //name
	    do {
			System.out.print("Input pilot name: ");
	       	try { // read the integer, parse it and break.
       			make = Integer.parseInt(in.readLine());
				if (name.length() <= 0 || name.length() > 128) {
			    	throw new RuntimeException("Invalid input. Pilot name cannot exceed 128 characters");
				}
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

		//nationality
	    do {
			System.out.print("Please make your choice: ");
	       	try { // read the integer, parse it and break.
       			model = Integer.parseInt(in.readLine());
			if(nationality.length() <= 0 || nationality.length() > 24) {
			    throw new RuntimeException("INVALID INPUT! Pilot's nationality cannot be 0, negative or greater than 24 characters");
			}
    		break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

	//query   
	 	try {
			query = "INSERT INTO Pilots (ID, name, nationality) VALUES (" + pilot_ID + ", \'" + name + ", " + nationality + "):";
			esql.executeUpdate(query);
	    }catch (Exception e){
			System.err.println("Query failed: " + e.getMessage());
	    }
	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
	    int flight_num;
	    int cost;
	    int num_sold;
	    int num_stops;
	    String actual_depart_date;
	    //String actual_depart_time;
	    String actual_arrive_date;
	    //String actual_arrive_time;
	    //String source;
	    //String destination;
		String arrival_airport;
		String departure_airport;
	    LocalDate departDate;
	    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


	    //flight_num
	    do {
		System.out.print("Input flight number: ");
		try {
		    flight_num = Integer.parseInt(in.readLine());
		    break;
		}catch(Exception e){
		    System.out.println("Your input is invalid!");
		}
	    }while(true);

	    //cost
	    do {
			System.out.print("Input flight cost: ");
	       	try { // read the integer, parse it and break.
       			model = Integer.parseInt(in.readLine());
				if (cost <= 0) {
			    	throw new RuntimeException("Invalid input. Cost cannot be less than or equal to 0");
				}
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);
	    
		//num_sold
	    do {
			System.out.print("Input number of seats sold: ");
	       	try { // read the integer, parse it and break.
       			num_sold = Integer.parseInt(in.readLine());
				if (num_sold < 0) {
			    	throw new RuntimeException("Invalid input. Cannot have negatve number of seats sold");
				}
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

	    //num_stops 
	    do {
			System.out.print("Input number of stops: ");
	       	try { // read the integer, parse it and break.
       			num_stops = Integer.parseInt(in.readLine());
				if (num_stops < 0) {
			    	throw new RuntimeException("Invalid input. Cannot have negative number of stops.");
				}
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

		//	String actual_arrive_date;
	    //String actual_arrive_time;
	    do {
			System.out.print("Input actual departure date: ");
	       	try { // read the integer, parse it and break.
       			actual_departure_date = in.readLine();
				//actual_departure time = in.readLine();
		        LocalDate add = LocalDate.parse(actual_departure_date, format);
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);
		
		//actual_arrival_date
	    do {
		System.out.print("Input actual departure date: ");
	       	try { // read the integer, parse it and break.
       			actual_arrival_date = in.readLine();
				//actual_arrival_time = in.readLine();
		        LocalDate aad = LocalDate.parse(actual_arrival_date, format);
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);

	    //source
	    /*do {
		System.out.print("Input flight source: ");
	       	try { // read the integer, parse it and break.
       			source = in.readLine();
				if(source <= 0 || source > 5) {
			    	throw new RuntimeException("Flight source cannot be 0 or negative or exceed 5 characters.");
				}
    			break;
       		}catch (Exception e) {
       			System.out.println("Your input is invalid!");
       			continue;
       		}//end try
	    }while (true);
		*/
		//destination
		/*do {
			System.out.print("Input flight destination: ");
			try { // read the integer, parse it and break.
				source = in.readLine();
				if(source <= 0 || source > 5) {
					throw new RuntimeException("Flight source cannot be 0 or negative or exceed 5 characters.");
				}
			    break;
			}catch (Exception e) {
			    System.out.println("Your input is invalid!");
			    continue;
			}//end try
		}*/
			//arrival_airport
			
		do {
			System.out.print("Please input arrival_airport code: ");
			try{
				arrival_airport = in.readLine();
				if (arrival_airport.length() <= 0) {
					throw new RuntimeException("INVALID INPUT! The airport code cannot be 0 or negative");
				}
				else if (arrival_airport.length() > 5) {
					throw new RuntimeException("INVALID INPUT! Airport code cannoy exceed 5 characters");
				}
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e);
				continue;
			}
					
		}while(true);
	    
		//departure_airport
		do {
			System.out.print("Please input arrival_airport code: ");
			try{
				departure_airport = in.readLine();
				if (departure_airport.length() <= 0) {
					throw new RuntimeException("INVALID INPUT! The departure code cannot be 0 or negative");
				}
				else if (departure_airport.length() > 5) {
					throw new RuntimeException("INVALID INPUT! Departure code cannoy exceed 5 characters");
				}
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e);
				continue;
			}
					
		}while(true);
		
		try {
			query = "INSERT INTO Flight (flight_num, cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) VALUES (" + flight_num + ", \'" + cost + "\', \'" + num_sold + "\', \'" + num_stops + "\', \'" + actual_departure_date + "\', \'" + actual_arrival_date + "\', \'" + arrival_airport + "\', \'" + departure_airport + "\');";
		}catch (Exception e){
			System.err.println("Query failed: " + e.getMessage());
	    }



	}

	public static void AddTechnician(DBproject esql) {//4
		int techId;
		String full_name;
		String query;
		
		do{
			System.out.print("Input technician's ID number: ");
			try {
				techID = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
			}
		}
		
		do {
			System.out.print("Please input Technician fullname: ");
			try{
				full_name = in.readLine();
				if (full_name.length() <= 0) {
					throw new RuntimeException("INVALID INPUT! Technician name cannot be 0 or negative");
				}
				else if (fullname.length() > 128) {
					throw new RuntimeException("INVALID INPUT! Technician name cannot exceed 128 characters");
				}
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}while(true);
		
		try {
			query = "INSERT INTO Technician (id, full_name) VALUES (" + techId + ", \'" + fullname + "\');";
			esql.executeUpdate(query);
		}catch (Exception e) {
			System.err.println("Query failed: " + e.getMessage());
		}
		
		
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		int custID;
	    int flightnumber;
        String userInput;
	    String query;
		int rNum;
		String query;
		
		do {
            System.out.print("Input Customer ID: ");
            try {
                custID = Integer.parseInt(in.readLine());
                break;
            }catch (Exception e){
                System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
                continue;
            }
		}while(true);
		
		do {
			
		}while(true);
		
		do {
			System.out.print("Input Customer ID: ");
			try {
				rNum = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		}while (true);
		
		/*try {
			query = "INSERT INTO BookFlight (rNum, full_name) VALUES (" + rNum + "\');";
			esql.executeUpdate(query);
		}catch (Exception e) {
			System.err.println("Query failed: " + e.getMessage());
		}
		*/
	        try
	        {
	            query = "SELECT status\nFROM Reservation\nWHERE cid = " + custID + " AND fid = " + flightNumber + ";";
	            if(esql.executeQueryAndPrintResult(query) == 0) //reservation doesn't exist
	            {
	                while (true)
	                {
	                    System.out.println("Your reservation is not in our database. Would you like to book one? (y/n): ");
	                    try
	                    {
	                        userInput = in.readLine();
	                        if (userInput.equals("y"))
	                        {
	                            while (true)
	                            {
	                                System.out.print("Please input Reservation Number: ");
	                                try
	                                {
	                                    reservationNum = Integer.parseInt(in.readLine());
	                                    break;
	                                }
	                                catch (Exception e)
	                                {
	                                    System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
	                                }
	                            }
	                            while (true)
	                            {
	                                System.out.print("Please input Reservation Status(W/R/C): ");
	                                try
	                                {
	                                    reservationStatus = in.readLine();
	                                    if(!reservationStatus.equals("W") && !reservationStatus.equals("R") && !reservationStatus.equals("C")) {
	                                        throw new RuntimeException("Your input is invalid! Status can only be W, R, or C");
	                                    }
	                                    break;
	                                }
	                                catch (Exception e)
	                                {
	                                    System.out.println("Your input is invalid! Your exception is " + e.getMessage());
	                                }
	                                try
	                                {
	                                    query = "INSERT INTO Reservation (rnum, cid, fid, status) VALUES (" + rNum + ", " + custID + ", " + flightNumber + ", \'" + reservationStatus + "\');";
	                                    esql.executeUpdate(query);
	                                }
	                                catch (Exception e)
	                                {
	                                    System.out.println("Your input is invalid! Your exception is " + e.getMessage());
	                                }
	                            }
	                        }
	                        else if (!userInput.equals("n"))
	                        {
	                            throw new RuntimeException("INVALID INPUT! Must input y/n");
	                        }
	                        break;
	                    }
	                    catch (Exception e)
	                    {
	                        System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
	                        continue;
	                    }
	                }
	            }
	                else
	                {
	                    while (true)
	                    {
	                        try
	                        {
	                            System.out.println("We found your reservation! Would you like to update it? (y/n)");
	                            userInput = in.readLine();
	                            if (userInput.equals("y"))
	                            {
	                                while (true)
	                                {
	                                    System.out.print("Please input new Reservation Status: " );
	                                    try
	                                    {
	                                        reservationStatus = in.readLine();
	                                        if(!reservationStatus.equals("W") && !reservationStatus.equals("R") && !reservationStatus.equals("C")) {
	                                            throw new RuntimeException("INVALID INPUT! Status can only be W, R, or C");
	                                        }
	                                        break;
	                                    }
	                                    catch (Exception e)
	                                    {
	                                        System.out.println("INVALID INPUT! Your exception is " + e.getMessage());
	                                        continue;
	                                    }
	                                }
	                                try
	                                {
	                                    query = "UPDATE Reservation SET status = \'" + reservationStatus + "\' WHERE cid = " + custID + " AND fid = " + flightNumber + ";";
	                                    esql.executeUpdate(query);
	                                }
	                                catch (Exception e)
	                                {
	                                    System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
	                                }
	                            }
	                            else if (!userInput.equals("n"))
	                            {
	                                throw new RuntimeException("INVALID INPUT! Must input y or n");
	                            }
	                            break;
	                        }
	                        catch (Exception e)
	                        {
	                            System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
	                            continue;
	                        }
	                    }
	                }
	            }
	            catch (Exception e)
	            {
	                System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
	            }
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		int flightNum;
		String departTime;
		String query;
		
		do {
			System.out.print("Input Flight Number: ");
			try {
				flightNum = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		} while (true);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		
		do {
			System.out.print("Input Departure Time (YYYY-MM-DD hh:mm): ");
			try {
				departTime = in.readLine();
				LocalDate leaveDate = LocalDate.parse(departTime, formatter);
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		} while (true);
		
		try {
			query = "SELECT Total_Seats - Seats_Sold as \"Seats Available\"\nFROM(\nSELECT P.seats as Total_Seats\nFROM Plane P, FlightInfo FI\nWHERE FI.flight_id = " + flightNum + " AND FI.plane_id = P.id\n)total,\n(\nSELECT F.num_sold as Seats_Sold\nFROM Flight F\nWHERE F.fnum = " + flightNum + " AND F.actual_departure_date = \'" + departTime + "\'\n)sold;";
			
			if(esql.executeQueryAndPrintResult(query) == 0) {
				System.out.println("Flight or Departure Time does not exist");
			}

		}catch (Exception e) {
			System.err.println (e.getMessage());
		}
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
		String query;
		
		try {
			query = "SELECT P.id, count(R.rid)\nFROM Plane P, Repairs R\nWHERE P.id = R.plane_id\nGROUP BY P.id\nORDER BY count DESC;";
			
			esql.executeQueryAndPrintResult(query);
		}catch (Exception e) {
			System.err.println (e.getMessage());
		}
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
		String query;
		
		try {
			query = "SELECT EXTRACT (year FROM R.repair_date) as \"Year\", count(R.rid)\nFROM repairs R\nGROUP BY \"Year\"\nORDER BY count ASC;";
			
			esql.executeQueryAndPrintResult(query);
		}catch (Exception e) {
			System.err.println (e.getMessage());
		}
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
		int flightNum;
		String status;
	    String query;
	    
	    do {
			System.out.print("Input Flight Number: ");
			try {
				flightNum = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid");
				continue;
			}
		}while (true);
		
		do {
			System.out.print("Input Passenger Status: ");
			try {
				status = in.readLine();
				if(!status.equals("W") && !status.equals("R") && !status.equals("C")) {
					throw new RuntimeException("Input is invalid. Valid inputs: W, R, C");
				}
				break;
			}catch (Exception e) {
				System.out.println(e);
				continue;
			}
		}while (true);
		
		try {
			query = "SELECT COUNT(*)\nFROM Reservation\nWHERE fid = " + flightNum + " AND status = \'" + status + "\';";
			
			esql.executeQueryAndPrintResult(query);
		}catch (Exception e) {
			System.err.println (e.getMessage());
		}

	}
}
