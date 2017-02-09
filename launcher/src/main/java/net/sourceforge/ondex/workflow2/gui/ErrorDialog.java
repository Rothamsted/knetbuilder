package net.sourceforge.ondex.workflow2.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author weilej
 */
public class ErrorDialog extends JDialog implements ActionListener {

    //####FIELDS####

    private JPanel topPanel, bottomPanel;

    private JScrollPane centerPanel;

    private JButton moreLess;

    private BufferedImage errorImg;

    private Throwable throwable;

    private Thread thread;

    private boolean running;

    private Dimension minDim, maxDim;

    /**
     * serial id.
     */
    private static final long serialVersionUID = 3398440747424344511L;

    //####CONSTRUCTOR####

    private ErrorDialog(boolean running, Throwable throwable, Thread thread, Frame f) {
        super(f);
        this.throwable = throwable;
        this.thread = thread;
        this.running = running;
        setupGUI();
    }


    //####METHODS####

    private void setupGUI() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        topPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = makeImgPanel();
        if (leftPanel != null) {
            topPanel.add(leftPanel, BorderLayout.WEST);
        }
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(makeMsgPanel(), BorderLayout.CENTER);
        rightPanel.add(makeMoreLessPanel(), BorderLayout.SOUTH);
        topPanel.add(rightPanel, BorderLayout.CENTER);

        centerPanel = createStackPanel();
        bottomPanel = createButtonPanel();

        less();

        int w_self = getWidth() + 100;
        int h_self = getHeight();
        int x, y, w, h;

        if (running) {
            x = getParent().getX();
            y = getParent().getY();
            w = getParent().getWidth();
            h = getParent().getHeight();
        } else {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            x = 0;
            y = 0;
            w = screen.width;
            h = screen.height;
        }
        this.setBounds(x + (w - w_self) / 2, y + (h - h_self) / 2, w_self, h_self);

        minDim = getSize();
        maxDim = new Dimension(getWidth(), getHeight() + 100);

        topPanel.setMaximumSize(new Dimension(1280, topPanel.getHeight()));
        bottomPanel.setMaximumSize(bottomPanel.getSize());

        setVisible(true);
    }

    private JScrollPane createStackPanel() {

        StringBuilder b = new StringBuilder();

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
        String date = df.format(new Date(System.currentTimeMillis()));

        b.append("Date: ");
        b.append(date);
        b.append('\n');

        String arch = System.getProperty("os.arch");
        String osname = System.getProperty("os.name");
        String osversion = System.getProperty("os.version");

        b.append("System:");
        b.append(arch);
        b.append(' ');
        b.append(osname);
        b.append(" v");
        b.append(osversion);
        b.append("\n");

        buildStackTrace(b, throwable);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setText(b.toString());
        return new JScrollPane(area);
    }

    private void buildStackTrace(StringBuilder b, Throwable t) {
//		String threadname = (thread != null)? thread.getName() : "unknown";
//		b.append(t.toString()+" in Thread "+threadname+"\n");
//		for (StackTraceElement e : throwable.getStackTrace()) {
//			b.append("        at "+e.getClassName()+"("+e.getFileName()+":"+e.getLineNumber()+")"+"\n");
//		}
//		if (t.getCause() != null) {
//			b.append("Caused by: ");
//			buildStackTrace(b, t.getCause());
//		}
//		return b;
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        t.printStackTrace(pw);
        b.append(writer.toString());
    }

    private JPanel makeMoreLessPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        moreLess = makeButton("Details >>", "more");
        p.add(moreLess);
        return p;
    }

    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(makeButton("OK", "ok"));
        return p;
    }

    private JButton makeButton(String title, String actionCommand) {
        JButton button = new JButton(title);
        button.setActionCommand(actionCommand);
        button.addActionListener(this);
        return button;
    }

    private JPanel makeMsgPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));

        String msg = (throwable.getMessage() != null) ? throwable.getMessage() : throwable.toString();
        rightPanel.add(new JLabel(" "));
        rightPanel.add(new JLabel("An error occurred:"));
        rightPanel.add(new JLabel(msg));

        return rightPanel;
    }

    private JPanel makeImgPanel() {
        String s = File.separator;
        File imgFile = new File("config" + s + "themes" + s + "default" + s + "icons" + s + "error25.png");
        if (imgFile.exists() && imgFile.canRead()) {
            try {
                errorImg = ImageIO.read(imgFile);
                if (errorImg != null) {
                    JPanel leftPanel = new JPanel() {

                        private static final long serialVersionUID = 858217031036743682L;

                        public void paint(Graphics g) {
                            super.paint(g);
                            Graphics2D g2 = (Graphics2D) g;
                            g2.drawImage(errorImg, null, 10, 17);
                        }

                    };
                    Dimension d = new Dimension(errorImg.getWidth() + 20,
                            errorImg.getHeight() + 20);
                    leftPanel.setMinimumSize(d);
                    leftPanel.setMaximumSize(d);
                    leftPanel.setPreferredSize(d);
                    leftPanel.setSize(d);
                    return leftPanel;
                }
            } catch (IOException ioe) {
                return null;
            }
        }
        return null;
    }

    private void less() {
        moreLess.setText("Details >>");
        moreLess.setActionCommand("more");

        getContentPane().removeAll();
        getContentPane().add(topPanel);
        getContentPane().add(bottomPanel);

        pack();
        if (minDim != null) {
            setSize(minDim);
        }
    }

    private void more() {
        moreLess.setText("<< Details");
        moreLess.setActionCommand("less");

        getContentPane().removeAll();
        getContentPane().add(topPanel);
        getContentPane().add(centerPanel);
        getContentPane().add(bottomPanel);

        setSize(maxDim);
        validate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("more")) {
            more();
        } else if (cmd.equals("less")) {
            less();
        } else if (cmd.equals("ok")) {
            dispose();
        }

    }

    public static void show(boolean running, Throwable throwable, Thread thread, Frame f) {
        new ErrorDialog(running, throwable, thread, f);
    }

    public static void show(Throwable throwable, Frame f) {
        new ErrorDialog(true, throwable, null, f);
    }

}