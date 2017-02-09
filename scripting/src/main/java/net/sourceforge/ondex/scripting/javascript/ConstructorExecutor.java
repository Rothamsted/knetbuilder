package net.sourceforge.ondex.scripting.javascript;

abstract class ConstructorExecutor{
	//abstract public T construct(Object [] inputs);
	abstract public Class<?> getType(int i);
	abstract public int getCount();
	protected static Object [] alignArgs(Object [] inputs, int count){
		if(count > inputs.length){
			Object[] result = new Object[count];
			for(int i = 0; i < inputs.length; i++){
				result[i] = inputs[i];
			}
			return result;
		}
		return inputs;
	}
}