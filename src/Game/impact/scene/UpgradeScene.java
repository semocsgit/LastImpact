package Game.impact.scene;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.util.GLState;

import Game.impact.base.BaseScene;
import Game.impact.manager.SceneManager;
import Game.impact.manager.SceneManager.SceneType;

public class UpgradeScene extends BaseScene implements IOnMenuItemClickListener
{
	//---------------------------------------------
	// VARIABLES
	//---------------------------------------------
	
	private MenuScene menuChildScene;
	
	private final int LAUNCH = 0;
	private final int BOOST = 1;
	private final int SPAWN = 2;
	private final int DURA = 3;
	private final int MENU_BACK = 4;
	private Text upg;
	private Sprite u1;
	private Sprite u2;
	private Sprite u3;
	private Sprite u4;
	//---------------------------------------------
	// METHODS FROM SUPERCLASS
	//---------------------------------------------

	@Override
	public void createScene()
	{
		createBackground();
		createMenuChildScene();
		changeables();
	}

	@Override
	public void onBackKeyPressed()
	{
		SceneManager.getInstance().loadMenuSceneFromUpgrade(engine);
	}

	@Override
	public SceneType getSceneType()
	{
		return SceneType.SCENE_UPGRADE;
	}
	

	@Override
	public void disposeScene()
	{
		// TODO Auto-generated method stub
	}
	
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
	{
		switch(pMenuItem.getID())
		{	
			case MENU_BACK:
				SceneManager.getInstance().loadMenuSceneFromUpgrade(engine);
				return true;
			case LAUNCH:
				//Load Game Scene!
				if(resourcesManager.u1C == 0 && resourcesManager.upgradePoints >= 300)
				{
					resourcesManager.launchVelocity += 10;
					resourcesManager.u1Pos +=400;
					resourcesManager.u1C++;
					resourcesManager.upgradePoints -= 300;
					updateChangeable();
				}
				else if(resourcesManager.u1C == 1 && resourcesManager.upgradePoints >= 500)
				{
					resourcesManager.launchVelocity += 10;
					resourcesManager.u1Pos +=115;
					resourcesManager.u1C++;
					resourcesManager.upgradePoints -= 500;
					updateChangeable();
				}
				else if(resourcesManager.u1C == 2 && resourcesManager.upgradePoints >= 700)
				{
					resourcesManager.launchVelocity += 5;
					resourcesManager.u1Pos +=110;
					resourcesManager.u1C++;
					resourcesManager.upgradePoints -= 700;
					updateChangeable();
				}
				return true;
			case BOOST:
				if(resourcesManager.u2C == 0 && resourcesManager.upgradePoints >= 100)
				{
					resourcesManager.boostValue += 100;
					resourcesManager.u2Pos +=400;
					resourcesManager.u2C++;
					resourcesManager.upgradePoints -= 100;
					updateChangeable();
				}
				else if(resourcesManager.u2C == 1 && resourcesManager.upgradePoints >= 300)
				{
					resourcesManager.boostValue += 100;
					resourcesManager.u2Pos +=115;
					resourcesManager.u2C++;
					resourcesManager.upgradePoints -= 300;
					updateChangeable();
				}
				else if(resourcesManager.u2C == 2 && resourcesManager.upgradePoints >= 500)
				{
					resourcesManager.boostValue += 100;
					resourcesManager.u2Pos +=110;
					resourcesManager.u2C++;
					resourcesManager.upgradePoints -= 500;
					updateChangeable();
				}
				return true;
			case SPAWN:
				if(resourcesManager.u3C && resourcesManager.upgradePoints >= 600)
				{
					resourcesManager.spawnRate = 1;
					resourcesManager.u3Pos +=400;
					resourcesManager.u3C = false;
					resourcesManager.upgradePoints -= 600;
					updateChangeable();
				}
				return true;
			case DURA:
				if(resourcesManager.u4C == 0 && resourcesManager.upgradePoints >= 200)
				{
					resourcesManager.boostValue += 50;
					resourcesManager.u4Pos +=400;
					resourcesManager.u4C++;
					resourcesManager.upgradePoints -= 200;
					updateChangeable();
				}
				else if(resourcesManager.u4C == 1 && resourcesManager.upgradePoints >= 400)
				{
					resourcesManager.boostValue += 50;
					resourcesManager.u4Pos +=115;
					resourcesManager.u4C++;
					resourcesManager.upgradePoints -= 400;
					updateChangeable();
				}
				else if(resourcesManager.u4C == 2 && resourcesManager.upgradePoints >= 800)
				{
					resourcesManager.slowdown += 100;
					resourcesManager.u4Pos +=110;
					resourcesManager.u4C++;
					resourcesManager.upgradePoints -= 800;
					updateChangeable();
				}
				return true;
			default:
				return false;
		}
	}
	
	//---------------------------------------------
	// CLASS LOGIC
	//---------------------------------------------
	
	private void createBackground()
	{
		attachChild(new Sprite(340, 600, resourcesManager.upgrade_background_region, vbom)
		{
    		@Override
            protected void preDraw(GLState pGLState, Camera pCamera) 
    		{
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
		});
	}
	
	private void createMenuChildScene()
	{
		menuChildScene = new MenuScene(camera);
		menuChildScene.setPosition(340,600);
		
		final IMenuItem launchMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(LAUNCH, resourcesManager.upgradeButton_region, vbom), .6f, .5f);
		final IMenuItem boostMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(BOOST, resourcesManager.upgradeButton_region , vbom), .6f, .5f);
		final IMenuItem spawnMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(SPAWN, resourcesManager.upgradeButton_region , vbom), .6f, .5f);
		final IMenuItem durabilityMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(DURA, resourcesManager.upgradeButton_region , vbom), .6f, .5f);
		final IMenuItem backButton = new SpriteMenuItem(MENU_BACK, resourcesManager.backButton_region, vbom);
		menuChildScene.addMenuItem(launchMenuItem);
		menuChildScene.addMenuItem(boostMenuItem);
		menuChildScene.addMenuItem(spawnMenuItem);
		menuChildScene.addMenuItem(durabilityMenuItem);
		menuChildScene.addMenuItem(backButton);
		
		menuChildScene.buildAnimations();
		menuChildScene.setBackgroundEnabled(false);
		
		launchMenuItem.setPosition(-150, 300);
		boostMenuItem.setPosition(-150, 150);
		spawnMenuItem.setPosition(-150, -100);
		durabilityMenuItem.setPosition(-150, -200);
		backButton.setPosition(245, -500);
		menuChildScene.setOnMenuItemClickListener(this);
		
		setChildScene(menuChildScene);
	}
	
	private void changeables()
	{	
		upg = new Text(340, 300, resourcesManager.font, "Upgrade Points = " + resourcesManager.upgradePoints, vbom);
		u1 = new Sprite(300 + resourcesManager.u1Pos, 990, resourcesManager.u1_region, vbom);
		u2 = new Sprite(300 + resourcesManager.u2Pos, 810, resourcesManager.u1_region, vbom);
		u3 = new Sprite(300 + resourcesManager.u3Pos, 655, resourcesManager.u1_region, vbom);
		u4 = new Sprite(300 + resourcesManager.u4Pos, 475, resourcesManager.u1_region, vbom);
		attachChild(u1);
		attachChild(u2);
		attachChild(u3);
		attachChild(u4);
		attachChild(upg);
	}
	
	private void updateChangeable()
	{
		upg.detachSelf();
		u1.detachSelf();
		u2.detachSelf();
		u3.detachSelf();
		u4.detachSelf();
		
		changeables();
	}
}
