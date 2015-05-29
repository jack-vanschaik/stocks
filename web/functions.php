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

function entireMarket($con) {

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
echo getDailyPrices($con, "WOOD");
*/

?>