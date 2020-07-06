//package net.sourceforge.ondex.ovtk2.util.chemical;
//
//import java.awt.BorderLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import javax.swing.BoxLayout;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JTextField;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.transform.Result;
//import javax.xml.transform.Source;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//
//import net.sourceforge.ondex.parser.chemblactivity.Parser;
//import net.sourceforge.ondex.parser.chemblactivity.Parser.EXMODE;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
///**
// * Present the user a GUI for filtering on certian XML document values
// * 
// * @author taubertj
// * 
// */
//public class BioactivitiesDocumentFilter extends JPanel implements
//		ActionListener {
//
//	/**
//	 * generated
//	 */
//	private static final long serialVersionUID = 811435531256115678L;
//
//	Map<String, Document> docs;
//
//	Map<String, Set<Element>> filtered;
//
//	EXMODE mode;
//
//	JLabel info;
//
//	JComboBox typeBox;
//
//	JComboBox unitBox;
//
//	JComboBox comparator;
//
//	JTextField numberField;
//
//	public BioactivitiesDocumentFilter(Map<String, Document> docs, EXMODE mode) {
//		super();
//
//		this.docs = docs;
//		this.filtered = new HashMap<String, Set<Element>>();
//		this.mode = mode;
//
//		initGUI();
//
//	}
//
//	/**
//	 * Some debug output of XML
//	 * 
//	 * @param document
//	 */
//	private void debugXML(Document document) throws Exception {
//		TransformerFactory tranFactory = TransformerFactory.newInstance();
//		Transformer aTransformer = tranFactory.newTransformer();
//		Source src = new DOMSource(document);
//		Result dest = new StreamResult(System.out);
//		aTransformer.transform(src, dest);
//	}
//
//	/**
//	 * Transforms internal filter results into document based structure
//	 * 
//	 * @return
//	 * @throws Exception
//	 */
//	public Map<String, Document> getFiltered() throws Exception {
//
//		// prepare for assembling all elements back into a document
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = factory.newDocumentBuilder();
//
//		Map<String, Document> results = new HashMap<String, Document>();
//		for (String key : filtered.keySet()) {
//			Set<Element> elements = filtered.get(key);
//			if (elements.size() > 0) {
//				Document document = builder.newDocument();
//				Element root = (Element) document.createElement("list");
//				document.appendChild(root);
//				for (Element e : elements) {
//					Node n = document.importNode(e, true);
//					root.appendChild(n);
//				}
//				// debugXML(document);
//				results.put(key, document);
//			}
//		}
//		return results;
//	}
//
//	private void initGUI() {
//
//		// simple layout of page boxes
//		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
//		this.setLayout(layout);
//
//		int number = 0;
//		Set<String> types = new HashSet<String>();
//		Set<String> units = new HashSet<String>();
//		for (String key : docs.keySet()) {
//			Document doc = docs.get(key);
//
//			// get matching activity elements
//			Set<Element> set = extractElements(doc);
//			number += set.size();
//			for (Element eElement : set) {
//				String type = Parser.getTagValue("bioactivity__type", eElement);
//				types.add(type);
//
//				String unit = Parser.getTagValue("units", eElement);
//				units.add(unit);
//
//				// if all matches add to map
//				if (!filtered.containsKey(key))
//					filtered.put(key, new HashSet<Element>());
//				filtered.get(key).add(eElement);
//			}
//		}
//		this.add(new JLabel("Matching documents: " + number));
//
//		// panel for selecting type
//		JPanel typePanel = new JPanel(new BorderLayout());
//		this.add(typePanel);
//		typePanel.add(new JLabel("Bioactivity type:"), BorderLayout.WEST);
//
//		// sort all types
//		String[] typeArray = types.toArray(new String[0]);
//		Arrays.sort(typeArray);
//		String[] allTypeArray = new String[typeArray.length + 1];
//		allTypeArray[0] = "NONE";
//		for (int i = 0; i < typeArray.length; i++) {
//			allTypeArray[i + 1] = typeArray[i];
//		}
//
//		// put type box together
//		typeBox = new JComboBox(allTypeArray);
//		typeBox.setSelectedIndex(0);
//		typeBox.addActionListener(this);
//		typePanel.add(typeBox, BorderLayout.EAST);
//
//		// panel for selecting unit
//		JPanel unitPanel = new JPanel(new BorderLayout());
//		this.add(unitPanel);
//		unitPanel.add(new JLabel("Units:"), BorderLayout.WEST);
//
//		// sort all units
//		String[] unitArray = units.toArray(new String[0]);
//		Arrays.sort(unitArray);
//		String[] allUnitArray = new String[unitArray.length + 1];
//		allUnitArray[0] = "NONE";
//		for (int i = 0; i < unitArray.length; i++) {
//			allUnitArray[i + 1] = unitArray[i];
//		}
//
//		// put unit box together
//		unitBox = new JComboBox(allUnitArray);
//		unitBox.setSelectedIndex(0);
//		unitBox.addActionListener(this);
//		unitPanel.add(unitBox, BorderLayout.EAST);
//
//		// panel for providing a matching value
//		JPanel valuePanel = new JPanel(new BorderLayout());
//		this.add(valuePanel);
//		comparator = new JComboBox(new String[] { "<", "=", ">" });
//		comparator.addActionListener(this);
//		valuePanel.add(comparator, BorderLayout.WEST);
//		numberField = new JTextField(20);
//		numberField.addActionListener(this);
//		valuePanel.add(numberField, BorderLayout.EAST);
//
//		// this will be the result line
//		info = new JLabel("Matching documents after filter: " + number);
//		this.add(info);
//	}
//
//	/**
//	 * Extract all bioactivity elements from Document
//	 * 
//	 * @param doc
//	 * @return
//	 */
//	private Set<Element> extractElements(Document doc) {
//
//		// get all bioactivity elements
//		Set<Element> elements = new HashSet<Element>();
//		NodeList nList = doc.getElementsByTagName("bioactivity");
//
//		// iterate over all activities
//		for (int temp = 0; temp < nList.getLength(); temp++) {
//
//			Node nNode = nList.item(temp);
//			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//
//				Element eElement = (Element) nNode;
//				elements.add(eElement);
//			}
//		}
//
//		return elements;
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent e) {
//		filtered.clear();
//
//		// get filter values
//		String filterType = typeBox.getSelectedItem().toString();
//		String filterUnit = unitBox.getSelectedItem().toString();
//
//		for (String key : docs.keySet()) {
//			Document doc = docs.get(key);
//
//			// get matching activity elements
//			Set<Element> set = extractElements(doc);
//
//			for (Element eElement : set) {
//
//				// first filter by activity type
//				String type = Parser.getTagValue("bioactivity__type", eElement);
//				if (filterType.equals("NONE") || filterType.equals(type)) {
//
//					// second filter by unit
//					String unit = Parser.getTagValue("units", eElement);
//					if (filterUnit.equals("NONE") || filterUnit.equals(unit)) {
//
//						if (numberField.getText().trim().length() == 0) {
//							// if all matches add to map
//							if (!filtered.containsKey(key))
//								filtered.put(key, new HashSet<Element>());
//							filtered.get(key).add(eElement);
//						} else {
//
//							// parse value
//							String value = Parser
//									.getTagValue("value", eElement);
//							if (value != null) {
//								try {
//									double v = Double.parseDouble(value);
//									double n = Double.parseDouble(numberField
//											.getText());
//
//									// filter numerical value here
//									boolean pass = false;
//									if (comparator.getSelectedItem()
//											.equals("<")) {
//										pass = v < n;
//									} else if (comparator.getSelectedItem()
//											.equals("=")) {
//										pass = v == n;
//									} else if (comparator.getSelectedItem()
//											.equals(">")) {
//										pass = v > n;
//									}
//
//									if (pass) {
//										// if all matches add to map
//										if (!filtered.containsKey(key))
//											filtered.put(key,
//													new HashSet<Element>());
//										filtered.get(key).add(eElement);
//									}
//
//								} catch (NumberFormatException nfe) {
//
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//		// calculate new number after filter
//		int number = 0;
//		for (String key : filtered.keySet()) {
//			number += filtered.get(key).size();
//		}
//		info.setText("Matching documents after filter: " + number);
//		info.revalidate();
//	}
//
//}
