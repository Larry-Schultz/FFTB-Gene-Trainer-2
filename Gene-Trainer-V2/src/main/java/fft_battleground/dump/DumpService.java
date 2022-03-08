package fft_battleground.dump;

import java.util.List;
import java.util.Map;

import fft_battleground.dump.model.DumpData;

public interface DumpService {
	Map<Long, DumpData> getTournamentTeamValueMap(List<Long> tournamentIds);
}
