package fft_battleground.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.cache.model.MatchCacheEntry;
import fft_battleground.exception.CacheException;
import fft_battleground.tournament.model.TournamentInfo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatchCacheServiceImpl implements MatchCacheService {
	private static final boolean useZip = true;
	private static final String folderName = "tournamentCache";
	
	private static final String zipFileExtension = ".gz";
	
	private final Map<Long, File> cacheFileMap;
	
	public MatchCacheServiceImpl() {
		this.cacheFileMap = this.walkTournamentsFileCache();
	}
	
	@Override
	public Set<Long> tournamentIdsContainedWithinCache(List<TournamentInfo> tournamentListings) {
		Set<Long> tournamentListingIds = tournamentListings.stream().map(TournamentInfo::getID).collect(Collectors.toSet());
		Set<Long> tournamentListingIdsContainedInTheCache = this.cacheFileMap.keySet().stream().filter(tournamentId -> tournamentListingIds.contains(tournamentId))
				.collect(Collectors.toSet());
		return tournamentListingIdsContainedInTheCache;
	}
		
	
	@Override
	public List<MatchCacheEntry> getMatchCacheEntryForTournamentListing(List<TournamentInfo> tournamentListings) {
		List<MatchCacheEntry> entries = new ArrayList<>();
		for(TournamentInfo listing: tournamentListings) {
			if(this.cacheFileMap.containsKey(listing.getID())) {
				try {
					MatchCacheEntry entry = this.readMatchCacheFileById(listing.getID());
					entries.add(entry);
				} catch (CacheException e) {
					log.warn("failure to read cache for tournament ID {}", listing.getID(), e);
				}
			}
		}
		return entries;
	}

	@Override
	public void cacheNewEntries(List<MatchCacheEntry> newMatchCacheEntries) {
		for(MatchCacheEntry newEntry: newMatchCacheEntries) {
			try {
				this.writeTournamentCacheFileForId(newEntry.getTournamentId(), newEntry);
			} catch (CacheException e) {
				log.warn("failure to write to cache for ID {}", newEntry.getTournamentId(), e);
			}
		}
	}
	
	protected MatchCacheEntry readMatchCacheFileById(Long id) throws CacheException {
		File tournamentCacheFile = this.cacheFileMap.get(id);
		MatchCacheEntry tournament = null;
		try {
			 InputStream fStream = new BufferedInputStream(new FileInputStream(tournamentCacheFile));
			if(useZip) {
				GZIPInputStream zStream = new GZIPInputStream(fStream);
				fStream = zStream;
			}
			ObjectMapper mapper = new ObjectMapper();
			tournament = mapper.readValue(fStream, MatchCacheEntry.class);
		} catch (IOException e) {
			log.error("error reading cache data for tournament {}", id, e);
			throw new CacheException(e);
		}
		
		log.info("reading match info for tournament {}", tournament.getTournamentId());
		
		return tournament;
	}
	
	protected void writeTournamentCacheFileForId(Long id, MatchCacheEntry matchCacheEntry) throws CacheException {
		String fileExtension = useZip ? ".gz" : ".json";
		String fileLocation = folderName + File.separator + id.toString() + fileExtension;

		try {
			File newTournamentCacheFile = new File(fileLocation);
			OutputStream fStream =new BufferedOutputStream(new FileOutputStream(newTournamentCacheFile));
			if(useZip) {
				fStream = new GZIPOutputStream(fStream);
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(fStream, matchCacheEntry);
		} catch (IOException e) {
			throw new CacheException(e);
		}
	}
	
	protected Map<Long, File> walkTournamentsFileCache() {
		Map<Long, File> tournamentFileMap = new HashMap<>();
		
		File tournamentFileCacheFolder = new File(folderName);
		String[] filenames = tournamentFileCacheFolder.list();
		if(filenames != null) {
			for(String filename: filenames) {
				File tournamentFile = new File(folderName + File.separator + filename);
				String cleanedFilename = StringUtils.replace(filename, zipFileExtension, "");
				cleanedFilename = StringUtils.replace(cleanedFilename, ".json", "");
				Long tournamentId = Long.valueOf(cleanedFilename);
				tournamentFileMap.put(tournamentId, tournamentFile);
			}
		}
		return tournamentFileMap;
	}

}
