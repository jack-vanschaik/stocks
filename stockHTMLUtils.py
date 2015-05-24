
def csvToTable(csvData):
    lines = csv.split("\n")
    buf = "<table>\n"
    for l in lines:
        buf += "<tr>\n"
        for d in l.split(","):
            buf += "<td>", d, "<\\td>\n"
        buf += "<\\tr>\n"
    buf += "<\\table>"
    return buf