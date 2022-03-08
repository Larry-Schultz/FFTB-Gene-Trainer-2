package fft_battleground.match;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.cache.MatchCacheService;
import fft_battleground.cache.model.MatchCacheEntry;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.model.DumpData;
import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.tournament.TournamentServiceImpl;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.model.TournamentInfo;
import fft_battleground.viewer.BetCountServiceImpl;
import fft_battleground.viewer.model.BotBetData;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatchDataServiceImpl implements MatchDataService {

	@Autowired
	private BetCountServiceImpl betCountService;
	
	@Autowired
	private TournamentServiceImpl tournamentService;
	
	@Autowired
	private MatchCacheService matchCacheService;
	
	@Autowired
	private DumpService dumpService;

	@Override
	public List<MatchCacheEntry> getGeneTestData(int count, BotBetData firstTournamentWithBotBetData) throws TournamentApiException, ViewerException, DumpException, CacheException { 
		List<TournamentInfo> tournamentInfoForCount = this.tournamentService.getLatestTournamentInfo(count);
		
		List<TournamentInfo> tournamentsWithBotBetDataAndComplete = tournamentInfoForCount.stream()
				.filter(tournamentInfo -> tournamentInfo.getLastMod().after(firstTournamentWithBotBetData.getCreateDateTime()))
				.filter(TournamentInfo::getComplete)
				.collect(Collectors.toList());
		
		List<MatchCacheEntry> dataset = this.getMatchDataByTournamentInfo(tournamentsWithBotBetDataAndComplete);
		return dataset;
	}
	
	protected List<MatchCacheEntry> getMatchDataByTournamentInfo(List<TournamentInfo> tournamentListings) throws TournamentApiException, ViewerException, DumpException, CacheException {
		Set<Long> tournamentIdsContainedWithinCache = this.matchCacheService.tournamentIdsContainedWithinCache(tournamentListings);
		List<TournamentInfo> missingTournamentInfoFromCache = tournamentListings.stream().filter(tournamentID -> !tournamentIdsContainedWithinCache.contains(tournamentID.getID()))
																.collect(Collectors.toList());
		List<TournamentInfo> tournamentInfoInCache = tournamentListings.stream().filter(tournamentID -> tournamentIdsContainedWithinCache.contains(tournamentID.getID()))
				.collect(Collectors.toList());
		
		ServiceResult lookups = this.lookupDataFromServices(missingTournamentInfoFromCache, tournamentInfoInCache);
		this.matchCacheService.cacheNewEntries(lookups.serviceData);
		
		return lookups.getAllResults();
	}
	
	protected ServiceResult lookupDataFromServices(List<TournamentInfo> missingTournaments, List<TournamentInfo> tournamentsInCache) throws TournamentApiException, ViewerException, DumpException, CacheException {
		ExecutorService pool = Executors.newFixedThreadPool(4);
		Future<List<Tournament>> tournamentLookup = pool.submit(() -> this.tournamentService.getTournaments(missingTournaments));
		Future<List<MatchCacheEntry>> cacheLookup = pool.submit(() -> this.matchCacheService.getMatchCacheEntryForTournamentListing(tournamentsInCache));
		
		List<Long> missingTournamentIds = missingTournaments.stream().map(TournamentInfo::getID).collect(Collectors.toList());
		Future<List<BotBetData>> botBetCountLookup = pool.submit(() -> this.betCountService.getBetBotDataAfterTournament(missingTournamentIds));
		Future<Map<Long, DumpData>> teamValueLookup = pool.submit(() -> this.dumpService.getTournamentTeamValueMap(missingTournamentIds));
		
		List<Tournament> tournamentResult = null;
		try {
			tournamentResult = tournamentLookup.get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error calling tournament service", e);
			throw new TournamentApiException(e, "Error calling tournament service");
		}
		
		List<BotBetData> botBetDataResult = null;
		try {
			botBetDataResult = botBetCountLookup.get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error calling tournament service", e);
			throw new ViewerException("Error calling tournament service", e);
		}
		
		Map<Long, DumpData> tournamentTeamValueMap = null;
		try {
			tournamentTeamValueMap = teamValueLookup.get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error calling dump service", e);
			throw new DumpException(e, "Error calling dump service");
		}
		
		List<MatchCacheEntry> cacheData;
		try {
			cacheData = cacheLookup.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new CacheException(e);
		}
		
		List<MatchCacheEntry> newMatchCacheEntries = MatchCacheEntry.collate(tournamentResult, botBetDataResult, tournamentTeamValueMap);
		return new ServiceResult(newMatchCacheEntries, cacheData);
	}
	
	record ServiceResult(List<MatchCacheEntry> serviceData, List<MatchCacheEntry> cacheData) {
		public List<MatchCacheEntry> getAllResults() {
			return Stream.of(this.serviceData, this.cacheData).flatMap(Collection::stream).collect(Collectors.toList());
		}
	};
}
