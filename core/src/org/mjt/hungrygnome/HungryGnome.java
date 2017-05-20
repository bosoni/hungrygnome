/*
 Hungry Gnome
 (c) mjt 2014

 kerätään pipareita ja pitää varoa vihollisia.
 kun kaikki piparit kerätty, oikeelle puolelle karttaa tulee
 pitkät tikapuut yläs ja sinne ku menee, kenttä vaihtuu:
 kentästä tulee pitempi, enemmän pipareita kerättävänä ja enemmän vihollisia.

 käytetään kolmiulotteista taulukkoa [layers][x][y]
 *** kaikein kauimpana o joku tausta mut se ei tuu toho taulukkoon
 * 0: talot + katto
 * 1: tikapuut (tämä layer tsekataan kun painetaan YLÖS/ALAS)
 * 2: maa-palat (tämä layer tsekataan joka framella että tippuuko,voiko liikkua ym)
 *** nyt piirretään gnome ja viholliset, ei tule taulukkon
 * 3: lumikinokset
 * 4: puskat
 * 5: piparit
 *** piirretään lumihiutaleita, ei tule taulukkoon

 */
package org.mjt.hungrygnome;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HungryGnome extends Game
{
	public static HungryGnome inst;

	public static boolean DESKTOP;
	private String PLATFORM;
	public float screenWidth, screenHeight;
	public float speed;
	public float fallSpeed, jumpSpeed;
	public int mapWidth = 0, mapHeight = 20;
	public int energy = 50;
	private final int SNOWFLAKES = 500;
	private int gingerbreadsMax = 0, childrenMax = 0;

	public Music sounds[] = new Music[10];

	// layers
	public static final int L_HOUSE = 0, L_LADDER = 1, L_GROUND = 2, L_SNOW = 3, L_BUSH = 4, L_GINGERBREAD = 5;
	public byte[][][] map; // layers, x,y

	public static final int I_arrow = 0, I_bg = 1, I_block1 = 2, I_block2 = 3, I_block3 = 4, I_block4 = 5, I_bush1 = 6,
			I_control1 = 7, I_control2 = 8, I_energy = 9, I_gingerbread = 10, I_ladder = 11, I_mystery = 12,
			I_playagain = 13, I_roof = 14, I_snow1 = 15, I_snow2 = 16, I_snow3 = 17, I_snow4 = 18;

	public float blockSize;

	private int level = 1;
	private BitmapFont font;

	private Camera camera;
	private Viewport viewport;
	public SpriteBatch batch;

	private TextureAtlas textureAtlas = null;
	private Array<Sprite> sprites = new Array<Sprite>();
	private Array<Sprite> animSprites = new Array<Sprite>();

	private final SnowFlake[] snowFlake = new SnowFlake[SNOWFLAKES];

	private int gingerbreadsFound = 0;
	public final Character player = new Character();
	private final Array<Character> child = new Array<Character>();

	private Sprite mainMenu, check;
	private int mode = 0;
	private float joystickX, joystickY;
	public int flash = 0;

	public HungryGnome()
	{
		Log("Hungry Gnome  v0.2 (c) mjt, 2014");
	}

	@Override
	public void create()
	{
		inst = this;

                DESKTOP = Gdx.app.getType() == ApplicationType.Desktop;

		if (DESKTOP)
		{
			PLATFORM = "desktop";
			speed = 15;
			fallSpeed = 10;
			jumpSpeed = 12;
			blockSize = 64;
		} else
		{
			PLATFORM = "android";
			final float MUL = 0.7f;
			speed = 10f * MUL;
			fallSpeed = 10f * MUL;
			jumpSpeed = 12f * MUL;
			blockSize = 64 / 2;
		}                
                
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		viewport = new ScreenViewport(camera);

		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/default.fnt"));

		/*
		sounds[0] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Gun_Cocking_Slow-Mike_Koenig-1019236976.wav"));
		sounds[1] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Jump-SoundBible.com-1007297584.wav"));
		sounds[2] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Realistic_Punch-Mark_DiAngelo-1609462330.wav"));
		sounds[3] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Shells_falls-Marcel-829263474.wav"));
		sounds[4] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Sleigh Bells Ringing-SoundBible.com-1890102065.wav"));
		sounds[5] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Zombie In Pain-SoundBible.com-134322253.wav"));
		 */
		sounds[0] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Gun_Cocking_Slow-Mike_Koenig-1019236976.ogg"));
		sounds[1] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Jump-SoundBible.com-1007297584.ogg"));
		sounds[2] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Realistic_Punch-Mark_DiAngelo-1609462330.ogg"));
		sounds[3] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Shells_falls-Marcel-829263474.ogg"));
		sounds[4] = Gdx.audio
				.newMusic(Gdx.files.internal("data/audio/Sleigh Bells Ringing-SoundBible.com-1890102065.ogg"));
		sounds[5] = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Zombie In Pain-SoundBible.com-134322253.ogg"));

		for (int q = 0; q < 6; q++)
			if (sounds[q] == null)
				Log("Error loading sound: " + q);

		if (sounds[4] != null)
		{
			sounds[4].play();
		}

		for (int c = 0; c < SNOWFLAKES; c++)
		{
			snowFlake[c] = new SnowFlake();
			snowFlake[c].create(screenWidth, screenWidth, true);
		}

		if (DESKTOP)
			mainMenu = new Sprite(new Texture(Gdx.files.internal("data/desktop/mainbg.png")));
		else
		{
			mainMenu = new Sprite(new Texture(Gdx.files.internal("data/android/mainbg.png")));
			check = new Sprite(new Texture(Gdx.files.internal("data/android/check.png")));
		}

		mode = 1;
	}

	void createLevel()
	{
		child.clear();
		energy += 20;
		mapWidth += 50;
		gingerbreadsFound = 0;
		gingerbreadsMax += 5;
		childrenMax += 5;
		map = new byte[6][mapWidth][mapHeight + 5]; // layers, x,y

		// lataa vain kerran.
		// vois käyttää assetManageria mutten tiä miten..
		if (textureAtlas == null)
		{
			textureAtlas = new TextureAtlas("data/" + PLATFORM + "/map_anim.txt");

			String names[] =
			{ "arrow", "bg", "block1", "block2", "block3", "block4", "bush1", "control1", "control2", "energy",
					"gingerbread", "ladder", "mystery", "playagain", "roof", "snow1", "snow2", "snow3", "snow4" };

			// lisää karttapalikat  sprites  listaan
			for (int q = 0; q < names.length; q++)
				sprites.add(textureAtlas.createSprite(names[q]));

			// lisää animaatiot  animSprites  listaan
			for (int q = 0; q < 10; q++)
			{
				Sprite s = textureAtlas.createSprite("c00" + q);
				if (s == null)
					continue;
				animSprites.add(s);
			}
			for (int q = 0; q < 10; q++)
			{
				Sprite s = textureAtlas.createSprite("g00" + q);
				if (s == null)
					continue;
				animSprites.add(s);
			}
		}

		player.pos.set(2 * blockSize - 5, 4 * blockSize);

		generateMap();
	}

	void generateMap()
	{
		// "seinät"
		for (int y = 0; y < mapHeight + 5; y++)
		{
			map[L_GROUND][0][y] = I_block3;
			map[L_GROUND][mapWidth - 1][y] = I_block3;
		}
		// maapalikat
		int x, y = 0;
		for (x = 1; x < mapWidth - 1; x++)
		{
			// täytetään maa
			for (int yy = 0; yy < y; yy++)
			{
				map[L_GROUND][x][yy] = I_block3;
			}

			// randomilla maatason block1 tai block2
			if (Math.random() < 0.5)
			{
				map[L_GROUND][x][y] = I_block1;
			} else
			{
				map[L_GROUND][x][y] = I_block2;
			}
			map[L_SNOW][x][y + 1] = I_snow1; // kinos

			if (Math.random() < 0.5f)
			{
				// nostetaanko vai lasketaanko maatasoa
				if (Math.random() < 0.6)
				{
					if (y > 0) // lasketaan jos voidaan
					{
						y--;
					}
				} else
				{
					// nostetaan
					if (y < mapHeight / 2)
					{
						// tämä siksi ettei tule 1 blokin koloja koska niihin ei voi pudota (tyyppi lentää silloin)
						if (map[L_GROUND][x - 1][y + 1] == 0)
						{
							y++;
						}
					}
				}
			}
		}

		// kinoksien loppu+alku
		for (x = 2; x < mapWidth - 1; x++)
		{
			if (Math.random() < 0.6)
			{
				for (y = 1; y < mapHeight; y++)
				{
					boolean ok = true;
					for (int c = 0; c < 4; c++)
					{
						// tsekkaa että kinoksia on (vähintään) 4 samalla tasolla
						if (map[L_SNOW][x - 2 + c][y] != I_snow1)
						{
							ok = false;
							break;
						}
					}
					if (ok)
					{
						map[L_SNOW][x][y] = I_snow3; // hiekkaa
						map[L_SNOW][x - 1][y] = I_snow2; // hiekkaa
					}

				}
			}
		}

		// puskat
		for (x = 2; x < mapWidth; x++)
		{
			if (Math.random() < 0.3)
			{
				for (y = 1; y < mapHeight; y++)
				{
					if (map[L_SNOW][x][y] == I_snow1)
					{
						map[L_BUSH][x][y] = I_bush1; // puska
						break;
					}
				}
			}
		}

		// talot
		for (x = 1; x < mapWidth - 6; x++)
		{
			int rw = (int) ((Math.random() * 2) + 3);
			int rh = (int) ((Math.random() * 2) + 3);
			for (y = 1; y < mapHeight - 6; y++)
			{
				boolean ok = true;
				for (int xx = x; xx < x + rw; xx++)
				{
					if (map[L_GROUND][xx][y - 1] != I_block1 && map[L_GROUND][xx][y - 1] != I_block2)
					{
						ok = false;
						break;
					}
				}
				if (!ok)
				{
					continue;
				}

				for (int xxx = 0; xxx < rw; xxx++)
				{
					for (int yyy = 0; yyy < rh; yyy++)
					{
						map[L_HOUSE][x + xxx][y + yyy] = I_block4; // talon seinä
					}
				}

				// katto
				for (int xxx = 0; xxx < rw; xxx++)
				{
					map[L_HOUSE][x + xxx][y + rh] = I_roof; // katto
				}

				// tikapuut
				int add = (int) (Math.random() * 3);
				for (int yyy = 0; yyy < rh + 1; yyy++)
				{
					map[L_LADDER][x + add][y + yyy] = I_ladder; // tikapuut
				}

				x += rw + 1;
				break;
			}
		}

		// laita piparit ja viholliset karttaan
		int gingerbreadCount = 0, childCount = 0, mysteryCount = 0, energyCount = 0;
		while (true)
		{
			for (x = 1; x < mapWidth - 2; x++)
			{
				for (y = mapHeight - 1; y > 1; y--)
				{
					if (map[L_HOUSE][x][y] == I_roof || // katto
							map[L_GROUND][x][y] == I_block1 || map[L_GROUND][x][y] == I_block2) // maapalat
					{
						if (energyCount < 1 && map[L_GINGERBREAD][x][y + 1] == 0 && Math.random() < 0.05)
						{
							map[L_GINGERBREAD][x][y + 1] = I_energy; // energy
							energyCount++;
							break;
						}

						if (gingerbreadCount < gingerbreadsMax && map[L_GINGERBREAD][x][y + 1] == 0
								&& Math.random() < 0.1)
						{
							map[L_GINGERBREAD][x][y + 1] = I_gingerbread; // pipari
							gingerbreadCount++;
							break;
						}
						if (mysteryCount < (level / 2) + 1 && map[L_GINGERBREAD][x][y + 1] == 0 && Math.random() < 0.1)
						{
							map[L_GINGERBREAD][x][y + 1] = I_mystery; // mystery pullo
							mysteryCount++;
						}
						if (x > 6 && childCount < childrenMax && Math.random() < 0.1)
						{
							Character ch = new Character();
							ch.pos.set(x * blockSize, (y + 1) * blockSize);
							child.add(ch);
							childCount++;
							break;
						}
					}
				}
			}
			if (energyCount == 1 && gingerbreadCount == gingerbreadsMax && childCount == childrenMax
					&& mysteryCount >= (level / 2) + 1)
			{
				break;
			}
		}
	}

	void renderMap()
	{
		float posX, posY;
		int layer;
		float sx = screenWidth / 2 - player.pos.x;
		float sy = screenHeight / 2 - player.pos.y;

		for (layer = 0; layer < 6; layer++)
		{
			posX = 0;
			for (int x = 0; x < mapWidth; x++)
			{
				posY = 0;
				for (int y = 0; y < mapHeight; y++)
				{
					int mp = map[layer][x][y];
					if (mp == 0)
					{
						posY += blockSize;
						continue;
					}
					if (sx + posX >= -blockSize && sy + posY >= -blockSize && sx + posX < screenWidth
							&& sy + posY < screenHeight)
					{
						// konvataan intiks niin ei tule rakoja tileihin
						batch.draw(sprites.get(mp), (int) (sx + posX), (int) (sy + posY));
					}
					posY += blockSize;
				}
				posX += blockSize;
			}
		}

		if (energy > 0)
		{
			int frame = (int) player.frame;
			frame %= 8;
			frame += 8;

			if (player.moveToRight)
			{
				if (animSprites.get(frame).isFlipX() == true)
					animSprites.get(frame).flip(true, false);
			} else
			{
				if (animSprites.get(frame).isFlipX() == false)
					animSprites.get(frame).flip(true, false);
			}
			final float down, left;
			if (DESKTOP)
			{
				down = 16;
				left = 16;
			} else
			{
				down = 8;
				left = 8;
			}
			animSprites.get(frame).setPosition(screenWidth / 2 - left, screenHeight / 2 - down);
			animSprites.get(frame).draw(batch);
		}

		for (Character ch : child)
		{
			int frame = (int) ch.frame;
			frame %= 8;

			if (ch.moveToRight)
			{
				if (animSprites.get(frame).isFlipX() == true)
					animSprites.get(frame).flip(true, false);
			} else
			{
				if (animSprites.get(frame).isFlipX() == false)
					animSprites.get(frame).flip(true, false);
			}
			final float down, left;
			if (DESKTOP)
			{
				down = 16;
				left = 16;
			} else
			{
				down = 8;
				left = 8;
			}
			animSprites.get(frame).setPosition(sx + ch.pos.x - left, sy + ch.pos.y - down);
			animSprites.get(frame).draw(batch);
		}
	}

	@Override
	public void dispose()
	{
		batch.dispose();
	}

	@Override
	public void render()
	{
		if (Gdx.input.isKeyPressed(Keys.ESCAPE))
		{
			Gdx.app.exit();
		}

		// alkukuva (tulee vain kerran)
		if (mode == 1)
		{
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.draw(mainMenu, 0, 0, screenWidth, screenHeight); // background
			if (DESKTOP == false)
				if (PLATFORM.equals("desktop") == false) // small images
				{
					batch.draw(check, 70f / 512f * screenWidth, 300f / 512f * screenHeight, 64, 64);
				} else
				{
					batch.draw(check, 310f / 512f * screenWidth, 300f / 512f * screenHeight, 64, 64);
				}

			batch.end();

			if (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER))
			{
				mainMenu = null;
				mode = 2;
			}

			if (Gdx.input.isTouched())
			{
				float mmx = Gdx.input.getX() / screenWidth, mmy = (Gdx.input.getY()) / screenHeight;
				// muuta koordinaatit 0.0 - 1.0 alueelle.
				// jos painetaan "Play"
				if (mmx > 143f / 512f && mmx < (143f + 222f) / 512f && mmy > 345f / 512f && mmy < (345 + 81f) / 512f)
				{
					mainMenu = null;
					check = null;
					mode = 2;
				} else if (DESKTOP == false) // androidilla voi valita pienet tai isot kuvat
				{
					if (mmx > 24f / 512f && mmx < (24f + 226f) / 512f && mmy > 192f / 512f && mmy < (191f + 62f) / 512f) // SMALL IMAGES
					{
						// muuta arvot puolet pienemmiksi koska käytetään puolet pienempiä kuvia
						PLATFORM = "android";
						final float MUL = 0.7f;
						speed = 10f * MUL;
						fallSpeed = 10f * MUL;
						jumpSpeed = 12f * MUL;
						blockSize = 64 / 2;
						if (sounds[2] != null)
							sounds[2].play();
					}

					else if (mmx > 265 / 512f && mmx < (265 + 226) / 512f && mmy > 192 / 512f
							&& mmy < (191 + 62) / 512f) // BIG IMAGES
					{
						PLATFORM = "desktop";
						speed = 10;
						fallSpeed = 10;
						jumpSpeed = 12;
						blockSize = 64;
						if (sounds[2] != null)
							sounds[2].play();
					}
				}
			}
			return;
		}
		if (mode == 2) // pelin aloitus
		{
			createLevel();
			mode = 999;
			if (sounds[4] != null) // poista alkukuvaääni muistista
			{
				sounds[4].stop();
				sounds[4].dispose();
				sounds[4] = null;
			}
		}
		//-------------------------------------------------------------------------

		if (gingerbreadsFound == gingerbreadsMax)
		{
			int mx = (int) ((player.pos.x + blockSize / 2) / blockSize);
			int my = (int) ((player.pos.y) / blockSize);
			if (mx >= mapWidth - 2 && my >= mapHeight - 2)
			{
				level++;
				createLevel();
			}
		}

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (energy > 0)
		{
			float spd = speed * Gdx.graphics.getDeltaTime() * 20;
			spd *= player.mysterySpeedMul;

			if (Gdx.input.isKeyPressed(Keys.LEFT))
			{
				if (player.canMove(-spd, 0))
				{
					player.pos.x -= spd;
					player.frame += Math.abs(spd) * 0.1f;

					player.moveToRight = false;
				}
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT))
			{
				if (player.canMove(spd, 0))
				{
					player.pos.x += spd;
					player.frame += Math.abs(spd) * 0.1f;

					player.moveToRight = true;
				}
			}
			if (Gdx.input.isKeyPressed(Keys.UP))
			{
				if (player.canMove(0, spd))
				{
					player.pos.y += spd;
					player.frame += Math.abs(spd) * 0.1f;
				}
			}
			if (Gdx.input.isKeyPressed(Keys.DOWN))
			{
				if (player.canMove(0, -spd))
				{
					player.pos.y -= spd;
					player.frame += Math.abs(spd) * 0.1f;
				}
			}
			if (Gdx.input.isKeyJustPressed(Keys.SPACE))
			{
				player.jump();
				player.frame = 0;
				if (sounds[1] != null)
				{
					sounds[1].setVolume(0.2f);
					sounds[1].play();
				}
			}

			// androidilla tarkistetaan joystick
			if (DESKTOP == false)
			{
				float AREASIZE = sprites.get(I_control1).getWidth();
				joystickX = joystickY = AREASIZE / 2;

				for (int touch = 0; touch < 2; touch++)
				{
					if (Gdx.input.isTouched(touch))
					{
						float xx = Gdx.input.getX(touch);
						float yy = screenHeight - Gdx.input.getY(touch);

						if (xx > screenWidth - AREASIZE && yy < AREASIZE) // jump
						{
							player.jump();
							if (sounds[1] != null)
							{
								sounds[1].setVolume(0.7f);
								sounds[1].play();
							}
						} else if (xx < AREASIZE + 50 && yy < AREASIZE + 50) // joystick
						{
							if (xx < 0)
								xx = 0;
							if (yy < 0)
								yy = 0;
							if (xx > AREASIZE)
								xx = AREASIZE;
							if (yy > AREASIZE)
								yy = AREASIZE;

							joystickX = xx;
							joystickY = yy;

							//TODO  laske etäisyys (tsekataan että ollaan ympyrän muotoisen napin sisällä)
							//if (Math.sqrt(xx * xx + yy * yy) < AREASIZE / 2)
							{

								xx /= AREASIZE; // 0.0 -> 1.0
								xx *= 2; // 0.0 -> 2.0
								xx -= 1; // -1  -> 1
								xx *= speed * player.mysterySpeedMul;

								yy /= AREASIZE; // 0.0 -> 1.0
								yy *= 2; // 0.0 -> 2.0
								yy -= 1; // -1  -> 1
								yy *= speed * player.mysterySpeedMul;

								if (xx < 0)
									player.moveToRight = false;
								else if (xx > 0)
									player.moveToRight = true;

								if (player.canMove(xx, 0))
								{
									player.pos.x += xx;
									player.frame += Math.abs(xx) * 0.1f;
								}
								if (player.canMove(0, yy))
								{
									player.pos.y += yy;
									player.frame += Math.abs(xx) * 0.1f;
								}
							}
						}

					}
				}
			}
			player.update();
			if (player.gingerbreadTaken())
			{
				gingerbreadsFound++;

				if (gingerbreadsFound == gingerbreadsMax)
				{
					for (int yyy = 1; yyy < mapHeight - 1; yyy++)
					{
						map[L_LADDER][mapWidth - 2][yyy] = I_ladder; // tikapuut
					}
				}
			}
		} // energy > 0

		for (Character ch : child)
		{
			ch.updateAI();
			ch.update();
		}

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(sprites.get(I_bg), 0, 0, screenWidth, screenHeight); // background
		renderMap();
		if (flash != 0)
			flash++;
		if (flash >= 5)
			batch.setColor(Color.WHITE);
		if (flash >= 15)
			flash = 0;

		for (SnowFlake sf : snowFlake)
		{
			sf.update();
			if (sf.alive == false)
			{
				sf.create(screenWidth, screenHeight);
			}

			sprites.get(I_snow4).setPosition(sf.pos.x, sf.pos.y);
			sprites.get(I_snow4).draw(batch); // lumihiutale
		}

		// piirrä nuoli
		if (gingerbreadsFound == gingerbreadsMax)
		{
			sprites.get(I_arrow).setPosition(screenWidth / 2 + (float) Math.sin(time * 3) * 10 - blockSize,
					screenHeight - 2 * blockSize); // nuoli
			sprites.get(I_arrow).draw(batch);
		}

		// androidilla piirrä vasempaan alanurkkaan joystick ja oikeaan alanurkkaan hyppynappi
		if (DESKTOP == false)
		{
			sprites.get(I_control1).setPosition(0, 0); // nappi
			sprites.get(I_control1).draw(batch);

			sprites.get(I_control1).setPosition(screenWidth - sprites.get(I_control1).getWidth(), 0);
			sprites.get(I_control1).draw(batch);

			float hw = sprites.get(I_control2).getWidth() / 2;
			sprites.get(I_control2).setPosition(joystickX - hw, joystickY - hw); // joystick
			sprites.get(I_control2).draw(batch);
		}

		if (energy > 0)
		{
			time += Gdx.app.getGraphics().getDeltaTime();
		} else
		{
			float AREASIZE = sprites.get(I_control1).getWidth();
			joystickX = joystickY = AREASIZE / 2;

			// laske pisteet
			float score = (level - 1) * 987 + gingerbreadsFound * 8 - time * 7;
			if (score < 0)
			{
				score = 0;
			}

			font.getData().setScale(1.5f);
			font.draw(batch, "SCORE: " + (int) score, 5, screenHeight - 70);

			// tsekataan painetaanko "Play Again" nappia
			float px = screenWidth / 2 - sprites.get(I_playagain).getWidth() / 2;
			float py = screenHeight / 2 - sprites.get(I_playagain).getHeight() / 2;
			sprites.get(I_playagain).setPosition(px, py);
			sprites.get(I_playagain).draw(batch);
			if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Keys.SPACE)
					|| Gdx.input.isKeyJustPressed(Keys.ENTER))
			{
				int x = Gdx.input.getX();
				int y = Gdx.input.getY();
				if (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER)
						|| (x > px && y > py && x < px + sprites.get(I_playagain).getWidth()
								&& y < py + sprites.get(I_playagain).getHeight()))
				{
					// reset
					time = 0;
					level = 1;
					mapWidth = 0;
					mapHeight = 20;
					energy = 50;
					gingerbreadsMax = 0;
					childrenMax = 0;
					player.mystery = false;
					player.mysterySpeedMul = 1;
					player.mysteryJumpMul = 1;
					player.mysteryTimer = 0;
					createLevel();
				}
			}

		}

		String timeStr = "" + time + "   ";
		timeStr = timeStr.replace(',', '.');
		//  String.format ei toimi androidilla eikä html:llä, siispä näin:
		int dot = timeStr.indexOf('.');
		timeStr = timeStr.substring(0, dot + 3); // aika 2 desimaalin tarkkuudella

		font.getData().setScale(1);
		font.draw(batch, "Level: " + level + "  Total time: " + timeStr, 5, screenHeight - 10);
		font.draw(batch, "Energy: " + energy, 5, screenHeight - 30);
		font.draw(batch, "Gingerbreads: " + gingerbreadsFound + " / " + gingerbreadsMax, 5, screenHeight - 50);
		batch.end();
	}

	float time = 0;

	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height, true);
		screenWidth = width;
		screenHeight = height;

		int c = 0;
		for (SnowFlake sf : snowFlake)
		{
			if (c++ == SNOWFLAKES / 2)
			{
				break;
			}
			sf.create(screenWidth, screenHeight, true);
		}
	}

	public static void Log(String str)
	{
		if (DESKTOP)
			System.out.println(str);
		else
			; //Gdx.app.debug("info", str);
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}
}

class SnowFlake
{
	public Vector2 pos = new Vector2();
	public float dirX;
	boolean alive = false;

	public void create(float w, float h)
	{
		create(w, h, false);
	}

	public void create(float w, float h, boolean fullArea)
	{
		if (fullArea == false && Math.random() < 0.999)
		{
			return;
		}

		pos.x = (float) Math.random() * w;
		if (fullArea == false)
		{
			pos.y = h;
		} else
		{
			pos.y = (float) Math.random() * h;
		}
		dirX = (float) Math.random() - 0.5f;
		alive = true;
	}

	public void update()
	{
		if (alive == false)
		{
			return;
		}

		pos.x += dirX;
		if (Math.random() < 0.1)
		{
			dirX += (float) Math.random() - 0.5f;
		}

		pos.y -= Gdx.graphics.getDeltaTime() * 100;

		if (pos.y < 0)
		{
			alive = false;
		}

	}
}

class Character
{
	public Vector2 pos = new Vector2();
	public Vector2 dir = new Vector2();
	public float frame = 0;
	public boolean moveToRight = true;
	public float mysterySpeedMul = 1, mysteryJumpMul = 1, mysteryTimer = 0;
	public boolean mystery = false;

	boolean canMove(float dx, float dy)
	{
		float w = 0;
		if (dx > 0)
		{
			w = HungryGnome.inst.blockSize;
		}

		int mx = (int) ((pos.x + dx + w) / HungryGnome.inst.blockSize);
		int my = (int) ((pos.y + dy) / HungryGnome.inst.blockSize);

		if (HungryGnome.inst.map[HungryGnome.L_GROUND][mx][my] != 0)
		{
			return false;
		}

		// check ladder
		if (dy > 0 || dy < 0)
		{
			mx = (int) ((pos.x + HungryGnome.inst.blockSize / 2) / HungryGnome.inst.blockSize);
			if (HungryGnome.inst.map[HungryGnome.L_LADDER][mx][my] != 0)
			{
				return true;
			}
			return false;
		}

		return true;
	}

	public boolean gingerbreadTaken()
	{
		int mx = (int) ((pos.x + HungryGnome.inst.blockSize / 2) / HungryGnome.inst.blockSize);
		int my = (int) ((pos.y + HungryGnome.inst.blockSize / 2) / HungryGnome.inst.blockSize);
		if (HungryGnome.inst.map[HungryGnome.L_GINGERBREAD][mx][my] == HungryGnome.I_energy) // energy
		{
			HungryGnome.inst.map[HungryGnome.L_GINGERBREAD][mx][my] = 0;
			if (HungryGnome.inst.sounds[0] != null)
			{
				HungryGnome.inst.energy += 20;
				HungryGnome.inst.sounds[0].play();
			}
			HungryGnome.inst.batch.setColor(0.2f, 1, 0.6f, 1);
			HungryGnome.inst.flash = 1;
			return false; // false koska piparia ei otettu
		}
		if (HungryGnome.inst.map[HungryGnome.L_GINGERBREAD][mx][my] == HungryGnome.I_gingerbread) // pipari
		{
			HungryGnome.inst.map[HungryGnome.L_GINGERBREAD][mx][my] = 0;
			if (HungryGnome.inst.sounds[0] != null)
			{
				HungryGnome.inst.sounds[0].play();
			}
			HungryGnome.inst.energy += 5;
			HungryGnome.inst.batch.setColor(1f, 170f / 255f, 0, 1);
			HungryGnome.inst.flash = 1;
			return true; // true koska pipari otettu
		}
		if (HungryGnome.inst.map[HungryGnome.L_GINGERBREAD][mx][my] == HungryGnome.I_mystery) // mystery pullo
		{
			/*
			 * kenttään tulee random kohtaan  "Mystery" lahja joka antaa vähäks aikaa joko
			 ** superhypyn
			 ** nopeutuksen
			 ** hidastuksen
			 */
			mysteryJumpMul = mysterySpeedMul = 1;
			mysteryTimer = 0;
			mystery = true;

			float myst = (float) Math.random();
			if (myst < 0.33f)
			{
				mysteryJumpMul = 2;
			} else if (myst > 0.66)
			{
				mysterySpeedMul = 2;
			} else
			{
				mysterySpeedMul = 0.5f;
			}

			HungryGnome.inst.map[HungryGnome.L_GINGERBREAD][mx][my] = 0;
			if (HungryGnome.inst.sounds[3] != null)
			{
				HungryGnome.inst.sounds[3].play();
			}
			HungryGnome.inst.batch.setColor(0, 0, 1, 1);
			HungryGnome.inst.flash = 1;
			return false; // false koska piparia ei otettu
		}

		return false; // false koska piparia ei otettu
	}

	public void update()
	{
		if (mystery)
		{
			mysteryTimer += Gdx.graphics.getDeltaTime();
			if (mysteryTimer > 5)
			{
				mysterySpeedMul = 1;
				mysteryJumpMul = 1;
				mysteryTimer = 0;
				mystery = false;
			}
		}

		// jos tyyppi on tikapuilla ja suunta alaspäin (tippuminen), nollaa suunta
		int mx = (int) ((pos.x + HungryGnome.inst.blockSize / 2) / HungryGnome.inst.blockSize);
		int my = (int) ((pos.y) / HungryGnome.inst.blockSize);
		if (HungryGnome.inst.map[HungryGnome.L_LADDER][mx][my] != 0)
		{
			if (dir.y > 0)
			{
				dir.y = 0;
				return;
			}
		}

		// pitääkä tyyppi pudottaa alaspäin
		mx = (int) ((pos.x) / HungryGnome.inst.blockSize);
		if (HungryGnome.inst.map[HungryGnome.L_GROUND][mx][my] == 0
				&& HungryGnome.inst.map[HungryGnome.L_GROUND][mx + 1][my] == 0
				&& HungryGnome.inst.map[HungryGnome.L_HOUSE][mx][my] != HungryGnome.I_roof
				&& HungryGnome.inst.map[HungryGnome.L_HOUSE][mx + 1][my] != HungryGnome.I_roof)
		{
			pos.y -= dir.y;
			if (dir.y < HungryGnome.inst.fallSpeed)
			{
				dir.y += Gdx.graphics.getDeltaTime() * 50;
			}

			my = (int) ((pos.y) / HungryGnome.inst.blockSize);
			if (HungryGnome.inst.map[HungryGnome.L_GROUND][mx][my] != 0
					|| HungryGnome.inst.map[HungryGnome.L_GROUND][mx + 1][my] != 0
					|| HungryGnome.inst.map[HungryGnome.L_HOUSE][mx][my] == HungryGnome.I_roof
					|| HungryGnome.inst.map[HungryGnome.L_HOUSE][mx + 1][my] == HungryGnome.I_roof)
			{
				dir.y = 0;
				pos.y = (my + 1) * HungryGnome.inst.blockSize;
			}
		}
	}

	public boolean jump()
	{
		int mx = (int) ((pos.x) / HungryGnome.inst.blockSize);
		int my = (int) ((pos.y - 1) / HungryGnome.inst.blockSize);

		int lmx = (int) ((pos.x + HungryGnome.inst.blockSize / 2) / HungryGnome.inst.blockSize);
		int lmy = (int) ((pos.y) / HungryGnome.inst.blockSize);
		if ((HungryGnome.inst.map[HungryGnome.L_GROUND][mx][my] != 0
				|| HungryGnome.inst.map[HungryGnome.L_GROUND][mx + 1][my] != 0)
				|| HungryGnome.inst.map[HungryGnome.L_LADDER][lmx][lmy] != 0)
		{
			dir.y = -HungryGnome.inst.jumpSpeed * mysteryJumpMul;
		}

		if (HungryGnome.inst.map[HungryGnome.L_HOUSE][mx][my] == HungryGnome.I_roof
				|| HungryGnome.inst.map[HungryGnome.L_HOUSE][mx + 1][my] == HungryGnome.I_roof)
		{
			pos.y = (my + 1) * HungryGnome.inst.blockSize;
			dir.y = -HungryGnome.inst.jumpSpeed * mysteryJumpMul;
		}
		return true;
	}

	public void updateAI()
	{
		float speedAI = HungryGnome.inst.speed * Gdx.graphics.getDeltaTime() * 20;

		float xl = (HungryGnome.inst.player.pos.x + HungryGnome.inst.blockSize / 2)
				- (pos.x + HungryGnome.inst.blockSize / 2);
		float yl = (HungryGnome.inst.player.pos.y + HungryGnome.inst.blockSize / 2)
				- (pos.y + HungryGnome.inst.blockSize / 2);
		float len = Math.abs(Vector2.len(xl, yl));

		final float MUL;
		if (HungryGnome.DESKTOP)
			MUL = 1.7f;
		else
			MUL = 1;

		if (len < 30 * MUL)
		{
			if (HungryGnome.inst.energy > 0)
			{
				HungryGnome.inst.energy--;
				if (HungryGnome.inst.sounds[2] != null && HungryGnome.inst.sounds[2].isPlaying() == false)
				{
					HungryGnome.inst.sounds[2].play();
				}

				if (HungryGnome.inst.energy <= 0)
					if (HungryGnome.inst.sounds[5] != null)
						HungryGnome.inst.sounds[5].play();

				HungryGnome.inst.batch.setColor(1, 0.4f, 0.4f, 1);
				HungryGnome.inst.flash = 1;
			}
			return;
		}

		if (Math.random() < 0.001)
		{
			idleTimerAI = (float) Math.random() * 2;
			idleAI = true;
		} else
		{
			idleAI = false;
		}

		if (idleAI)
		{
			idleTimerAI -= Gdx.graphics.getDeltaTime();
			if (idleTimerAI < 0)
			{
				idleAI = false;
			}
			return;
		}

		if (Math.random() < 0.01)
		{
			if (Math.random() < 0.5)
			{
				towardsAI = true;
			} else
			{
				towardsAI = false;
			}
		}

		int mx = (int) ((pos.x + HungryGnome.inst.blockSize / 2) / HungryGnome.inst.blockSize);
		int my = (int) ((pos.y) / HungryGnome.inst.blockSize);

		if (mx < 2)
		{
			towardsAI = false;
		}

		// check ladder
		if (HungryGnome.inst.map[HungryGnome.L_LADDER][mx][my] != 0)
		{
			if (Math.random() < 0.8)
			{
				climbAI = true;
			} else
			{
				climbAI = false;
			}
		}

		if (climbAI)
		{
			if (canMove(0, speedAI))
			{
				pos.y += speedAI;
				return;
			}
		}

		if (pos.x > HungryGnome.inst.player.pos.x)
		{
			dir.x = -speedAI;
		} else
		{
			dir.x = speedAI;
		}

		if (towardsAI == false) // jos ei mennä kohti,
		{
			dir.x = -dir.x; // käännä suunta
		}

		boolean ok = false;
		if (canMove(dir.x, 0))
		{
			ok = true;
			pos.x += dir.x;
			frame += Math.abs(dir.x) * 0.1f;

			if (dir.x > 0)
				moveToRight = true;
			else if (dir.x < 0)
				moveToRight = false;
		}

		if (!ok)
		{
			if (Math.random() < 0.2)
			{
				jump();
			}
		}
	}

	boolean climbAI = false, towardsAI = true, idleAI = true;
	float idleTimerAI = 0;

}
