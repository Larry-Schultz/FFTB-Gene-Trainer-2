package fft_battleground.botland.genedata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import fft_battleground.botland.genedata.GeneFileService.V1GeneData;
import fft_battleground.botland.genedata.GeneFileService.V2GeneData;

public class GeneFileServiceTest {

	private final GeneFileService service = new GeneFileService();

	@Test
	public void testLoadV2WinnerFile() throws Exception {
		V2GeneData data = this.service.loadV2GeneData("winner05-29-2022.txt");

		assertNotNull(data.geneValues().get("Faith"));
		//the file header claims geneCount 994 but it ships 992 key/value pairs; the loader reports what is actually there
		assertEquals(992, data.geneValues().size(), "the shipped V2 winner file has 992 gene values");
		long betGeneCount = data.geneValuesBySection().get("betGeneAttributes").keySet().stream()
				.filter(name -> name.endsWith("-bet")).count();
		assertEquals(100, betGeneCount, "the bet gene section must have all 100 bet genes");
		assertTrue(data.geneValuesBySection().containsKey("mapGeneAttributes"));
		assertTrue(data.geneValuesBySection().containsKey("missingGeneAttributes"));
	}

	@Test
	public void testLoadViewerJuneGenome() throws Exception {
		V2GeneData june = this.service.loadV2GeneData("winner06-09-2022.txt");

		//256 map + 4 bravefaith + 3 pot + 7 playerdata + 11 side + 100 bet + 772 missing genes; the file's top-level
		//percentiles table is NOT part of the genome and must not leak into the gene values
		assertEquals(1153, june.geneValues().size(), "the June 2022 viewer genome has 1153 clean, duplicate-free gene values");
		assertTrue(june.geneValuesBySection().containsKey("mapGeneAttributes"));
		assertTrue(june.geneValuesBySection().containsKey("braveFaithAttributes"));
		assertTrue(june.geneValuesBySection().containsKey("betGeneAttributes"));
		assertTrue(june.geneValuesBySection().containsKey("missingGeneAttributes"));
		long betGeneCount = june.geneValuesBySection().get("betGeneAttributes").keySet().stream()
				.filter(name -> name.endsWith("-bet")).count();
		assertEquals(100, betGeneCount, "the bet gene section must be fully populated");
	}

	@Test
	public void testLoadV1GeneFiles() throws Exception {
		V1GeneData winner100 = this.service.loadV1GeneData("winner-100.txt");
		assertTrue(winner100.geneticAttributes().size() > 100, "winner-100.txt should have hundreds of attributes");
		assertTrue(winner100.geneticAttributes().containsKey("Charge"));
		assertEquals(99, winner100.percentiles().size(), "winner-100.txt ships a 99 entry percentile table");

		//winner-1000.txt has no percentile table; the viewer default of 50 applies
		V1GeneData winner1000 = this.service.loadV1GeneData("winner-1000.txt");
		assertTrue(winner1000.percentiles().isEmpty());

		V1GeneData april2026 = this.service.loadV1GeneData("genebotv1-4-17-26.txt");
		assertTrue(april2026.geneticAttributes().size() > 100, "the April 2026 output should have hundreds of attributes");
	}

	@Test
	public void testPercentileCalculation() {
		//percentiles[i] < scoreDifference wins, taking the last (highest) matching index
		Map<Integer, Double> percentiles = Map.of(
				1, 0.0,
				2, 5.0,
				3, 10.0,
				50, 100.0,
				99, 1000.0);

		assertEquals(3, GeneFileService.calculatePercentile(percentiles, 15));
		assertEquals(50, GeneFileService.calculatePercentile(percentiles, 500));
		assertEquals(99, GeneFileService.calculatePercentile(percentiles, 5000));
		assertEquals(0, GeneFileService.calculatePercentile(percentiles, 0), "no percentile threshold below the difference means 0");

		//no table at all: the viewer default
		assertEquals(50, GeneFileService.calculatePercentile(Map.of(), 100));
		assertEquals(50, GeneFileService.calculatePercentile(null, 100));
	}
}
