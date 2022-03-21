package fft_battleground.botland.bot;

import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.model.BraveFaith;
import fft_battleground.botland.util.Bet;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.TeamAttributes;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;

public class BraveFaithBot extends SimulatedBetBot {

	private static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";
	private static final String BRAVE_OR_FAITH = "bravefaith";
	
	private BraveFaith braveFaith;
	private String betAmountExpression;
	
	public BraveFaithBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(BRAVE_OR_FAITH)) {
			this.braveFaith = BraveFaith.parseString(map.get(BRAVE_OR_FAITH).getValue());
		}
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			super.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
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
		Pair<Float, Float> scoresBySide = new ImmutablePair<>(this.generateScoreForUnits(match.leftTeam()), this.generateScoreForUnits(match.rightTeam()));
		BattleGroundTeam chosenTeam = scoresBySide.getLeft() >= scoresBySide.getRight() ? match.leftTeam().team() : match.rightTeam().team();
		
		Integer betAmount = this.calculateBetAmount(scoresBySide.getLeft(), scoresBySide.getRight(), gil);
		Bet bet = new Bet(chosenTeam, betAmount);
		return bet;
	}

	
	protected Float generateScoreForUnits(TeamAttributes teamAttributes) {
		short[] brave = teamAttributes.braves();
		short[] faith = teamAttributes.faiths();
		Float score = 0f;
		if(this.braveFaith == BraveFaith.BRAVE) {
			score = (float) IntStream.range(0, brave.length).map(i -> (int) brave[i]).sum();
		} else if(this.braveFaith == BraveFaith.FAITH) {
			score = (float) IntStream.range(0, faith.length).map(i -> (int) faith[i]).sum();
		} else if(this.braveFaith == BraveFaith.BOTH) {
			score = (float) IntStream.range(0, Math.min(brave.length, faith.length)).map(i -> (int) brave[i] + faith[i]).sum();
		}
		return score;
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


}
