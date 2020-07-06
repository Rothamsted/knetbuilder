//package net.sourceforge.ondex.parser;
//
//import java.util.function.Function;
//import java.util.stream.Stream;
//
//import net.sourceforge.ondex.core.ONDEXGraph;
//
///**
// * TODO: comment me!
// *
// * @author brandizi
// * <dl><dt>Date:</dt><dd>21 May 2018</dd></dl>
// *
// */
//public class TransformingTextsMapper<S> extends DecoratingMapper<S, Stream<String>> implements TextsMapper<S>
//{
//	protected Function<String, String> transformer = Function.identity ();
//
//	public TransformingTextsMapper () {
//		super ();
//	}
//
//	public TransformingTextsMapper ( TextsMapper<S> baseMapper, Function<String, String> transformer ) 
//	{
//		super ( baseMapper );
//		this.transformer = transformer;
//	}
//	
//	
//	@Override
//	@SuppressWarnings ( "unchecked" )
//	public Stream<String> map ( S source, ONDEXGraph graph ) 
//	{
//		TextsMapper<S> txtMapper = (TextsMapper<S>) this.baseMapper;
//		return txtMapper.map ( source, graph ).map ( getTransformer () );
//	}
//
//	
//	@Override
//	@SuppressWarnings ( { "unchecked", "rawtypes" } )
//	public TextsMapper<S> getBaseMapper () {
//		return (TextsMapper<S>) (Mapper) super.getBaseMapper ();
//	}
//
//	public void setBaseMapper ( TextsMapper<S> baseMapper ) {
//		super.setBaseMapper ( baseMapper );
//	}
//
//	
//	public Function<String, String> getTransformer ()
//	{
//		return transformer;
//	}
//
//	public void setTransformer ( Function<String, String> transformer )
//	{
//		this.transformer = transformer;
//	}
//}
