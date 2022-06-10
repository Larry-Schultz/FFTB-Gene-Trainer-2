package fft_battleground.genetic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.application.StatisticsService;
import fft_battleground.application.model.ApplicationStatistics;
import fft_battleground.application.model.HighScore;
import fft_battleground.botland.model.BotPlacement;
import fft_battleground.genetic.model.GenomeFile;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import io.jenetics.DoubleGene;
import io.jenetics.engine.EvolutionResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GenomeManagerImpl implements GenomeManager {
	public static final String winnerFilenameTemplate = "winner%s.txt";
	
	@Autowired
    private SimpMessagingTemplate template;
	
	@Autowired
	private StatisticsService statisticsService;
	
	private AtomicLong highestCurrentGilResult = new AtomicLong(0L);
	private Lock winnerFileLock = new ReentrantLock();
	
	@Override
	public Runnable createGenomeFileCheckAndWriteRunnable(GenomeFile genomeData) {
		Runnable genomeFileCheckAndWriteRunnable = new ResultChecker(genomeData, this.highestCurrentGilResult, this.winnerFileLock);
		return genomeFileCheckAndWriteRunnable;
	}

	@Override
	public Runnable logGeneration(EvolutionResult<DoubleGene, Long> evolutionResult, long perfectScore, List<BotPlacement> botLeaderboard) {
			return () -> {
				DecimalFormat df=new DecimalFormat("#,###");
				Runtime runtime = Runtime.getRuntime();
				Double currentMemoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory());
				Double memoryUsageInGigabytes = (((currentMemoryUsage/1024.0)/1024.0)/1024.0);
				String generation = df.format(evolutionResult.generation());
				String currentGil = df.format(evolutionResult.bestFitness());
				String perfectScoreGil = df.format(perfectScore);
				int placeOnLeaderboard = this.placeOnLeaderboard(evolutionResult.bestFitness(), botLeaderboard);
				log.info("current generation: {}. currentGil: {}. perfectScore: {} placeOnLeaderboard: {} current memory usage: {}G", generation, 
						currentGil, perfectScoreGil, placeOnLeaderboard, df.format(memoryUsageInGigabytes));
			};
		}
		
	@Override
	public Runnable sendGenomeStatsUpdate(CompleteBotGenome genome, EvolutionResult<DoubleGene, Long> evolutionResult, 
			long perfectScore, List<BotPlacement> botLeaderboard, int matchesAnalyzed) {
		return () -> {
			long gil = evolutionResult.bestFitness();
			long generation = evolutionResult.generation();
			int unitGeneSize = genome.getMissingGeneAttributes().getGeneAttributes().size();
			int totalGeneSize = genome.size();
			int placeOnLeaderboard = this.placeOnLeaderboard(evolutionResult.bestFitness(), botLeaderboard);
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm a z");
			String updateDate = sdf.format(new Date());
			
			HighScore highScore = new HighScore(gil, perfectScore, placeOnLeaderboard, matchesAnalyzed, unitGeneSize, totalGeneSize, 
					updateDate);
			ApplicationStatistics<HighScore> stats = this.statisticsService.getApplicationStatistics(generation, highScore);
			this.template.convertAndSend("/chain/stats", stats);
		};
	}
	
	private int placeOnLeaderboard(long gil, List<BotPlacement> botLeaderboard) {
		for(int i = botLeaderboard.size() - 1; i >= 0; i--) {
			if(botLeaderboard.get(i).gil().longValue() > gil) {
				return i + 1;
			}
		}
		
		return 1;
	}

}

@Slf4j
class ResultChecker implements Runnable {
	
	private GenomeFile genomeData;
	private AtomicLong highestCurrentGilResultRef;
	private Lock winnerFileLock;
	
	public ResultChecker(GenomeFile genomeData, AtomicLong highestCurrentGilResultRef, Lock winnerFileLock) {
		this.genomeData = genomeData;
		this.highestCurrentGilResultRef = highestCurrentGilResultRef;
		this.winnerFileLock = winnerFileLock;
	}

	@Override
	public void run() {
		this.winnerFileLock.lock();
		if(this.highestCurrentGilResultRef.get() < this.genomeData.getGilResult()) {
			DecimalFormat df=new DecimalFormat("#,###");
			String oldHighScoreFormatted = df.format(this.highestCurrentGilResultRef.get());
			String newHighScoreFormatted = df.format(this.genomeData.getGilResult());
			log.info("New highest score found: {} beat old score of {}", newHighScoreFormatted, 
					oldHighScoreFormatted);
			this.writeWinnerFile(genomeData);
			this.highestCurrentGilResultRef.set(this.genomeData.getGilResult());
		}
		this.winnerFileLock.unlock();
	}
	
	protected void writeWinnerFile(GenomeFile genomeData) {
		log.info("Writing genome file");
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			String filename = this.generateWinnerFilename(genomeData.getCreationDate());
			File file = new File(filename);
			OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
			mapper.writerWithDefaultPrettyPrinter().writeValue(stream, genomeData);
			log.info("winner file written successfully");
		} catch (IOException e) {
			log.error("Error writing winner file", e);
		}
	}
	
	protected String generateWinnerFilename(Date creationDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
		String dateString = sdf.format(creationDate);
		String filename = String.format(GenomeManagerImpl.winnerFilenameTemplate, dateString);
		return filename;
	}
	
	
}
