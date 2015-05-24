from datetime import datetime


def getMessageStart():
    return "HTTP/1.1 200 OK\r\n"

def getDateHeader():
    dt = datetime.now()
    return dt.strftime("Date: %a, %d %b %Y %X %Z\r\n")

def getEmptyLine():
    return "\r\n"

def makeBasicReply(htmlData):
    reply = getMessageStart()
    reply += getDateHeader()
    reply += getEmptyLine()
    return reply + htmlData

