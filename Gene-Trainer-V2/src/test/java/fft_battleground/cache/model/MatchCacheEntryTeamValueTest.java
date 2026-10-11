package fft_battleground.cache.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.model.BattleGroundTeam;

public class MatchCacheEntryTeamValueTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testTeamValuesRoundTrip() throws Exception {
		Map<BattleGroundTeam, Integer> teamValues = Map.of(
				BattleGroundTeam.RED, 250,
				BattleGroundTeam.BLUE, 300,
				BattleGroundTeam.CHAMPION, 1000);
		MatchCacheEntry entry = new MatchCacheEntry(123L, null, null, teamValues);

		String json = this.mapper.writeValueAsString(entry);
		MatchCacheEntry parsed = this.mapper.readValue(json, MatchCacheEntry.class);

		assertEquals(250, parsed.getTeamValues().get(BattleGroundTeam.RED));
		assertEquals(300, parsed.getTeamValues().get(BattleGroundTeam.BLUE));
		assertEquals(1000, parsed.getTeamValues().get(BattleGroundTeam.CHAMPION));
	}

	@Test
	public void testOldCacheFilesWithoutTeamValuesDeserializeToNull() throws Exception {
		String oldFormatJson = "{\"tournamentId\":123}";

		MatchCacheEntry parsed = this.mapper.readValue(oldFormatJson, MatchCacheEntry.class);

		assertEquals(123L, parsed.getTournamentId());
		assertNull(parsed.getTeamValues(), "cache files written before the teamValues field existed must still load");
	}
}
