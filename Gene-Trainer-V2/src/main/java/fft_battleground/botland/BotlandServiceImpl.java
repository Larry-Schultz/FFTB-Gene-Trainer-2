package fft_battleground.botland;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.botland.bot.ArbitraryBot;
import fft_battleground.botland.bot.BetCountBot;
import fft_battleground.botland.bot.BraveFaithBot;
import fft_battleground.botland.bot.DataDrivenBot;
import fft_battleground.botland.bot.LiveBettingBotSimulator;
import fft_battleground.botland.bot.OddsBot;
import fft_battleground.botland.bot.SimulatedBetBot;
import fft_battleground.botland.model.BotData;
import fft_battleground.botland.model.BotPlacement;
import fft_battleground.botland.util.BotNames;
import fft_battleground.exception.BotConfigException;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.simulation.SimulatorFactory;
import fft_battleground.simulation.model.SimulationConfig;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BotlandServiceImpl implements BotlandService {
	
	@Autowired
	private SimulatorFactory simulatorFactory;
	
	@Override
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
		try {
			switch(currentBot) {
			case BETCOUNT:
				betBot = new BetCountBot(config);
				this.setupBot(betBot, botData);
				break;
			case DATA:
				betBot = new DataDrivenBot(config);
				this.setupBot(betBot, botData);
				break;
			case ODDS:
				betBot = new OddsBot(config);
				this.setupBot(betBot, botData);
				break;
			case ARBITRARY:
				betBot = new ArbitraryBot(config);
				this.setupBot(betBot, botData);
				break;
			case BRAVEFAITH:
				betBot = new BraveFaithBot(config);
				this.setupBot(betBot, botData);
				break;
			case LIVESIMULATOR:
				betBot = new LiveBettingBotSimulator(config);
				this.setupBot(betBot, botData);
				break;
			default:
				//log.error("botData with data: {} failed", botData);
				break;
			}
		} catch(BotConfigException e) {
			log.error("Error with {}'s bot config", botData.getName());
		}
		
		return betBot;
	}
	
	private List<BotPlacement> getAndOrderResults(List<? extends SimulatedBetBot> betBots) {
		return betBots.stream()
				.filter(Objects::nonNull)
				.map(bot -> new BotPlacement(bot.getName(), bot.gilResultFromSimulation(null)))
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());

	}
	
	private void setupBot(SimulatedBetBot betBot, BotData botData) throws BotConfigException {
		betBot.setName(botData.getName());
		betBot.initParams(botData.getParams());
		betBot.init();
		log.info("found bot with name {}", botData.getName());
	}
}
