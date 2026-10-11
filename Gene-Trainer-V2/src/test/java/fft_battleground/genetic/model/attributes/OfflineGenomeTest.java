package fft_battleground.genetic.model.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import fft_battleground.tournament.model.Tips;
import io.jenetics.DoubleChromosome;

//builds a genome from the classpath Maps.txt so no network is needed
public class OfflineGenomeTest {

	public static CompleteBotGenome buildOfflineGenome() throws Exception {
		String mapsFileLocation = new ClassPathResource("Maps.txt").getFile().getPath();
		CompleteBotGenome genome = new CompleteBotGenome(new Tips(), mapsFileLocation);
		genome.generateAttributeMap();
		return genome;
	}

	@Test
	public void testBetGenesAreEnabledAndResolvable() throws Exception {
		CompleteBotGenome genome = buildOfflineGenome();

		assertNotNull(genome.getAttributeMap().get(BetGeneAttributes.first), "the \"1-bet\" gene must resolve so SimulatorFactoryImpl doesn't NPE");
		assertNotNull(genome.getAttributeMap().get("100-bet"));
		assertEquals(100, genome.getBetGeneAttributes().getGeneAttributes().size(), "bet chromosome must have 100 genes");
		assertEquals(7, genome.getGeneAttributes().size(), "map, bet, bravefaith, pot, playerdata, side, missing sections");
	}

	@Test
	public void testOfflineGenomeSize() throws Exception {
		CompleteBotGenome genome = buildOfflineGenome();

		//maps are read from the classpath file (118 maps x 2 sides), plus bet/bravefaith/pot/playerdata/side and the "-Item" sentinel
		int expectedMaps = MapGeneAttributes.numberOfMaps * 2;
		int expected = expectedMaps + 100 + BraveFaithAttributes.braveFaith.size() + PotGeneAttributes.potGeneAttributes.size()
				+ PlayerDataGeneAttributes.attributes.size() + SideGeneAttributes.sides.size() + 1;
		assertEquals(expected, genome.size());
		assertTrue(genome.size() > 300, "genome should have hundreds of genes");
	}

	@Test
	public void testBetChromosomeBuildsWithDefaultAlleles() throws Exception {
		CompleteBotGenome genome = buildOfflineGenome();

		DoubleChromosome betChromosome = genome.getBetGeneAttributes().generateChromosome();
		assertEquals(100, betChromosome.length());
	}

	@Test
	public void testAddMissingGeneIsIdempotentOffline() throws Exception {
		CompleteBotGenome genome = buildOfflineGenome();

		int firstId = genome.addMissingGene("Chemist Female");
		int secondId = genome.addMissingGene("Chemist Female");

		assertEquals(firstId, secondId);
	}
}
