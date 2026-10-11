package fft_battleground.match;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fft_battleground.match.model.Match;

public class MatchOrderingTest {

	@Test
	public void testMatchesSortByTournamentThenMatchNumber() {
		List<Match> outOfOrder = new ArrayList<>();
		outOfOrder.add(match(200, 1));
		outOfOrder.add(match(100, 5));
		outOfOrder.add(match(100, 1));
		outOfOrder.add(match(200, 0));
		outOfOrder.add(match(100, 0));

		outOfOrder.sort(TrainerDataGenerator.MATCH_ORDER);

		assertEquals(100, outOfOrder.get(0).tournamentId());
		assertEquals(0, outOfOrder.get(0).matchNumber());
		assertEquals(100, outOfOrder.get(1).tournamentId());
		assertEquals(1, outOfOrder.get(1).matchNumber());
		assertEquals(100, outOfOrder.get(2).tournamentId());
		assertEquals(5, outOfOrder.get(2).matchNumber());
		assertEquals(200, outOfOrder.get(3).tournamentId());
		assertEquals(0, outOfOrder.get(3).matchNumber());
		assertEquals(200, outOfOrder.get(4).tournamentId());
		assertEquals(1, outOfOrder.get(4).matchNumber());
	}

	@Test
	public void testSliceLastNKeepsMostRecentInChronologicalOrder() {
		Match[] matches = new Match[] { match(100, 0), match(100, 1), match(200, 0), match(200, 1), match(300, 0) };

		Match[] sliced = TrainerDataGenerator.sliceLastN(matches, 3);

		assertEquals(3, sliced.length);
		//chronological order must be preserved: (200,0), (200,1), (300,0)
		assertArrayEquals(new long[] { 200, 200, 300 }, new long[] { sliced[0].tournamentId(), sliced[1].tournamentId(), sliced[2].tournamentId() });
		assertArrayEquals(new int[] { 0, 1, 0 }, new int[] { sliced[0].matchNumber(), sliced[1].matchNumber(), sliced[2].matchNumber() });
	}

	@Test
	public void testSliceLastNReturnsAllWhenCountExceedsLength() {
		Match[] matches = new Match[] { match(100, 0), match(100, 1) };

		Match[] sliced = TrainerDataGenerator.sliceLastN(matches, 10);

		assertEquals(2, sliced.length);
		assertEquals(matches, sliced);
	}

	private Match match(long tournamentId, int matchNumber) {
		return new Match(tournamentId, matchNumber, 1, null, null, null, null);
	}
}
