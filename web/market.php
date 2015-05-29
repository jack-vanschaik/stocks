<?php

include "functions.php";

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
 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title> STOCKS Web Interface </title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    
 </head>
 <body>
 
 
<!-- navbar -->
 
 <nav class="navbar navbar-default">
  <div class="container-fluid">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="index.php">STOCKS Web Interface</a>
    </div>
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
      <ul class="nav navbar-nav">
        <li><a href="market.php">Check the entire market</a></li>
      </ul>
    </div>
  </div>
</nav>

<!-- end navbar -->



 </body>

 </html>
