package fft_battleground.cache.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fft_battleground.dump.model.DumpData;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.viewer.model.BotBetData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class MatchCacheEntry implements Comparable<MatchCacheEntry> {
	private Long tournamentId;
	private Tournament tournament;
	private List<BotBetData> botBetData;
	private DumpData dumpData;
	
	public static List<MatchCacheEntry> collate(List<Tournament> tournaments, List<BotBetData> botBetData, Map<Long, DumpData> tournamentDumpDataMap) {
		//group data by tournament id
		Map<Long, Tournament> tournamentIdMap = tournaments.stream().collect(Collectors.toMap(Tournament::getID, Function.identity()));
		Map<Long, List<BotBetData>> botBetDataByTournamentId = botBetData.stream().collect(Collectors.groupingBy(BotBetData::getTournamentId));
		
		//ensure that there are 8 entries in BotBetData.  One for each match in a tournament
		Map<Long, List<BotBetData>> filterdBotBetDataByTournamentId = new HashMap<>();
		botBetDataByTournamentId.forEach((id, list) ->{
			if(id.longValue() == 1639305296545L  || id.longValue() == 1639281050642L) {
				log.info("found it");
			}
			if(list.size() == 8) {
				filterdBotBetDataByTournamentId.put(id, list);
			} else {
				log.warn("filtering out botbetdata for tournament {} because there are {} entries instead of the expected 8.", id, list.size());
			}
		});		
		
		//find list of tournament ids shared by both datasets
		
		Set<Long> allTournamentIdsInBothDatasets = Stream.of(tournamentIdMap.keySet(), filterdBotBetDataByTournamentId.keySet(), tournamentDumpDataMap.keySet())
				.flatMap(Collection::stream)
				.filter(tournamentId -> tournamentIdMap.containsKey(tournamentId))
				.filter(tournamentId -> filterdBotBetDataByTournamentId.containsKey(tournamentId))
				.filter(tournamentId -> tournamentDumpDataMap.containsKey(tournamentId))
				.collect(Collectors.toSet());
		
		List<MatchCacheEntry> matchCacheEntries = allTournamentIdsInBothDatasets.stream()
				.map(tournamentId -> new MatchCacheEntry(tournamentId, tournamentIdMap.get(tournamentId), filterdBotBetDataByTournamentId.get(tournamentId), 
						tournamentDumpDataMap.get(tournamentId)))
				.collect(Collectors.toList());
		return matchCacheEntries;
	}

	@Override
	public int compareTo(MatchCacheEntry o) {
		return tournament.getID().compareTo(o.tournament.getID());
	}

}
