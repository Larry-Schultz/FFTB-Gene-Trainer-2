package fft_battleground.simulation;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fft_battleground.genetic.model.attributes.BetGeneAttributes;
import fft_battleground.genetic.model.attributes.BraveFaithAttributes;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.genetic.model.attributes.MapGeneAttributes;
import fft_battleground.genetic.model.attributes.PlayerDataGeneAttributes;
import fft_battleground.genetic.model.attributes.PotGeneAttributes;
import fft_battleground.genetic.model.attributes.SideGeneAttributes;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.GeneArray;
import fft_battleground.simulation.model.SimulationConfig;
import fft_battleground.simulation.model.SimulationIndexConfig;

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
		Match[] matches = Arrays.stream(dataset.matches()).filter(Objects::nonNull).toArray(Match[]::new);
		double[] playerBetRatios = dataset.playerBetRatios();
		double[] playerFightRatios = dataset.playerFightRatios();
		BitSet subscriberBitSet = dataset.subscriberBitSet();
		Map<String, Integer> botBetIndexes = dataset.botBetIndexes();
		
		int faithIndex = genome.getAttributeMap().get(BraveFaithAttributes.FAITH);
		int braveIndex = genome.getAttributeMap().get(BraveFaithAttributes.BRAVE);
		int braveFaithIndex = genome.getAttributeMap().get(BraveFaithAttributes.BRAVEFAITH);
		int raidBossIndex = genome.getAttributeMap().get(BraveFaithAttributes.RAIDBOSS);
		int fightWinRatioIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.FIGHT_WIN_RATIO);
		int missingFightWinRatioIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.MISSING_FIGHT_WIN_RATIO);
		
		int betCountIndex = genome.getAttributeMap().get(PotGeneAttributes.BET_COUNT);
		int betRatioIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.BET_WIN_RATIO);
		int oddsIndex = genome.getAttributeMap().get(PotGeneAttributes.ODDS);
		int potAmountIndex = genome.getAttributeMap().get(PotGeneAttributes.POT_AMOUNT);
		
		int startOfBetArrayIndex = genome.getAttributeMap().get(BetGeneAttributes.first);
		
		int playerHumanIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.HUMAN);
		int playerBotIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.ROBOT);
		int playerSubscriberIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.SUBSCRIBER);
		int playerBalanceBetRatioIndex = genome.getAttributeMap().get(PlayerDataGeneAttributes.CURRENT_BALANCE_TO_BET_RATIO);
		
		Map<BattleGroundTeam, Integer> sideIndexes = SideGeneAttributes.sides.stream()
				.collect(Collectors.toMap(Function.identity(), side -> genome.getAttributeMap().get(side.getProperName())));
		
		int leftMapsStartIndex = genome.getAttributeMap().get(MapGeneAttributes.buildMapName(1, BattleGroundTeam.LEFT));
		int rightMapStartIndex = genome.getAttributeMap().get(MapGeneAttributes.buildMapName(1, BattleGroundTeam.RIGHT));
		
		GeneArray geneArray = new GeneArray(genome);
		
		SimulationIndexConfig indexConfig = new SimulationIndexConfig(faithIndex, braveIndex, braveFaithIndex, raidBossIndex, fightWinRatioIndex, 
				missingFightWinRatioIndex, betCountIndex, betRatioIndex, startOfBetArrayIndex, playerHumanIndex, playerBotIndex, 
				playerSubscriberIndex, playerBalanceBetRatioIndex, sideIndexes, leftMapsStartIndex, rightMapStartIndex, oddsIndex, potAmountIndex); 
		
		SimulationConfig config = new SimulationConfig(indexConfig, matches, playerBetRatios, playerFightRatios, 
				subscriberBitSet, botBetIndexes, geneArray);
		return config;
	}
	
}
