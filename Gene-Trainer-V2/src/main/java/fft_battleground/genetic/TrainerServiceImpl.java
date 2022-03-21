package fft_battleground.genetic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.botland.BotlandService;
import fft_battleground.botland.model.BotPlacement;
import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.match.TrainerDataService;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.simulation.Simulator;
import fft_battleground.simulation.SimulatorFactory;
import fft_battleground.tournament.TournamentService;
import fft_battleground.tournament.model.Tips;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TrainerServiceImpl implements TrainerService {

	@Autowired
	private TrainerDataService trainerDataService;

	@Autowired
	private TournamentService tournamentService;

	@Autowired
	private BotlandService botlandService;

	@Autowired
	private SimulatorFactory simulatorFactory;
	
	@Autowired
	private GenomeFileManager genomeFileManager;
	
	@Autowired
    private SimpMessagingTemplate template;

	public void test() {
		try {
			this.trainBot(AGENT_COUNT, TimeUnit.HOURS.toMillis(DURATION_IN_HOURS), TOURNAMENT_COUNT);
			//this.writeWinnerFile(resultingGenome);
		} catch (TournamentApiException | ViewerException | DumpException | CacheException e) {
			log.error("It died :(", e);
		}
		 
	}

	@Override
	public CompleteBotGenome trainBot(int agentCount, long duration, int tournamentCount)
			throws TournamentApiException, ViewerException, DumpException, CacheException 
	{
		Trainer trainer = this.buildTrainer(agentCount, duration, TOURNAMENT_COUNT);
		CompleteBotGenome genome = trainer.train();

		return genome;
	}
	
	public Trainer buildTrainer(int agentCount, long duration, int tournamentCount) 
			throws TournamentApiException, ViewerException, DumpException, CacheException 
	{
		Tips tips = this.tournamentService.getCurrentTips();
		CompleteBotGenome genome = new CompleteBotGenome(tips);
		MultipleTournamentDataset data = this.trainerDataService.generateDataset(tournamentCount, genome);
		Simulator simulator = this.simulatorFactory.createSimulatorFromDataset(genome, data);
		List<BotPlacement> botlandLeaderboard = this.botlandService.getBotleaderboard(genome, data);
		log.info("Loaded {} matches", data.matches().length);
		this.logBotlandLeaderboard(botlandLeaderboard);
		Trainer trainer = new Trainer(simulator, this.genomeFileManager, genome, botlandLeaderboard, agentCount, duration, 
				data.matches().length, simulator.getGeneArray().size(), template);
		
		return trainer;
	}

	protected void writeWinnerFile(CompleteBotGenome genome) {
		log.info("Writing genome file");
		ObjectMapper mapper = new ObjectMapper();
		File file = new File("winner.txt");
		try {
			OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
			mapper.writerWithDefaultPrettyPrinter().writeValue(stream, genome);
			log.info("gneome file written successfully");
		} catch (IOException e) {
			log.error("Error writing winner file", e);
		}
	}

	protected void logBotlandLeaderboard(List<BotPlacement> botlandLeaderboard) {
		log.info("Botland Leaderboard: ");
		DecimalFormat df = new DecimalFormat("#,###.00");
		for (int i = 0; i < botlandLeaderboard.size(); i++) {
			BotPlacement currentBot = botlandLeaderboard.get(i);
			log.info("{}. {}: {}", i + 1, currentBot.botName(), df.format(currentBot.gil()));
		}
	}

}
