//package net.sourceforge.ondex.ovtk2.annotator.graphalgo;
//
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JTextField;
//import javax.swing.SpringLayout;
//import javax.swing.event.CaretEvent;
//import javax.swing.event.CaretListener;
//
//import net.sourceforge.ondex.ONDEXPluginArguments;
//import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
//import net.sourceforge.ondex.ovtk2.config.Config;
//import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
//import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
//import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
//import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
//import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;
//import net.sourceforge.ondex.transformer.graphalgo.ArgumentNames;
//import net.sourceforge.ondex.transformer.graphalgo.GraphAlgoTransformer;
//
///**
// * Provides different graph algorithms to annotate concepts and / or relations
// * with statistical properties.
// * 
// * @author taubertj
// */
//public class GraphAlgoAnnotator extends OVTK2Annotator implements
//		ActionListener {
//
//	/**
//	 * generated
//	 */
//	private static final long serialVersionUID = 1772141399433729760L;
//
//	private String[] algorithms = new String[] { "allcliques",
//			"connectedcomponents", "cutnodes", "minimalequivalent",
//			"stronglyconnected" };
//
//	private JComboBox box = null;
//
//	private JTextField text = null;
//
//	private JButton button = new JButton("Annotate Graph");
//
//	/**
//	 * Annotator has been used
//	 */
//	private boolean used = false;
//
//	public GraphAlgoAnnotator(OVTK2PropertiesAggregator viewer) {
//		super(viewer);
//
//		initGUI();
//	}
//
//	@Override
//	public String getName() {
//		return Config.language.getProperty("Name.Menu.Annotator.GraphAlgo");
//	}
//
//	private void initGUI() {
//
//		setLayout(new SpringLayout());
//
//		add(new JLabel("Select an algorithm:"));
//
//		box = new JComboBox(algorithms);
//		box.setSelectedIndex(0);
//		add(box);
//
//		add(new JLabel("Enter an identifier:"));
//
//		text = new JTextField(20);
//		text.addCaretListener(new CaretListener() {
//			@Override
//			public void caretUpdate(CaretEvent e) {
//				if (text.getText().trim().length() > 0)
//					button.setEnabled(true);
//				else
//					button.setEnabled(false);
//			}
//		});
//		add(text);
//
//		button.setEnabled(false);
//		button.addActionListener(this);
//		add(button);
//
//		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
//				5, 5, 5);
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent e) {
//		execute();
//		used = true;
//	}
//
//	private void execute() {
//		String name = box.getSelectedItem().toString();
//		name = "net.sourceforge.ondex.transformer.graphalgo." + name
//				+ ".Transformer";
//		try {
//			Class<?> c = GraphAlgoTransformer.class.getClassLoader().loadClass(
//					name);
//			final GraphAlgoTransformer agat = (GraphAlgoTransformer) c
//					.newInstance();
//
//			final ONDEXPluginArguments ta = new ONDEXPluginArguments(
//					agat.getArgumentDefinitions());
//			ta.addOption(ArgumentNames.IDENTIFIER_ARG, text.getText());
//
//			IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
//				public void task() {
//					agat.setArguments(ta);
//					agat.setONDEXGraph(graph);
//					try {
//						agat.start();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			};
//			p.start();
//			OVTKProgressMonitor
//					.start(OVTK2Desktop.getInstance().getMainFrame(),
//							"Calculating", p);
//
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	public boolean hasBeenUsed() {
//		return used;
//	}
//}
