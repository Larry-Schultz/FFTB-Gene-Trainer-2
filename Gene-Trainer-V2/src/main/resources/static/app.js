let stompClient = null;
let sentencesStompClient = null;
let channelsStompClient = null;
let statsStompclient = null;
let highScore = null;

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
    const socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        //console.log('Connected: ' + frame);
        stompClient.subscribe('/chain/stats', function (chatMessages) {
			const serverData = JSON.parse(chatMessages.body);
            logHighScoreData(serverData);
			showStats(serverData);
        });
        stompClient.subscribe('/chain/attributes', function (chatMessage) {
			logAttribute(chatMessage.body);
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

function logHighScoreData(message) {
	const generation = message.generation.toLocaleString();
	const generationString = `generation ${generation}`;
	const timeStatement = '[' + getFormattedTime() + ']';
	
	const formattedHighScoreValue = message.data.highScore.toLocaleString();
	const formattedPerfectScore = message.data.perfectScore.toLocaleString();
	const scoreStatement = `Score for this bot is ${formattedHighScoreValue}.  Perfect Score: ${formattedPerfectScore}`;
	const botLeaderboardPlacement = `Botland position: ${message.data.botLeaderboardPlacement}`;
	const logStatement = scoreStatement + ' ' + botLeaderboardPlacement;
	
	$("#greetings").prepend("<li>" + timeStatement + '  ' + generationString + ' ' + logStatement + "</li>");
	
	if(!highScore || message.data.highScore > highScore.highScore) {
		highScore = message.data;
		setScoreStats(highScore);
	}
	
	
	const greetingLength = $("#greetings").children().length;
	if(greetingLength > 50) {
		$("#greetings").children().slice(50, greetingLength).remove();
	};
}

function logAttribute(message) {
	$("#greetings").prepend("<li>" + message + "</li>");
	const greetingLength = $("#greetings").children().length;
	if (greetingLength > 50) {
		$("#greetings").children().slice(50, greetingLength).remove();
	};
}

function setScoreStats(highScoreObj) {
	$('#score').text(highScoreObj.highScore.toLocaleString());
	$('#matchCount').text(highScoreObj.matchesAnalyzed.toLocaleString());
	
	$('#totalGenes').text(highScoreObj.numberOfTotalGenes.toLocaleString());
	$('#totalUnitGenes').text(highScoreObj.numberOfUnitGenes.toLocaleString());
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
	$('#chainCountText').text(message.generation.toLocaleString());
}

function start() {
	const population = $('#populationTextbox').val();
	const tournaments = $('#tournamentsTextbox').val();
	const duration = $('#durationTextbox').val();
	const url = '/start?population=' + population + '&tournaments=' + tournaments + '&duration=' + duration;
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