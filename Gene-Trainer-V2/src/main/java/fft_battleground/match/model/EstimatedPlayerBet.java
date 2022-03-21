package fft_battleground.match.model;

import fft_battleground.model.BattleGroundTeam;
import lombok.Value;

@Value
public class EstimatedPlayerBet {
	private int playerBetId;
	private int betAmount;
	private int balanceAtTimeOfBet;
	private BattleGroundTeam team;
}