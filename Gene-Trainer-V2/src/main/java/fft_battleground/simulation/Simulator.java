package fft_battleground.simulation;

import fft_battleground.genetic.model.BetGeneAttributes;
import fft_battleground.match.model.EstimatedPlayerBet;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.TeamAttributes;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Simulator extends AbstractSimulator {
	
	public Simulator(SimulationConfig simulationConfig) {
		super(simulationConfig);
	}
	
	@Override
	public long gilResultFromSimulation(Genotype<DoubleGene> genotype) {
		long gil = GIL_FLOOR;
		double[] geneArray = this.convertGenoTypeToDoubleArray(genotype);
		for(Match match: this.matches) {
			Bet matchBet = this.makeBet(match, geneArray, gil);
			if(matchBet.side() == match.winner()) {
				if(matchBet.side() == match.leftTeam().team()) {
					gil += match.bets().actualPot().leftOdds() * matchBet.value();
				} else {
					gil += match.bets().actualPot().rightOdds() * matchBet.value();
				}
			} else if(matchBet.side() != match.winner() && matchBet.side() != null) {
				gil -= matchBet.value();
				gil = Math.max(gil, GIL_FLOOR);
			}
		}
		
		return gil;
	}
	
	public long perfectScoreGil() {
		long gil = GIL_FLOOR;
		for(Match match: this.matches) {
			int betAmount = Math.min((int) gil, BetGeneAttributes.BET_MAX);
			if(match.winner() == match.leftTeam().team()) {
				gil += match.bets().actualPot().leftOdds() * betAmount;
			} else if(match.winner() == match.rightTeam().team()) {
				gil += match.bets().actualPot().rightOdds() * betAmount;
			} else {
				log.warn("missing winner from match");
			}
		}
		
		return gil;
	}
	
	protected Bet makeBet(Match match, double[] geneArray, long gil) {
		int leftScore = this.scoreTeamAttributes(match.leftTeam(), geneArray);
		leftScore += this.scoreTeamBets(match.bets().leftTeamEstimatedBets(), geneArray);
		
		int rightScore = this.scoreTeamAttributes(match.rightTeam(), geneArray);
		rightScore += this.scoreTeamBets(match.bets().rightTeamEstimatedBets(), geneArray);
		
		BattleGroundTeam side = null;
		int winnerScore = 0;
		int loserScore = 0;
		if(leftScore >= rightScore) {
			winnerScore = leftScore;
			loserScore = rightScore;
			side = match.leftTeam().team();
		} else if(rightScore > leftScore) {
			winnerScore = rightScore;
			loserScore = leftScore;
			side = match.rightTeam().team();
		}
		
		double scoreRatio = ((double) winnerScore + 1) / ((double) loserScore + winnerScore + 1);
		scoreRatio = scoreRatio > 1 ? 1 : scoreRatio;
		int ratioRounded = (int) Math.round(scoreRatio * 100);
		if(ratioRounded > 99) {
			log.warn("ratio is greater than 99!  The ratio is {}", ratioRounded);
			ratioRounded = 99;
		}
		
		int betAmount = (int) Math.round(geneArray[this.startOfBetArrayIndex + ratioRounded]);
		if(gil < betAmount) {
			betAmount = (int) gil;
		}
		if(betAmount < GIL_FLOOR) {
			betAmount = (int) GIL_FLOOR;
		}
		Bet bet = new Bet(side, betAmount);
		return bet;
	}
	
	protected int scoreTeamAttributes(TeamAttributes teamAttributes, double[] geneArray) {
		int score = 0;
		for(short attribute : teamAttributes.attributes()) {
			score += (int) geneArray[attribute];
		}
		for(short faith : teamAttributes.faiths()) {
			score += (int) faith * (geneArray[this.faithIndex] + geneArray[this.braveFaithIndex]);
		}
		for(short brave : teamAttributes.braves()) {
			score += (int) brave * (geneArray[this.faithIndex] + geneArray[this.braveFaithIndex]);
		}
		score += teamAttributes.raidBossCount() * geneArray[this.raidBossIndex];
		
		for(double fightWinRatio: teamAttributes.unitFightWinRatios()) {
			score += fightWinRatio * geneArray[this.fightWinRatioIndex];
		}
		if(teamAttributes.champStreak() != null) {
			score += teamAttributes.champStreak() * geneArray[this.champStreakIndex];
		}
		
		return score;
	}
	
	protected int scoreTeamBets(EstimatedPlayerBet[] playerBets, double[] geneArray) {
		int score = 0;
		score += playerBets.length * geneArray[this.betCountIndex];
		for(EstimatedPlayerBet bet: playerBets) {
			score+= playerBetRatios[bet.getPlayerBetId()] * geneArray[this.betRatioIndex];
		}
		return score;
	}
	
	protected double[] convertGenoTypeToDoubleArray(Genotype<DoubleGene> genotype) {
		return genotype.stream().flatMap(chromosome -> chromosome.stream())
				.mapToDouble(DoubleGene::doubleValue).toArray();
	}
}

record Bet(BattleGroundTeam side, int value) {};
