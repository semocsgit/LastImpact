package Game.impact.scene;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;


import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;


import Game.impact.GameActivity;
import Game.impact.base.BaseScene;
import Game.impact.extras.LevelCompleteWindow;
import Game.impact.extras.LevelCompleteWindow.StarsCount;
import Game.impact.manager.ResourcesManager;
import Game.impact.manager.SceneManager;
import Game.impact.manager.SceneManager.SceneType;
import Game.impact.object.Enemy;
import Game.impact.object.Player;


public class GameScene extends BaseScene implements IOnSceneTouchListener, SensorEventListener, IOnMenuItemClickListener
{
	private int score = 0;
	private int l;
	private int b;
	private int s;
	
	private MenuScene menuChildScene;
	
	private final int MENU_PLAY = 0;
	private final int MENU_UPGRADE = 1;
	
	private HUD gameHUD;
	private Text scoreText;
	private PhysicsWorld physicsWorld;
	private LevelCompleteWindow levelCompleteWindow;
	
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVELBOUND = "levelBound";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BIRD = "bird";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN = "coin";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE = "levelComplete";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ROTATOR = "rotator";
	
	public static Player player;
	public AnimatedSprite explosion;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	public float TILT;
	private boolean gameOverDisplayed = false;
	private Text calcEndVelocity;
	
	private boolean firstTouch = false;
	private float pX,pY;
	
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
	{
		switch(pMenuItem.getID())
		{
		case MENU_PLAY:
			SceneManager.getInstance().loadGameScene(engine);
			return true;
		case MENU_UPGRADE:
			SceneManager.getInstance().loadUpgradeScene(engine);
			default:
				return false;
		}
	}
	
	private void createMenuChildScene()
	{
		menuChildScene = new MenuScene(camera);
		menuChildScene.setPosition(340,600);
		final IMenuItem playMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_PLAY, resourcesManager.play_region, vbom), 1.2f, 1);
		final IMenuItem upgradeMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_UPGRADE, resourcesManager.options_region, vbom), 1.2f, 1);
		menuChildScene.addMenuItem(playMenuItem);
		menuChildScene.addMenuItem(upgradeMenuItem);
		menuChildScene.buildAnimations();
		menuChildScene.setBackgroundEnabled(true);
		playMenuItem.setPosition(50, +55);
		upgradeMenuItem.setPosition(50, -55);
		menuChildScene.setOnMenuItemClickListener(this);
		setChildScene(menuChildScene);
		
	}
		
	public void destroyPhysicsWorld()
	{
	    engine.runOnUpdateThread(new Runnable()
	    {
	        public void run()
	        {
	            Iterator<Body> localIterator = physicsWorld.getBodies();
	            while (true)
	            {
	                if (!localIterator.hasNext())
	                {
	                    physicsWorld.clearForces();
	                    physicsWorld.clearPhysicsConnectors();
	                    physicsWorld.reset();
	                    physicsWorld.dispose();
	                    System.gc();
	                    return;
	                }
	                try
	                {
	                    final Body localBody = (Body) localIterator.next();
	                    GameScene.this.physicsWorld.destroyBody(localBody);
	                } 
	                catch (Exception localException)
	                {
	                    Debug.e(localException);
	                }
	            }
	        }
	    });
	}
	
	@Override
	public void createScene()
	{
		l = resourcesManager.launchVelocity;
		b = resourcesManager.boostValue;
		s = resourcesManager.slowdown;
		
		createBackground();
		createHUD();
		createPhysics();
		loadLevel(1);
		levelCompleteWindow = new LevelCompleteWindow(vbom);
		
		mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

		if (mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) 
		{
			mAccelerometer = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		}
		
		setOnSceneTouchListener(this); 
		
	}

	@Override
	public void onBackKeyPressed()
	{
		SceneManager.getInstance().loadMenuScene(engine);
	}

	@Override
	public SceneType getSceneType()
	{
		return SceneType.SCENE_GAME;
	}

	@Override
	public void disposeScene()
	{
		camera.setHUD(null);
		camera.setChaseEntity(null); //TODO
		camera.setCenter(400, 240);
		destroyPhysicsWorld();
		// TODO code responsible for disposing scene
		// removing all game scene objects.
	}
	
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
	{
		if (pSceneTouchEvent.isActionUp())
		{
			if (!firstTouch)
			{
				player.launch(l);
				firstTouch = true;
			}
		}
		return false;
	}
	
	private void loadLevel(int levelID)
	{
		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
		{
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
			{
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				
				camera.setBounds(0, 0, width, height); 
				camera.setBoundsEnabled(true);

				return GameScene.this;
			}
		});
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
		{
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
			{
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
				
				final Sprite levelObject;
				
				if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
				{
					levelObject = new Sprite(x, y, ResourcesManager.platform1_region, vbom);
					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
				} 
				
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BIRD))
				{	
					Sprite sprite = new Sprite(x,y,ResourcesManager.bird_region, vbom);
					levelObject = new Enemy(sprite, vbom, physicsWorld)
					{
						@Override
						protected void onManagedUpdate(float pSecondsElapsed)
						{
							super.onManagedUpdate(pSecondsElapsed);

							if(player.collidesWith(this))
							{	
								pX=getX();
								pY=getY();
								createExplosion(pX,pY);
								this.setVisible(false);
								this.setIgnoreUpdate(true);
								player.setVelocityY(b);
							}
							
						}
					};
				} 
				
				
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
				{	
					Sprite sprite = new Sprite(x,y,ResourcesManager.platform2_region,vbom);
					levelObject = new Enemy(sprite, vbom, physicsWorld)
					{	
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);
							
							if (player.collidesWith(this))
							{	
								pX=getX();
								pY=getY();
								createExplosion(pX,pY);
								addToScore(15);
								this.setVisible(false);
								this.setIgnoreUpdate(true);
								if(player.getBody().getLinearVelocity().y > 0)
									player.setVelocityY(s);
								detachChild(explosion);
							}
						}
					};
					
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVELBOUND))
				{
					Sprite sprite = new Sprite(x,y,ResourcesManager.levelBound_region, vbom);
					levelObject = new Enemy( sprite, vbom, physicsWorld)
					{	
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);
							
							if (player.collidesWith(this))
							{	
							}
						}
					};
	
				} 
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3))
				{
					levelObject = new Sprite(x, y, ResourcesManager.platform3_region, vbom)
					{	
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);

							if (player.collidesWith(this))
							{
								GameActivity.explosion3.play();
								addToScore(15);
								this.setVisible(false);
								this.setIgnoreUpdate(true);
								if(player.getBody().getLinearVelocity().y > 0)
									player.setVelocityY(s);
							}
						}
					};
				}
				
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN))
				{	
					
					levelObject = new Sprite(x, y, resourcesManager.coin_region, vbom)
					{
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);

							if (player.collidesWith(this))
							{
								addToScore(10);
								this.setVisible(false);
								this.setIgnoreUpdate(true);
								if(player.getBody().getLinearVelocity().y > 0)
									player.setVelocityY(s);
							}
						}
					};
					levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
				}	
				
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
				{
					player = new Player(x, y, vbom, camera, physicsWorld)
					{
						@Override
						public void onDie()
						{	
							player.stopAnimation();
							if (!gameOverDisplayed)
							{	
								createGameOverText();
								displayGameOverText();
								player.getBody().setActive(false);
								createMenuChildScene();
							}
						}
					};
					levelObject = player;
					player.setLaunch(false);
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ROTATOR))  // to do od dodoo
				{
					levelObject = new Sprite(x, y, resourcesManager.rotator_region, vbom)
					{
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);

							if (player.collidesWith(this))
							{
								this.setVisible(false);
								this.setIgnoreUpdate(true);
								if(player.getBody().getLinearVelocity().y > 0)
									player.setVelocityY(s);
							}
						}
					};
					levelObject.registerEntityModifier(new LoopEntityModifier(new RotationModifier(6,180,0)));
				}	
				// ANTIQUDATED-------------------
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE))
				{
					levelObject = new Sprite(x, y, resourcesManager.complete_stars_region, vbom)
					{
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);

							if (player.collidesWith(this))
							{
								levelCompleteWindow.display(StarsCount.TWO, GameScene.this, camera);
								this.setVisible(false);
								this.setIgnoreUpdate(true);
							}
						}
					};
					levelObject.registerEntityModifier(new LoopEntityModifier(new RotationModifier(254,180,0)));
				}// end-----------------------------
				else
				{
					throw new IllegalArgumentException();
				}

				levelObject.setCullingEnabled(true);

				return levelObject;
			}
		});
		
		try
		{
			FileInputStream in = activity.openFileInput("" + levelID + ".lvl"); //reads a file created by Generator
			levelLoader.loadLevelFromStream(in);
			in.close();
		}catch(Exception e)
		{
			System.out.println("Failed to open file");
		}
		
		//levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
	}
	
	private void createGameOverText()
	{  
		calcEndVelocity= new Text(140, 540, resourcesManager.font, "Velocity: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
		calcEndVelocity.setText("Velocity: " + player.getFinalVelocity());
		calcEndVelocity.setSize(250, 50);
	}
	
	private void displayGameOverText()
	{
		camera.setChaseEntity(null);
		scoreText.setAnchorCenter(0, 0);	
		attachChild(calcEndVelocity);
		gameOverDisplayed = true;
		resourcesManager.upgradePoints+=score;
	}
	
	private void createHUD()
	{
		gameHUD = new HUD();
		scoreText = new Text(20, 420, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
		scoreText.setAnchorCenter(0, 0);	
		scoreText.setText("Score: 0");
		gameHUD.attachChild(scoreText);
		
		camera.setHUD(gameHUD);
	}
	
	public int getScore()
	{
		return score;
	}
	
	private void addToScore(int i)
	{
		score += i;
		scoreText.setText("Score: " + score);
	}
	
	private void createBackground()
	{
		setBackground(new Background(Color.BLUE));
	}
	
	private void createExplosion(float pX, float pY)
	{
		GameActivity.explosion1.play();

		explosion = new AnimatedSprite(pX,pY, ResourcesManager.explosion_region, vbom);
		explosion.animate(new long[] {55,55,55,55}, 0, 3, false);
		explosion.registerEntityModifier(new ScaleModifier(3, 1, 2.5f));
		explosion.
		attachChild(explosion);
		explosion.registerUpdateHandler(new IUpdateHandler()
		{

	        @Override
	        public void onUpdate(float pSecondsElapsed)
	        {
	            if(explosion.getCurrentTileIndex() == 3)
	            {
	                explosion.setVisible(false);
	                explosion.unregisterUpdateHandler(this);
	            }
	        }

			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	private void createPhysics()
	{
		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -5), false); 
		physicsWorld.setContactListener(contactListener());
		registerUpdateHandler(physicsWorld);
	}
	
	// ---------------------------------------------
	// INTERNAL CLASSES
	// ---------------------------------------------
	
	private ContactListener contactListener()
	{	
		ContactListener contactListener = new ContactListener()
		{

			public void beginContact(Contact contact)
			{
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();
				
				if (firstTouch)
				{
					if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
					{   
						if (x1.getBody().getUserData().equals("platform1") && x2.getBody().getUserData().equals("player"))
						{	
							player.setFinalVelocity(player.getBody().getLinearVelocity().y);
							player.setVelocityX(0);
							player.onDie();
							player.setLaunch(false);
						}
					}
				}
			}

			public void endContact(Contact contact)
			{
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
				{
					if (x1.getBody().getUserData().equals("player") && !(x2.getBody().getUserData().equals("platform1") || x2.getBody().getUserData().equals("levelBound")));
						
					
				}
			}

			public void preSolve(Contact contact, Manifold oldManifold)
			{	
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();
				
				if (x1.getBody().getUserData().equals("player") && !((x2.getBody().getUserData().equals("platform1") || x2.getBody().getUserData().equals("levelBound")) ))
				{			
					if(!(x2.getBody().getUserData().equals("levelBound")))
						x2.getBody().setActive(false);
					
				}
			}

			public void postSolve(Contact contact, ContactImpulse impulse)
			{

				
			}
		};
		
		return contactListener;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{	
		TILT = event.values[0];
		player.setVelocityX(-TILT*5);
	}
	
}