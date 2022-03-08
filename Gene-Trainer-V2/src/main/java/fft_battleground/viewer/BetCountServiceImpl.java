package fft_battleground.viewer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;

import fft_battleground.util.GenericResponse;
import fft_battleground.viewer.model.BotBetData;
import fft_battleground.viewer.model.PlayerListRequest;
import fft_battleground.viewer.model.TournamentListRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BetCountServiceImpl implements BetCountService {
	
	String preprodUrl = "https://fftbview.com:9443/betcount/";
	
	private RateLimiter limit = RateLimiter.create(1);

	@Override
	public BotBetData getFirstTrackedTournament() {
		RestTemplate firstTrackedCallTemplate = new RestTemplate();
		limit.acquire();
		ResponseEntity<FirstTrackedResultResponse> firstTrackedResult = firstTrackedCallTemplate.getForEntity(this.preprodUrl + "first", FirstTrackedResultResponse.class);
		return firstTrackedResult.getBody().getResponse().getData();
	}
	
	@Override
	public List<BotBetData> getBetBotDataAfterTournament(List<Long> tournamentIds) {
		List<List<Long>> tournamentIdSplice = Lists.partition(tournamentIds, 100);
		ExecutorService pool = Executors.newFixedThreadPool(1);
		Function<List<Long>, List<BotBetData>> callBetBotDataService = this::callBetBotDataService;
		List<Future<List<BotBetData>>> botBetDataFutures = tournamentIdSplice.stream()
		  .map(tournamentIdObject -> new Callable<List<BotBetData>>() {
				@Override
				public List<BotBetData> call() throws Exception {
					return callBetBotDataService.apply(tournamentIdObject);
				}
		}).map(callable -> pool.submit(callable))
		  .collect(Collectors.toList());
		List<BotBetData> data = new ArrayList<>();
		for(Future<List<BotBetData>> botBetDataFuture : botBetDataFutures) {
			try {
				data.addAll(botBetDataFuture.get());
			} catch (InterruptedException | ExecutionException e) {
				log.error("Call to bot bet data service failed", e);
			}
		}
		return data;
	}
	
	@Override
	public Map<String, Double> getPlayerBetWinRatios(Date after) {
		log.info("loading player bet win ratios");
		RestTemplate firstTrackedCallTemplate = new RestTemplate();
		PlayerListRequest request = new PlayerListRequest(after);
		log.info("Making call for player bet win ratios");
		limit.acquire();
		PlayerWinRatioResult result = firstTrackedCallTemplate.postForObject(this.preprodUrl + "playerBetWinRatios", request, PlayerWinRatioResult.class);
		Map<String, Double> betRatioMap = result.getResponse().getData().stream().collect(Collectors.toMap(PlayerWinRatio::getPlayer, PlayerWinRatio::getWinRate));
		if(betRatioMap != null) {
			log.info("player bet win ratio load successful");
		}
		return betRatioMap;
	}

	@Override
	public Map<String, Double> getFightWinRatios(Date after) {
		log.info("loading player fight win ratios");
		RestTemplate firstTrackedCallTemplate = new RestTemplate();
		PlayerListRequest request = new PlayerListRequest(after);
		log.info("Making call for player fight win ratios");
		limit.acquire();
		PlayerWinRatioResult result = firstTrackedCallTemplate.postForObject(this.preprodUrl + "playerFightWinRatios", request, PlayerWinRatioResult.class);
		Map<String, Double> betRatioMap = result.getResponse().getData().stream().collect(Collectors.toMap(PlayerWinRatio::getPlayer, PlayerWinRatio::getWinRate));
		if(betRatioMap != null) {
			log.info("player fight win ratio load successful");
		}
		return betRatioMap;
	}
	
	@Override
	public List<String> getSubscribers(Date after) {
		RestTemplate subscriberTemplate = new RestTemplate();
		PlayerListRequest request = new PlayerListRequest(after);
		log.info("Making call for player subscribers");
		limit.acquire();
		PlayerListResult result = subscriberTemplate.postForObject(this.preprodUrl + "playerSubscribers", request, PlayerListResult.class);
		return result.getResponse().getData().getPlayers();
	}
	
	@Override
	public List<String> getBots() {
		RestTemplate botTemplate = new RestTemplate();
		log.info("Making call for bots");
		limit.acquire();
		PlayerListResult result = botTemplate.getForObject(this.preprodUrl + "bots", PlayerListResult.class);
		return result.getResponse().getData().getPlayers();
	}
	
	protected List<BotBetData> callBetBotDataService(List<Long> tournamentIds) {
		log.info("Calling betbotdata service for bet data for {} tournaments in range {} to {}", tournamentIds.size(), tournamentIds.get(0), tournamentIds.get(tournamentIds.size() - 1));
		RestTemplate firstTrackedCallTemplate = new RestTemplate();
		TournamentListRequest request = new TournamentListRequest(tournamentIds);
		limit.acquire();
		ListBotBetDataWrapper response = firstTrackedCallTemplate.postForObject(this.preprodUrl, request, ListBotBetDataWrapper.class);
		if(response != null && response.getResponse() != null && response.getResponse().getData() != null) {
			log.info("bet data for tournaments {} to {} was successful", tournamentIds.get(0), tournamentIds.get(tournamentIds.size() - 1));
		}
		return response.getResponse().getData();
	}

	


}

@Data
@NoArgsConstructor
@AllArgsConstructor
class FirstTrackedResultResponse {
	@JsonUnwrapped
	GenericResponse<BotBetData> response;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ListBotBetDataWrapper {
	@JsonUnwrapped 
	GenericResponse<List<BotBetData>> response;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlayerWinRatioResult {
	@JsonUnwrapped 
	GenericResponse<List<PlayerWinRatio>> response;
};


@Data
@NoArgsConstructor
@AllArgsConstructor
class PlayerWinRatio {
	String player;
	double winRate;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlayerListResult {
	@JsonUnwrapped
	GenericResponse<PlayerList> response;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlayerList {
	List<String> players;
}
