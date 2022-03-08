package fft_battleground.genetic.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper=true)
@Slf4j
public final class MapGeneAttributes extends GeneAttributes {

	public MapGeneAttributes(AtomicInteger idTracker, String mapFile) {
		for(Integer mapNumber: mapNumbers(mapFile)) {
			List<String> attributes = new ArrayList<>();
			String leftAttributeName = mapNumber.toString() + "-Left";
			attributes.add(leftAttributeName);
			
			String rightAttributeNema = mapNumber.toString() + "-Right";
			attributes.add(rightAttributeNema);
			
			this.populateMapsFromAttributeList(idTracker, attributes);
		}
	}
	
	private static List<Integer> mapNumbers(String mapFileLocation) {
		List<Integer> mapNumbers = new LinkedList<>();
		File mapFile = new File(mapFileLocation);
		try(BufferedReader reader = new BufferedReader(new FileReader(mapFile))) {
			String line;
			while((line = reader.readLine()) != null) {
				Integer mapNumber = mapNumber(line);
				mapNumbers.add(mapNumber);
			}
		} catch (FileNotFoundException e) {
			log.error("Error reading map file", e);
		} catch (IOException e) {
			log.error("Error reading map file", e);
		}
		
		return mapNumbers;
	}
	
	private static Integer mapNumber(String mapName) {
		String numberString = StringUtils.substringBefore(mapName, ")");
		Integer value = Integer.valueOf(numberString);
		return value;
	}
}
