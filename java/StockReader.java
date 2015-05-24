import yahoofinance.*;
import yahoofinance.quotes.stock.*;
import java.math.*;
import java.io.*;
import java.util.*;
import java.lang.Thread;
import java.sql.*;

/*
java -classpath .:/home/jack/code/stocks/java/lib/YahooFinanceAPI-1.3.0.jar StockReader
javac -classpath .:/home/jack/code/stocks/java/lib/YahooFinanceAPI-1.3.0.jar StockReader.java
*/


/*
* STRUCTURE:
*  - Loads the tickers.txt file and parses all the lines as stock tickers
*  - Groups the tickers into pools of POOL_SIZE
*  - Each pool has it's own thread, set to loop a task every five minutes
*  - This task is to get stock data from Yahoo finance for each ticker in 
*    the pool and record it in the MySQL database 
*/

public class StockReader implements Runnable{
    
    Connection connection;
    public static final int POOL_SIZE = 100;
    public static final int SLEEP_TIME = 1000*60*5;
    public static final String TICKER_FILE = "tickers.txt";
    public static final String MYSQL_CONNECTION = 
        "jdbc:mysql://localhost/test?user=root&password=asecret";
    public static Object key = new Object();
    private String[] tickerList;
    
    
    /*
    * Opens a connetion to the database and sets global variable
    */
    public void openConnection () throws SQLException, IOException {
      	String url = "jdbc:mysql://localhost:3306/STOCKS";
      	String username = "root";
      	String password = "asecret";

		connection = DriverManager.getConnection( url, username, password);
   	}

    /*
    * Adds a new table with named by the ticker
    */	
	public void createTable( String ticker ) throws SQLException, IOException {
		openConnection();
        Statement stat = connection.createStatement();
			
		// Create the table for that stock ticker
        stat.executeUpdate("CREATE TABLE " + ticker + "(date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, price FLOAT, PRIMARY KEY (date))");
	}
    
    
    /*
    * Adds a new price entry to the TICKER table with the current time and date
    */
    public void insertPrice ( String ticker, float price ) throws SQLException, IOException {
		String query = "INSERT INTO ? ( date , price ) VALUES ( DEFAULT , ? )";
		PreparedStatement stat = connection.prepareStatement(query);
		stat.setString( 1, ticker );
		stat.setFloat( 2, price );
        // For efficiency this could later be modified to insert multiple  
		stat.close();
	}
    
    
    public StockReader(String[] tickerList) {
        this.tickerList = tickerList;
    }
   
   
    public void run() {
        boolean loop = true;
        while (loop) {
            try {
                Map<String, Stock> thisMap = YahooFinance.get(this.tickerList);
                for (String tick : this.tickerList) {
                    Stock st = thisMap.get(tick);
                    StockQuote sq = st.getQuote();
                    mysqlDeposit(tick, sq );
                }
                Thread.sleep(SLEEP_TIME);
            }
            catch (InterruptedException e) {
                System.out.println("Thread interrupted!");
                loop = false;
            }
        }
    }
    
    // TODO 
    public synchronized void mysqlDeposit(String tickerName, StockQuote squote) {
        System.out.println(tickerName);
    }
    
    /*
    * Checks to see if each ticker has it's own table.
    * If not, it creates it.
    */
    public void checkTables(ArrayList<String[]> al) {
        Connection conn = null;
        Statement s = null;
        ResultSet res = null;
        try {
            //queries to get all the tables
            conn = DriverManager.getConnection(MYSQL_CONNECTION);
            s = conn.createStatement();
            res = s.executeQuery("SHOW TABLES");
            //get the table list as an ArrayList for easy indexOf
            String[] ta = (String[])res.getArray(0).getArray();
            ArrayList<String> tables = new ArrayList<String>(Arrays.asList(ta));
            
            //look through our ArrayList of ticker pools and make sure each
            // ticker has it's own table
            for (int i = 0; i < al.size(); i ++ ) {
                String arr[] = al.get(i);
                for (int j = 0; j < arr.length; j ++) {
                    if ((tables.indexOf(arr[j]) == -1) && (!arr[j].equals(""))) {
                        //TODO
                        //create the tables
                    }
                }
            }
        }
        catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }
    
    
    /*
    * returns: an ArrayList of Arrays of length POOL_SIZE
    *  meant to open the tickers.txt file with all the stock ticker names
    */
    public static ArrayList<String[]> parseTickerFile() {
        return parseTickerFile(TICKER_FILE);
    }
    
    /*
    * parseTickerFile is overloaded just in case :)
    */
    public static ArrayList<String[]> parseTickerFile(String fileName) {
        ArrayList<String[]> al = new ArrayList<String[]>();
        try {
            FileInputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String bufLine;
            int count = 0;
            String[] pool = new String[POOL_SIZE];
            while ((bufLine = br.readLine()) != null) {
                if (bufLine.equals("")) {
                    String[] lastPool = new String[count];
                    for (int i = 0; i < count; i ++) {
                        lastPool[i] = pool[i];
                    }
                    al.add(lastPool);
                    break;
                }
                if (count < POOL_SIZE) {
                    pool[count] = bufLine;
                    count ++;
                }
                else {
                    count = 0;
                    al.add(pool);
                    pool = new String[POOL_SIZE];
                }
            }
        }
        catch (Exception e) {
            System.out.println("Couldn't parse ticker file.");
        }
        return al;
    }
   
    public static void main(String[] args ) {
    
        //parse the ticker file and prep the StockReader threads
        ArrayList<String[]> parsedTickers = parseTickerFile();
        Thread[] readers = new Thread[parsedTickers.size()];
        for (int i = 0; i < parsedTickers.size(); i ++) {
            readers[i] = new Thread(new StockReader(parsedTickers.get(i)));
        }
        
        //start the threads!
        for (int i = 0; i < readers.length; i ++) {
            readers[i].start();
        }
    }
 
}