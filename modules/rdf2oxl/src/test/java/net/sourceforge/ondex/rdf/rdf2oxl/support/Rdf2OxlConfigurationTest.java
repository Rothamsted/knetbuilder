//package net.sourceforge.ondex.rdf.rdf2oxl.support;
//
//import static java.lang.System.out;
//
//import java.util.List;
//import java.util.stream.Stream;
//
//import org.apache.commons.beanutils.PropertyUtils;
//import org.apache.commons.lang3.StringEscapeUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import static com.machinezoo.noexception.Exceptions.sneak;
//
///**
// * TODO: comment me!
// *
// * @author brandizi
// * <dl><dt>Date:</dt><dd>9 Oct 2018</dd></dl>
// *
// */
//public class Rdf2OxlConfigurationTest
//{
//	private static ClassPathXmlApplicationContext springContext;
//	
//	@BeforeClass
//	public static void initResources ()
//	{
//		springContext = new ClassPathXmlApplicationContext ( "default_beans.xml" );
//	}
//	
//	@AfterClass
//	public static void closeResources ()
//	{
//		springContext.close ();
//	}
//	
//	@Test @Ignore ( "Scrap temp code, Not a real test. TODO: Remove" )
//	public void toXml ()
//	{
//		Rdf2OxlConfiguration cfg = springContext.getBean ( Rdf2OxlConfiguration.class );
//		for ( ItemConfiguration item: cfg.getItemConfigurations () )
//		{
//			out.println ( "<bean class = 'net.sourceforge.ondex.rdf.rdf2oxl.support.ItemConfiguration'>" );
//			
//			Stream.of ( 
//				"name", "resourcesQueryName", "constructTemplateName", "graphTemplateName" )
//			.filter ( prop ->  sneak().getAsBoolean ( () -> PropertyUtils.getProperty ( item, prop ) != null ) )
//			.forEach ( prop -> out.printf ( 
//				"\t<property name = '%s' value = '%s' />\n",
//				prop, sneak ().get ( () -> PropertyUtils.getProperty ( item, prop ) )
//			));
//			
//			Stream.of ( "header", "trailer" )
//			.filter ( prop ->  sneak().getAsBoolean ( () -> PropertyUtils.getProperty ( item, prop ) != null ) )
//			.forEach ( prop -> out.printf ( 
//				"\t<property name = '%s'>\n\t\t<value><![CDATA[%s]]></value>\n\t</property>",
//				prop, (String) sneak ().get ( () -> PropertyUtils.getProperty ( item, prop ) )
//			));
//
//			Stream.of ( 
//				"queryProcessor", "querySolutionHandler" )
//			.filter ( prop ->  sneak().getAsBoolean ( () -> PropertyUtils.getProperty ( item, prop ) != null ) )
//			.forEach ( prop -> out.printf ( 
//				"\t<property name = '%s' ref = '%s' />\n",
//				prop, StringUtils.uncapitalize ( sneak ().get ( () -> PropertyUtils.getProperty ( item, prop ) ).getClass ().getSimpleName () )
//			));
//			
//			out.println ( "</bean>\n" );
//		}
//	}
//	
//	@Test @Ignore ( "Scrap temp code, Not a real test. TODO: Remove" )
//	public void testGetList ()
//	{
//		@SuppressWarnings ( "unchecked" )
//		List<ItemConfiguration> list = springContext.getBean ( "itemConfigurations", List.class );
//		out.println ( "SIZE IS: " + list.size () );
//	}
//}
