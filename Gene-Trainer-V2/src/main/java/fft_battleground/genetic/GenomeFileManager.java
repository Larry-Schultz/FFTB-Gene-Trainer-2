package fft_battleground.genetic;

import fft_battleground.genetic.model.GenomeFile;

public interface GenomeFileManager {
	public Runnable createGenomeFileCheckAndWriteRunnable(GenomeFile genomeData);
}
