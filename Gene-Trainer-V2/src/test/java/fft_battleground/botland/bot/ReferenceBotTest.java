package fft_battleground.botland.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import fft_battleground.botland.SyntheticBotlandData;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.genetic.model.attributes.OfflineGenomeTest;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.SimulatorFactoryImpl;
import fft_battleground.simulation.model.SimulationConfig;

public class ReferenceBotTest {

	@Test
	public void testReferenceBotMirrorsThePlayersActualBet() throws Exception {
		SimulationConfig config = this.buildConfig();

		ReferenceBot bot = new ReferenceBot(config);
		bot.setName("birbreference");
		bot.initParams(Map.of("reference_player", new BotParam("reference_player", "birbbrainsbot", "")));
		bot.init();

		//in the synthetic dataset birbbrainsbot (player id 0) bet 500 on RED
		Bet bet = bot.generateBetAmount(config.matches()[0], 10000);
		assertEquals(BattleGroundTeam.RED, bet.getTeam());
		assertEquals(500, bet.getAmount());
	}

	@Test
	public void testReferenceBotCapsAtCurrentGil() throws Exception {
		SimulationConfig config = this.buildConfig();

		ReferenceBot bot = new ReferenceBot(config);
		bot.setName("birbreference");
		bot.initParams(Map.of("reference_player", new BotParam("reference_player", "birbbrainsbot", "")));
		bot.init();

		Bet bet = bot.generateBetAmount(config.matches()[0], 300);
		assertEquals(300, bet.getAmount(), "cannot bet more than the current gil");
	}

	@Test
	public void testUnknownReferencePlayerFallsBackToOneGilOnLeft() throws Exception {
		SimulationConfig config = this.buildConfig();

		ReferenceBot bot = new ReferenceBot(config);
		bot.setName("ghostreference");
		bot.initParams(Map.of("reference_player", new BotParam("reference_player", "notabot", "")));
		bot.init();

		Bet bet = bot.generateBetAmount(config.matches()[0], 10000);
		assertEquals(BattleGroundTeam.RED, bet.getTeam(), "fallback mirrors the viewer: left side");
		assertEquals(1, bet.getAmount(), "fallback mirrors the viewer: a 1 gil bet");
	}

	private SimulationConfig buildConfig() throws Exception {
		CompleteBotGenome genome = OfflineGenomeTest.buildOfflineGenome();
		MultipleTournamentDataset dataset = SyntheticBotlandData.dataset(genome);
		return new SimulatorFactoryImpl().createSimulatorConfig(genome, dataset);
	}
}
