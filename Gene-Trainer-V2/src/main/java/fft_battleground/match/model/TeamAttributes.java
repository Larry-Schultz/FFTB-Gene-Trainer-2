package fft_battleground.match.model;

import fft_battleground.model.BattleGroundTeam;

public record TeamAttributes(BattleGroundTeam team, short[] attributes, short[] braves, short[] faiths, int raidBossCount, double[] unitFightWinRatios) {

}
