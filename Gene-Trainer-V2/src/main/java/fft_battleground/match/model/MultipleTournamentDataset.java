package fft_battleground.match.model;

import java.util.BitSet;
import java.util.Map;

public record MultipleTournamentDataset(Match[] matches, double[] playerBetRatios, double[] playerFightRatios, 
		BitSet subscriberBitSet, Map<String, Integer> botBetIndexes) {
	
}
