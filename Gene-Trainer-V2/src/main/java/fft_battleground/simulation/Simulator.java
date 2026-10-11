package fft_battleground.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.google.common.math.Quantiles;

import fft_battleground.genetic.model.attributes.BetGeneAttributes;
import fft_battleground.match.model.Match;
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

	public Map<Integer, Double> calculatePercentiles(Genotype<DoubleGene> genotype) {
		List<Integer> scoreDifferences = new LinkedList<>();
		for(Match match: this.matches) {
			int leftScore = this.calculateLeftScore(match, genotype);
			int rightScore = this.calculateRightScore(match, genotype);

			int difference = Math.abs(leftScore - rightScore);
			scoreDifferences.add(difference);
		}

		Map<Integer, Double> percentiles = Quantiles.percentiles()
				.indexes(IntStream.range(1, 100).toArray())
				.compute(scoreDifferences);

		return percentiles;
	}

	protected Bet makeBet(Match match, Genotype<DoubleGene> genotype, long gil) {
		int leftScore = this.calculateLeftScore(match, genotype);
		int rightScore = this.calculateRightScore(match, genotype);

		BattleGroundTeam side = leftScore >= rightScore ? match.leftTeam().team() : match.rightTeam().team();
		int betAmount = this.calculateBetAmount(leftScore, rightScore, genotype);
		if(gil < betAmount) {
			betAmount = (int) gil;
		}
		if(betAmount < GIL_FLOOR) {
			betAmount = (int) GIL_FLOOR;
		}
		Bet bet = new Bet(side, betAmount);
		return bet;
	}
}

record Bet(BattleGroundTeam side, int value) {};
