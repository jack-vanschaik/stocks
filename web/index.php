<?php

//simply gets the number of tables in the database
function getTableCount($con) {
    $q = "SELECT COUNT(*) from information_schema.tables WHERE table_type = 'base table'";
    $result = mysqli_query($con, $q);
    if (mysqli_num_rows($result)->num_rows > 0) {
        $row = mysqli_fetch_assoc($result);
        return $row["COUNT(*)"];
    }
    else {
        return "0";
    }
}

$host = "localhost";
$user = "root";
$pass = "asecret";
$dbname = "STOCKS";

//attempt a connection
$con = new mysqli($host, $user, $pass, $dbname);
if ($con->connect_error) {
    die("Connection failed:" . $con->connect_error);
}
?>

 <!DOCTYPE html>
 <html lang="en">
 <head>
    <title> STOCKS Web Interface </title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
 </head>
 <body>
 
 
 <nav class="navbar navbar-default">
  <div class="container-fluid">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="#">STOCKS Web Interface</a>
    </div>
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
      <ul class="nav navbar-nav">
        <li><a href="#">Link</a></li>
      </ul>
    </div>
  </div>
</nav>

<div class="panel panel-default">
  <div class="panel-heading">Tracking:</div>
  <div class="panel-body">
    <?php getTableCount($con); ?> stock tickers
  </div>
</div>

 </body>
 </html>