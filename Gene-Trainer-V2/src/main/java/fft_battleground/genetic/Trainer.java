package fft_battleground.genetic;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;

import fft_battleground.genetic.model.CompleteBotGenome;
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
	private CompleteBotGenome genome;
	private int agentCount;
	private Duration duration;
	
	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
	
	public Trainer(Simulator simulator, CompleteBotGenome data, int agentCount, long duration) {
		this.genome = data;
		this.simulator = simulator;
		this.agentCount = agentCount;
		this.duration = Duration.ofMillis(duration);
	}
	
	public CompleteBotGenome train() {
		Factory<Genotype<DoubleGene>> gtf = genome.generateGenome();
		long perfectScore = this.simulator.perfectScoreGil();
		
        ExecutorService executorService = Executors.newFixedThreadPool(7);
        // 3.) Create the execution environment.
        Engine<DoubleGene, Long> engine = Engine
            .builder(gt -> this.eval(gt), gtf)
            .survivorsSelector(new TournamentSelector<>())
            .offspringSelector(new StochasticUniversalSelector<>())
            //.alterers(new UniformCrossover<>(0.2), new Mutator<>(0.15))
            .populationSize(agentCount)
            .executor(executorService)
            .build();
        
        Predicate<? super EvolutionResult<DoubleGene, Long>> winsLimit = Limits.byExecutionTime(duration);
        
        Genotype<DoubleGene> result = engine.stream()
        		.limit(winsLimit)
        		.parallel()
        		.peek(evolutionResult -> {
        			this.handleEvolutionStatistics(evolutionResult, perfectScore);
        		})
        		.collect(EvolutionResult.toBestGenotype());
        
        this.genome.loadGeneData(result);
        return this.genome;
	}
	
	private long eval(Genotype<DoubleGene> genotype) {
		return this.simulator.gilResultFromSimulation(genotype);
    }
	
	private void handleEvolutionStatistics(final EvolutionResult<DoubleGene, Long> evolutionResult, long perfectScore) {
		this.executor.execute(() -> {
			DecimalFormat df=new DecimalFormat("#,###.00");
			Runtime runtime = Runtime.getRuntime();
			Double currentMemoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory());
			Double memoryUsageInGigabytes = (((currentMemoryUsage/1024.0)/1024.0)/1024.0);
			String currentGil = df.format(evolutionResult.bestFitness());
			String perfectScoreGil = df.format(perfectScore);
			log.info("current generation: {}. currentGil: {}. perfectScore: {} current memory usage: {}G", evolutionResult.generation(), currentGil, perfectScoreGil,
					df.format(memoryUsageInGigabytes));
		});
	}
}
