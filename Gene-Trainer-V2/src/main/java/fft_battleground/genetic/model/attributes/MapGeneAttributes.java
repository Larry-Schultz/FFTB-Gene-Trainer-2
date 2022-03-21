package fft_battleground.genetic.model.attributes;

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

import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper=true)
@Slf4j
public final class MapGeneAttributes extends GeneAttributes {
	
	public static int numberOfMaps = 0;
	
	public MapGeneAttributes(AtomicInteger idTracker, String mapFile) {
		List<Integer> maps = mapNumbers(mapFile);
		numberOfMaps = maps.size();
		List<String> attributes = new ArrayList<>();
		for(Integer mapNumber: maps) {
			String leftAttributeName = buildMapName(mapNumber, BattleGroundTeam.LEFT);
			attributes.add(leftAttributeName);
		}
		for(Integer mapNumber: maps) {
			String rightAttributeNema = buildMapName(mapNumber, BattleGroundTeam.RIGHT);
			attributes.add(rightAttributeNema);
		}
		
		this.populateMapsFromAttributeList(idTracker, attributes);
	}
	
	public static String buildMapName(Integer mapNumber, BattleGroundTeam side) {
		String output = null;
		if(side == BattleGroundTeam.LEFT) {
			output = mapNumber.toString() + "-Left";
		} else if(side == BattleGroundTeam.RIGHT) {
			output = mapNumber.toString() + "-Right";
		}
		
		return output;
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
