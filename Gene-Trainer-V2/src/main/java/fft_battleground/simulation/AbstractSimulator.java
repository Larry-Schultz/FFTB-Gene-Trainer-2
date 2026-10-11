package fft_battleground.simulation;

import java.util.BitSet;
import java.util.Map;

import fft_battleground.genetic.model.attributes.BetGeneAttributes;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.match.model.EstimatedPlayerBet;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.MatchPot;
import fft_battleground.match.model.TeamAttributes;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.GeneArray;
import fft_battleground.simulation.model.SimulationConfig;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import lombok.Data;

@Data
public abstract class AbstractSimulator {
	public static final long GIL_FLOOR = BetGeneAttributes.GIL_FLOOR.longValue();
	public static final long BET_MAX = BetGeneAttributes.BET_MAX.longValue();

	protected Match[] matches;
	protected double[] playerBetRatios;
	protected double[] playerFightRatios;
	protected BitSet subscriberBitSet;
	protected Map<String, Integer> botBetIndexes;
	protected GeneArray geneArray;
	protected CompleteBotGenome genome;
	
	protected int faithIndex;
	protected int braveIndex;
	protected int braveFaithIndex;
	protected int raidBossIndex;
	protected int fightWinRatioIndex;
	protected int missingfightWinRatioIndex;
	
	protected int betCountIndex;
	protected int betRatioIndex;
	protected int oddsIndex;
	protected int potAmountIndex;
	
	protected int playerHumanIndex;
	protected int playerBotIndex;
	protected int playerSubscriberIndex;
	protected int playerBalanceBetRatioIndex;
	
	protected int startOfBetArrayIndex;
	
	protected Map<BattleGroundTeam, Integer> sideIndexes;
	protected int leftMapStartIndex;
	protected int rightMapStartIndex;
	
	public AbstractSimulator(SimulationConfig simulatorConfig) {
		this.faithIndex = simulatorConfig.simulatorIndexConfig().faithIndex();
		this.braveIndex = simulatorConfig.simulatorIndexConfig().braveIndex();
		this.raidBossIndex = simulatorConfig.simulatorIndexConfig().raidBossIndex();
		
		this.fightWinRatioIndex = simulatorConfig.simulatorIndexConfig().fightWinRatioIndex();
		this.missingfightWinRatioIndex = simulatorConfig.simulatorIndexConfig().missingFightWinRatioIndex();
		this.betCountIndex = simulatorConfig.simulatorIndexConfig().betCountIndex();
		this.betRatioIndex = simulatorConfig.simulatorIndexConfig().betRatioIndex();
		
		this.playerHumanIndex = simulatorConfig.simulatorIndexConfig().playerHumanIndex();
		this.playerBotIndex = simulatorConfig.simulatorIndexConfig().playerBotIndex();
		this.playerSubscriberIndex = simulatorConfig.simulatorIndexConfig().playerSubscriberIndex();
		this.playerBalanceBetRatioIndex = simulatorConfig.simulatorIndexConfig().playerBalanceBetRatioIndex();
		
		this.startOfBetArrayIndex = simulatorConfig.simulatorIndexConfig().startOfBetArrayIndex();
		
		this.sideIndexes = simulatorConfig.simulatorIndexConfig().sideIndexes();
		this.leftMapStartIndex = simulatorConfig.simulatorIndexConfig().leftMapsStartIndex();
		this.rightMapStartIndex = simulatorConfig.simulatorIndexConfig().rightMapStartIndex();
		
		this.matches = simulatorConfig.matches();
		this.playerBetRatios = simulatorConfig.playerBetRatios();
		this.playerFightRatios = simulatorConfig.playerFightRatios();
		this.subscriberBitSet = simulatorConfig.subscriberBitSet();
		this.botBetIndexes = simulatorConfig.botBetIndexes();
		this.geneArray = simulatorConfig.geneArray();
		this.genome = simulatorConfig.genome();
	}

	public abstract long gilResultFromSimulation(Genotype<DoubleGene> genotype);

	//scoring shared by the genetic trainer (Simulator) and the gene bots (Botland); moved verbatim from Simulator so a
	//trained genome scores identically in both, known quirks included
	protected int calculateLeftScore(Match match, Genotype<DoubleGene> genotype) {
		int leftScore = this.scoreTeamAttributes(match.leftTeam(), genotype, match.map(), BattleGroundTeam.LEFT);
		leftScore += this.scoreTeamBets(match.bets().leftTeamEstimatedBets(), genotype);
		leftScore += this.scoreLeftTeamPot(match.bets().botEstimatedPot(), genotype);

		return leftScore;
	}

	protected int calculateRightScore(Match match, Genotype<DoubleGene> genotype) {
		int rightScore = this.scoreTeamAttributes(match.rightTeam(), genotype, match.map(), BattleGroundTeam.RIGHT);
		rightScore += this.scoreTeamBets(match.bets().rightTeamEstimatedBets(), genotype);
		rightScore += this.scoreRightTeamPot(match.bets().botEstimatedPot(), genotype);

		return rightScore;
	}

	protected int calculateBetAmount(int leftScore, int rightScore, Genotype<DoubleGene> genotype) {
		int winnerScore = 0;
		int loserScore = 0;
		if(leftScore >= rightScore) {
			winnerScore = leftScore;
			loserScore = rightScore;
		} else if(rightScore > leftScore) {
			winnerScore = rightScore;
			loserScore = leftScore;
		}

		double scoreRatio = ((double) (winnerScore - loserScore + 1)) / ((double) winnerScore + 1);
		int ratioRounded = (int) Math.round(scoreRatio * 100);
		if(ratioRounded < 1) {
			ratioRounded = 1;
		}
		if(ratioRounded > 99) {
			ratioRounded = 99;
		}

		int betAmount = (int) Math.round(this.geneArray.get(genotype, this.startOfBetArrayIndex + ratioRounded));
		return betAmount;
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

	protected int scoreLeftTeamPot(MatchPot matchPot, Genotype<DoubleGene> genotype) {
		return scoreTeamPot(genotype, matchPot.leftPot(), matchPot.leftOdds());
	}

	protected int scoreRightTeamPot(MatchPot matchPot, Genotype<DoubleGene> genotype) {
		return scoreTeamPot(genotype, matchPot.rightPot(), matchPot.rightOdds());
	}

	protected int scoreTeamPot(Genotype<DoubleGene> genotype, int pot, double odds) {
		int score = 0;
		score+= pot * this.geneArray.get(genotype, this.potAmountIndex);
		score+= odds * this.geneArray.get(genotype, this.oddsIndex);
		return score;
	}

	protected int scoreTeamBets(EstimatedPlayerBet[] playerBets, Genotype<DoubleGene> genotype) {
		int score = 0;
		score += playerBets.length * this.geneArray.get(genotype, this.betCountIndex);
		for(EstimatedPlayerBet bet: playerBets) {
			score+= playerBetRatios[bet.getPlayerBetId()] * this.geneArray.get(genotype, this.betRatioIndex);
			score+= this.botBetIndexes.values().contains(bet.getPlayerBetId()) ? this.geneArray.get(genotype, this.playerBotIndex) : this.geneArray.get(genotype, this.playerHumanIndex);
			score+= this.subscriberBitSet.get(bet.getPlayerBetId()) ? this.geneArray.get(genotype, this.playerSubscriberIndex) : 0;
			score+= ((double) bet.getBetAmount()) / ((double) bet.getBalanceAtTimeOfBet()) * this.geneArray.get(genotype, this.playerBalanceBetRatioIndex);
		}
		return score;
	}
}
