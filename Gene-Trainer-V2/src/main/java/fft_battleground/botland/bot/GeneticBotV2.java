package fft_battleground.botland.bot;

import java.util.Map;

import fft_battleground.botland.genedata.GeneFileService;
import fft_battleground.botland.genedata.GeneFileService.V2GeneData;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;

public class GeneticBotV2 extends SimulatedBetBot {
	private static final String GENE_FILE_PARAMETER = "geneFile";
	private static final String USE_BET_GENES_PARAMETER = "use_bet_genes";

	private final GeneFileService geneFileService;

	private String geneFileName;
	private boolean useBetGenes = false;
	private Genotype<DoubleGene> genotype;

	public GeneticBotV2(SimulationConfig simulatorConfig, GeneFileService geneFileService) {
		super(simulatorConfig);
		this.geneFileService = geneFileService;
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(GENE_FILE_PARAMETER)) {
			this.geneFileName = map.get(GENE_FILE_PARAMETER).getValue();
		}
		if(map.containsKey(USE_BET_GENES_PARAMETER)) {
			this.useBetGenes = Boolean.valueOf(map.get(USE_BET_GENES_PARAMETER).getValue());
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}

	@Override
	public void init() throws BotConfigException {
		if(this.geneFileName == null) {
			throw new BotConfigException("GeneticBotV2 " + this.name + " has no geneFile param");
		}
		V2GeneData geneData = this.geneFileService.loadV2GeneData(this.geneFileName);
		//build a genotype holding the file's values over the shared genome's id space; the shared GeneArray stays valid
		this.genotype = GeneFileService.buildGenotype(super.genome, geneData.geneValues());
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
		int leftScore = this.calculateLeftScore(match, this.genotype);
		int rightScore = this.calculateRightScore(match, this.genotype);
		BattleGroundTeam chosenTeam = leftScore >= rightScore ? match.leftTeam().team() : match.rightTeam().team();

		//bet gene sizing by confidence ratio, or a flat max bet when bet genes are disabled
		int betAmount = this.useBetGenes ? this.calculateBetAmount(leftScore, rightScore, this.genotype) : (int) BET_MAX;
		betAmount = Math.max(1, Math.min(betAmount, (int) Math.min(gil, BET_MAX)));
		return new Bet(chosenTeam, betAmount);
	}
}
