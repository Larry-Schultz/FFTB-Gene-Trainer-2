package fft_battleground.botland.bot;

import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.genedata.GeneFileService;
import fft_battleground.botland.genedata.GeneFileService.V1GeneData;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.TeamAttributes;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneticBot extends SimulatedBetBot {
	private static final String GENE_FILE_PARAMETER = "geneFile";

	private final GeneFileService geneFileService;

	private String geneFileName;
	private String betAmountExpression;
	private double[] valueByGeneId;
	private Map<Integer, Double> percentiles;

	public GeneticBot(SimulationConfig simulatorConfig, GeneFileService geneFileService) {
		super(simulatorConfig);
		this.geneFileService = geneFileService;
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(GENE_FILE_PARAMETER)) {
			this.geneFileName = map.get(GENE_FILE_PARAMETER).getValue();
		}
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}

	@Override
	public void init() throws BotConfigException {
		if(this.geneFileName == null) {
			throw new BotConfigException("GeneticBot " + this.name + " has no geneFile param");
		}
		V1GeneData geneData = this.geneFileService.loadV1GeneData(this.geneFileName);
		this.percentiles = geneData.percentiles();
		this.valueByGeneId = GeneFileService.buildValueByGeneId(super.genome, geneData.geneticAttributes());
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
		int leftScore = this.scoreByGenes(match.leftTeam());
		int rightScore = this.scoreByGenes(match.rightTeam());
		BattleGroundTeam chosenTeam = leftScore >= rightScore ? match.leftTeam().team() : match.rightTeam().team();

		int percentile = GeneFileService.calculatePercentile(this.percentiles, Math.abs(leftScore - rightScore));
		int betAmount = this.calculateBetAmount((float) leftScore, (float) rightScore, percentile, gil);
		//gene bots are allowed to bet below the floor when their expression says so (e.g. smartbet's 1 on percentile <= 1)
		betAmount = Math.max(1, Math.min(betAmount, (int) Math.min(gil, BET_MAX)));
		return new Bet(chosenTeam, betAmount);
	}

	protected int scoreByGenes(TeamAttributes teamAttributes) {
		int score = 0;
		for(short attribute : teamAttributes.attributes()) {
			if(attribute >= 0 && attribute < this.valueByGeneId.length) {
				score += (int) this.valueByGeneId[attribute];
			}
		}
		return score;
	}

	protected Integer calculateBetAmount(Float leftScore, Float rightScore, Integer percentile, long gil) {
		if(this.betAmountExpression == null) {
			return (int) Math.min(gil, BET_MAX);
		}
		Argument leftScoreArg = new Argument("leftScore", (double) leftScore);
		Argument rightScoreArg = new Argument("rightScore", (double) rightScore);
		Constant minBet = new Constant("mnBet", (double) GIL_FLOOR);
		Constant minBetAlias = new Constant("minBet", (double) GIL_FLOOR); //the V1 gene files' smart bet expressions use this spelling
		Constant maxBet = new Constant("mxBet", (double) BET_MAX);
		Argument balanceArg = new Argument("balance", gil);
		Argument percentileArg = new Argument("percentile", percentile);

		Expression exp = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, minBetAlias, maxBet, balanceArg, percentileArg);
		double result = exp.calculate();
		if(Double.isNaN(result)) {
			log.warn("Bet expression for bot {} produced NaN, betting the max instead", this.name);
			return (int) Math.min(gil, BET_MAX);
		}
		return Double.valueOf(result).intValue();
	}
}
