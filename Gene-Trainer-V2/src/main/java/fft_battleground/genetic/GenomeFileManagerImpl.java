package fft_battleground.genetic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.genetic.model.GenomeFile;
import lombok.extern.slf4j.Slf4j;

@Service
public class GenomeFileManagerImpl implements GenomeFileManager {
	public static final String winnerFilenameTemplate = "winner%s.txt";
	private AtomicLong highestCurrentGilResult = new AtomicLong(0L);
	private Lock winnerFileLock = new ReentrantLock();
	
	@Override
	public Runnable createGenomeFileCheckAndWriteRunnable(GenomeFile genomeData) {
		Runnable genomeFileCheckAndWriteRunnable = new ResultChecker(genomeData, this.highestCurrentGilResult, this.winnerFileLock);
		return genomeFileCheckAndWriteRunnable;
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
			String newHighScoreFormatted = df.format(this.highestCurrentGilResultRef.get());
			String oldHighScoreFormatted = df.format(this.genomeData.getGilResult());
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
		String filename = String.format(GenomeFileManagerImpl.winnerFilenameTemplate, dateString);
		return filename;
	}
}
