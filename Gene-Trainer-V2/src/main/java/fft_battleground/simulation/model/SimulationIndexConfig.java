package fft_battleground.simulation.model;

import java.util.Map;

import fft_battleground.model.BattleGroundTeam;

public record SimulationIndexConfig(int faithIndex, int braveIndex, int braveFaithIndex, int raidBossIndex, int fightWinRatioIndex, 
		int missingFightWinRatioIndex, int betCountIndex, int betRatioIndex, int champStreakIndex, int startOfBetArrayIndex, 
		int playerHumanIndex, int playerBotIndex, int playerSubscriberIndex, int playerBalanceBetRatioIndex, Map<BattleGroundTeam, Integer> sideIndexes, 
		int leftMapsStartIndex, int rightMapStartIndex) {

}
