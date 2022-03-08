package fft_battleground.simulation;

import fft_battleground.genetic.model.BetGeneAttributes;
import fft_battleground.match.model.Match;
import fft_battleground.simulation.model.SimulationConfig;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;

public abstract class AbstractSimulator {
	public static final long GIL_FLOOR = BetGeneAttributes.GIL_FLOOR.longValue();
	public static final long BET_MAX = BetGeneAttributes.BET_MAX.longValue();
	
	protected Match[] matches;
	protected double[] playerBetRatios;
	
	protected int faithIndex;
	protected int braveIndex;
	protected int braveFaithIndex;
	protected int raidBossIndex;
	protected int fightWinRatioIndex;
	protected int champStreakIndex;
	
	protected int betCountIndex;
	protected int betRatioIndex;
	
	protected int startOfBetArrayIndex;
	
	public AbstractSimulator(SimulationConfig simulatorConfig) {
		this.faithIndex = simulatorConfig.faithIndex();
		this.braveIndex = simulatorConfig.braveIndex();
		this.raidBossIndex = simulatorConfig.raidBossIndex();
		
		this.fightWinRatioIndex = simulatorConfig.fightWinRatioIndex();
		this.champStreakIndex = simulatorConfig.champStreakIndex();
		this.betCountIndex = simulatorConfig.betCountIndex();
		this.betRatioIndex = simulatorConfig.betRatioIndex();
		
		this.startOfBetArrayIndex = simulatorConfig.startOfBetArrayIndex();
		
		this.matches = simulatorConfig.matches();
		this.playerBetRatios = simulatorConfig.playerBetRatios();
	}
	
	public abstract long gilResultFromSimulation(Genotype<DoubleGene> genotype);
}
