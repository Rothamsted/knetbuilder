package net.sourceforge.ondex.doclet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.metadata.AttributeNameRequired;
import net.sourceforge.ondex.annotations.metadata.ConceptClassRequired;
import net.sourceforge.ondex.annotations.metadata.DataSourceRequired;
import net.sourceforge.ondex.annotations.metadata.EvidenceTypeRequired;
import net.sourceforge.ondex.annotations.metadata.RelationTypeRequired;
import net.sourceforge.ondex.init.PluginType;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

/**
 * @author hindlem
 */
public class PluginDoclet {

	private static final String NAMESPACE = "http://ondex.sourceforge.net/workflow_element";

	public static boolean start(RootDoc root) {

		String filename = "workflow-component-description.xml";
		String artifactName = null;
		String groupId = null;
		String version = null;

		File f = new File("tmp.del");
		f.deleteOnExit();
		String classDir = f.getParent();
		String libsDir = f.getParent();

		String[][] options = root.options();

		for (int i = 0; i < options.length; i++) {
			String[] opt = options[i];
			if (opt[0].equals("-filename")) {
				filename = opt[1];
			} else if (opt[0].equals("-artifactId")) {
				artifactName = opt[1];
			} else if (opt[0].equals("-groupId")) {
				groupId = opt[1];
			} else if (opt[0].equals("-version")) {
				version = opt[1];
			} else if (opt[0].equals("-classdir")) {
				classDir = opt[1];
				libsDir = opt[2];
			}
		}

		if (classDir == null) {
			System.err.println("ClassDir is null.");
			return true;
		}

		Set<URL> jarRegisterBuilder = new HashSet<URL>();
		File rootDir = new File(classDir);

		addFiles(new File(libsDir), jarRegisterBuilder);

		URLClassLoader ucl = null;
		try {
			URL[] urls = jarRegisterBuilder.toArray(new URL[jarRegisterBuilder
					.size() + 1]);
			urls[urls.length - 1] = rootDir.toURI().toURL();

			// NB you must extend the ContextClass loader as JavaDoc uses its
			// own URLClassLoader
			ucl = URLClassLoader.newInstance(urls, Thread.currentThread()
					.getContextClassLoader());
			Thread.currentThread().setContextClassLoader(ucl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		System.out.println("Generating :" + filename);

		try {
			// Stand-alone version from http://sjsxp.dev.java.net:
			System.setProperty("javax.xml.stream.XMLInputFactory",
					"com.sun.xml.stream.ZephyrParserFactory");
			System.setProperty("javax.xml.stream.XMLOutputFactory",
					"com.sun.xml.stream.ZephyrWriterFactory");

			XMLOutputFactory xof = XMLOutputFactory.newInstance();
			xof.setProperty("javax.xml.stream.isRepairingNamespaces",
					new Boolean(true));
			XMLStreamWriter xtw = xof.createXMLStreamWriter(
					new FileOutputStream(filename), "UTF8");

			ClassDoc pluginClass = root.classNamed(ONDEXPlugin.class
					.getCanonicalName());
			ClassDoc validatorClass = root
					.classNamed(AbstractONDEXValidator.class.getCanonicalName());

			Set<ClassDoc> pluginTypes = new HashSet<ClassDoc>();
			for (PluginType pt : PluginType.values()) {
				ClassDoc ptd = root.classNamed(pt.getPluginClass()
						.getCanonicalName());
				if (ptd != null) {
					pluginTypes.add(ptd);
				}
			}

			xtw.writeStartDocument();
			xtw.writeProcessingInstruction("xml-stylesheet type='text/xsl' href='workflow-component-description.xsl'");
			xtw.writeStartElement("plugins");
			xtw.setPrefix("wfc", NAMESPACE);
			
			if (artifactName != null) {
				xtw.writeStartElement(NAMESPACE, "artifactId");
				xtw.writeCharacters(artifactName);
				xtw.writeEndElement(); // comment
			} else {
				System.err.println("WARN: -artifactId not specified");
			}

			if (groupId != null) {
				xtw.writeStartElement(NAMESPACE, "groupId");
				xtw.writeCharacters(groupId);
				xtw.writeEndElement(); // comment
			} else {
				System.err.println("WARN: -groupId not specified");
			}

			if (version != null) {
				xtw.writeStartElement(NAMESPACE, "version");
				xtw.writeCharacters(version);
				xtw.writeEndElement(); // comment
			} else {
				System.err.println("WARN: -version not specified");
			}

			ClassDoc[] classes = root.classes();
			for (ClassDoc classDoc : classes) {

				if ((classDoc.subclassOf(pluginClass) || (validatorClass != null && classDoc
						.subclassOf(validatorClass)))
						&& classDoc.isOrdinaryClass()) {

					for (ClassDoc pluginType : pluginTypes) {
						if (classDoc.subclassOf(pluginType)) {

							xtw.writeStartElement(NAMESPACE, "plugin");
							xtw.writeAttribute("type",
									pluginType.qualifiedName());

							xtw.writeStartElement(NAMESPACE, "entryClass");
							xtw.writeCharacters(classDoc.qualifiedName());
							xtw.writeEndElement(); // comment

							try {
								Class<?> classPlugin = ucl.loadClass(classDoc
										.qualifiedName());

								if (Modifier.isAbstract(classPlugin
										.getModifiers())) {
									System.err
											.println(classPlugin.getName()
													+ " is abstract and not a valid plugin: ignoring");
									continue;
								} else if (Modifier.isInterface(classPlugin
										.getModifiers())) {
									System.err
											.println(classPlugin.getName()
													+ " is an interface and not a valid plugin: ignoring");
									continue;
								}

								Status statusAnnotation = classPlugin
										.getAnnotation(Status.class);
								if (statusAnnotation != null) {
									String status = statusAnnotation.status()
											.getValue().trim();
									String description = statusAnnotation
											.description().trim();

									xtw.writeStartElement(NAMESPACE, "status");
									xtw.writeAttribute("type", status);
									xtw.writeStartElement(NAMESPACE,
											"description");
									xtw.writeCData(description);
									xtw.writeEndElement(); // comment
									xtw.writeEndElement(); // comment
								} else {
									System.err.println(classDoc.qualifiedName()
											+ " is missing annotation tag "
											+ Status.class.getName());
								}

								DatabaseTarget dbAnnotation = classPlugin
										.getAnnotation(DatabaseTarget.class);
								if (dbAnnotation != null) {
									String name = dbAnnotation.name().trim();
									String description = dbAnnotation
											.description().trim();
									String url = dbAnnotation.url().trim();

									xtw.writeStartElement(NAMESPACE, "database");
									xtw.writeAttribute("name", name);

									xtw.writeStartElement(NAMESPACE, "url");
									xtw.writeCData(url);
									xtw.writeEndElement(); // end url

									xtw.writeStartElement(NAMESPACE,
											"description");
									xtw.writeCData(description);
									xtw.writeEndElement(); // end desc

									xtw.writeEndElement(); // end database
								} else if (pluginType.qualifiedName().equals(
										ONDEXParser.class.getCanonicalName())) {
									System.err.println(classDoc.qualifiedName()
											+ " is missing annotation tag "
											+ DatabaseTarget.class.getName());
								}

								DataURL dburlAnnotation = classPlugin
										.getAnnotation(DataURL.class);
								if (dburlAnnotation != null) {
									String name = dburlAnnotation.name().trim();
									String description = dburlAnnotation
											.description().trim();
									String[] urls = dburlAnnotation.urls();

									xtw.writeStartElement(NAMESPACE,
											"data_files");
									xtw.writeAttribute("name", name);

									xtw.writeStartElement(NAMESPACE, "urls");
									for (String url : urls) {
										xtw.writeStartElement(NAMESPACE, "url");
										xtw.writeCData(url.trim());
										xtw.writeEndElement(); // end url
									}
									xtw.writeEndElement(); // end urls

									xtw.writeStartElement(NAMESPACE,
											"description");
									xtw.writeCData(description);
									xtw.writeEndElement(); // end desc

									xtw.writeEndElement(); // end database
								} else if (pluginType.qualifiedName().equals(
										ONDEXParser.class.getCanonicalName())) {
									System.err.println(classDoc.qualifiedName()
											+ " is missing annotation tag "
											+ DataURL.class.getName());
								}

								xtw.writeStartElement(NAMESPACE,
										"ondex-metadata");

								// Do datasource
								DataSourceRequired cvAnnotation = classPlugin
										.getAnnotation(DataSourceRequired.class);
								if (cvAnnotation != null) {
									writeMetaData(xtw, "data_source", cvAnnotation.ids());
								}
								// Do conceptclass
								ConceptClassRequired conceptclassAnnotation = classPlugin
										.getAnnotation(ConceptClassRequired.class);
								if (conceptclassAnnotation != null) {
									writeMetaData(xtw, "concept_class",
											conceptclassAnnotation.ids());
								}
								// Do attribute name
								AttributeNameRequired attributeAnnotation = classPlugin
										.getAnnotation(AttributeNameRequired.class);
								if (attributeAnnotation != null) {
									writeMetaData(xtw, "attribute_name",
											attributeAnnotation.ids());
								}
								// Do relation type
								RelationTypeRequired relationtypeAnnotation = classPlugin
										.getAnnotation(RelationTypeRequired.class);
								if (relationtypeAnnotation != null) {
									writeMetaData(xtw, "relation_type",
											relationtypeAnnotation.ids());
								}
								// Do evidence type
								EvidenceTypeRequired evidencetypeAnnotatio = classPlugin
										.getAnnotation(EvidenceTypeRequired.class);
								if (evidencetypeAnnotatio != null) {
									writeMetaData(xtw, "evidence_type",
											evidencetypeAnnotatio.ids());
								}

								xtw.writeEndElement(); // end ondex-metadata

							} catch (ClassNotFoundException e) {
								System.err.println("Missing ");
							} catch (SecurityException e) {
								e.printStackTrace();
							}

							xtw.writeStartElement(NAMESPACE, "comment");
							xtw.writeCData(classDoc.commentText());
							xtw.writeEndElement(); // comment

							xtw.writeStartElement(NAMESPACE, "authors");

							for (Tag author : classDoc.tags("author")) {
								String[] authors = author.text().split(",");
								for (String authorName : authors) {
									xtw.writeStartElement(NAMESPACE, "author");
									xtw.writeCharacters(authorName.trim());
									xtw.writeEndElement();// author
								}
							}

							xtw.writeEndElement(); // authors

							xtw.writeEndElement(); // plugin
						}
					}
				}
			}
			xtw.writeEndElement(); // end plugins

			xtw.writeEndDocument();
			xtw.flush();
			xtw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * writes a metadata description block
	 * 
	 * @param xtw
	 * @param type
	 * @param ids
	 * @throws XMLStreamException
	 */
	private static void writeMetaData(XMLStreamWriter xtw, String type,
			String[] ids) throws XMLStreamException {
		xtw.writeStartElement(NAMESPACE, "metadata");
		xtw.writeAttribute("type", type);

		for (String id : ids) {
			xtw.writeStartElement(NAMESPACE, "id");
			xtw.writeCharacters(id.trim());
			xtw.writeEndElement(); // comment
		}

		xtw.writeEndElement(); // comment
	}

	private static void addFiles(File classDir, Set<URL> classRegisterBuilder) {
		if (classDir.listFiles() == null) {
			if (!classDir.exists()) {
				throw new RuntimeException("The file: " + classDir
						+ " does not exist");
			} else if (!classDir.isDirectory()) {
				throw new RuntimeException("The file: " + classDir
						+ " is not a directory");
			} else {
				throw new NullPointerException(
						"File.listFiles() returned null for an unknown reason");
			}
		}
		for (File file : classDir.listFiles()) {
			if (file.isDirectory()) {
				addFiles(file, classRegisterBuilder);
			} else if (file.isFile() && file.getName().endsWith(".jar")
					&& !file.getName().startsWith("workflow-api")) {
				try {
					classRegisterBuilder.add(file.toURI().toURL());
					System.out.println(file.toURI().toURL().toString());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static int optionLength(String option) {
		if (option.equals("-filename") || option.equals("-artifactId")
				|| option.equals("-groupId") || option.equals("-version")) {
			return 2;
		} else if (option.equals("-classdir")) {
			return 3;
		}
		return 0;
	}

	public static boolean validOptions(String options[][],
			DocErrorReporter reporter) {
		boolean foundFileNameOption = false;
		for (String[] opt : options) {
			if (opt[0].equals("-filename")) {
				if (foundFileNameOption) {
					reporter.printError("Only one -tag option allowed.");
					return false;
				} else {
					foundFileNameOption = true;
				}
			}
		}
		if (!foundFileNameOption) {
			reporter.printError("Usage: javadoc -filename file -artifactId bla -groupId de -version blabla-0.1 -classdir dir...");
		}
		return foundFileNameOption;
	}

}
