
package uk.ac.birmingham;

import org.renjin.invoke.codegen.scalars.ByteType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Util;

/**
 * This is a minimal ImageJ command implements a manual threshold
 */

@Plugin(type = Command.class, menuPath = "Plugins>maual threshold")
public class SimpleThreshold<T extends RealType<T>> implements Command {
	
	@Parameter(label = "Define the threshold value: ", persist = false)
	private double thresholdVal = 0.;
	
	@Parameter
	private Dataset currentData;

	@Parameter(type = ItemIO.OUTPUT)
	private Img<BitType> result;

	@Override
	public void run() {
		final Img<T> img = (Img<T>) currentData.getImgPlus();
		final Object type = Util.getTypeFromInterval( img );
		System.out.println( "Pixel Type: " + type.getClass() );
		System.out.println( "Img Type: " + img.getClass() );
		long[] dims = new long[img.numDimensions()];
		img.dimensions(dims);
		try {
			result = img.factory().imgFactory(new BitType()).create(img, new BitType());
		} catch (IncompatibleTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		T thresholdD = img.firstElement();
		thresholdD.setReal(thresholdVal);
		threshold(img, result, thresholdD);

	}

	public static <K extends Comparable<K>> void threshold(final RandomAccessible<K> source,
			final IterableInterval<BitType> target, K thresh) {
		
		final RandomAccess<K> in = source.randomAccess();
		final Cursor<BitType> out = target.localizingCursor();
		
		while (out.hasNext()) {
			out.fwd();
			in.setPosition(out);
			final BitType type = out.get();
			if (in.get().compareTo(thresh) > 0) {
				type.setOne();
			} else {
				type.setZero();
			}

		}
	}

	/**
	 * This main function serves for development purposes.
	 *
	 * @param args
	 *            whatever, it's ignored
	 * @throws Exception
	 */
	public static void main(final String... args) throws Exception {

		// create the ImageJ application context
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// load example dataset
		final Dataset dataset = ij.scifio().datasetIO().open("http://imagej.net/images/FluorescentCells.jpg");

		// show the image
		ij.ui().show(dataset);

		// invoke the plugin
		ij.command().run(SimpleThreshold.class, true);

	}

}
