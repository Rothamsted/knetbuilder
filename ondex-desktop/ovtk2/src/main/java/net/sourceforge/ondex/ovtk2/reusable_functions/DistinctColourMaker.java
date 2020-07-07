package net.sourceforge.ondex.ovtk2.reusable_functions;

import java.awt.Color;
import java.util.Random;

public class DistinctColourMaker {
	float[] saturation = new float[] { 1f, 0.75f, 0.50f, 0.25f };
	float[] brightness = new float[] { 1f, 0.75f, 0.50f, 0.25f };
	float hueCounter = 0f;
	final float hueMax;
	int saturationCounter = 0;
	int brightnessCounter = 0;
	Random r = new Random();

	public DistinctColourMaker(float hueMax) {
		this.hueMax = hueMax;
	}

	public Color getNextColor() {
		if (brightnessCounter > 3) {
			return new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		}
		if (hueCounter >= hueMax) {
			hueCounter = 0f;
			saturationCounter++;
			if (saturationCounter > 3) {
				saturationCounter = 0;
				brightnessCounter++;
				if (brightnessCounter > 3) {
					return new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
				}
			}
		}
		// System.err.println(hueCounter*12+" "+saturation[saturationCounter]+" "+brightness[brightnessCounter]);
		Color result = Color.getHSBColor(hueCounter / hueMax, saturation[saturationCounter], brightness[brightnessCounter]);
		hueCounter++;
		return result;

	}

	public void reset() {
		hueCounter = 0;
		saturationCounter = 0;
		brightnessCounter = 0;
	}
}
