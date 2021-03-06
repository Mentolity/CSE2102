package pokemon;

import status.Status;
import items.Item;



public abstract class Pokemon {
	public String name;
	public String description;
	public int ID, catchRate, fleeChance;
	public int baseMaxHP, baseAttack, baseDefense, baseSpecialAttack, baseSpecialDefense, baseSpeed;
	public int IVmaxHP, IVattack, IVdefense, IVspecialAttack, IVspecialDefense, IVspeed;
	//public int EVmaxHP, EVattack, EVdefense, EVspecialAttack, EVspecialDefense, EVspeed;
	public int level, currentHP, maxHP, attack, defense, specialAttack, specialDefense, speed;
	public enum Type {NORMAL, FIRE, WATER, ELECTRIC, GRASS, ICE, FIGHTING, POISON,
		GROUND, FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY};
	public enum Gender{Male, Female};
	public Type t;
	public Gender g;
	public Status status;
	public Item item;
	/*Stat = floor((2 * B + I + E) * L / 100 + L + 10)
	 * B = base stat
	 * I = IV
	 * E = Effort Value (NOT IMPLEMENTED)
	 * L = Level
	 */
	public void calculateStats(Pokemon p) {
		p.maxHP				=(int) Math.floor(((2*p.baseMaxHP+p.IVmaxHP)*p.level)/(110+p.level));
		p.currentHP			= maxHP;
		p.attack			=(int) Math.floor(((2*p.baseAttack+p.IVattack)*p.level)/(110+p.level));
		p.defense			=(int) Math.floor(((2*p.baseDefense+p.IVdefense)*p.level)/(110+p.level));
		p.specialAttack		=(int) Math.floor(((2*p.baseSpecialAttack+p.IVspecialAttack)*p.level)/(110+p.level));
		p.specialDefense	=(int) Math.floor(((2*p.baseSpecialDefense+p.IVspecialDefense)*p.level)/(110+p.level));
		p.speed				=(int) Math.floor(((2*p.baseSpeed+p.IVspeed)*p.level)/(110+p.level));
	}
	public void levelUp() {
		level += 1;
		calculateStats(this);
	}
	public Pokemon giveItem(Item i){
		item = i;
		return this;
	}
	public Item takeItem(){
		if(item!=null){
			Item i = item;
			item = null;
			return i;
		}
		else return null;
	}
	public void useItem(){
		if(item!=null){
			int id = item.ID;
			item = null;
			if(id==1){
				currentHP+=20;
				if(currentHP>maxHP) currentHP = maxHP;
			}
			else if(id==2){
				
			}
		}
	}
	public void useItem(Item i){
		if(i!=null){
			int id = i.ID;
			if(id==1){
				currentHP+=20;
				if(currentHP>maxHP) currentHP = maxHP;
			}
			else if(id==2){
				
			}
		}
	}

	/* Damage = [((2L+10)/250) * (A/D) * BP + 2) * Modifier
	 * L = level,	A = Attacker's attack, B = Defender's defense,	BP = Base power of attack
	 * 
	 * Modifier = STAB * Type * random(.85, 1) * Critical(NOT IMPLEMENTED) * other (held items, NOT IMPLEMENTED)
	 * STAB = 1.5 if attack same type as user, 1 otherwise
	 * Type = type effectiveness (0 to 4)
	 */
	public void hit(Pokemon a, Pokemon b) {
		
	}
}
