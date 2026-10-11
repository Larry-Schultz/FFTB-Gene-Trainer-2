package fft_battleground.botland.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import fft_battleground.botland.SyntheticBotlandData;
import fft_battleground.botland.genedata.GeneFileService;
import fft_battleground.botland.genedata.GeneFileService.V2GeneData;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.genetic.model.attributes.BetGeneAttributes;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.genetic.model.attributes.OfflineGenomeTest;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.simulation.SimulatorFactoryImpl;
import fft_battleground.simulation.model.GeneArray;
import fft_battleground.simulation.model.SimulationConfig;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;

public class GeneticBotV2Test {

	@Test
	public void testWinnerFileValuesLandAtTheRightGeneIds() throws Exception {
		CompleteBotGenome genome = OfflineGenomeTest.buildOfflineGenome();
		//a gene the file cannot mention, to prove the default is 0
		int unmentionedGeneId = SyntheticBotlandData.geneId(genome, "Zzz Synthetic Test Gene");
		MultipleTournamentDataset dataset = SyntheticBotlandData.dataset(genome);

		GeneFileService geneFileService = new GeneFileService();
		V2GeneData geneData = geneFileService.loadV2GeneData("winner05-29-2022.txt");
		Genotype<DoubleGene> genotype = GeneFileService.buildGenotype(genome, geneData.geneValues());
		GeneArray geneArray = new GeneArray(genome);

		int faithId = genome.getAttributeMap().get("Faith");
		assertEquals(geneData.geneValues().get("Faith"), geneArray.get(genotype, faithId), 0.0001,
				"the file's Faith value must be readable through the shared gene array");

		int firstBetGeneId = genome.getAttributeMap().get(BetGeneAttributes.first);
		assertEquals(geneData.geneValues().get("1-bet"), geneArray.get(genotype, firstBetGeneId), 0.0001,
				"bet gene values must land in the bet chromosome");

		assertEquals(0.0, geneArray.get(genotype, unmentionedGeneId), 0.0001,
				"genes the file doesn't mention must default to 0");

		//chromosome sizes line up with the genome sections
		int betChromosomeIndex = genome.getGeneAttributes().indexOf(genome.getBetGeneAttributes());
		assertEquals(100, genotype.get(betChromosomeIndex).length());
	}

	@Test
	public void testGeneticBotV2BetsWithinBounds() throws Exception {
		CompleteBotGenome genome = OfflineGenomeTest.buildOfflineGenome();
		MultipleTournamentDataset dataset = SyntheticBotlandData.dataset(genome);
		SimulationConfig config = new SimulatorFactoryImpl().createSimulatorConfig(genome, dataset);

		GeneticBotV2 smartBetBot = new GeneticBotV2(config, new GeneFileService());
		smartBetBot.setName("testbotv2");
		smartBetBot.initParams(Map.of(
				"geneFile", new BotParam("geneFile", "winner05-29-2022.txt", ""),
				"use_bet_genes", new BotParam("use_bet_genes", "true", "")));
		smartBetBot.init();

		Bet bet = smartBetBot.generateBetAmount(dataset.matches()[0], 10000);
		assertNotNull(bet);
		assertNotNull(bet.getTeam());
		assertTrue(bet.getAmount() >= 1 && bet.getAmount() <= 1000, "bet must stay within replay bounds but was " + bet.getAmount());

		GeneticBotV2 flatBetBot = new GeneticBotV2(config, new GeneFileService());
		flatBetBot.setName("testbotv2flat");
		flatBetBot.initParams(Map.of(
				"geneFile", new BotParam("geneFile", "winner05-29-2022.txt", ""),
				"use_bet_genes", new BotParam("use_bet_genes", "false", "")));
		flatBetBot.init();

		Bet flatBet = flatBetBot.generateBetAmount(dataset.matches()[0], 500);
		assertEquals(500, flatBet.getAmount(), "without bet genes the flat max bet is capped by gil");
	}
}
