package fft_battleground.simulation;

import java.util.BitSet;
import java.util.Map;

import fft_battleground.genetic.model.attributes.BetGeneAttributes;
import fft_battleground.match.model.Match;
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
	
	protected int faithIndex;
	protected int braveIndex;
	protected int braveFaithIndex;
	protected int raidBossIndex;
	protected int fightWinRatioIndex;
	protected int missingfightWinRatioIndex;
	protected int champStreakIndex;
	
	protected int betCountIndex;
	protected int betRatioIndex;
	
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
		this.champStreakIndex = simulatorConfig.simulatorIndexConfig().champStreakIndex();
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
	}
	
	public abstract long gilResultFromSimulation(Genotype<DoubleGene> genotype);
}
