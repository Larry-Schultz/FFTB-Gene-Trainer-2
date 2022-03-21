package fft_battleground.simulation.model;

import static org.junit.Assert.assertEquals;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fft_battleground.exception.TournamentApiException;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.genetic.model.attributes.GeneAttributes;
import fft_battleground.tournament.TournamentService;
import fft_battleground.tournament.model.Tips;
import io.jenetics.DoubleChromosome;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class GeneArrayTest {
	
	@Autowired
	private TournamentService tournamentService;
	
	private CompleteBotGenome testGenome;
	
	public void setup() throws TournamentApiException {
		Tips tips = this.tournamentService.getCurrentTips();
		this.testGenome = new CompleteBotGenome(tips);
		
		this.testGenome.geneAttributes.stream().forEach(geneAttribute -> {
			geneAttribute.getGeneAttributes().entrySet().stream().forEach(entry -> {
				geneAttribute.getGeneAttributes().put(entry.getKey(), entry.getKey().doubleValue());
			});
		});
	}
	
	@Test
	public void testGeneArray() throws TournamentApiException {
		this.setup();
		List<DoubleChromosome> chromosomes = this.testGenome.generateChromosomes();
		GeneArray geneArray = new GeneArray(this.testGenome);
		for(int i = 0; i < geneArray.size(); i++) {
			MappingData currentMappingData = geneArray.getMappingFunctionsArray().get(i);
			int geneValue = chromosomes.get(currentMappingData.chromosomeIndex()).get(i - currentMappingData.getterIndexOffset()).intValue();
			if(i != geneValue) {
				log.error("index = {}, geneValue = {}", i, geneValue);
			}
		}
	}
	
	@Test
	public void testMappingFunctionCreation() throws TournamentApiException {
		this.setup();
		Map<Class<? extends GeneAttributes>, MappingFunctionData> mappingFunction = GeneArray.createMappingFunctions(this.testGenome);
		List<Class<? extends GeneAttributes>> geneClasses = this.testGenome.geneAttributes.stream().map(GeneAttributes::getClass).collect(Collectors.toList());
		List<Class<? extends GeneAttributes>> mappingFunctionGeneClasses = mappingFunction.values().stream()
				.sorted(Comparator.comparing(MappingFunctionData::geneAttributeIndex))
				.map(MappingFunctionData::clazz).collect(Collectors.toList());
		assertEquals(geneClasses.size(), mappingFunctionGeneClasses.size());
		for(int i = 0; i < geneClasses.size(); i++) {
			assertEquals(geneClasses.get(i), mappingFunctionGeneClasses.get(i));
		}
	}
	
	@Test
	public void testMappingFunctionArray() throws TournamentApiException {
		
	}
	
}
