package fft_battleground.simulation.model;

import java.util.BitSet;
import java.util.Map;

import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.match.model.Match;

public record SimulationConfig(SimulationIndexConfig simulatorIndexConfig, Match[] matches, double[] playerBetRatios,
		double[] playerFightRatios, BitSet subscriberBitSet, Map<String, Integer> botBetIndexes, GeneArray geneArray,
		CompleteBotGenome genome) {

}
