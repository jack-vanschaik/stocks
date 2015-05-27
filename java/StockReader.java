import yahoofinance.*;
import yahoofinance.quotes.stock.*;
import java.math.*;
import java.io.*;
import java.util.*;
import java.util.logging.LogManager;
import java.util.Calendar;
import java.lang.Thread;
import java.sql.*;
import java.lang.*;

/*
* ---------> BUGS NEEDING FIXED:
*
*  -  updateTables doesn't do the last element of the ArrayList
*  -  tickers with mysql keyword names can't be made into tables
*  -  getLastTradeTime column is blank
*
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
    
    public static Connection connection;
    public static final int POOL_SIZE = 100;
    public static final int SLEEP_TIME = 1000*60*5;
    public static final String TICKER_FILE = "tickers.txt";
    public static Object key = new Object();
    public static String tableColumns =
"( time TIMESTAMP DEFAULT CURRENT_TIMESTAMP , ask DECIMAL(32, 8), askSize INT, avgVol DECIMAL(32,8), bid DECIMAL(32, 8)," +
" bidSize DECIMAL(32,8), chng DECIMAL(32,8), changefvt DECIMAL(32,8), changefvf DECIMAL(32,8)," +
" changefyh DECIMAL(32,8), changefyl DECIMAL(32,8), lastTradeSize DECIMAL(32,8), " +
"lastTradeTime varchar(256), price DECIMAL(32, 8), pricevt DECIMAL(32, 8), pricevf DECIMAL(32, 8))";
    private String[] tickerList;

    /*
    * The Yahoo Finanace API creates a lot of pesky logs by default. This
    * will simply turn them off
    */
    public static void resetLogManger() {
        LogManager l = LogManager.getLogManager();
        l.reset();
    }
    
    /*
    * Opens a connetion to the database and sets global variable
    */
    public static void openConnection () throws SQLException, IOException {
        String url = "jdbc:mysql://localhost:3306/STOCKS";
        String username = "root";
        String password = "asecret";
        connection = DriverManager.getConnection( url, username, password);
        System.out.println("Connection to mysql database successful");
    }

    /*
    * Adds a new table with named by the ticker
    */
    public static void createTable( String ticker ) throws SQLException, IOException {
        System.out.printf("=> Creating table for ticker %s\n", ticker);
        Statement stat = connection.createStatement();
        // Create the table for that stock ticker
        stat.executeUpdate("CREATE TABLE " + ticker + " " + tableColumns);
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
                int l = this.tickerList.length;
                System.out.printf("=> Updated %d tickers, %s through %s\n", l, tickerList[0], tickerList[l - 1]);
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
        //System.out.println(tickerName);

        try {
            String query = "INSERT INTO " + tickerName + " (ask, askSize, avgVol, bid, bidSize, chng, changefvt, " + 
                           "changefvf, changefyh, changefyl, lastTradeSize, lastTradeTime, price, " + 
                           "pricevt, pricevf) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement stat = connection.prepareStatement(query);
            stat.setBigDecimal(1 , squote.getAsk()); 
            stat.setInt( 2, squote.getAskSize()); 
            stat.setLong( 3 ,squote.getAvgVolume());
            stat.setBigDecimal( 4 , squote.getBid());
            stat.setInt( 5 , squote.getBidSize()); 
            stat.setBigDecimal( 6 , squote.getChange());
            stat.setBigDecimal( 7 , squote.getChangeFromAvg200());
            stat.setBigDecimal( 8 , squote.getChangeFromAvg50());
            stat.setBigDecimal( 9 , squote.getChangeFromYearHigh());
            stat.setBigDecimal( 10 , squote.getChangeFromYearLow());
            stat.setInt( 11, squote.getLastTradeSize());
            stat.setString( 12 , " "); //TODO: Fix this using squote.getLastTradeTime()
            stat.setBigDecimal( 13 , squote.getPrice());
            stat.setBigDecimal( 14 , squote.getPriceAvg200());
            stat.setBigDecimal( 15 , squote.getPriceAvg50());
            stat.executeUpdate();
            // For efficiency this could later be modified to insert multiple  
            stat.close();
        }
        catch (Exception e) {
            System.out.println("Couldn't deposit into table " + tickerName);
            e.printStackTrace();
            System.exit(0);
        }
        
    }
    
    /*
    * Checks to see if each ticker has it's own table.
    * If not, it creates it.
    */
    public static void checkTables(ArrayList<String[]> al) {
        Statement s = null;
        ResultSet res = null;
        try {
            //queries to get all the tables
            s = connection.createStatement();
            res = s.executeQuery("SHOW TABLES");
            //get the table list as an ArrayList for easy indexOf
            ArrayList<String> tables = new ArrayList<String>();
            while(res.next()) {
                tables.add(res.getString(1));
            }
            System.out.printf("=> %d ticker tables already in database\n", tables.size());
            //look through our ArrayList of ticker pools and make sure each
            // ticker has it's own table
            for (int i = 0; i < al.size(); i ++ ) {
                String[] arr = al.get(i);
                for (int j = 0; j < arr.length; j ++) {
                    if ((tables.indexOf(arr[j]) == -1) && (!arr[j].equals(""))) {
                        try {
                            //create the table
                            createTable(arr[j]);
                        }
                        catch (Exception e) {
                            System.out.println("=> Couldn't make table " + arr[j]);
                            System.out.println("       possibly because of SQL keyword");
                            //e.printStackTrace();
                            //System.out.println("=> debug exit");
                            //System.exit(0);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }
    
    
    /*
    * returns: an ArrayList of Arrays of length POOL_SIZE
    *  meant to open the tickers.txt file with all the stock ticker names
    *
    * NOTE: The way this works is kind of retarded. It has an array list of String[] arrays.
    *       these are arrays of tickers, each handled by it's own thread
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
   
    public static void displayTicker(ArrayList<String[]> al, boolean verbose) {
        for (int i = 0; i < al.size(); i ++ ) {
            String[] tickers = al.get(i);
            System.out.printf("=> %d - ticker array of %d values\n", i, tickers.length);
            if (verbose) {
                for (int j = 0; j < tickers.length; j ++) {
                     System.out.println(tickers[j]);
                }
            }
        }
    }

    public static void main(String[] args ) {
        System.out.println("----- Booting up StockReader -----\n");

        //disable those pesky loggers
        resetLogManger();
        
        //parse the ticker file and prep the StockReader threads
        ArrayList<String[]> parsedTickers = parseTickerFile();
        displayTicker(parsedTickers, false);

        //try to start a mysql connection
        try {
            openConnection();
        }
        catch (SQLException e) {
            System.out.println("Error connecting to mysql.");
            e.printStackTrace();
            System.out.println(e.getSQLState());
        }
        catch (Exception e) {
            System.out.println("Error opening connection");
        }

        //prep the tables
        checkTables(parsedTickers);

        //create threads
        System.out.println("=> Creating threads");
        Thread[] readers = new Thread[parsedTickers.size()];
        for (int i = 0; i < parsedTickers.size(); i ++) {
            readers[i] = new Thread(new StockReader(parsedTickers.get(i)));
        }
        //start the threads!
        System.out.println("=> Starting threads");
        for (int i = 0; i < readers.length; i ++) {
            readers[i].start();
        }
    }
 
}
