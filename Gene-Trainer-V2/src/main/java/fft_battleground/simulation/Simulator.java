package fft_battleground.simulation;

import fft_battleground.genetic.model.attributes.BetGeneAttributes;
import fft_battleground.match.model.EstimatedPlayerBet;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.TeamAttributes;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Simulator extends AbstractSimulator {
	
	public Simulator(SimulationConfig simulationConfig) {
		super(simulationConfig);
	}
	
	@Override
	public long gilResultFromSimulation(Genotype<DoubleGene> genotype) {
		long gil = GIL_FLOOR;
		for(Match match: this.matches) {
			Bet matchBet = this.makeBet(match, genotype, gil);
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
			
			if(gil < GIL_FLOOR) {
				gil = GIL_FLOOR;
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
	
	protected Bet makeBet(Match match, Genotype<DoubleGene> genotype, long gil) {
		int leftScore = this.scoreTeamAttributes(match.leftTeam(), genotype, match.map(), BattleGroundTeam.LEFT);
		leftScore += this.scoreTeamBets(match.bets().leftTeamEstimatedBets(), genotype);
		
		int rightScore = this.scoreTeamAttributes(match.rightTeam(), genotype, match.map(), BattleGroundTeam.RIGHT);
		rightScore += this.scoreTeamBets(match.bets().rightTeamEstimatedBets(), genotype);
		
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
		
		double scoreRatio = ((double) (winnerScore - loserScore + 1)) / ((double) winnerScore + 1);
		scoreRatio = scoreRatio;
		int ratioRounded = (int) Math.round(scoreRatio * 100);
		if(ratioRounded < 1) {
			//log.warn("ratio was less than 1! The ratio is {}", ratioRounded);
			ratioRounded = 1;
		}
		if(ratioRounded > 99) {
			//log.warn("ratio is greater than 99!  The ratio is {}", ratioRounded);
			ratioRounded = 99;
		}
		
		int betAmount = (int) Math.round(this.geneArray.get(genotype, this.startOfBetArrayIndex + ratioRounded));
		if(gil < betAmount) {
			betAmount = (int) gil;
		}
		if(betAmount < GIL_FLOOR) {
			betAmount = (int) GIL_FLOOR;
		}
		Bet bet = new Bet(side, betAmount);
		return bet;
	}
	
	protected int scoreTeamAttributes(TeamAttributes teamAttributes, Genotype<DoubleGene> genotype, int mapNumber, BattleGroundTeam side) {
		int score = 0;
		for(short attribute : teamAttributes.attributes()) {
			score += (int) this.geneArray.get(genotype, attribute);
		}
		for(short faith : teamAttributes.faiths()) {
			score += (int) faith * (this.geneArray.get(genotype, this.faithIndex) + this.geneArray.get(genotype, this.braveFaithIndex));
		}
		for(short brave : teamAttributes.braves()) {
			score += (int) brave * (this.geneArray.get(genotype, this.faithIndex) + this.geneArray.get(genotype, this.braveFaithIndex));
		}
		score += teamAttributes.raidBossCount() * this.geneArray.get(genotype, this.raidBossIndex);
		
		for(double fightWinRatio: teamAttributes.unitFightWinRatios()) {
			if(fightWinRatio >= 0) {
				score += fightWinRatio * this.geneArray.get(genotype, this.fightWinRatioIndex);
			} else if(fightWinRatio < 0) {
				score += this.geneArray.get(genotype, this.missingfightWinRatioIndex) * this.geneArray.get(genotype, this.fightWinRatioIndex);
			}
		}
		
		score+= this.sideIndexes.get(side);
		score+= this.sideIndexes.get(teamAttributes.team());
		
		if(side == BattleGroundTeam.LEFT) {
			score+= this.geneArray.get(genotype, this.leftMapStartIndex + mapNumber);
		} else if(side == BattleGroundTeam.RIGHT) {
			score+= this.geneArray.get(genotype, this.rightMapStartIndex + mapNumber);
		}
		
		return score;
	}
	
	protected int scoreTeamBets(EstimatedPlayerBet[] playerBets, Genotype<DoubleGene> genotype) {
		int score = 0;
		score += playerBets.length * this.geneArray.get(genotype, this.betCountIndex);
		for(EstimatedPlayerBet bet: playerBets) {
			score+= playerBetRatios[bet.getPlayerBetId()] * this.geneArray.get(genotype, this.betRatioIndex);
			score+= super.getBotBetIndexes().values().contains(bet.getPlayerBetId()) ? this.geneArray.get(genotype, playerBotIndex) : this.geneArray.get(genotype, playerHumanIndex);
			score+= super.subscriberBitSet.get(bet.getPlayerBetId()) ? this.geneArray.get(genotype, playerSubscriberIndex) : 0;
			score+= ((double) bet.getBetAmount()) / ((double) bet.getBalanceAtTimeOfBet()) * this.geneArray.get(genotype, this.playerBalanceBetRatioIndex);
		}
		return score;
	}
	
	protected double[] convertGenoTypeToDoubleArray(Genotype<DoubleGene> genotype) {
		return genotype.stream().flatMap(chromosome -> chromosome.stream())
				.mapToDouble(DoubleGene::doubleValue).toArray();
	}
}

record Bet(BattleGroundTeam side, int value) {};
