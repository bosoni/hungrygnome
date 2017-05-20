package org.mjt.hungrygnome.desktop;

import org.mjt.hungrygnome.HungryGnome;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher
{
	public static void main(String[] arg)
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Hungry Gnome";
		config.width = 1024;
		config.height = 768;
		new LwjglApplication(new HungryGnome(), config);
	}
}
