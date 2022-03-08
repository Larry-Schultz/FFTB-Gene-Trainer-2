package fft_battleground.genetic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.botland.BotlandService;
import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.genetic.model.CompleteBotGenome;
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
	
	public static final int agentCount = 200000;
	public static final int tournamentCount = 250;
	
	@EventListener(ApplicationReadyEvent.class)
	public void test() {
		try {
			CompleteBotGenome resultingGenome = this.trainBot(agentCount, TimeUnit.HOURS.toMillis(3));
		} catch (TournamentApiException | ViewerException | DumpException | CacheException e) {
			log.error("It died :(", e);
		}
	}
	
	@Override
	public CompleteBotGenome trainBot(int agentCount, long duration) throws TournamentApiException, ViewerException, DumpException, CacheException {
		Tips tips = this.tournamentService.getCurrentTips();
		CompleteBotGenome genome = new CompleteBotGenome(tips);
		MultipleTournamentDataset data = this.trainerDataService.generateDataset(tournamentCount, genome);
		Simulator simulator = this.simulatorFactory.createSimulatorFromDataset(genome, data);
		Trainer trainer = new Trainer(simulator, genome, agentCount, duration);
        
		genome = trainer.train();
		this.writeWinnerFile(genome);
		
		return genome;
	}
	
	protected void writeWinnerFile(CompleteBotGenome genome) {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File("winner.txt");
		try {
			OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
			mapper.writeValue(stream, genome);
		} catch (IOException e) {
			log.error("Error writing winner file", e);
		}
	}

}
