package fft_battleground.botland.bot;

import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.botland.util.BetType;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BetCountBot extends SimulatedBetBot {
	private static final String BET_TYPE_PARAMETER = "betType";
	private static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";
	
	private String betAmountExpression;
	private BetType type = BetType.FLOOR;
	
	public BetCountBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
	}


	@Override
	public void initParams(Map<String, BotParam> parameters) throws BotConfigException {
		if(parameters.containsKey(BET_TYPE_PARAMETER)) {
			this.type = BetType.getBetType(parameters.get(BET_TYPE_PARAMETER).getValue());
		}
		if(parameters.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = parameters.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
	}

	@Override
	public void init() {
		if(this.betAmountExpression != null) {
			Argument leftScoreArg = new Argument("leftScore", (double) 5f);
			Argument rightScoreArg = new Argument("rightScore", (double) 10f);
			Argument minBet = new Argument("mnBet", (double) GIL_FLOOR);
			Argument maxBet = new Argument("mxBet", (double) BET_MAX);
			Argument balanceArg = new Argument("balance", 100);
			Expression testBetAmountExpression = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
			if(!testBetAmountExpression.checkSyntax() || !testBetAmountExpression.checkLexSyntax()) {
				log.warn("The syntax of the bet Amount expression {} is faulty with error: {}", this.betAmountExpression, testBetAmountExpression.getErrorMessage());
			}
		}
	}
	
	@Override
	protected Bet generateBetAmount(Match match, long gil) {
		float leftScore = this.leftScore(match);
		float rightScore = this.rightScore(match);
		BattleGroundTeam chosenTeam = BattleGroundTeam.NONE;
		if(leftScore >= rightScore) {
			chosenTeam = match.leftTeam().team();
		} else {
			chosenTeam = match.rightTeam().team();
		}
		Bet result = null;
		if(type == BetType.FLOOR) {
			result = new Bet(chosenTeam, BetType.FLOOR);
		} else if(type == BetType.ALLIN) {
			result = new Bet(chosenTeam, BetType.ALLIN);
		} else if(type == BetType.PERCENTAGE) {
			result = new Bet(chosenTeam, (int) gil, this.type);
		} else if(this.betAmountExpression != null) {
			Integer betAmount = this.calculateBetAmount(leftScore, rightScore, gil);
			result = new Bet(chosenTeam, betAmount);
		}
		return result;
	}
	
	protected Integer calculateBetAmount(Float leftScore, Float rightScore, long gil) {
		Integer result = null;
		
		Argument leftScoreArg = new Argument("leftScore", (double) leftScore);
		Argument rightScoreArg = new Argument("rightScore", (double) rightScore);
		Constant minBet = new Constant("mnBet", (double) GIL_FLOOR);
		Constant maxBet = new Constant("mxBet", (double) BET_MAX);
		Argument balanceArg = new Argument("balance", gil);
		
		Expression exp = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
		
		result = Double.valueOf(exp.calculate()).intValue();
		
		return result;
	}
	
	private float leftScore(Match match) {
		return match.bets().leftTeamEstimatedBets().length;
	}
	
	private float rightScore(Match match) {
		return match.bets().rightTeamEstimatedBets().length;
	}


	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}



}
