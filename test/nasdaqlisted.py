from ftplib import FTP

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

def parseNasdaqListed():
    d = readFile("nasdaqlisted.txt")
    tickerList = d.split("\n")
    buf = ""
    for n in range(1, len(tickerList) - 2):
        if tickerList[n] != "":
            buf += tickerList[n].split("|")[0] + "\n"
    f = open("parsed.txt", "wb")
    f.write(buf)
    f.close()
    
def downloadList():
   ftp = FTP("ftp.nasdaqtrader.com")
   ftp.login("anonymous", "anonymous@")
   ftp.cwd("SymbolDirectory")
   ftp.retrbinary('RETR nasdaqlisted.txt', open('nasdaqlisted.txt', 'wb').write)
   ftp.quit()

downloadList()    
parseNasdaqListed()