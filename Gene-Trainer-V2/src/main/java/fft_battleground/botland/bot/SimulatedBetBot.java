package fft_battleground.botland.bot;

import java.util.Map;

import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.AbstractSimulator;
import fft_battleground.simulation.model.SimulationConfig;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;

public abstract class SimulatedBetBot extends AbstractSimulator {
	protected static final String INVERSE_PARAM = "inverse";
	protected static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";
	
	protected boolean inverse = false;
	protected String name;
	
	public SimulatedBetBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
	}

	@Override
	public long gilResultFromSimulation(Genotype<DoubleGene> genotype) {
		long gil = 0L;
		for(Match match: this.matches) {
			Bet matchBet = this.generateBetAmount(match, gil);
			if(inverse) {
				matchBet = this.inverseBet(matchBet, match);
			}
			if(matchBet.getTeam() == match.winner()) {
				if(matchBet.getTeam() == match.leftTeam().team()) {
					gil += match.bets().actualPot().leftOdds() * matchBet.getAmount();
				} else {
					gil += match.bets().actualPot().rightOdds() * matchBet.getAmount();
				}
			} else if(matchBet.getTeam() != match.winner() && matchBet.getTeam() != null) {
				gil -= matchBet.getAmount();
				gil = Math.max(gil, GIL_FLOOR);
			}
		}
		
		return gil;
	}
	
	public abstract void initParams(Map<String, BotParam> map) throws BotConfigException;
	public abstract void init();
	public abstract String getName();
	public abstract void setName(String name);
	protected abstract Bet generateBetAmount(Match match, long gil);
	
	private Bet inverseBet(Bet matchBet, Match match) {
		BattleGroundTeam newTeam = matchBet.getTeam() == match.rightTeam().team() ? match.leftTeam().team() : match.rightTeam().team();
		Bet inverseMatchBet = new Bet(newTeam, matchBet.getAmount());
		return inverseMatchBet;
	}
}
