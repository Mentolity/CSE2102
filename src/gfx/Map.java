package gfx;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import game.Game;
import maps.Door;
import maps.InteractableObject;
import game.Music;
import game.AudioInit;
import game.Effect;

public class Map {
	String mapPath;
	String mapPointPath;

	int width;
	int height;
	
	private int[] mPixels;
	private int[] mpPixels;
	
	//At some point should replace with more efficient data-structure for retrieving elements
	protected ArrayList<Door> doors = new ArrayList<Door>();
	protected ArrayList<InteractableObject> interactableObjects = new ArrayList<InteractableObject>();
	
	public Map (String mp, String mpp){
		BufferedImage image;
		
		mapPath = mp;
		mapPointPath = mpp;
		
		
		try {
			image = ImageIO.read(Map.class.getResourceAsStream(mapPath));
			width = image.getWidth();
			height = image.getHeight();
			mPixels = image.getRGB(0, 0, width, height, null, 0, width);
			
			image = ImageIO.read(Map.class.getResourceAsStream(mapPointPath));
			mpPixels = image.getRGB(0, 0, width, height, null, 0, width);
		} catch (IOException e) {
			e.printStackTrace();
			
		}
	}
	
	public InteractableObject isTileInteractable(int direction, PlayerCharacter mc){
		switch (direction){
			case 1:
				return isUpInteractable(mc);
			case 2:
				return isLeftInteractable(mc);
			case 3: 
				return isDownInteractable(mc);
			case 4:
				return isRightInteractable(mc);
			default:
				return null;
		}
	}
	
	public InteractableObject isUpInteractable(PlayerCharacter mc){
		return isInteractable(mc, 0, -16);
	}
	
	public InteractableObject isDownInteractable(PlayerCharacter mc){
		return isInteractable(mc, 0, 16);
	}
	
	public InteractableObject isLeftInteractable(PlayerCharacter mc){
		return isInteractable(mc, -16, 0);
	}
	
	public InteractableObject isRightInteractable(PlayerCharacter mc){
		return isInteractable(mc, 16, 0);
	}
	
	private InteractableObject isInteractable(PlayerCharacter mc, int xDir, int yDir){
		boolean movingUp = (xDir == 0 && yDir == -16);
		boolean movingDown = (xDir == 0 && yDir == 16);
		boolean movingLeft = (xDir == -16 && yDir == 0);
		boolean movingRight = (xDir == 16 && yDir == 0);
		
		for(InteractableObject i : interactableObjects){
			if(movingUp && mc.getxPos() == i.getxPos() && mc.getyPos()-16 == i.getyPos())
				return i;
			if(movingDown && mc.getxPos() == i.getxPos() && mc.getyPos()+16 == i.getyPos())
				return i;
			if(movingRight && mc.getxPos()+16 == i.getxPos() && mc.getyPos() == i.getyPos())
				return i;
			if(movingLeft && mc.getxPos()-16 == i.getxPos() && mc.getyPos() == i.getyPos())
				return i;
		}
		return null;
	}
	
	
	
	public boolean isPassable(int direction, PlayerCharacter mc){
		switch (direction){
			case 1:
				return isUpPassable(mc);
			case 2:
				return isLeftPassable(mc);
			case 3: 
				return isDownPassable(mc);
			case 4:
				return isRightPassable(mc);
			default:
				return false;
		}
	}
	
	public boolean isUpPassable(PlayerCharacter mc){
		return isPassable(mc, 0, -16);
	}
	
	public boolean isDownPassable(PlayerCharacter mc){
		return isPassable(mc, 0, 16);
	}
	
	public boolean isLeftPassable(PlayerCharacter mc){
		return isPassable(mc, -16, 0);
	}
	
	public boolean isRightPassable(PlayerCharacter mc){
		return isPassable(mc, 16, 0);
	}
	
	public Door onDoor(PlayerCharacter mc){
		for(Door d : doors){
			if(d.getxPos() == mc.getxPos() && d.getyPos() == mc.getyPos()){
				return d;
			}
		}
		return null;
	}
	
	private boolean isPassable(PlayerCharacter mc, int xDir, int yDir){
		try{
			int topLeftPixel = mpPixels[(mc.getxPos()+xDir + ((mc.getyPos()+yDir)*width))] & 0xffffff;
			//int topRightPixel = mpPixels[(mc.getxPos()+xDir+15 + ((mc.getyPos()+yDir)*width))] & 0xffffff;
			//int bottomLeftPixel = mpPixels[(mc.getxPos()+xDir + ((mc.getyPos()+yDir+15)*width))] & 0xffffff;
			int bottomRightPixel = mpPixels[(mc.getxPos()+xDir+15 + ((mc.getyPos()+yDir+15)*width))] & 0xffffff;
			
			//boolean movingUp = (xDir == 0 && yDir == -16);
			boolean movingDown = (xDir == 0 && yDir == 16);
			boolean movingLeft = (xDir == -16 && yDir == 0);
			boolean movingRight = (xDir == 16 && yDir == 0);
			
			switch (topLeftPixel){
				case 0x12ff00: //Trees_GREEN
					return false;
				case 0x0F00FF: //Water
					return false;
				case 0xFFFF00: //Water
					return false;
				case 0x00FFFC: //Water_Ledge
					return false;
				case 0xff9000: //ledge_ORANGE
					if (movingLeft) //if the the top left is orange then the only time this is pass-able is if the mc is moving left
						return true;
					else
						return false;
				case 0xf6ff00: //ledge_YELLOW
					if (movingDown && bottomRightPixel == 0xff9000)
						return true;
					if (movingRight && bottomRightPixel == 0xff9000)
						return true;
					return false;		
				default:
					return true;
			}
		}catch(ArrayIndexOutOfBoundsException e){
			return false;
		}
	}
	
	private boolean transitioning = false;
	private int transitionCounterEndPoint = 33;					//11 ticks fade-out, 11 ticks white, 11 ticks fade-in 
	private int transitionCounter = transitionCounterEndPoint;
	private int waterAnimationTickCounter = 0;
	private int waterAnimationTickPosition = 0;
	
	private int flowerAnimationTickCounter = 0;
	private int flowerAnimationTickPosition = 0;
	
	private final int animationTimer = 8;
	public void render(Screen screen, PlayerCharacter mc){
		//if in a transition phases render that animation
		if(transitioning && transitionCounter > 0){
			renderMapTransition(screen, mc);			
			transitionCounter--;
		//else render the map and mc normally
		}else{
			transitionCounter = transitionCounterEndPoint;
			transitioning = false;
			screen.renderMap(this);
			screen.renderMapBackgroundAnimationLayer(this);
			mc.render(screen);
			screen.renderMapForegroundAnimationLayer(this);
		}
		
		waterAnimationTickCounter++;
		if(waterAnimationTickCounter < 5*animationTimer){
			waterAnimationTickPosition = 0;
		}else if(waterAnimationTickCounter < 6*animationTimer){
			waterAnimationTickPosition = 1;
		}else if(waterAnimationTickCounter < 11*animationTimer){
			waterAnimationTickPosition = 2;
		}else if(waterAnimationTickCounter < 16*animationTimer){
			waterAnimationTickPosition = 3;
		}else if(waterAnimationTickCounter < 21*animationTimer){
			waterAnimationTickPosition = 2;
		}else if(waterAnimationTickCounter < 22*animationTimer){
			waterAnimationTickPosition = 1;
		}else{
			waterAnimationTickCounter = 0;
		}
		
		flowerAnimationTickCounter++;
		if(flowerAnimationTickCounter < 4*animationTimer){
			flowerAnimationTickPosition = 0;
		}else if(flowerAnimationTickCounter < 8*animationTimer){
			flowerAnimationTickPosition = 1;
		}else{
			flowerAnimationTickCounter = 0;
		}
		//System.out.println(animationTickCounter);
	}
	
	public int getWaterAnimationTickPosition(){
		return waterAnimationTickPosition;
	}
	
	public int getFlowerAnimationTickPosition(){
		return flowerAnimationTickPosition;
	}
	
	
	public void setTransitioning(){
		transitioning = true;
	}
	public boolean isTransitioning(){
		return transitioning;
	}
	
	
	public void renderMapTransition(Screen screen, PlayerCharacter mc){
		int argb, r, g, b, rPlus, gPlus, bPlus;
		int transitionCounter;
		int target, rTarget, gTarget, bTarget;
		int xPos = screen.xOffset;
		int yPos = screen.yOffset;
		
		//iterate over the whole screen
		if(this.transitionCounter > (2*transitionCounterEndPoint)/3){
			transitionCounter = this.transitionCounter - (2*transitionCounterEndPoint)/3;
			for(int y=0; y<screen.height; y++){
				for(int x=0; x<screen.width; x++){
					argb = screen.pixels[(x) + (y) * screen.width];		//got argb of current pixel
					r = (argb>>16)&0xFF;								//r of argb
					g = (argb>>8)&0xFF;									//g of argb
					b = (argb>>0)&0xFF;									//b of argb
					
					rPlus = (255-r)/transitionCounter;					//increment values towards white
					gPlus = (255-g)/transitionCounter;
					bPlus = (255-b)/transitionCounter;
					
					
					if(rPlus == 0 && r < 255)							//if almost white just jump there
						rPlus = (255-r);
					if(gPlus == 0 && g < 255)
						gPlus = (255-g);
					if(bPlus == 0 && b < 255)
						bPlus = (255-b);
	
					if(r < 255)											//if already white don't overflow, else increment up towards white
						argb += (rPlus<<16);
					
					if(g < 255)
						argb += (gPlus<<8);
		
					if(b < 255)
						argb += bPlus;	
					
					screen.pixels[(x) + (y) * screen.width] = argb;		//update pixel to new argb value
				}
			}
		}else if(this.transitionCounter < (transitionCounterEndPoint)/3){	//Fade-in to the new map					
			//this is current map so use mPixels for target colors
			transitionCounter = this.transitionCounter;
			
			for(int y=0; y<screen.height; y++){
				for(int x=0; x<screen.width; x++){
					argb = screen.pixels[(x) + ((y) * screen.width)];		//get argb of current pixel
					r = (argb>>16)&0xFF;								//r of argb
					g = (argb>>8)&0xFF;									//g of argb
					b = (argb>>0)&0xFF;									//b of argb
					
					
					//If where outside of the defined range of the map set target to 0
					if((x + xPos < 0 || x + xPos >= width) || (y + yPos < 0 || y + yPos >= height)){
						target = 0x000000;	
					}else{//else set the target to the pixel at the given point of the map
						target = mPixels[(x+xPos) + ((y+yPos)*width)];
					}		
					
					//rgb variables for target
					rTarget = (target>>16)&0xFF;
					gTarget = (target>>8)&0xFF;
					bTarget = (target>>0)&0xFF;
					
					
					rPlus = (rTarget-r)/transitionCounter;					//increment values towards target
					gPlus = (gTarget-g)/transitionCounter;
					bPlus = (bTarget-b)/transitionCounter;
					
					
					if(rPlus == 0 && r > rTarget)							//if almost to target jump there
						rPlus = (rTarget-r);
					if(gPlus == 0 && g > gTarget)
						gPlus = (gTarget-g);
					if(bPlus == 0 && b > bTarget)
						bPlus = (bTarget-b);
	
					if(r > rTarget)											//if already the at the don't overflow, else increment up towards the target
						argb += (rPlus<<16);
					
					if(g > gTarget)
						argb += (gPlus<<8);
		
					if(b > bTarget)
						argb += bPlus;	
					
					screen.pixels[(x) + (y) * screen.width] = argb;		//update pixel to new argb value
				}
			}
			mc.render(screen);				//render mc over fade-in
											//Should figure out a way to do this such that mc renders only once as opposed to every transition tick <1/3EndPoint
		}
				
	}
	
	private int directionToxDir (int direction)
	{
		int xDir;
		
		if (direction == 1 || direction == 3)
		{
			xDir = 0;
		}
		else if (direction == 2)
		{
			xDir = -16;
		}
		else
		{
			xDir = 16;
		}
		return xDir;
	}
	
	private int directionToyDir (int direction)
	{
		int yDir;
		
		if (direction == 2 || direction == 4)
		{
			yDir = 0;
		}
		else if (direction == 1)
		{
			yDir = -16;
		}
		else
		{
			yDir = 16;
		}
		return yDir;
	}
	
	public void switchSongMainMap(int direction, PlayerCharacter mc)
	{
		int xDir = directionToxDir(direction);
		int yDir = directionToyDir(direction);
		
		int topLeftPixel = mpPixels[(mc.getxPos()+xDir + ((mc.getyPos()+yDir)*width))] & 0xffffff;
		
		switch (topLeftPixel)
		{
			case 0x808080:
			{
				Music.stopClip(AudioInit.route29);
				Music.startClip(AudioInit.newBarkTown);
				break;
			}
			case 0xB200FF:
			{
				Music.stopClip(AudioInit.newBarkTown);
				Music.startClip(AudioInit.route29);
				break;
			}
			case 0xC0C0C0:
			{
				Music.stopClip(AudioInit.route29);
				Music.startClip(AudioInit.cherryGrove);
				break;
			}
			case 0xFF006E:
			{
				Music.stopClip(AudioInit.cherryGrove);
				Music.startClip(AudioInit.route29);
				break;
			}
		}
	}
	
	public void switchSongMap(Game game, Screen screen, Map previousMap)
	{
		if (game.getMap().getMapPath() != "/worldMap.png")
		{
			Effect doorEnter = new Effect (AudioInit.effectPaths.get(2));
			doorEnter.start();
			
			if (game.getMap().getMapPath() == "/ElmsLabMap.png")
			{
				Music.stopClip(AudioInit.newBarkTown);
				Music.startClip(AudioInit.elmLab);
			}
			if (game.getMap().getMapPath() == "/PokeCenterMap.png")
			{
				Music.stopClip(AudioInit.cherryGrove);
				Music.startClip(AudioInit.pokeCenter);
			}
		}
		
		else if (game.getMap().getMapPath() == "/worldMap.png")
		{
			Effect doorLeave = new Effect (AudioInit.effectPaths.get(3));
			doorLeave.start();
			
			if (previousMap.getMapPath() == "/ElmsLabMap.png")
			{
				Music.stopClip(AudioInit.elmLab);
				Music.startClip(AudioInit.newBarkTown);
			}
			else if (previousMap.getMapPath() == "/PokeCenterMap.png")
			{
				Music.stopClip(AudioInit.pokeCenter);
				Music.startClip(AudioInit.cherryGrove);
			}
		}
	}
	
	public void playCollision()
	{
			Effect bump = new Effect (AudioInit.effectPaths.get(1));
			bump.start();
	}
	
	
	public int[] getPixels(){
		return mPixels;
	}
	
	public int[] getPointPixels(){
		return mpPixels;
	}

	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public String getMapPath()
	{
		return mapPath;
	}
	
}
