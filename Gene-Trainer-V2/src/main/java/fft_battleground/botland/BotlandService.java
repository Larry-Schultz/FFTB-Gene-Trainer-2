package fft_battleground.botland;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.botland.bot.BetCountBot;
import fft_battleground.botland.bot.SimulatedBetBot;
import fft_battleground.botland.model.BotData;
import fft_battleground.botland.model.BotPlacement;
import fft_battleground.botland.util.BotNames;
import fft_battleground.genetic.model.CompleteBotGenome;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.simulation.SimulatorFactory;
import fft_battleground.simulation.model.SimulationConfig;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BotlandService {
	
	@Autowired
	private SimulatorFactory simulatorFactory;
	
	public List<BotPlacement> getBotleaderboard(CompleteBotGenome genome, MultipleTournamentDataset dataset) {
		SimulationConfig simulatorConfig = this.simulatorFactory.createSimulatorConfig(genome, dataset);
		SecondaryBotConfig config = new SecondaryBotConfig("Botland.xml");
		List<BotData> botlandData = config.parseXmlFile();
		List<? extends SimulatedBetBot> betBots = botlandData.stream().map(data -> this.findBestFit(data, simulatorConfig))
				.collect(Collectors.toList());
		List<BotPlacement> result = this.getAndOrderResults(betBots);
		return result;
	}
	
	private SimulatedBetBot findBestFit(BotData botData, SimulationConfig config) {
		SimulatedBetBot betBot = null;
		BotNames currentBot = BotNames.parseBotname(botData.getClassname());
		switch(currentBot) {
		case BETCOUNT:
			betBot = new BetCountBot(config);
			break;
		default:
			log.error("botData with data: {} failed", botData);
			break;
		}
		
		return betBot;
	}
	
	private List<BotPlacement> getAndOrderResults(List<? extends SimulatedBetBot> betBots) {
		return betBots.stream()
				.map(bot -> new BotPlacement(bot.getName(), bot.gilResultFromSimulation(null)))
				.sorted(Comparator.comparing(bot -> bot.gil()))
				.collect(Collectors.toList());

	}
}
