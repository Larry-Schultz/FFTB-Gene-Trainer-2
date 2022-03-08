package fft_battleground.simulation.model;

import fft_battleground.match.model.Match;

public record SimulationConfig(int faithIndex, int braveIndex, int braveFaithIndex, int raidBossIndex, int fightWinRatioIndex, 
		int betCountIndex, int betRatioIndex, int champStreakIndex, int startOfBetArrayIndex, Match[] matches, 
		double[] playerBetRatios) {

}
