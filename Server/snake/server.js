/*

Structure for all games map:

guid -> {port, playerSockets, server, gamestate}


*/
var allGames = {};
var gameNumber = 0;

// All required utils for the app
var express    = require('express');
var bodyParser = require('body-parser');
var app        = express();
var uriUtil = require('mongodb-uri');
var net = require('net');

// connect with server

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var port     = process.env.PORT || 8080; // set our port
//var cbBikeEntry = require('./app/models/cbBikeEntry');
var router = express.Router();

// middleware to use for all requests
router.use(function(req, res, next) {
	// do logging
	res.header("Access-Control-Allow-Origin", "*");
  	res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
	console.log('API Being Accessed');
	next();
});

router.get('/', function(req, res){
	res.json({message: "working"});
  console.log(allGames);
});

router.route('/games')
	.get(function(req, res){
    var result = getAllGames();
    res.json(result);
    res.send();
	});

router.route('/games/:game_id')
  .get(function(req, res){
    res.json(allGames[req.params.game_id]);
    res.send();
  });

router.route('/games/create')
	.post(function(req, res) {
 for (var key in allGames) {
     if (allGames.hasOwnProperty(key)) {
         if (allGames[key].playerSockets.length < 2) {
             res.json(allGames[key].port);
             res.send();
	     return;
         }
     }
 }
    gameNumber++;
    console.log("ln(5) creating game");
    createGame(gameNumber, res);
	});

router.route('/games/join')
    .get(function(req, res) {
 console.log("here");
 console.log(allGames);
 for (var key in allGames) {
     if (allGames.hasOwnProperty(key)) {
	 if (allGames[key].playerSockets.length < 2) {
	     res.json(allGames[key].port);
	     res.send();
	 }
     }
 } 
 res.json(-1);
 res.send();
});

router.route('/route')
	.post(function(req, res) {

	});
app.use('/api', router);

// START THE SERVER
// =============================================================================
app.listen(port);
console.log('Server on: ' + port);

/* var server = net.createServer(function(sock) {
    sock.on('data', function(data) {
        console.log('DATA ' + sock.remoteAddress + ': ' + data);
        broadcastData(sock, data)
    });

    sock.on('close', function(data) {
        console.log('CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);
    });
    allSockets.push(sock);
}); */

function relayMove(playerSockets, currentSocket, move) {
  for (var i = 0; i < playerSockets.length; i++) {
    if (playerSockets[i] != currentSocket) {
      console.log(i);
      playerSockets[i].write(move);
    }
  }
}

function getAllGames() {
  var result = [];
  for (var key in allGames) {
    if (allGames.hasOwnProperty(key)) {
      result.push(key);
    }
  }
}

function joinGame(guid, playerSocket) {
  allGames[guid].playerSockets.push(playerSocket);
}

function createGame(guid, res) {
  var server = net.createServer(function(sock){
    sock.p3_gameData = {}
    sock.p3_gameData.guid = guid;
    sock.on('close', function() {
	this.destroy();
	endGame(sock.p3_gameData.guid);
    });
    sock.on('data', function(data) {
	if (typeof allGames[this.p3_gameData.guid] === "undefined"){
	    this.destroy();
	    return;
	}
	// console.log("starts here");
	    console.log("" + data);
	var strdat = "" + data;
	// console.log("wtf is going on");
	if (strdat.indexOf("a:") != -1) {
	    console.log(strdat);
	    // console.log("here 325");
	    var res = strdat.substring(strdat.indexOf("a:"));
	    // console.log(res);
	    if (res.startsWith("a:[0,0]")) { 
		var tdata = createRandomFood(19, 28, data);
		// console.log("creating temp data here: ");
		// console.log(tdata);
		// relayMove(allGames[this.p3_gameData.guid].playerSockets, null, createRandomFood(19, 28));
	    }
	}
        relayMove(allGames[this.p3_gameData.guid].playerSockets, this, data);
    });
      while (typeof allGames[sock.p3_gameData.guid] === "undefined") {
	  continue;
      }
      if (allGames[sock.p3_gameData.guid].playerSockets.length == 2) {
	  sock.close();
      }
    allGames[sock.p3_gameData.guid].playerSockets.push(sock);
      if (allGames[sock.p3_gameData.guid].playerSockets.length > 1) {
	  // 22 x 31
	  // TODO(akshatsh): fix race condition, do this after connection is done
	  var firstSnake = "";
	  var header = "start\np:";
	  firstSnake += "6 8";
	  for (var i = 5; i > 1; i--) {
	      firstSnake += " " + i + " 8";
	  }
	  sock.write("\n");
	  var secondSnake = "10 15";
	  for (var i = 16; i < 20; i++) {
	      secondSnake += " 10 " + i;
	  }

	  var footer = "\r\n";
	  relayMove(allGames[sock.p3_gameData.guid].playerSockets, 
		    allGames[sock.p3_gameData.guid].playerSockets[0], header + firstSnake + "\nq:" + secondSnake + footer);
	  relayMove(allGames[sock.p3_gameData.guid].playerSockets, 
		    allGames[sock.p3_gameData.guid].playerSockets[1], header + secondSnake + "\nq:"+ firstSnake + footer);
	  // sock.write("start " + firstpos[0] + " " + firstpos[1] + " " + secondPos[0] + " " + secondPos[1]);
      }
  });

  server.close(function () {
      server.unref();
  });

  server.listen(0, function () {
    allGames[guid] = {port: server.address().port, playerSockets : [], server: server,
    grid: createGrid(10)};
    res.json(server.address().port);
    res.send();
    console.log(server.address().port);
  });
}

function endGame(guid) {
  if (typeof allGames[guid] === "undefined") {
      return;
  }
  sockets = allGames[guid].playerSockets;
  for (var i = 0; i < sockets.length; i++) {
    // sockets[i].destroy();
  }
  // allGames[guid].server.destroy();
  delete allGames[guid];
}

function createGrid(size) {
  var grid = new Array(size);
  for (var i = 0; i < size; i++) {
    grid[i] = new Array(size);
    for (var j = 0; j < size; j++) {
      grid[i][j] = false;
    }
  }
  return grid;
}

function createRandomFood(x, y, data) {
  var randX, randY;
  randX = Math.floor(Math.random() * x);
  randY = Math.floor(Math.random() * y);
  var res = "[" + randX + "," + randY + "]";
  console.log("apple: " + res);
  var temp = data.split("\n");
  temp[1] = "a:" + res;
  return res.join("\n");
}

function processMove(grid, move) {
  // TODO(akshatsh): handle how moves are processed
}
