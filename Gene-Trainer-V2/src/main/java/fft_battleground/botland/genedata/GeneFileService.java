package fft_battleground.botland.genedata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.exception.BotConfigException;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.genetic.model.attributes.GeneAttributes;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeneFileService {
	private static final String V1_GENE_FILE_LOCATION = "GeneFiles/GeneTrainerV1/";
	private static final String V2_GENE_FILE_LOCATION = "GeneFiles/";

	private final Map<String, V1GeneData> v1GeneDataCache = new ConcurrentHashMap<>();
	private final Map<String, V2GeneData> v2GeneDataCache = new ConcurrentHashMap<>();

	//V1 trainer output: flags + "geneticAttributes": [{"key","value"}] + optional "percentiles": [{"key","value"}]
	public record V1GeneData(Map<String, Double> geneticAttributes, Map<Integer, Double> percentiles) {}

	//V2 trainer output: "genome": {"<section>": {"geneAttributes": {"attributes": [{"key","value"}]}}}
	public record V2GeneData(Map<String, Double> geneValues, Map<String, Map<String, Double>> geneValuesBySection) {}

	public V1GeneData loadV1GeneData(String filename) throws BotConfigException {
		V1GeneData cached = this.v1GeneDataCache.get(filename);
		if(cached != null) {
			return cached;
		}
		V1GeneData data = this.parseV1GeneData(filename);
		this.v1GeneDataCache.put(filename, data);
		return data;
	}

	public V2GeneData loadV2GeneData(String filename) throws BotConfigException {
		V2GeneData cached = this.v2GeneDataCache.get(filename);
		if(cached != null) {
			return cached;
		}
		V2GeneData data = this.parseV2GeneData(filename);
		this.v2GeneDataCache.put(filename, data);
		return data;
	}

	protected V1GeneData parseV1GeneData(String filename) throws BotConfigException {
		JsonNode root = this.readGeneFile(V1_GENE_FILE_LOCATION + filename);
		Map<String, Double> geneticAttributes = this.parseKeyValueList(root.get("geneticAttributes"), "geneticAttributes", filename);
		Map<Integer, Double> percentiles = new HashMap<>();
		JsonNode percentileNode = root.get("percentiles");
		if(percentileNode != null && percentileNode.isArray()) {
			for(JsonNode entry : percentileNode) {
				percentiles.put(entry.get("key").asInt(), entry.get("value").asDouble());
			}
		}
		log.info("loaded V1 gene file {} with {} attributes and {} percentiles", filename, geneticAttributes.size(), percentiles.size());
		return new V1GeneData(geneticAttributes, percentiles);
	}

	protected V2GeneData parseV2GeneData(String filename) throws BotConfigException {
		JsonNode root = this.readGeneFile(V2_GENE_FILE_LOCATION + filename);
		Map<String, Map<String, Double>> geneValuesBySection = new LinkedHashMap<>();
		JsonNode genomeNode = root.get("genome");
		if(genomeNode == null || !genomeNode.isObject()) {
			throw new BotConfigException("V2 gene file " + filename + " has no genome section");
		}
		genomeNode.fields().forEachRemaining(section -> {
			JsonNode attributesNode = section.getValue() == null ? null : section.getValue().get("geneAttributes");
			JsonNode attributeList = attributesNode == null ? null : attributesNode.get("attributes");
			if(attributeList != null && attributeList.isArray()) {
				Map<String, Double> sectionValues = new LinkedHashMap<>();
				for(JsonNode attribute : attributeList) {
					sectionValues.put(attribute.get("key").asText(), attribute.get("value").asDouble());
				}
				geneValuesBySection.put(section.getKey(), sectionValues);
			}
		});
		Map<String, Double> geneValues = new HashMap<>();
		geneValuesBySection.values().forEach(geneValues::putAll);
		log.info("loaded V2 gene file {} with {} gene values across {} sections", filename, geneValues.size(), geneValuesBySection.size());
		return new V2GeneData(geneValues, geneValuesBySection);
	}

	protected JsonNode readGeneFile(String location) throws BotConfigException {
		Resource resource = new ClassPathResource(location);
		if(!resource.exists()) {
			throw new BotConfigException("Gene file not found on the classpath: " + location);
		}
		try {
			return new ObjectMapper().readTree(resource.getInputStream());
		} catch(IOException e) {
			throw new BotConfigException("Could not read gene file " + location);
		}
	}

	private Map<String, Double> parseKeyValueList(JsonNode listNode, String fieldName, String filename) throws BotConfigException {
		Map<String, Double> values = new LinkedHashMap<>();
		if(listNode == null || !listNode.isArray()) {
			throw new BotConfigException("Gene file " + filename + " has no " + fieldName + " list");
		}
		for(JsonNode entry : listNode) {
			values.put(entry.get("key").asText(), entry.get("value").asDouble());
		}
		return values;
	}

	//percentile of a score difference in the gene file's percentile table; the viewer returns 50 when no table exists
	public static int calculatePercentile(Map<Integer, Double> percentiles, int scoreDifference) {
		if(percentiles == null || percentiles.isEmpty()) {
			return 50;
		}
		int percentile = 0;
		for(int i = 1; i <= 99; i++) {
			Double percentileValue = percentiles.get(i);
			if(percentileValue != null && percentileValue < scoreDifference) {
				percentile = i;
			}
		}
		return percentile;
	}

	//maps gene values by attribute NAME onto the genome's global gene id space; ids are contiguous 0..size-1
	public static double[] buildValueByGeneId(CompleteBotGenome genome, Map<String, Double> valuesByName) {
		double[] valueByGeneId = new double[genome.size()];
		for(GeneAttributes section : genome.getGeneAttributes()) {
			section.getIdNameMap().forEach((id, name) -> {
				Double value = valuesByName.get(name);
				if(value != null && id >= 0 && id < valueByGeneId.length) {
					valueByGeneId[id] = value;
				}
			});
		}
		return valueByGeneId;
	}

	//builds a genotype holding the file's values (0 for genes the file doesn't mention), index-compatible with the
	//genome's GeneArray because chromosomes are built in genome section order with ids ascending, like generateChromosome()
	public static Genotype<DoubleGene> buildGenotype(CompleteBotGenome genome, Map<String, Double> valuesByName) {
		List<DoubleChromosome> chromosomes = new ArrayList<>();
		for(GeneAttributes section : genome.getGeneAttributes()) {
			List<DoubleGene> genes = new ArrayList<>();
			section.getIdNameMap().entrySet().stream()
					.sorted(Comparator.comparing(Entry::getKey))
					.forEach(entry -> {
						Double value = valuesByName.getOrDefault(entry.getValue(), Double.valueOf(0));
						genes.add(DoubleGene.of(value, section.getMinValue(), section.getMaxValue()));
					});
			chromosomes.add(DoubleChromosome.of(genes));
		}
		return Genotype.of(chromosomes);
	}
}
