package fft_battleground.botland.bot;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.genedata.GeneFileService;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.TeamAttributes;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnitAwareBot extends SimulatedBetBot {
	//control params that are not unit weights; note the viewer's typo'd spelling of the faith multiplier
	private static final String BRAVE_MULTIPLIER_PARAMETER = "bravemultiplier";
	private static final String FAITH_MULTIPLIER_PARAMETER = "faithmuliplier";

	private String betAmountExpression;
	private double braveMultiplier = 0;
	private double faithMultiplier = 0;
	private double[] weightByGeneId;
	private Map<String, BotParam> paramMap;

	public UnitAwareBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		this.paramMap = map;
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
		if(map.containsKey(BRAVE_MULTIPLIER_PARAMETER)) {
			this.braveMultiplier = Double.parseDouble(map.get(BRAVE_MULTIPLIER_PARAMETER).getValue());
		}
		if(map.containsKey(FAITH_MULTIPLIER_PARAMETER)) {
			this.faithMultiplier = Double.parseDouble(map.get(FAITH_MULTIPLIER_PARAMETER).getValue());
		}
	}

	@Override
	public void init() {
		//every remaining numeric param is a unit weight, mapped onto the genome's gene id space
		Map<String, Double> weightsByName = parseUnitWeights(this.paramMap);
		this.weightByGeneId = GeneFileService.buildValueByGeneId(super.genome, weightsByName);
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
		int leftScore = this.scoreByUnits(match.leftTeam());
		int rightScore = this.scoreByUnits(match.rightTeam());
		BattleGroundTeam chosenTeam = leftScore >= rightScore ? match.leftTeam().team() : match.rightTeam().team();

		int betAmount = this.calculateBetAmount((float) leftScore, (float) rightScore, gil);
		betAmount = Math.max((int) GIL_FLOOR, Math.min(betAmount, (int) Math.min(gil, BET_MAX)));
		return new Bet(chosenTeam, betAmount);
	}

	protected int scoreByUnits(TeamAttributes teamAttributes) {
		int score = 0;
		for(short brave : teamAttributes.braves()) {
			score += (int) (brave * this.braveMultiplier);
		}
		for(short faith : teamAttributes.faiths()) {
			score += (int) (faith * this.faithMultiplier);
		}
		for(short attribute : teamAttributes.attributes()) {
			if(attribute >= 0 && attribute < this.weightByGeneId.length) {
				score += (int) this.weightByGeneId[attribute];
			}
		}
		return score;
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

	static Map<String, Double> parseUnitWeights(Map<String, BotParam> params) {
		return params.entrySet().stream()
				.filter(entry -> !isControlParam(entry.getKey()))
				.filter(entry -> StringUtils.isNumeric(entry.getValue().getValue().replace("-", "").replace(".", "")))
				.collect(Collectors.toMap(entry -> StringUtils.lowerCase(entry.getKey()), entry -> Double.valueOf(entry.getValue().getValue()), (first, second) -> first, HashMap::new));
	}

	private static boolean isControlParam(String paramName) {
		return StringUtils.equalsAnyIgnoreCase(paramName, "personality", "betExpression", "inverse", "betType", "choice",
				BRAVE_MULTIPLIER_PARAMETER, FAITH_MULTIPLIER_PARAMETER);
	}
}
