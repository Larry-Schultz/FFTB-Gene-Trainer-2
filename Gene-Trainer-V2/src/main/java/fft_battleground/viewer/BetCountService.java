package fft_battleground.viewer;

import java.util.Date;
import java.util.List;
import java.util.Map;

import fft_battleground.viewer.model.BotBetData;

public interface BetCountService {
	BotBetData getFirstTrackedTournament();
	List<BotBetData> getBetBotDataAfterTournament(List<Long> tournamentIds);
	Map<String, Double> getPlayerBetWinRatios(Date after);
	Map<String, Double> getFightWinRatios(Date after);
	List<String> getSubscribers(Date after);
	List<String> getBots();
}
