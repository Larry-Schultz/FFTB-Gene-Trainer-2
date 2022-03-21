package fft_battleground.genetic;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import fft_battleground.botland.model.BotPlacement;
import fft_battleground.genetic.model.GenomeFile;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.simulation.Simulator;

import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.StochasticUniversalSelector;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Trainer {
	private Simulator simulator;
	private GenomeFileManager genomeFileManagerRef;
	private SimpMessagingTemplate websocketTemplateRef;
	private CompleteBotGenome genome;
	private int agentCount;
	private Duration duration;
	private List<BotPlacement> botlandLeaderboard;
	
	long perfectScore;
	int matchesAnalyzed;
	int geneCount;
	
	public Trainer(Simulator simulator, GenomeFileManager genomeFileManager, CompleteBotGenome data, List<BotPlacement> botlandLeaderboard, 
			int agentCount, long duration, int matchesAnalyzed, int geneCount, SimpMessagingTemplate template) {
		this.genome = data;
		this.simulator = simulator;
		this.genomeFileManagerRef = genomeFileManager;
		this.botlandLeaderboard = botlandLeaderboard;
		this.agentCount = agentCount;
		this.duration = Duration.ofMillis(duration);
		
		this.perfectScore = this.simulator.perfectScoreGil();
		this.matchesAnalyzed = matchesAnalyzed;
		this.geneCount = geneCount;
		
		this.websocketTemplateRef = template;
	}
	
	public CompleteBotGenome train() {
		log.info("Starting training");
		Factory<Genotype<DoubleGene>> gtf = genome.generateGenome();
		
		int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService geneTrainerThreadPool = Executors.newFixedThreadPool(cores - 1);
        ExecutorService midTrainingThreadPool = Executors.newFixedThreadPool(1);
        // 3.) Create the execution environment.
        Engine<DoubleGene, Long> engine = Engine
            .builder(gt -> this.eval(gt), gtf)
            .survivorsSelector(new TournamentSelector<>())
            .offspringSelector(new StochasticUniversalSelector<>())
            //.alterers(new UniformCrossover<>(0.2), new Mutator<>(0.15))
            .populationSize(agentCount)
            .executor(geneTrainerThreadPool)
            .build();
        
        Predicate<? super EvolutionResult<DoubleGene, Long>> winsLimit = Limits.byExecutionTime(duration);
        
        Genotype<DoubleGene> result = engine.stream()
        		.limit(winsLimit)
        		.parallel()
        		.peek(evolutionResult -> {
        			this.handleEvolutionStatistics(evolutionResult, this.perfectScore, midTrainingThreadPool);
        			midTrainingThreadPool.submit(this.genomeFileManagerRef.createGenomeFileCheckAndWriteRunnable(this.createGenomeFile(evolutionResult))); 
        		})
        		.collect(EvolutionResult.toBestGenotype());
        
        this.genome.loadGeneData(result);
        
        return this.genome;
	}
	
	private long eval(Genotype<DoubleGene> genotype) {
		return this.simulator.gilResultFromSimulation(genotype);
    }
	
	private void handleEvolutionStatistics(final EvolutionResult<DoubleGene, Long> evolutionResult, long perfectScore,
			ExecutorService midTrainingThreadPool) {
		midTrainingThreadPool.execute(() -> {
			DecimalFormat df=new DecimalFormat("#,###");
			Runtime runtime = Runtime.getRuntime();
			Double currentMemoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory());
			Double memoryUsageInGigabytes = (((currentMemoryUsage/1024.0)/1024.0)/1024.0);
			String currentGil = df.format(evolutionResult.bestFitness());
			String perfectScoreGil = df.format(perfectScore);
			int placeOnLeaderboard = this.placeOnLeaderboard(evolutionResult.bestFitness());
			log.info("current generation: {}. currentGil: {}. perfectScore: {} placeOnLeaderboard: {} current memory usage: {}G", evolutionResult.generation(), 
					currentGil, perfectScoreGil, placeOnLeaderboard, df.format(memoryUsageInGigabytes));
		});
	}
	
	private int placeOnLeaderboard(long gil) {
		for(int i = this.botlandLeaderboard.size() - 1; i >= 0; i--) {
			if(this.botlandLeaderboard.get(i).gil().longValue() > gil) {
				return i + 1;
			}
		}
		
		return 1;
	}
	
	private GenomeFile createGenomeFile(final EvolutionResult<DoubleGene, Long> evolutionResult) {
		long currentGil = evolutionResult.bestFitness();
		long generation = evolutionResult.generation();
		Genotype<DoubleGene> bestFitnessGenotype = evolutionResult.bestPhenotype().genotype();
		this.genome.loadGeneData(bestFitnessGenotype);
		GenomeFile genomeDate = new GenomeFile(this.botlandLeaderboard, this.matchesAnalyzed, currentGil, this.perfectScore, this.geneCount, 
				generation, this.genome);
		return genomeDate;
	}
}
