import StockReader
import threading
from datetime import datetime

#time in seconds for threads to wait before collecting data again
WAIT_TIME = 5*60

def readFile(name):
    f = open(name, "rb")
    buf = ""
    reading = True
    while reading:
        data = f.read(4096)
        if not data:
            reading = False
        buf += data
    return buf


#this will run a StockReader, and set up another one on a timer
# if all the conditions are still valid
def doStock(ticker):
    sReader = StockReader.StockReader(ticker)
    sReader.doConnection()
    sReader.logToFile("data/" + ticker + ".csv")
    if (datetime.now().hour < 4):
        t = threading.Timer(WAIT_TIME, doStock, args=(ticker,))
        t.start()

def info():
    print "Number of active threads: " + str(threading.activeCount())
    

tickers = readFile("tickers.txt")
if (tickers[-1:] == "\n"):
    tickers = tickers[:-1]
tickerList = tickers.split("\n")
threadList = []
for t in tickerList:
    threadList.append(threading.Thread(group=None, target=doStock, args=(t,)))

#this starts StockReaders in different threads immediately    
for s in threadList:
    s.start()

while True:
    i = threading.Timer(10, info)
    i.daemon = True
    i.start()
    i.join()