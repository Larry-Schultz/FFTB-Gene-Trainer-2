var stompClient = null;
var sentencesStompClient = null;
var channelsStompClient = null;
var statsStompclient = null;
var highScore = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
    $("#sentences").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        //console.log('Connected: ' + frame);
        stompClient.subscribe('/chain/stats', function (chatMessages) {
			var serverData = JSON.parse(chatMessages.body);
            showGreeting(serverData);
			showStats(serverData);
        });
    });

}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function showGreeting(message) {
	var generationString = `generation ${message.generation}`;
	var timeStatement = '[' + getFormattedTime() + ']';
	$("#greetings").prepend("<li>" + timeStatement + '  ' + generationString + "</li>");
	
	if(!highScore || message.highScore.numberOfMatchesCorrect > highScore.numberOfMatchesCorrect) {
		highScore = message.highScore;
		var scoreStatement = `Score for this bot is ${message.highScore.highScore}.`;
		var matchesStatement = `${message.highScore.numberOfMatchesCorrect} out of ${message.highScore.numberOfMatches} matches were guessed correctly (${message.highScore.percentage}%).`;
		var maxLevelStatement = `Max level ${message.highScore.maxLevel} with ${message.highScore.numberAtMaxLevel} genes of this level.`;
		var minLevelStatement = `Min level ${message.highScore.minLevel} with ${message.highScore.numberAtMinLevel} genes of this level.`;
		var totalGenesStatement = `${message.highScore.numberOfGenes} total genes`;
		var logStatement = scoreStatement + ' ' + matchesStatement + ' ' + maxLevelStatement + ' ' + minLevelStatement + ' ' + totalGenesStatement;
		
		$("#greetings").prepend("<li>" + timeStatement + '  ' + logStatement + "</li>");
		
		setScoreStats(highScore);
	}
	
	
	var greetingLength = $("#greetings").children().length;
	if(greetingLength > 50) {
		$("#greetings").children().slice(50, greetingLength).remove();
	};
}

function setScoreStats(highScoreObj) {
	$('#score').text(highScoreObj.highScore);
	$('#correctMatches').text(highScoreObj.numberOfMatchesCorrect);
	$('#matchCount').text(highScoreObj.numberOfMatches);
	$('#percentage').text(highScoreObj.percentage);
	
	$('#maxLevel').text(highScoreObj.maxLevel);
	$('#maxLevelGeneCount').text(highScoreObj.numberAtMaxLevel);
	$('#minLevel').text(highScoreObj.minLevel);
	$('#minLevelGeneCount').text(highScoreObj.numberAtMinLevel);
	$('#totalGenes').text(highScoreObj.numberOfGenes);
	$('#highScoreTime').text(highScoreObj.updateDate);
}

function getFormattedTime() {
	const date = new Date();
	const time = date.toLocaleString();
	return time;
}

function showStats(message) {
	$('#cpuUsageText').text(message.cpuUsage);
	$('#ramUsageText').text(message.ramUsage);
	$('#chainCountText').text(message.generation);
}

function start() {
	const population = $('#populationTextbox').val();
	const tournaments = $('#tournamentsTextbox').val();
	const url = '/start?population=' + population + '&tournaments=' + tournaments;
	$.getJSON(url, function(data) {
		var geneStatData = data
		$('#tournaments').text(geneStatData.tournamentsToAnalyze);
		$('#population').text(geneStatData.population);
		$('#threads').text(geneStatData.threads);
		$('#maxGeneLevel').text(geneStatData.maxGeneLevel);
		$('#minGeneLevel').text(geneStatData.minGeneLevel);
		$('#threshold').text(geneStatData.threshold);
	});
	$('#startText').text('started');
}
	
$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    }); 
    $("#connect").click(function() { connect(); });
    $("#disconnect").click(function() { disconnect(); });
	$('#startButton').click(function() { start(); });
});