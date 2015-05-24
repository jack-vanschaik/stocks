import socket
import HTMLrequest

class stockServer:
    
    """Makes a basic web server for basic visual output"""

    HOST = ''
    _port = 80
    htmlHead = "<head><title> Stocks </title></head>"
    

    def __init__(self, port):
        self._port = port
    
    def makeResponse(self):
        html = "<html>" + self.htmlHead + "<body>hey</body></html>"
        return HTMLrequest.makeBasicReply(html)
    
    def serve(self):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind((self.HOST, self._port))
        listening = True
        s.listen(1)
        print "Listening on port ", str(self._port)
        while listening:
            conn, addr = s.accept()
            print "Connection by", addr
            buf = ""
            #while True:
            #    data = conn.recv(1024)
            #    if data == "": break
            #    buf += data
            data = self.makeResponse()
            print data
            conn.sendall(data)
            conn.close()
            
ss = stockServer(80)
ss.serve()
        
        

