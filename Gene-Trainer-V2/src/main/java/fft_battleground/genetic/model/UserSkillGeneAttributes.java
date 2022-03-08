package fft_battleground.genetic.model;

import java.util.List;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class UserSkillGeneAttributes extends GeneAttributes {

	private static final List<String> JOB_SKILLS = List.of("BasicSkill","BattleSkill","Item","Charge","PunchArt","Elemental","Jump","DrawOut","Throw","Steal",
			"TalkSkill","Dance","Sing","WhiteMagic","BlackMagic","TimeMagic","SummonMagic","YinYangMagic","BlueMagic");
	
	private static final List<String> REACTION_SKILLS = List.of("Caution","Parry","ArrowGuard","SunkenState","PASave","MASave","SpeedSave","BraveSave","FaithSave",
			"AutoPotion","HPRestore","MPRestore","AbsorbUsedMP","Regenerator","DragonSpirit","CriticalQuick","MeatboneSlash","Counter","CounterTackle","CounterMagic","CounterFlood",
			"Hamedo","DamageSplit","Catch","Earplug","Abandon","Distribute","ManaShield");
	
	private static final List<String> MOVEMENT_SKILLS = List.of("Move+1","Move+2","Move+3","Jump+1","Jump+2","Jump+3","Swim","Waterbreathing","Waterwalking",
			"LavaWalking","Levitate","Move-HPUp","Move-MPUp","Teleport","Fly","IgnoreHeight","IgnoreTerrain","Retreat");
	
	private static final List<String> EQUIPMENT_SKILLS  = List.of("108Gems","AngelRing","CursedRing","DefenseRing","MagicRing","ReflectRing","Bracer","DefenseArmlet",
			"DiamondArmlet","JadeArmlet","N-KaiArmlet","PowerWrist","MagicGauntlet","GenjiGauntlet","BattleBoots","FeatherBoots","GerminasBoots","RedShoes","RubberShoes","SpikeShoes",
			"SprintShoes","LeatherMantle","FeatherMantle","WizardMantle","SmallMantle","ElfMantle","VanishMantle","DraculaMantle");
	
	private static final List<String> SUPPORT_SKILLS  = List.of("Concentrate","MartialArts","Maintenance","Doublehand","DualWield","Defend","ShortCharge","HalveMP",
			"Beastmaster","SecretHunt","Sicken","MonsterTalk","ThrowItem","LongStatus","ShortStatus","AttackUP","DefenseUP","MagicAttackUP","MagicDefenseUP","EquipArmor","EquipShield",
			"EquipKnife","EquipBow","EquipSword","EquipGun","EquipAxe","EquipPolearm");
	
	private static final List<String> ENTRY_SKILLS  = List.of("BraveBoost","FaithBoost","FashionSense","PreferredArms","NeutralZodiac","GearedUp","HighlySkilled",
			"GilgameHeart","EXPBoost");
	
	private static final List<String> PRESTIGE_SKILLS = List.of("RaidBoss","MathSkill","EquipPerfume","Teleport2","BladeGrasp","Doppelganger");
	
	private static final List<String> LEGENDARY_SKILLS = List.of("BirbBrain", "ProgrammingUp");
	
	public UserSkillGeneAttributes(AtomicInteger idTracker) {
		List<String> names = Stream.of(JOB_SKILLS, REACTION_SKILLS, MOVEMENT_SKILLS, EQUIPMENT_SKILLS, SUPPORT_SKILLS, ENTRY_SKILLS, PRESTIGE_SKILLS,LEGENDARY_SKILLS)
				.flatMap(Collection::stream)
				.distinct()
				.collect(Collectors.toList());
		this.populateMapsFromAttributeList(idTracker, names);
	}
}
