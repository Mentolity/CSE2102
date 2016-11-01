package game;

import maps.Doors;
import menu.MainMenu;
import menu.Menu;
import gfx.PlayerCharacter;
import gfx.Screen;
import gfx.WhiteSpace;

public class Controller {
	public Menu menu = new MainMenu();
	public PlayerCharacter mc = new PlayerCharacter("/mcSpriteSheet.png");
	Game game;
	Screen screen;
	
	
	public Controller(Game game, Screen screen){
		this.game = game;
		this.screen = screen;
	}
	
	public void tick(){
		if(!game.map.isTransitioning())
			updatePlayerCharacter();
		
		//check if you're on a door and if so go to that map
		Doors d = game.map.onDoor(mc); 
		if(d != null){
			screen.xOffset = d.getxInitPos();
			screen.yOffset = d.getyInitPos();
			
			game.map = d.getMap();
			game.map.setTransitioning();
		}
		
		//debug info bound to 'P'
		if(game.input.debug.isPressed() && game.input.debug.ticksPressed() <= 1){
			System.out.println("PC Position: " + mc.getxPos() + ", " + mc.getyPos());
			System.out.println("Screen Position: " + screen.xOffset + ", " + screen.yOffset);
		}
	}
	
	private void updatePlayerCharacter(){
		mc.setyPos(screen.yOffset + mc.yCenter);
		mc.setxPos(screen.xOffset + mc.xCenter);
		if(game.input.enter.isPressed()){
			if(game.input.enter.ticksPressed()<=1){
				if(!menu.isOpen()){
					menu.updateCursor();
					menu.open();
				}
				else{
					Menu opt = menu;
					menu = menu.goUp();
					if(menu!=null) menu.updateCursor();
					else{
						menu = opt;
						menu.close();
					}	
				}
			}	
		}
		if(menu.isOpen()){
			menu.navigate(game.input);
			if(menu.next!=null){
				Menu m = menu.next;
				menu.next = null;
				menu = m;
			}
		}
		else mc.walk(game.input, screen, game.map);
	}
}
