package net.sourceforge.ondex.ovtk2.util;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.fop.render.ps.EPSTranscoder;
import org.apache.fop.render.ps.PSTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.xmlgraphics.image.writer.internal.JPEGImageWriter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Exports images in the following formats (via the SVG):svg, jpg, png, pdf, ps,
 * eps and tiff
 * 
 * @author lysenkoa
 * 
 */
public class SVGExport {

	public SVGExport(OVTK2PropertiesAggregator viewer, File file, String option) {

		// initialise SVG generator
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		boolean useCSS = false;

		VisualizationViewer<ONDEXConcept, ONDEXRelation> vv = viewer.getVisualizationViewer();

		// get size of area to paint
		Rectangle rect = vv.getVisibleRect();
		svgGenerator.clip(rect);

		// make rendering look nice
		Map<Key, Object> m = new HashMap<Key, Object>(viewer.getVisualizationViewer().getRenderingHints());
		vv.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		vv.getRenderingHints().put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		vv.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		vv.getRenderingHints().put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		vv.getRenderingHints().put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		vv.getRenderingHints().put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		vv.getRenderingHints().put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		vv.setDoubleBuffered(false);

		// paint once on SVG
		vv.paint(svgGenerator);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if (option.equalsIgnoreCase("svg")) {
				svgGenerator.stream(new OutputStreamWriter(fos, "UTF-8"), useCSS);
			} else if (option.equalsIgnoreCase("jpg") || option.equalsIgnoreCase("jpeg")) {
				JPEGImageWriter jiw = new JPEGImageWriter();
				jiw.isFunctional();
				JPEGTranscoder t = new JPEGTranscoder();
				t.setErrorHandler(new ErrorHandlerBridge());
				t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(1.0));
				t.addTranscodingHint(JPEGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(25.4f / 300f));
				transcode(t, svgGenerator, useCSS, rect, fos);
			} else if (option.equalsIgnoreCase("eps")) {
				EPSTranscoder t = new EPSTranscoder();
				t.setErrorHandler(new ErrorHandlerBridge());
				transcode(t, svgGenerator, useCSS, rect, fos);
			} else if (option.equalsIgnoreCase("png")) {
				PNGTranscoder t = new PNGTranscoder();
				t.setErrorHandler(new ErrorHandlerBridge());
				transcode(t, svgGenerator, useCSS, rect, fos);
			} else if (option.equalsIgnoreCase("pdf")) {
				PDFTranscoder t = new PDFTranscoder();
				t.setErrorHandler(new ErrorHandlerBridge());
				transcode(t, svgGenerator, useCSS, rect, fos);
			} else if (option.equalsIgnoreCase("ps")) {
				PSTranscoder t = new PSTranscoder();
				t.setErrorHandler(new ErrorHandlerBridge());
				transcode(t, svgGenerator, useCSS, rect, fos);
			} else if (option.equalsIgnoreCase("tiff")) {
				TIFFTranscoder t = new TIFFTranscoder();
				t.setErrorHandler(new ErrorHandlerBridge());
				transcode(t, svgGenerator, useCSS, rect, fos);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (TranscoderException e) {
			e.printStackTrace();
		} catch (SVGGraphics2DIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			vv.getRenderingHints().clear();
			vv.getRenderingHints().putAll(m);
			vv.setDoubleBuffered(true);
		}
	}

	/**
	 * Transcodes the image using the transcoder specified
	 * 
	 * @param t
	 * @param svgGenerator
	 * @param useCSS
	 * @param rect
	 * @param fos
	 * @throws IOException
	 * @throws TranscoderException
	 */
	public static void transcode(Transcoder t, SVGGraphics2D svgGenerator, boolean useCSS, Rectangle rect, FileOutputStream fos) throws IOException, TranscoderException {
		PipedWriter pout = new PipedWriter();
		BufferedReader rin = new BufferedReader(new PipedReader(pout));
		new Piper(svgGenerator, useCSS, pout).start();
		t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, Float.valueOf(rect.height));
		t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, Float.valueOf(rect.width));
		t.transcode(new TranscoderInput(rin), new TranscoderOutput(fos));
	}

	/**
	 * A thread that feeds the piped reader
	 * 
	 * @author lysenkoa
	 * 
	 */
	static class Piper extends Thread {
		SVGGraphics2D svgGenerator;
		boolean useCSS;
		Writer out;

		public Piper(SVGGraphics2D svgGenerator, boolean useCSS, Writer out) {
			this.svgGenerator = svgGenerator;
			this.useCSS = useCSS;
			this.out = out;
		}

		public void run() {
			try {
				BufferedWriter bw = new BufferedWriter(out);
				svgGenerator.stream(out, useCSS);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ErrorHandlerBridge implements ErrorHandler {
		@Override
		public void error(TranscoderException e) throws TranscoderException {
			net.sourceforge.ondex.ovtk2.util.ErrorHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}

		@Override
		public void fatalError(TranscoderException e) throws TranscoderException {
			net.sourceforge.ondex.ovtk2.util.ErrorHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}

		@Override
		public void warning(TranscoderException arg0) throws TranscoderException {
			JOptionPane.showMessageDialog(OVTK2Desktop.getInstance().getMainFrame(), arg0.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}
}
