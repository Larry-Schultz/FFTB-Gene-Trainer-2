package fft_battleground.botland.model;

import java.util.Date;
import java.util.List;

import fft_battleground.botland.model.BotPlacement;

public record BotlandRunResult(List<BotPlacement> leaderboard, int matchesRequested, int matchesAnalyzed,
		int totalMatchesAvailable, Date startedAt, Date finishedAt, long durationMs) {

}
