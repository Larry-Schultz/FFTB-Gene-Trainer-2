let stompClient = null;
let statusPollTimer = null;

function connect() {
    const socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        stompClient.subscribe('/botland/progress', function (message) {
            showProgress(JSON.parse(message.body));
        });
        stompClient.subscribe('/botland/results', function (message) {
            showResult(JSON.parse(message.body));
        });
    });
}

function showProgress(progress) {
    setProgressBar(progress.percent);
    $('#progressPhaseText').text(progress.phase);
    $('#progressDetailText').text(progress.detail || '');
    if (progress.phase === 'complete' || progress.phase === 'failed') {
        finishRun();
    }
}

function showResult(result) {
    showLeaderboard(result.leaderboard || []);
    $('#matchSummary').text(result.matchesAnalyzed + ' matches simulated (of ' + result.totalMatchesAvailable + ' available, ' + result.matchesRequested + ' requested)');
    $('#runDuration').text('in ' + (result.durationMs / 1000).toFixed(1) + 's');
}

function showLeaderboard(leaderboard) {
    const body = $('#leaderboardBody');
    body.empty();
    const formatter = new Intl.NumberFormat('en-US');
    for (let i = 0; i < leaderboard.length; i++) {
        const placement = leaderboard[i];
        const winner = i === 0;
        const row = $('<tr>')
            .append($('<td>').text(i + 1))
            .append($('<td>').text(placement.botName))
            .append($('<td class="text-right">').text(formatter.format(placement.gil)));
        if (winner) {
            row.addClass('success');
            $('#winnerName').text(placement.botName);
            $('#winnerGil').text(formatter.format(placement.gil));
        }
        body.append(row);
    }
    $('#resultSummary').show();
}

function setProgressBar(percent) {
    const clamped = Math.max(0, Math.min(100, percent));
    $('#progressBar').css('width', clamped + '%').attr('aria-valuenow', clamped);
    $('#progressBar').removeClass('progress-bar-striped active');
    $('#progressPercentText').text(clamped + '%');
}

function startRun() {
    const matches = $('#matchesTextbox').val();
    $.getJSON('/botland/start?matches=' + matches, function (data) {
        if (data.accepted) {
            $('#startButton').prop('disabled', true);
            $('#startText').text('started');
            $('#progressBar').addClass('progress-bar-striped active');
            setProgressBar(1);
            $('#progressPhaseText').text('starting');
            if (!statusPollTimer) {
                statusPollTimer = setInterval(pollStatus, 2000);
            }
        } else {
            $('#startText').text(data.error || 'could not start');
        }
    }).fail(function (xhr) {
        const body = xhr.responseJSON;
        $('#startText').text((body && body.error) || 'could not start');
    });
}

function pollStatus() {
    $.getJSON('/botland/status', function (status) {
        showProgress({ phase: status.phase, percent: status.percent, detail: status.detail });
        if (!status.running) {
            finishRun();
            loadLastResult();
        }
    });
}

function finishRun() {
    $('#startButton').prop('disabled', false);
    if (statusPollTimer) {
        clearInterval(statusPollTimer);
        statusPollTimer = null;
    }
}

function loadLastResult() {
    $.getJSON('/botland/last', function (result) {
        showResult(result);
        showLeaderboard(result.leaderboard || []);
    });
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#startButton").click(function () { startRun(); });
    connect();
    loadLastResult();
});
