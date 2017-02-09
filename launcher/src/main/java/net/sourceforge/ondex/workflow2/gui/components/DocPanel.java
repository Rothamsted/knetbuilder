package net.sourceforge.ondex.workflow2.gui.components;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class DocPanel extends JTabbedPane implements ComponentListener {

    private static final Pattern sizeAdjustReplace = Pattern.compile("width=\"[0-9]+\"");

    public static enum State {
        DOCKED, WINDOWED, HIDDEN
    }

    private final JTextPane doc = new JTextPane();
    private final JTextPane arguments = new JTextPane();
    private final JTextPane data = new JTextPane();

    private final JTextPane comment = new JTextPane();
    private State currentState;
    private final JFrame parentFrame;
    private final JPopupMenu menu = new JPopupMenu();
    private final PopupMouseListener pl = new PopupMouseListener();

    public DocPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.currentState = State.DOCKED;
        this.addTab("Arguments", getPaddedComponent(new JScrollPane(arguments)));
        this.addTab("Documentation", getPaddedComponent(new JScrollPane(doc)));
        this.addTab("Required data", getPaddedComponent(new JScrollPane(data)));
        this.addTab("Comments", getPaddedComponent(new JScrollPane(comment)));
        this.setSelectedIndex(0);
        doc.getParent().getParent().getParent().addComponentListener(this);
        doc.setEditable(false);
        doc.setContentType("text/html");
        doc.addHyperlinkListener(new HyperlinkListen());

        arguments.setEditable(false);
        arguments.setContentType("text/html");
        arguments.addHyperlinkListener(new HyperlinkListen());

        data.setEditable(false);
        data.setContentType("text/html");
        data.addHyperlinkListener(new HyperlinkListen());

        configureTextArea(doc);
        configureTextArea(comment);
        configureMenu();
    }

    public void configureMenu() {
        JMenuItem menuItem = new JMenuItem("Show in main frame");
        menu.add(menuItem);
        menuItem = new JMenuItem("Show in window");
        menu.add(menuItem);
        menuItem = new JMenuItem("Hide");
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Show in main frame")) {
                    DocPanel.this.dock();
                } else if (e.getActionCommand().equals("Show in window")) {
                    DocPanel.this.showInWindow();
                } else if (e.getActionCommand().equals("Hide")) {
                    DocPanel.this.hide();
                }
            }
        });

        this.addMouseListener(pl);
        doc.addMouseListener(pl);
        comment.addMouseMotionListener(pl);
    }

    private class PopupMouseListener extends MouseAdapter {
        public void mouseReleased(MouseEvent event) {
            if (event.isPopupTrigger()) {
                menu.show(event.getComponent(), event.getX(), event.getY());
            }
        }
    }


    public void setDocumentation(String text) {
        int newWidth = doc.getParent().getWidth() < 200 ? 200 : doc.getParent().getWidth();
        String text1 = sizeAdjustReplace.matcher(text).replaceAll("width=\"" + (newWidth - 45) + "\"");
        doc.setText(text1);
        doc.setCaretPosition(0);
        doc.validate();
    }

    public void setArguments(String text) {
        int newWidth = arguments.getParent().getWidth() < 200 ? 200 : doc.getParent().getWidth();
        String text1 = sizeAdjustReplace.matcher(text).replaceAll("width=\"" + (newWidth - 45) + "\"");
        arguments.setText(text1);
        arguments.setCaretPosition(0);
        arguments.validate();
    }

    public void setData(String text) {
        int newWidth = data.getParent().getWidth() < 200 ? 200 : doc.getParent().getWidth();
        String text1 = sizeAdjustReplace.matcher(text).replaceAll("width=\"" + (newWidth - 45) + "\"");
        data.setText(text1);
        data.setCaretPosition(0);
        data.validate();
    }

    public void setComment(String text) {
        comment.setText(text);
        comment.setCaretPosition(0);
        comment.validate();
    }

    public String getComment() {
        return comment.getText();
    }

    private void configureTextArea(JTextPane t) {
        t.setMargin(new Insets(5, 5, 5, 5));
    }

    private JPanel getPaddedComponent(Component comp) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.fill = GridBagConstraints.BOTH;
        con.weightx = 1;
        con.weighty = 1;
        con.gridx = 0;
        con.gridy = 0;
        con.insets = new Insets(2, 2, 3, 3);
        p.add(comp, con);
        return p;
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        this.setDocumentation(doc.getText());
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    public void dock() {
        switch (currentState) {
            case DOCKED:
                break;
            case WINDOWED:
                break;
            case HIDDEN:
                break;
        }
    }

    public void showInWindow() {
        switch (currentState) {
            case DOCKED:
                break;
            case WINDOWED:
                break;
            case HIDDEN:
                break;
        }
    }

    public void hide() {
        switch (currentState) {
            case DOCKED:
                break;
            case WINDOWED:
                break;
            case HIDDEN:
                break;
        }
    }

    class HyperlinkListen implements HyperlinkListener {

        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                URL url = e.getURL();
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE))
                    try {
                        desktop.browse(url.toURI());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
            }
        }
    }
}