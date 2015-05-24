import httplib 

class stockReader:
    """ Uses the Yahoo Finance API and httplib to retreive stock ticker
    data """
    
    
    tickerName = ""
    requestString = "nabc"
    URL_PREPEND = "/d/quotes.csv?s="
    
    _csvData = ""
    _headerData = ""
    _logFileName = "log.csv"
    
    
    def __init__(self, tickerName):
        self.tickerName = tickerName.upper()
        
    def doConnection(self):
        #connects to the yahoo finance API to collect stock ticker data
        conn = httplib.HTTPConnection("download.finance.yahoo.com", 80)
        requestUrl = self.URL_PREPEND + self.tickerName 
        requestUrl += "&f=" + self.requestString
        conn.request("GET", requestUrl)
        res = conn.getresponse()
        
        if res.status != 200:
            msg = "Can't to connect to yahoo finance API: " + str(res.status) 
            msg += ", "+ res.reason
            raise Exception(msg)
        self._csvData = res.read()
        self._headerData = res.getheaders()
        conn.close()
        
    def logToFile(self, fileName):
        if fileName:
            self._logFileName = fileName
            try:
                f = open(self._logFileName, "a")
                f.write(self._csvData)
                f.close()
            except IOError:
                print "IOError"
        
    def parseCSVData(self):
        #currently only supports one ticker symbol at a time
        return self._csvData.split(",")
 
sr = stockReader("YHOO")
sr.doConnection()
sr.logToFile("logfile.csv")