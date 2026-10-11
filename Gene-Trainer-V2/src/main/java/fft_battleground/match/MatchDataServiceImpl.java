package fft_battleground.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.cache.BadTournamentService;
import fft_battleground.cache.MatchCacheService;
import fft_battleground.cache.model.MatchCacheEntry;
import fft_battleground.dump.DumpService;
import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.model.BattleGroundTeam;
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

	@Autowired
	private BadTournamentService badTournamentService;

	@Override
	public List<MatchCacheEntry> getGeneTestData(int count, BotBetData firstTournamentWithBotBet) throws TournamentApiException, ViewerException, DumpException, CacheException {
		return this.getGeneTestData(count, firstTournamentWithBotBet, message -> {});
	}

	@Override
	public List<MatchCacheEntry> getGeneTestData(int count, BotBetData firstTournamentWithBotBet, Consumer<String> progress) throws TournamentApiException, ViewerException, DumpException, CacheException {
		progress.accept("loading latest tournament info");
		List<TournamentInfo> tournamentInfoForCount = this.tournamentService.getLatestTournamentInfo(count).stream()
				.filter(tournamentInfo -> !this.badTournamentService.getBadTournamentIds().contains(tournamentInfo.getID()))
				.collect(Collectors.toList());

		List<TournamentInfo> tournamentsWithBotBetDataAndComplete = tournamentInfoForCount.stream()
				.filter(tournamentInfo -> tournamentInfo.getLastMod().after(firstTournamentWithBotBet.getCreateDateTime()))
				.filter(TournamentInfo::getComplete)
				.collect(Collectors.toList());

		progress.accept("found " + tournamentsWithBotBetDataAndComplete.size() + " complete tournaments to analyze");

		List<MatchCacheEntry> dataset = this.getMatchDataByTournamentInfo(tournamentsWithBotBetDataAndComplete, progress);
		return dataset;
	}

	protected List<MatchCacheEntry> getMatchDataByTournamentInfo(List<TournamentInfo> tournamentListings, Consumer<String> progress) throws TournamentApiException, ViewerException, DumpException, CacheException {
		Set<Long> tournamentIdsContainedWithinCache = this.matchCacheService.tournamentIdsContainedWithinCache(tournamentListings);
		List<TournamentInfo> missingTournamentInfoFromCache = tournamentListings.stream().filter(tournamentID -> !tournamentIdsContainedWithinCache.contains(tournamentID.getID()))
																	.collect(Collectors.toList());
		List<TournamentInfo> tournamentInfoInCache = tournamentListings.stream().filter(tournamentID -> tournamentIdsContainedWithinCache.contains(tournamentID.getID()))
				.collect(Collectors.toList());

		progress.accept("fetching " + missingTournamentInfoFromCache.size() + " tournaments from the api (" + tournamentInfoInCache.size() + " already cached)");

		ServiceResult lookups = this.lookupDataFromServices(missingTournamentInfoFromCache, tournamentInfoInCache);
		List<MatchCacheEntry> entriesToCache = new ArrayList<>(lookups.serviceData());

		//backfill team values for entries loaded from older cache files that lack them, so the dump lookup is paid once per tournament
		List<Long> backfillTeamValueIds = lookups.cacheData().stream()
				.filter(entry -> entry.getTeamValues() == null)
				.map(MatchCacheEntry::getTournamentId)
				.collect(Collectors.toList());
		if(!backfillTeamValueIds.isEmpty()) {
			progress.accept("fetching team values for " + backfillTeamValueIds.size() + " cached tournaments");
			Map<Long, Map<BattleGroundTeam, Integer>> backfillTeamValues = this.dumpService.getTeamValuesForTournaments(backfillTeamValueIds);
			for(MatchCacheEntry cachedEntry : lookups.cacheData()) {
				if(cachedEntry.getTeamValues() == null) {
					Map<BattleGroundTeam, Integer> teamValues = backfillTeamValues.get(cachedEntry.getTournamentId());
					if(teamValues != null) {
						cachedEntry.setTeamValues(teamValues);
						entriesToCache.add(cachedEntry);
					}
				}
			}
		}

		this.matchCacheService.cacheNewEntries(entriesToCache);

		return lookups.getAllResults();
	}

	protected ServiceResult lookupDataFromServices(List<TournamentInfo> missingTournaments, List<TournamentInfo> tournamentsInCache) throws TournamentApiException, ViewerException, DumpException, CacheException {
		ExecutorService pool = Executors.newFixedThreadPool(4);
		Future<List<Tournament>> tournamentLookup = pool.submit(() -> this.tournamentService.getTournaments(missingTournaments));
		Future<List<MatchCacheEntry>> cacheLookup = pool.submit(() -> this.matchCacheService.getMatchCacheEntryForTournamentListing(tournamentsInCache));

		List<Long> missingTournamentIds = missingTournaments.stream().map(TournamentInfo::getID).collect(Collectors.toList());
		Future<List<BotBetData>> botBetCountLookup = pool.submit(() -> this.betCountService.getBetBotDataAfterTournament(missingTournamentIds));
		Future<Map<Long, Map<BattleGroundTeam, Integer>>> teamValueLookup = pool.submit(() -> this.dumpService.getTeamValuesForTournaments(missingTournamentIds));

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

		Map<Long, Map<BattleGroundTeam, Integer>> teamValueResult = null;
		try {
			teamValueResult = teamValueLookup.get();
		} catch (InterruptedException | ExecutionException e) {
			log.warn("Error fetching team values; continuing without them", e);
		}

		List<MatchCacheEntry> cacheData;
		try {
			cacheData = cacheLookup.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new CacheException(e);
		}

		// prep ids to check for bad tournaments
		Set<Long> tournamentIdsToCollate = this.getAllTournamentIds(tournamentResult, botBetDataResult);

		List<MatchCacheEntry> newMatchCacheEntries = MatchCacheEntry.collate(tournamentResult, botBetDataResult);

		//attach the fetched team values to the fresh entries before they are cached
		final Map<Long, Map<BattleGroundTeam, Integer>> teamValuesToAttach = teamValueResult;
		if(teamValuesToAttach != null) {
			newMatchCacheEntries.forEach(entry -> entry.setTeamValues(teamValuesToAttach.get(entry.getTournamentId())));
		}

		Set<Long> matchEntryIds = newMatchCacheEntries.stream().map(MatchCacheEntry::getTournamentId).collect(Collectors.toSet());
		Set<Long> badIds = tournamentIdsToCollate.stream().filter(id -> !matchEntryIds.contains(id)).collect(Collectors.toSet());
		this.badTournamentService.addBadTournament(badIds);

		return new ServiceResult(newMatchCacheEntries, cacheData);
	}

	protected Set<Long> getAllTournamentIds(List<Tournament> tournamentResult, List<BotBetData> botBetDataResult) {
		Set<Long> tournamentIdsToCollate = new HashSet<>();

		Set<Long> tournamentResultIds = tournamentResult.stream().map(Tournament::getID).collect(Collectors.toSet());
		Set<Long> botBetDataResultIds = botBetDataResult.stream().map(BotBetData::getTournamentId).collect(Collectors.toSet());

		tournamentIdsToCollate.addAll(tournamentResultIds);
		tournamentIdsToCollate.addAll(botBetDataResultIds);

		return tournamentIdsToCollate;
	}

	record ServiceResult(List<MatchCacheEntry> serviceData, List<MatchCacheEntry> cacheData) {
		public List<MatchCacheEntry> getAllResults() {
			return Stream.of(this.serviceData, this.cacheData).flatMap(Collection::stream).collect(Collectors.toList());
		}
	};
}
