package fft_battleground.botland.bot;

import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeamValueBot extends SimulatedBetBot {
	private static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";

	private String betAmountExpression;
	private boolean warnedAboutMissingTeamValues = false;

	public TeamValueBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}

	@Override
	public void init() {
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected Bet generateBetAmount(Match match, long gil) {
		int leftScore = this.teamValue(match.leftTeam());
		int rightScore = this.teamValue(match.rightTeam());
		BattleGroundTeam chosenTeam = leftScore >= rightScore ? match.leftTeam().team() : match.rightTeam().team();

		int betAmount = this.calculateBetAmount((float) leftScore, (float) rightScore, gil);
		betAmount = Math.max((int) GIL_FLOOR, Math.min(betAmount, (int) Math.min(gil, BET_MAX)));
		return new Bet(chosenTeam, betAmount);
	}

	//team value is the gil spent to enter the tournament; teams from cache files written before team values existed score 0
	private int teamValue(fft_battleground.match.model.TeamAttributes teamAttributes) {
		int teamValue = teamAttributes.teamValue();
		if(teamValue == 0 && !this.warnedAboutMissingTeamValues) {
			log.warn("Bot {} found a match with missing team value data; that side will score 0", this.name);
			this.warnedAboutMissingTeamValues = true;
		}
		return teamValue;
	}

	protected Integer calculateBetAmount(Float leftScore, Float rightScore, long gil) {
		if(this.betAmountExpression == null) {
			return (int) Math.min(gil, BET_MAX);
		}
		Argument leftScoreArg = new Argument("leftScore", (double) leftScore);
		Argument rightScoreArg = new Argument("rightScore", (double) rightScore);
		Constant minBet = new Constant("mnBet", (double) GIL_FLOOR);
		Constant maxBet = new Constant("mxBet", (double) BET_MAX);
		Argument balanceArg = new Argument("balance", gil);

		Expression exp = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
		double result = exp.calculate();
		if(Double.isNaN(result)) {
			log.warn("Bet expression for bot {} produced NaN, betting the max instead", this.name);
			return (int) Math.min(gil, BET_MAX);
		}
		return Double.valueOf(result).intValue();
	}
}
