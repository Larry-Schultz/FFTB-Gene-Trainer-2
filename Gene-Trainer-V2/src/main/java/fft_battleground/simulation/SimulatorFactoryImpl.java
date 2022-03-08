package fft_battleground.simulation;

import org.springframework.stereotype.Service;

import fft_battleground.genetic.model.BetGeneAttributes;
import fft_battleground.genetic.model.BraveFaithAttributes;
import fft_battleground.genetic.model.CompleteBotGenome;
import fft_battleground.genetic.model.PlayerDataGeneAttributes;
import fft_battleground.genetic.model.PotGeneAttributes;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.simulation.model.SimulationConfig;

@Service
public class SimulatorFactoryImpl implements SimulatorFactory {

	@Override
	public Simulator createSimulatorFromDataset(CompleteBotGenome genome, MultipleTournamentDataset dataset) {
		SimulationConfig simulationConfig = this.createSimulatorConfig(genome, dataset);
		
		Simulator simulator = new Simulator(simulationConfig);
		return simulator;
	}

	@Override
	public SimulationConfig createSimulatorConfig(CompleteBotGenome genome, MultipleTournamentDataset dataset) {
		Match[] matches = dataset.matches();
		double[] playerBetRatios = dataset.playerBetRatios();
		
		int faithIndex = genome.getAttributeMap().get(BraveFaithAttributes.FAITH);
		int braveIndex = genome.getAttributeMap().get(BraveFaithAttributes.BRAVE);
		int braveFaithIndex = genome.getAttributeMap().get(BraveFaithAttributes.BRAVEFAITH);
		int raidBossIndex = genome.getAttributeMap().get(BraveFaithAttributes.RAIDBOSS);
		int fightWinRatioIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.FIGHT_WIN_RATIO);
		
		int betCountIndex = genome.getAttributeMap().get(PotGeneAttributes.BET_COUNT);
		int betRatioIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.BET_WIN_RATIO);
		int champStreakIndex = genome.getAttributeMap().get(PotGeneAttributes.CHAMP_STREAK);
		
		int startOfBetArrayIndex = genome.getAttributeMap().get(BetGeneAttributes.first);
		
		SimulationConfig config = new SimulationConfig(faithIndex, braveIndex, braveFaithIndex, raidBossIndex, fightWinRatioIndex, betCountIndex, betRatioIndex, 
				champStreakIndex, startOfBetArrayIndex, matches, playerBetRatios);
		return config;
	}
	
}
