package fft_battleground.tournament.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tournament implements Comparable<Tournament> {
	@JsonProperty("Type")
	private String Type;
	@JsonProperty("ID")
	private Long ID;
	
	@JsonProperty("LastMod")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ssXXX")
	private Date LastMod;
	@JsonProperty("Teams")
	private Teams Teams;
	@JsonProperty("Maps")
	private List<String> Maps;
	@JsonProperty("Winners")
	private List<String> Winners;
	@JsonProperty("Pots")
	private List<Pot> pots;
	@JsonProperty("SkillDrop")
	private String SkillDrop;
	@JsonProperty("Entrants")
	private List<String> Entrants;
	@JsonProperty("Snubs")
	private List<String> Snubs;
	
	private Integer winnersCount;
	private List<String> raidbosses;
	private Set<String> allPlayers;
	
	public Tournament() {}

	@Override
	public int compareTo(Tournament o) {
		return this.ID.compareTo(o.getID());
	}
}
