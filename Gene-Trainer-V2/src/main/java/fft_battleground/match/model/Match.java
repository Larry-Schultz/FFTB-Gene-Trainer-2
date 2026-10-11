package fft_battleground.match.model;

import fft_battleground.model.BattleGroundTeam;

public record Match (long tournamentId, int matchNumber, int map, TeamAttributes leftTeam, TeamAttributes rightTeam, TeamBetData bets, BattleGroundTeam winner) {

}
