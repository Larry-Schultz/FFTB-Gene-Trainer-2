package fft_battleground.match;

import java.util.List;
import java.util.function.Consumer;

import fft_battleground.cache.model.MatchCacheEntry;
import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.viewer.model.BotBetData;

public interface MatchDataService {
	List<MatchCacheEntry> getGeneTestData(int count, BotBetData firstTournamentWithBotBet) throws TournamentApiException, ViewerException, DumpException, CacheException;
	List<MatchCacheEntry> getGeneTestData(int count, BotBetData firstTournamentWithBotBet, Consumer<String> progress) throws TournamentApiException, ViewerException, DumpException, CacheException;
}
