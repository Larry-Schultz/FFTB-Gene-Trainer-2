package fft_battleground.match.model;


public record TeamBetData(EstimatedPlayerBet[] leftTeamEstimatedBets, EstimatedPlayerBet[] rightTeamEstimatedBets,
		ActualPlayerBet[] leftTeamActualBets, ActualPlayerBet[] rightTeamActualBets, MatchPot botEstimatedPot, MatchPot actualPot) {

}
