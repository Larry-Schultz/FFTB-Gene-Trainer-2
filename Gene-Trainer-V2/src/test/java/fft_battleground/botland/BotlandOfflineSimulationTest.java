package fft_battleground.botland;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import fft_battleground.botland.genedata.GeneFileService;
import fft_battleground.botland.model.BotPlacement;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.genetic.model.attributes.OfflineGenomeTest;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.simulation.SimulatorFactoryImpl;

//runs the full 58 bot roster from Botland.xml over a hand-built dataset; completely offline
public class BotlandOfflineSimulationTest {

	@Test
	public void testFullViewerRosterSimulatesOffline() throws Exception {
		CompleteBotGenome genome = OfflineGenomeTest.buildOfflineGenome();
		MultipleTournamentDataset dataset = SyntheticBotlandData.dataset(genome);

		BotlandServiceImpl service = new BotlandServiceImpl();
		ReflectionTestUtils.setField(service, "simulatorFactory", new SimulatorFactoryImpl());
		ReflectionTestUtils.setField(service, "geneFileService", new GeneFileService());

		List<BotPlacement> leaderboard = service.getBotleaderboard(genome, dataset);

		assertEquals(58, leaderboard.size(), "every bot in Botland.xml must simulate, none silently dropped");

		for(int i = 1; i < leaderboard.size(); i++) {
			assertTrue(leaderboard.get(i - 1).gil() >= leaderboard.get(i).gil(), "leaderboard must be sorted by gil descending");
		}

		Set<String> botNames = leaderboard.stream().map(BotPlacement::botName).collect(Collectors.toSet());
		//representative bots from every classname, including all the newly supported ones
		assertTrue(botNames.contains("minbetbot"), "betcountbot");
		assertTrue(botNames.contains("datadrivenbot"), "databetbot");
		assertTrue(botNames.contains("oddsbot"), "oddsbot");
		assertTrue(botNames.contains("leftallinbot"), "arbitrarybot");
		assertTrue(botNames.contains("bravebot"), "bravefaithbot");
		assertTrue(botNames.contains("winner1000geneticbot"), "genebot V1");
		assertTrue(botNames.contains("genebotv1-4-17-26"), "the revived April 2026 gene bot");
		assertTrue(botNames.contains("genebotv2"), "genebotV2");
		assertTrue(botNames.contains("genebotv2smartbet"), "genebotV2 with bet genes");
		assertTrue(botNames.contains("teamvaluebot"), "teamvalue");
		assertTrue(botNames.contains("chemistbot"), "unitAwareBot");
		assertTrue(botNames.contains("pokemonmaster"), "unitAwareBot with the big weight map");
		assertTrue(botNames.contains("birbbrainsbotreference"), "reference");
	}

	@Test
	public void testWinnerIsTheTopOfTheLeaderboard() throws Exception {
		CompleteBotGenome genome = OfflineGenomeTest.buildOfflineGenome();
		MultipleTournamentDataset dataset = SyntheticBotlandData.dataset(genome);

		BotlandServiceImpl service = new BotlandServiceImpl();
		ReflectionTestUtils.setField(service, "simulatorFactory", new SimulatorFactoryImpl());
		ReflectionTestUtils.setField(service, "geneFileService", new GeneFileService());

		List<BotPlacement> leaderboard = service.getBotleaderboard(genome, dataset);

		long best = leaderboard.stream().mapToLong(BotPlacement::gil).max().orElseThrow();
		assertEquals(best, leaderboard.get(0).gil(), "the winner must be the first row the page highlights");
	}
}
