package fft_battleground.controller;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fft_battleground.genetic.TrainerService;
import fft_battleground.genetic.model.GeneStats;
import lombok.SneakyThrows;

@Controller
public class HomeController {

	@Autowired
	private TrainerService trainerService;
	
	private AtomicBoolean started = new AtomicBoolean(false);
	
	@GetMapping("/start")
	@SneakyThrows
	public ResponseEntity<GeneStats> startProcess(@RequestParam(name = "population") Optional<Integer> population, 
			@RequestParam(name="tournaments") Optional<Integer> tournaments, @RequestParam(name = "duration") Optional<Integer> duration) {
		if(!this.started.get()) {
			int agentCount = TrainerService.AGENT_COUNT;
			if(population.isPresent()) {
				agentCount = population.get();
			}
			
			int tournamentCount = TrainerService.TOURNAMENT_COUNT;
			if(tournaments.isPresent()) {
				tournamentCount = tournaments.get();
			}
			
			long hours = TimeUnit.HOURS.toMillis(TrainerService.DURATION_IN_HOURS);
			if(duration.isPresent()) {
				hours = TimeUnit.HOURS.toMillis(duration.get());
			}
			
			this.trainerService.trainBot(agentCount, hours, tournamentCount);
			
			int threads = Runtime.getRuntime().availableProcessors() - 1;
			GeneStats stats = new GeneStats(tournamentCount, agentCount, threads);
			
			return new ResponseEntity<GeneStats>(stats, HttpStatus.OK);
		}
		
		return new ResponseEntity<GeneStats>(new GeneStats(0, 0, 0), HttpStatus.IM_USED);
	}
}
