package fft_battleground.tournament;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.util.concurrent.RateLimiter;

import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.model.Tips;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.model.TournamentInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TournamentServiceImpl implements TournamentService {
	private static final String tournamentInfoApiUri = "https://fftbg.com/api/tournaments?limit=";
	private static final String tournamentApiBaseUri = "https://fftbg.com/api/tournament/";
	private static final String tipsApiUrl = "https://fftbg.com/api/tips";
	
	private RateLimiter limit = RateLimiter.create(1.000/8.000);
	
	public TournamentServiceImpl() {}
	
	@Override
	public List<Tournament> getTournaments(List<TournamentInfo> tournamentInfoList) throws TournamentApiException {
		List<Tournament> tournaments = new ArrayList<Tournament>();
		tournamentInfoList.parallelStream().forEach(tournamentInfo -> {
			try {
				Tournament tournament = this.getTournamentById(tournamentInfo.getID());
				tournaments.add(tournament);
			} catch (TournamentApiException e) {
				log.error("Could not pull tournament data for id {}", tournamentInfo.getID(), e);
			}
		});
		Collections.sort(tournaments);
		
		return tournaments;
	}
	
	@Override
	public List<TournamentInfo> getLatestTournamentInfo(Integer count) throws TournamentApiException {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<TournamentInfo[]> tournamentInfo;
		String url = tournamentInfoApiUri + count.toString();
		try {
			limit.acquire();
			tournamentInfo = restTemplate.getForEntity(url, TournamentInfo[].class);
		} catch(Exception e) {
			log.error("Error found getting latest tournament info", e);
			throw new TournamentApiException(e);
		}
		//TournamentInfo tournamentInfo = restTemplate.getForObject(tournamentInfoApiUri, TournamentInfo.class);
		List<TournamentInfo> tournamentInfoList = Arrays.asList(tournamentInfo.getBody());
		Collections.sort(tournamentInfoList);
		return tournamentInfoList;
	}
	
	@Override
	public Tips getCurrentTips() throws TournamentApiException {
		Tips currentTip;
		Resource resource;
		try {
			limit.acquire();
			resource = new UrlResource(tipsApiUrl);
		} catch (MalformedURLException e) {
			log.error("Error found getting latest tournament info", e);
			throw new TournamentApiException(e);
		}
		Tips tips = new Tips(resource);
		currentTip = tips;
		
		return currentTip;
	}
	
	protected Tournament getTournamentById(Long id) throws TournamentApiException {
		Tournament tournament = this.getTournamentFromApiById(id);
		log.info("Loading tournament {} from api", id);
		return tournament;
	}
	
	protected Tournament getTournamentFromApiById(Long id) throws TournamentApiException {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Tournament> latestTournament;
		try {
			limit.acquire();
			latestTournament = restTemplate.getForEntity(tournamentApiBaseUri + id.toString(), Tournament.class);
		} catch(Exception e) {
			log.error("Error found getting latest tournament info", e);
			throw new TournamentApiException(e);
		}
		return latestTournament.getBody();
	}
	
	
	

}
