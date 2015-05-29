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

if (isset($_GET["ticker"])) {
    $ticker = $_GET["ticker"];
}
else {
    $ticker = "WOOD";
}
?>

 <!DOCTYPE html>
 <html lang="en">
 <head>
 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title> STOCKS Web Interface </title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    
<script type="text/javascript">

function fillCanvas(canvas) {
    canvas.ctx.fillStyle = "#FFFFFF";
    canvas.ctx.fillRect(0, 0, canvas.x, canvas.y);
}


//graphs an array of number on the given canvas
//canvas is an object {cxt, x, y}, so all drawing is done relative to the
// size of the canvas; this function should work for any dimension canvas 
function graphData(canvas, data, ticker) {
    //first draw all the data points
    var points = data.length;
    var xInc = canvas.x / points;
    var xOffset = xInc/2;
    var min = Math.min(...data);
    var max = Math.max(...data);
    var yRange = max - min;
    console.log(yRange);
    var yk = canvas.y/yRange; // a constant to save computation
    canvas.ctx.beginPath();
    canvas.ctx.moveTo(xOffset, canvas.y - (yk*(data[0] - min)));
    for (var i = 1; i < points; i ++ ) {
        //console.log(data[i]);
        canvas.ctx.lineTo(xOffset + (i * xInc), canvas.y - (yk*(data[i]- min)));
    }
    canvas.ctx.strokeStyle = "#FF0000";
    canvas.ctx.lineWidth = "5";
    canvas.ctx.stroke();
    
    
    //then draw the grid
    canvas.ctx.beginPath();
    canvas.ctx.moveTo(0,0);
    canvas.ctx.lineTo(0,canvas.y);
    canvas.ctx.lineTo(canvas.x,canvas.y);
    canvas.ctx.strokeStyle = "#000000";
    canvas.ctx.stroke();
    canvas.ctx.beginPath();
    for (var i =0; i < points; i ++ ) {
        canvas.ctx.moveTo(xOffset + (i*xInc), canvas.y);
        canvas.ctx.lineTo(xOffset + (i*xInc), canvas.y - 10);
    }
    canvas.ctx.lineWidth = "1";
    canvas.ctx.stroke();
    
    //write some info in that bee-yotch
    //canvas.ctx.font = "30px Arial";
    canvas.ctx.beginPath()
    canvas.ctx.fillStyle = "#000000";
    canvas.ctx.font = "16px Arial";
    var minStr = "Minimum price: " + min.toString();
    var maxStr = "Maxmimum price: " + max.toString();
    canvas.ctx.fillText(minStr, 32, canvas.y  - 32);
    canvas.ctx.fillText(maxStr, 32, 64);
    canvas.ctx.fillText(ticker, 32, 32);
    canvas.ctx.fill();
    canvas.ctx.stroke();

}
</script>
    
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

<div class="row">
<div class="col-lg-4">
<div class="bs-component">

  <div class="panel panel-info">
  <div class="panel-heading">Tracking:</div>
  <div class="panel-body">
    <?php echo getTableCount($con); ?> NASDAQ stock tickers
  </div>
  </div>
<form class="form-horizontal" action="index.php" method="GET">
  <fieldset>
    <legend>Actions</legend>
    <div class="form-group">
      <label for="inputEmail" class="col-lg-2 control-label">Ticker</label>
      <div class="col-lg-10">
        <input class="form-control" id="inputTicker" name="ticker" placeholder="TICKER" type="text">
      </div>
    </div>
    <div class="form-group">
      <div class="col-lg-10 col-lg-offset-2">
        <button type="submit" class="btn btn-primary" >Lookup Stock</button>
      </div>
    </div>
  </fieldset>
</form>

</div>
</div>
<div class="col-lg-6">
<div class="bs-component">


<div class="panel panel-primary">
  <div class="panel-heading">
    <h3 class="panel-title">Graph of Today's Prices</h3>
  </div>
  <div class="panel-body">
    <center>
    <canvas id="stockGraph" width="512" height="256"></canvas>
    </center>
  </div>
</div>


</div>
</div>


<!-- Start of Yahoo! Finance code -->

<iframe allowtransparency="true" marginwidth="0" marginheight="0" hspace="0" vspace="0" frameborder="0" scrolling="no" src="http://badge.finance.yahoo.com/instrument/1.0/<?php echo $ticker; ?>/badge;chart=5d;news=5;quote/HTML?AppID=N8scBZu.w2DMtDnxMpXakbOH.jObQ7k5&sig=NYmQ_Sp8TTSbZfgnG2BgwE.TfN4-&t=1432863813296" width="200px" height="709px"><a href="http://finance.yahoo.com">Yahoo! Finance</a><br/><a href="http://finance.yahoo.com/q?s=<?php echo $ticker; ?>/">Quote for <?php echo $ticker; ?>/</a></iframe>
<!-- End of Yahoo! Finance code -->

</div>
</div>

</div>

<?php
if (isset($_GET["ticker"])) {
    echo getDailyData($con, $_GET["ticker"]);
}

?>
 </body>
 <script type="text/javascript">
var c = document.getElementById("stockGraph");
var ctx = c.getContext("2d");
canvas = {ctx:ctx, x:512, y:256};

fillCanvas(canvas);
<?php
if (isset($_GET["ticker"])) {
    $data = getDailyPrices($con, $_GET["ticker"]);
    echo "var data = $data;\n";
    echo "var ticker = \"" . $_GET["ticker"] . "\"";
}
else {
    echo "var data = [0];\n";
    echo "var ticker = \"no ticker\"";
}

?>

if (data.length > 1) {
    graphData(canvas, data, ticker);
}
</script>
 </html>
