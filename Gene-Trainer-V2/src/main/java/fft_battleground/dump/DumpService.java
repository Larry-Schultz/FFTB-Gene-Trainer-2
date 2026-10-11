package fft_battleground.dump;

import java.util.List;
import java.util.Map;

import fft_battleground.dump.model.DumpData;
import fft_battleground.model.BattleGroundTeam;

public interface DumpService {
	Map<Long, DumpData> getTournamentTeamValueMap(List<Long> tournamentIds);
	Map<Long, Map<BattleGroundTeam, Integer>> getTeamValuesForTournaments(List<Long> tournamentIds);
}
