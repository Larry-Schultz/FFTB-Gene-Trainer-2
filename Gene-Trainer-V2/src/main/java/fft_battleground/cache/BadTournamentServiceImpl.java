package fft_battleground.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BadTournamentServiceImpl implements BadTournamentService {
	private static final String badTournamentIdFilename = "BadTournaments.txt";
	
	private transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private Set<Long> badTournamentIds = new HashSet<>();
	
	public BadTournamentServiceImpl() {
		List<Long> badTournaments = List.of(1639305296545L, 1639281050642L, 1639305296545L, 1639410234093L, 1639436642684L);
		this.badTournamentIds = this.readBadTournamentFile();
		this.badTournamentIds.addAll(badTournaments);
	}
	
	@Override
	public void addBadTournament(Collection<Long> tournamentId) {
		log.info("adding bad tournaments {}", tournamentId);
		Lock writeLock = this.readWriteLock.writeLock();
		writeLock.lock();
		try {
			Set<Long> newTournamentIds = tournamentId.stream().filter(id -> !this.badTournamentIds.contains(id))
					.collect(Collectors.toSet());
			if(newTournamentIds.size() > 0) {
				this.badTournamentIds.addAll(newTournamentIds);
				this.writeBadTournamentFile(this.badTournamentIds);
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public Set<Long> getBadTournamentIds() {
		return this.badTournamentIds;
	}

	private Set<Long> readBadTournamentFile() {
		Lock readLock = this.readWriteLock.readLock();
		readLock.lock();
		Set<Long> tournamentIds = new HashSet<>();
		File mapFile = new File(badTournamentIdFilename);
		try(BufferedReader reader = new BufferedReader(new FileReader(mapFile))) {
			String line;
			while((line = reader.readLine()) != null) {
				Long tournamentId = Long.valueOf(StringUtils.trim(line));
				tournamentIds.add(tournamentId);
			}
		} catch (FileNotFoundException e) {
			log.error("Error reading map file", e);
		} catch (IOException e) {
			log.error("Error reading map file", e);
		} finally {
			readLock.unlock();
		}
		
		return tournamentIds;
	}
	
	private void writeBadTournamentFile(Set<Long> tournamentIds) {
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(badTournamentIdFilename));) {
			for(Long tournamentId: tournamentIds) {
				writer.write(tournamentId.toString() + "\n");
			}
		} catch (IOException e) {
			log.error("Error writing to bad tournament file", e);
		}
	}
}
