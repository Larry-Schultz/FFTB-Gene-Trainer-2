package fft_battleground.match;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.cache.model.MatchCacheEntry;
import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.genetic.model.CompleteBotGenome;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.viewer.BetCountService;
import fft_battleground.viewer.model.BotBetData;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TrainerDataServiceImpl implements TrainerDataService {

	@Autowired
	private MatchDataService matchDataService;
	
	@Autowired
	private BetCountService betCountService;
	
	@Override
	public MultipleTournamentDataset generateDataset(int count, CompleteBotGenome genome) throws TournamentApiException, ViewerException, DumpException, CacheException {
		BotBetData firstTournamentWithBotBetData = this.betCountService.getFirstTrackedTournament();
		ExecutorService pool = Executors.newFixedThreadPool(3);
		Future<List<MatchCacheEntry>> datasetFuture = pool.submit(() -> this.matchDataService.getGeneTestData(count, firstTournamentWithBotBetData));
		Future<Map<String, Double>> playerBetWinRatiosFuture = pool.submit(() -> this.betCountService.getPlayerBetWinRatios(firstTournamentWithBotBetData.getCreateDateTime()));
		Future<Map<String, Double>> playerFightWinRatiosFuture = pool.submit(() -> this.betCountService.getFightWinRatios(firstTournamentWithBotBetData.getCreateDateTime()));
		Future<List<String>> futuresSubscribers = pool.submit(() -> this.betCountService.getSubscribers(firstTournamentWithBotBetData.getCreateDateTime()));
		Future<List<String>> futureBots = pool.submit(() -> this.betCountService.getBots());
		
		List<MatchCacheEntry> dataset = null;
		Map<String, Double> playerBetWinRatios = null;
		Map<String, Double> playerFightWinRatios = null;
		List<String> subscribers = null;
		List<String> bots = null;
		
		try {
			dataset = datasetFuture.get();
			playerBetWinRatios = playerBetWinRatiosFuture.get();
			playerFightWinRatios = playerFightWinRatiosFuture.get();
			subscribers = futuresSubscribers.get();
			bots = futureBots.get()
		} catch (InterruptedException | ExecutionException e) {
			log.error("Something weird went wrong getting data from viewer");
			throw new ViewerException(e);
		}
		TrainerDataGenerator generator = new TrainerDataGenerator(genome, dataset, playerBetWinRatios, playerFightWinRatios);
		Match[] matchData = generator.generateDataset();
		double[] playerBetRatios = generator.getPlayerBetWinRatioList().stream().mapToDouble(Double::doubleValue).toArray();
		MultipleTournamentDataset multipleTournamentDataset = new MultipleTournamentDataset(matchData, playerBetRatios);
		return multipleTournamentDataset;
	}

}