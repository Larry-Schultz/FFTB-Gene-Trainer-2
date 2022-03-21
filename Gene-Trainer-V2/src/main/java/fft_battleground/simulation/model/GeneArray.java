package fft_battleground.simulation.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.genetic.model.attributes.GeneAttributes;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class GeneArray {
	
	private List<MappingData> mappingFunctionsArray;
	
	public GeneArray(CompleteBotGenome genome) {
		Map<Class<? extends GeneAttributes>, MappingFunctionData> mappingFunctions = createMappingFunctions(genome);
		
		this.mappingFunctionsArray = mappingFunctions.entrySet().stream()
			.flatMap(entry -> entry.getValue().getIndexFunctionPair().stream())
			.map(Pair::getRight).sorted()
			.collect(Collectors.toList());
	}
	
	public double get(Genotype<DoubleGene> genotype, int index) {
		MappingData data = null;
		try {
			data = this.mappingFunctionsArray.get(index);
		} catch(IndexOutOfBoundsException e) {
			log.error("Out of Bounds Exception.  Attempting to access index {} of array with length {}", index, this.mappingFunctionsArray.size(), e);
		}
		double result = genotype.get(data.chromosomeIndex()).get(index - data.getterIndexOffset()).allele();
		return result;
	}
	
	public int size() {
		return this.mappingFunctionsArray.size();
	}
	
	boolean testIndexFunctionMap(Map<Integer, BiFunction<Integer, Genotype<DoubleGene>, Double>> map) {
		int min = Collections.min(map.keySet());
		int max = Collections.max(map.keySet());
		boolean test = IntStream.rangeClosed(min, max).allMatch(map::containsKey);
		return test;
	}
	
	static Map<Class<? extends GeneAttributes>, MappingFunctionData> createMappingFunctions(CompleteBotGenome genome) {
		Map<Class<? extends GeneAttributes>, MappingFunctionData> mappingFunctions = new HashMap<>();
		
		int currentOffset = 0;
		for(int i = 0 ; i < genome.getGeneAttributes().size(); i++) {
			GeneAttributes attribute = genome.getGeneAttributes().get(i);
			Class<? extends GeneAttributes> geneClass = attribute.getClass();
			MappingData mappingData = new MappingData(i, currentOffset, geneClass);
			
			int attributeSize = attribute.getGeneAttributes().values().size();
			MappingFunctionData data = new MappingFunctionData(geneClass, i, mappingData, currentOffset, currentOffset + attributeSize);
			mappingFunctions.put(geneClass, data);
			currentOffset += attributeSize;
		}
		
		return mappingFunctions;
	}

}

record MappingFunctionData(Class<? extends GeneAttributes> clazz, int geneAttributeIndex, MappingData mappingData, int inclusiveStart, int exclusiveEnd) {
	
	public List<Pair<Integer, MappingData>> getIndexFunctionPair() {
		List<Pair<Integer, MappingData>> result = IntStream.range(inclusiveStart, exclusiveEnd)
				.mapToObj(index -> new ImmutablePair<Integer, MappingData>(index, this.mappingData))
				.collect(Collectors.toList());
		return result;
	}
}

record MappingData(Integer chromosomeIndex, Integer getterIndexOffset, Class<? extends GeneAttributes> clazz) implements Comparable<MappingData> {

	@Override
	public int compareTo(MappingData o) {
		int chromosomeCompareResult = this.chromosomeIndex.compareTo(o.chromosomeIndex());
		
		if(chromosomeCompareResult != 0) {
			return chromosomeCompareResult;
		}
		
		int offsetResult = this.getterIndexOffset.compareTo(o.getterIndexOffset());
		return offsetResult;
	}

}
