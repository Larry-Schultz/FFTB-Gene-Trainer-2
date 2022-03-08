package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;

import fft_battleground.dump.model.BattlegroundRetryState;
import fft_battleground.dump.model.DumpData;
import fft_battleground.dump.model.TournamentTeamValues;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DumpServiceImpl implements DumpService {

	private static final String teamValueUrlFormat =  "http://www.fftbattleground.com/fftbg/tournament_%s/teamvalue.txt";
	private static final String streakUrlFormat =  "http://www.fftbattleground.com/fftbg/tournament_%s/streak.txt";
	
	@Autowired
	private DumpResourceRetryManager dumpResourceRetryManager;
	
	private RateLimiter limit = RateLimiter.create(1);
	
	@Override
	public Map<Long, DumpData> getTournamentTeamValueMap(List<Long> tournamentIds) {
		Map<Long, DumpData> dumpDataTournamentMap = new HashMap<>();
		for(Long tournamentId : tournamentIds) {
			Map<BattleGroundTeam, Integer> teamValuesForTournament = null;
			Integer champStreak = null;
			try {
				teamValuesForTournament = this.parseTeamValueFile(tournamentId);
				champStreak = this.getChampionStreak(tournamentId);
			} catch (DumpException e) {
				log.warn("unable to poll team value data from dump for tournament {}", tournamentId, e);
			}
			if(teamValuesForTournament != null) {
				TournamentTeamValues tournamentTeamValue = new TournamentTeamValues(teamValuesForTournament);
				DumpData dumpData = new DumpData(tournamentId, champStreak, tournamentTeamValue);
				dumpDataTournamentMap.put(tournamentId, dumpData);
			}
			
		}
		return dumpDataTournamentMap;
	}
	
	protected Map<BattleGroundTeam, Integer> parseTeamValueFile(Long tournamentId) throws DumpException {
		log.info("doing team value lookup for tournament {}", tournamentId);
		Map<BattleGroundTeam, Integer> teamValues = new HashMap<>();
		Resource resource;
		try {
			resource = new UrlResource(String.format(teamValueUrlFormat, tournamentId.toString()));
		} catch (MalformedURLException e1) {
			throw new DumpException(e1);
		}
		try(BufferedReader botReader = this.openDumpResource(resource)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = line;
				cleanedString = StringUtils.replace(cleanedString, ",", "");
				cleanedString = StringUtils.trim(cleanedString);
				String teamString = StringUtils.substringBefore(cleanedString, ":");
				BattleGroundTeam team = BattleGroundTeam.parse(teamString);
				String valueString = StringUtils.substringBetween(cleanedString, ": ", "G");
				Integer value = Integer.valueOf(valueString);
				teamValues.put(team, value);
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		List<BattleGroundTeam> coreTeams = new ArrayList<>(BattleGroundTeam.coreTeams());
		coreTeams.add(BattleGroundTeam.CHAMPION);
		for(BattleGroundTeam team: coreTeams) {
			if(!teamValues.containsKey(team)) {
				teamValues.put(team, 0);
			}
		}
		
		
		return teamValues;
	}
	
	protected Integer getChampionStreak(Long tournamentId) throws DumpException {
		log.info("doing champ streak lookup for tournament {}", tournamentId);
		Integer streak = null;
		Resource resource;
		try {
			resource = new UrlResource(String.format(streakUrlFormat, tournamentId.toString()));
		} catch (MalformedURLException e1) {
			throw new DumpException(e1);
		}
		try(BufferedReader botReader = this.openDumpResource(resource)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = line;
				streak = Integer.valueOf(cleanedString);
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		
		return streak;
	}
	
	public BufferedReader openDumpResource(Resource resource) throws DumpException {
		this.limit.acquire();
		final BattlegroundRetryState state = new BattlegroundRetryState();
		BufferedReader reader = this.dumpResourceRetryManager.openConnection(resource, state);
		
		return reader;
	}

}
