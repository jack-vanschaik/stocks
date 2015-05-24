import yahoofinance.*;
import yahoofinance.quotes.stock.*;
import java.math.*;
import java.io.*;
import java.util.*;
import java.lang.Thread;

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
 
    public static final int POOL_SIZE = 100;
    public static final int SLEEP_TIME = 1000*60*5;
    public static final String TICKER_FILE = "tickers.txt";
    public static Object key = new Object();
    private String[] tickerList;
    
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
   
    public static void main(String args[] ) {
        //parse the ticker file and prep the StockReader threads
        ArrayList<String[]> parsedTickers = parseTickerFile();
        StockReader[] readers = new StockReader[parsedTickers.size()];
        for (int i = 0; i < parsedTickers.size(); i ++) {
            readers[i] = new StockReader(parsedTickers.get(i));
        }
        //start the threads!
    }
 
}