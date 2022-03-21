package fft_battleground.tournament.model;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.genetic.model.attributes.ItemGeneAttributes;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Unit {
	@JsonProperty("Name")
	private String Name;
	@JsonProperty("Gender")
	private String Gender;
	@JsonProperty("Sign")
	private String Sign;
	@JsonProperty("Brave")
	private Short Brave;
	@JsonProperty("Faith")
	private Short Faith;
	@JsonProperty("Class")
	private String className;
	@JsonProperty("ActionSkill")
	private String ActionSkill;
	@JsonProperty("ReactionSkill")
	private String ReactionSkill;
	@JsonProperty("MoveSkill")
	private String MoveSkill;
	@JsonProperty("Mainhand")
	private String Mainhand;
	@JsonProperty("Offhand")
	private String Offhand;
	@JsonProperty("Head")
	private String Head;
	@JsonProperty("Armor")
	private String Armor;
	@JsonProperty("Accessory")
	private String Accessory;
	@JsonProperty("ClassSkills")
	private List<String> ClassSkills;
	@JsonProperty("ExtraSkills")
	private List<String> ExtraSkills;
	@JsonProperty("RaidBoss")
	private Boolean raidBoss;
	
	public Unit() {}
	
	public List<String> getUnitGeneAbilityElements() {
		List<String> elements = new LinkedList<>();
		elements.add(this.className);
		elements.add(this.ActionSkill);
		elements.add(this.ReactionSkill);
		elements.add(this.MoveSkill);
		
		String mainHand = this.addItemSuffixIfNecessary(this.Mainhand);
		elements.add(mainHand);
		
		String offHand = this.addItemSuffixIfNecessary(this.Offhand);
		elements.add(offHand);

		String head = this.addItemSuffixIfNecessary(this.Head);
		elements.add(head);
		
		String armor = this.addItemSuffixIfNecessary(this.Armor);
		elements.add(armor);
		
		String accessory = this.addItemSuffixIfNecessary(this.Accessory);
		elements.add(accessory);
		
		if(this.ClassSkills != null) {
			elements.addAll(this.ClassSkills);
		}
		if(this.ExtraSkills != null) {
			elements.addAll(ExtraSkills);
		}
		
		return elements;
	}
	
	private String addItemSuffixIfNecessary(final String item) {
		String result = item;
		if(ItemGeneAttributes.itemsToPrefix.contains(item)) {
			result = this.Offhand + ItemGeneAttributes.itemSuffix;
		}
		
		return result;
	}
	
}
