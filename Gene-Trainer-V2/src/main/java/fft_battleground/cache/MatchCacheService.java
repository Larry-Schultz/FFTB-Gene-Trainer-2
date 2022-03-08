package fft_battleground.cache;

import java.util.List;
import java.util.Set;

import fft_battleground.cache.model.MatchCacheEntry;
import fft_battleground.exception.CacheException;
import fft_battleground.tournament.model.TournamentInfo;

public interface MatchCacheService {
	Set<Long> tournamentIdsContainedWithinCache(List<TournamentInfo> tournamentListings);
	List<MatchCacheEntry> getMatchCacheEntryForTournamentListing(List<TournamentInfo> tournamentListings) throws CacheException;
	void cacheNewEntries(List<MatchCacheEntry> newMatchCacheEntries) throws CacheException;
}
