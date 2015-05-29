<?php

//simply gets the number of tables in the database
function getTableCount($con) {
    $q = "SELECT COUNT(*) from information_schema.tables WHERE table_type = 'base table'";
    $result = mysqli_query($con, $q);
    if (!$result) {
        return "0";
    }
    $arr = $result->fetch_array(MYSQL_NUM);
    return $arr[0];
    
}

function getDailyPrices($con, $ticker) {
    $t = preg_replace("/[^a-zA-Z0-9]+/", "", $ticker);
    $today = date("o-m-d", time());
    $q = "SELECT price FROM $t WHERE time BETWEEN '$today 9:30:00' AND '$today 16:00:00'";
    $result = mysqli_query($con, $q);
    if (!$result) {
        return "no data";
    }
    $row = $result->fetch_array(MYSQL_NUM);
    $buf = $row[0];
    while ($row = $result->fetch_array(MYSQL_NUM)) {
        $buf = $buf. ", " . $row[0] ;
    }
    return "[" . $buf . "0]";
}

function getDailyData($con, $ticker) {
    $t = preg_replace("/[^a-zA-Z0-9]+/", "", $ticker);
    $today = date("o-m-d", time());
    $columns = "time,ask,askSize,avgVol,bid,bidSize,chng,changefvt,changefvf,changefyh,changefyl,"; $columns = $columns . "lastTradeSize,price,pricevt,pricevf";
    $q = "SELECT $columns FROM $t WHERE time BETWEEN '$today 9:30:00' AND '$today 16:00:00'";
    $result = mysqli_query($con, $q);
    if (!$result) {
        return "no data";
    }
    $body = "";
    $head = "";
    $columnList = explode(",", $columns);
    for ($i = 0; $i < 15; $i ++ ){
        $head = $head."\n<th>".$columnList[$i]."</th>";
    }
    $head = "<table class=\"table table-striped table-hover \"><tr>$head</tr><tbody>";
    $tail = "</tbody></table>";
    
    while ($row = $result->fetch_array(MYSQL_NUM)) {
        $body = $body . "<tr>";
        for ($i = 0; $i < 15; $i ++ ){
            $body = $body . "<td>" . $row[$i] . "</td>";
        }
        $body = $body . "</tr>\n";
    }
    
    return $head . $body . $tail;
}

function entireMarket($con) {
    $buf = "";
    $q1 = "SELECT TABLE_NAME FROM information_schema.tables WHERE TABLE_SCHEMA='STOCKS'";
    $result = mysqli_query($con, $q1);
    if (!$result) {
        return "";
    }
    $tableList = array();
    while ($row = $result->fetch_array(MYSQL_NUM)) {
        $table = $row[0];
        array_push($tableList, $table);
    }
    for ($i = 0; $i < count($tableList); $i ++ ) {
        $table = $tableList[$i];
        $q2 = "SELECT price FROM $table WHERE time = (SELECT MAX(time) FROM $table WHERE TRUE);";
        $result2 = mysqli_query($con, $q2);
        $row2 = $result2->fetch_array(MYSQL_NUM);
        $price = $row2[0];
        //echo $price." ";
        $buf = $buf . "\n<tr><td>$table</td><td>$price</td><td><a href=\"index.php?ticker=$table\" class=\"btn btn-info\">Graph $table</a></td></tr>";
    }
    return "<table class=\"table table-striped table-hover \" ><tr><th>ticker</th><th>price</th><th>button</th></tr><tbody>$buf\n</tbody></table>";

}

/*
$host = "localhost";
$user = "root";
$pass = "asecret";
$dbname = "STOCKS";

//attempt a connection
$con = new mysqli($host, $user, $pass, $dbname);
if ($con->connect_error) {
    die("Connection failed:" . $con->connect_error);
} 

echo getDailyData($con, "WOOD");
*/
?>