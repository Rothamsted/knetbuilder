package net.sourceforge.ondex.transformer.sharedgi;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

public class Plotter {
	
	private int[] pixbuf;
	private int w,h, maxX, maxY;
	
	private static final int red   = 0xff0000,
							 green = 0x00ff00,
						     blue  = 0x0000ff,
	                         white = 0xffffff;
	
	public Plotter(int w, int h, int maxX, int maxY) {
		this.w = w;
		this.h = h;
		this.maxX = maxX;
		this.maxY = maxY;
		this.pixbuf = new int[w*h];
		Arrays.fill(pixbuf, white);
	}
	
	private void plot(int x, int y, int color) {
		int i = x * w / maxX;
		int j = h - (y * h / maxY) -1;
		if (i >= 0 && i < w && j >= 0 && j < h) {
			int index = (w * j) + i;
			pixbuf[index] = color;
		}
	}
	
	public void plot(int x, int y) {
		plot(x,y,blue);
	}
	
	public void exportImage(String filename) {
		int[] bitMasks = new int[] {red, green, blue};
//		plot(1,1, red);
//		plot(w-2,1, green);
		SampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, w, h, bitMasks);
		ColorModel colormodel = new DirectColorModel(24, bitMasks[0], bitMasks[1], bitMasks[2]);
		
		DataBuffer buffer = new DataBufferInt(pixbuf, pixbuf.length);
		
		WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, new Point(0,0));
		
		BufferedImage img = new BufferedImage(colormodel,raster ,false,new Hashtable<Object, Object>());
		try {
			ImageIO.write(img, "png", new FileImageOutputStream(new File(filename)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Plotter p = new Plotter(10,10,10,10);
		p.exportImage("/home/jweile/tmp/test.png");
	}
	
}
