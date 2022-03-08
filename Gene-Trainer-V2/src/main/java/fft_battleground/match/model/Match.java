package fft_battleground.match.model;

import fft_battleground.model.BattleGroundTeam;

public record Match (int map, TeamAttributes leftTeam, TeamAttributes rightTeam, TeamBetData bets, BattleGroundTeam winner) {

}
