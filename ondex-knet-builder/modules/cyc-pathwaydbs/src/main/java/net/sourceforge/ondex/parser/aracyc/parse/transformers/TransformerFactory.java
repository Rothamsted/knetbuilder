package net.sourceforge.ondex.parser.aracyc.parse.transformers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.sourceforge.ondex.parser.aracyc.Parser;

/**
 * creates a new transformer and takes care, that there is just one instance of
 * each transformer available.
 * 
 * @author peschr
 */
public class TransformerFactory {
	private static HashMap<Class<?>, AbstractTransformer> instanceRegister = new HashMap<Class<?>, AbstractTransformer>();

	public static AbstractTransformer getInstance(Class<?> transformerName,
			Parser p) throws SecurityException, NoSuchMethodException,
			Exception {
		AbstractTransformer transformer = null;
		try {

			if ((transformer = instanceRegister.get(transformerName)) != null)
				return transformer;
			else {
				if (transformerName.getConstructor(Parser.class)
						.getParameterTypes().length != 1) {
					throw new Exception("strange constructor"
							+ " "
							+ transformerName
							+ " "
							+ transformerName.getConstructor(Parser.class)
									.getParameterTypes().length);
				}
				transformer = (AbstractTransformer) transformerName
						.getConstructor(Parser.class).newInstance(p);
				instanceRegister.put(transformerName, transformer);

			}
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
			throw new Exception();
		}
		return transformer;
	}
}
