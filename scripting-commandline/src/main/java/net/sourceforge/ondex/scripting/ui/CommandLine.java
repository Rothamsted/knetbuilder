package net.sourceforge.ondex.scripting.ui;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import net.sourceforge.ondex.scripting.CommandCompletenessStrategy;
import net.sourceforge.ondex.scripting.FunctionException;
import net.sourceforge.ondex.scripting.InterpretationController;
import net.sourceforge.ondex.scripting.OutputPrinter;

/**
 * @author lysenkoa Implementation of a command line GUI. Supports Up/Down arrow
 *         to browse history buffer, is an output printer
 */
public class CommandLine extends JTextArea implements KeyListener,
		ComponentListener, OutputPrinter {
	private static CommandLine currentInstance;
	private static final long serialVersionUID = 424446128004520635L;
	private CommandBuffer cmdHistory;
	private String command = "";
	private int inputPos = 0;
	private String promptString;
	private Process process = new Process();
	private Print fout = new Print();
	private Prompt prompt = new Prompt();
	private ExecutorService stq = Executors.newFixedThreadPool(1);
	private Set<Character> stringEnclosingChar = new HashSet<Character>(
			Arrays.asList(new Character[] { '"', '\'' }));
	private char stringEscapeChar = "\\".charAt(0);
	private List<Character> openInputChar = new ArrayList<Character>(
			Arrays.asList(new Character[] { '{', '(', '[' }));
	private List<Character> closeInputChar = new ArrayList<Character>(
			Arrays.asList(new Character[] { '}', ')', ']' }));
	private boolean readyForInput = false;
	private Map<Character, Character> closeToOpenMatcher = new HashMap<Character, Character>();
	private static volatile Exception errorState = null;
	private static volatile boolean finished = true;
	private static Object finishedLock = new Object();
	private CommandCompletenessStrategy ccs = new DefaultCommandCompletenessStrategy();
	private boolean commandInterpreterReady = false;

	private static void setFinished(boolean f) {
		synchronized (finishedLock) {
			finished = f;
			finishedLock.notifyAll();
		}
	}

	public static CommandLine getCurrentInstance() {
		return currentInstance;
	}

	/**
     *
     */
	public CommandLine() {
		super();
		currentInstance = this;
		// this.setBackground(Color.BLACK);
		// this.setForeground(new Color(0, 122, 197));
		// this.getFont().deriveFont(Font.BOLD)
		// this.putClientProperty(new StringBuffer("AATextPropertyKey"),
		// Boolean.TRUE );
		// this.setFont(new Font("Lucida Typewriter Bold", Font.BOLD, 12));
		this.addKeyListener(this);
		this.addComponentListener(this);
		cmdHistory = new CommandBuffer(250);
		this.setEnabled(false);
		for (int i = 0; i < openInputChar.size(); i++) {
			closeToOpenMatcher.put(closeInputChar.get(i), openInputChar.get(i));
		}
	}

	public void initialize(String welcomeMsg) {

	}

	/**
	 * @param ic
	 *            add the listener that will receive commands from this source
	 */
	public void setCommandInterpreter(InterpretationController ic) {
		this.addCommandListener(ic);
		this.setText(ic.getWelcomeMessage());
		this.promptString = ic.getPrompt();
		this.prompt();
		this.setEnabled(true);
		setCommandInterpreterReady(true);

	}

	/**
	 * @param distance
	 *            parent container at the specified depth up the hirarchy
	 * @return parent at the specified depth
	 */
	public Component getParent(int distance) {
		Component p = this;
		for (int i = 0; i < distance; i++) {
			try {
				p = p.getParent();
			} catch (NullPointerException z) {
				return null;
			}

		}
		return p;
	}

	public void keyPressed(KeyEvent e) {
		if (!readyForInput) {
			e.consume();
			return;
		}
		if (e.getKeyCode() == 10) {
			command = this.getText().substring(inputPos,
					this.getText().length());
			if (!isComplete(command) || e.getModifiers() == 8) {
				return;
			}
			cmdHistory.add(command);
			e.consume();
			fireCommandEvent(new CommandEvent(this, command, this));
		}
		if (e.getKeyCode() == 38) {
			try {
				this.getDocument().remove(inputPos,
						this.getText().length() - inputPos);
			} catch (BadLocationException e1) {
			}
			this.append(cmdHistory.getPrev());
			e.consume();
		}
		if (e.getKeyCode() == 40) {
			try {
				this.getDocument().remove(inputPos,
						this.getText().length() - inputPos);
			} catch (BadLocationException e1) {
			}
			this.append(cmdHistory.getNext());
			e.consume();
		}
		if (this.getCaretPosition() <= inputPos
				|| this.getSelectionStart() < inputPos) {
			if ((this.getCaretPosition() == inputPos)) {
				if (e.getKeyCode() == 8) {
					e.consume();
				} else {
					this.setEditable(true);
				}
			} else {
				this.setEditable(false);
			}
		} else {
			this.setEditable(true);
		}
		/*
		 * if(!(e.getKeyCode() == 39 || e.getKeyCode() == 37)){
		 * if(this.getCaretPosition() < inputPos ||
		 * (this.getCaretPosition()<inputPos+1 && e.getKeyCode() == 8)){
		 * e.consume(); return; } } // 35 = end if(e.getKeyCode() == 127 &&
		 * this.getSelectionStart()<inputPos){ this.setSelectionStart(inputPos);
		 * }
		 */

	}

	public boolean isComplete(String line) {
		return ccs.isComplete(line);
	}

	private class DefaultCommandCompletenessStrategy implements
			CommandCompletenessStrategy {
		public boolean isComplete(String line) {
			Character insideString = null;
			Stack<Character> braketMatcher = new Stack<Character>();
			for (int i = 0; i < line.length(); i++) {
				if (insideString == null) {
					if (stringEnclosingChar.contains(line.charAt(i))) {
						insideString = line.charAt(i);
					} else if (openInputChar.contains(line.charAt(i))) {
						braketMatcher.push(line.charAt(i));
					} else if (!braketMatcher.isEmpty()
							&& braketMatcher.peek() == closeToOpenMatcher
									.get(line.charAt(i))) {
						braketMatcher.pop();
					}
				} else {
					if (line.charAt(i) == insideString
							&& line.charAt(i - 1) != stringEscapeChar) {
						insideString = null;
					}
				}
			}
			return braketMatcher.isEmpty();
		}
	}

	public String print(Object... info) {
		StringBuffer temp = new StringBuffer();
		for (Object o : info) {
			temp.append(o.toString());
		}
		fout.setInfo(temp.toString(), false);
		SwingUtilities.invokeLater(fout);
		return "Done.";
	}

	public void printAndPrompt(String info) {
		try {
			if (CommandLine.this.getDocument().getText(
					CommandLine.this.getDocument().getLength() - 1, 1) != "\n") {
				info = "\n" + info;
			}
		} catch (BadLocationException e) {
			info = "\n" + info;
		}
		fout.setInfo(info, true);
		SwingUtilities.invokeLater(fout);
	}

	/**
	 * promt the user for the next input
	 */
	public void prompt() {
		SwingUtilities.invokeLater(prompt);
	}

	/**
	 * @param listener
	 */
	public void addCommandListener(CommandListener listener) {
		if (listenerList == null)
			listenerList = new javax.swing.event.EventListenerList();
		listenerList.add(CommandListener.class, listener);
	}

	/**
	 * @param listener
	 */
	public void removeCommandListener(CommandListener listener) {
		if (listenerList == null)
			return;
		listenerList.remove(CommandListener.class, listener);
	}

	/**
	 * @param evt
	 *            create a command event when the new command is entered
	 */
	public void fireCommandEvent(CommandEvent evt) {
		if (listenerList == null)
			return;
		process.setVars(listenerList.getListenerList(), evt);
		errorState = null;
		setFinished(false);
		try {
			stq.execute(process);
		} catch (Exception e) {
			setFinished(true);
		}

	}

	class Print implements Runnable {
		private volatile BlockingQueue<String> info = new LinkedBlockingDeque<String>();
		private volatile BlockingQueue<Boolean> prompt = new LinkedBlockingDeque<Boolean>();

		public Print() {
		}

		public synchronized void setInfo(String data, boolean doPrompt) {
			this.prompt.add(doPrompt);
			this.info.add(data);
		}

		public synchronized void run() {
			Boolean lastPrompt = false;
			while (!info.isEmpty()) {
				readyForInput = false;
				try {
					CommandLine.this.append(info.take());
					lastPrompt = prompt.poll();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (lastPrompt == true) {
				try {
					if (CommandLine.this
							.getDocument()
							.getText(
									CommandLine.this.getDocument().getLength() - 1,
									1).charAt(0) != '\n') {
						CommandLine.this.append("\n");
					}
				} catch (BadLocationException e) {
					CommandLine.this.append("\n");
				}
				CommandLine.this.append(promptString);
				readyForInput = true;
			}
			inputPos = CommandLine.this.getDocument().getLength();
		}
	}

	class Prompt implements Runnable {
		public Prompt() {
		}

		public void run() {
			try {
				if (CommandLine.this
						.getDocument()
						.getText(
								CommandLine.this.getDocument().getLength() - 1,
								1).charAt(0) != '\n') {
					CommandLine.this.append("\n");
				}
			} catch (BadLocationException e) {
				CommandLine.this.append("\n");
			}
			CommandLine.this.append(promptString);
			inputPos = CommandLine.this.getDocument().getLength();
			readyForInput = true;
		}
	}

	class Process implements Runnable {
		private Object[] listeners;
		private CommandEvent evt;

		public Process() {
		}

		;

		public void setVars(Object[] listeners, CommandEvent evt) {
			this.listeners = listeners;
			this.evt = evt;
		}

		public void run() {
			for (int i = 0; i < listeners.length; i += 2) {
				if (listeners[i] == CommandListener.class) {
					try {
						((CommandListener) listeners[i + 1]).newCommand(evt);
					} catch (FunctionException e) {
						print(e.getMessage());
					} catch (Exception e) {
						errorState = e;
					}
				}
			}
			setFinished(true);
		}
	}

	/**
	 * @author lysenkoa Command history buffer implementation
	 */
	class CommandBuffer {
		private String[] buffer;
		private int fill = 0;
		private int current = 0;
		private boolean loop = false;

		public CommandBuffer(int size) {
			buffer = new String[size];
			buffer[0] = "";
		}

		public void add(String str) {
			if (fill == buffer.length) {
				fill = 0;
				loop = true;
			}
			buffer[fill] = str;
			current = fill;
			fill++;
		}

		public String getPrev() {
			String result = buffer[current];
			if (current != fill)
				current--;
			if (loop && current < 0)
				current = buffer.length - 1;
			else if (current < 0)
				current = 0;
			return result;
		}

		public String getNext() {
			if (current == buffer.length - 1 && loop)
				current = 0;
			else if (current == buffer.length - 1 || current == fill - 1)
				return buffer[current];
			else
				current++;
			return buffer[current];
		}

		public String makeDump() {
			String result;
			if (!loop) {
				current = 0;
				result = buffer[0];
			} else {
				current = fill;
				result = buffer[fill];
			}
			int maxVal = fill - 1;
			if (loop)
				maxVal = buffer.length - 1;
			for (int i = 0; i < maxVal; i++)
				result = result + "\n\n" + getNext();
			return result;
		}
	}

	/**
	 * Unused
	 */
	public void componentHidden(ComponentEvent arg0) {
	}

	/**
	 * Unused
	 */
	public void componentMoved(ComponentEvent arg0) {
	}

	/**
	 * Unused
	 */
	public void componentResized(ComponentEvent arg0) {
	}

	/**
	 * Unused
	 */
	public void componentShown(ComponentEvent arg0) {
	}

	/**
	 * Unused
	 */
	public void keyReleased(KeyEvent e) {
		if (!readyForInput) {
			e.consume();
			return;
		}
		if (this.getCaretPosition() < inputPos) {
			e.consume();
			return;
		}
	}

	public void keyTyped(KeyEvent e) {
		if (!readyForInput) {
			e.consume();
			return;
		}
		if (this.getCaretPosition() < inputPos) {
			e.consume();
			return;
		}
	}

	public void finalize() {
		stq.shutdownNow();
		finished = true;
	}

	public void waitForCommandCompletion() {
		synchronized (finishedLock) {
			while (!finished) {
				try {
					finishedLock.wait(100);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		if (errorState != null) {
			errorState.printStackTrace();
			errorState = null;
		}
	}

	protected synchronized void setCommandInterpreterReady(boolean isReady) {
		this.commandInterpreterReady = isReady;
	}

	/**
	 * @return true, if command interpreter is set to this {@link CommandLine}
	 *         object. Methods like {@link #fireCommandEvent(CommandEvent)} and
	 *         {@link #executeJavaScriptFile(String)} are then ready to be used.
	 */
	public synchronized boolean isCommandInterpreterReady() {
		return commandInterpreterReady;
	}

	public void setDefaultCommnadCompletenessStrategy() {
		this.ccs = new DefaultCommandCompletenessStrategy();
	}

	public void setCommandCompletenessStrategy(CommandCompletenessStrategy ccs) {
		this.ccs = ccs;
	}

	/**
	 * Executes the given JavaScript file.
	 * 
	 * @param file
	 */
	public void executeJavaScriptFile(String file) {
		try {
			String code = readTextFile(file);
			printAndPrompt(code);
			fireCommandEvent(new CommandEvent(this, code, this));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "File not found: " + file);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "IO exception: " + file);
			e.printStackTrace();
		}
	}

	/**
	 * Read a whole text file from hard disk and return as String.
	 * 
	 * @param f
	 *            File name of the text file to read.
	 * @return Content of the text file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readTextFile(String f) throws FileNotFoundException,
			IOException {

		// special case when reading from URL in applet
		InputStream in;
		if (f.startsWith("http:") || f.startsWith("https:") || f.startsWith("file:")) {
			URL url = new URL(f);
			in = url.openStream();
		} else {
			FileInputStream fstream = new FileInputStream(f);
			in = new DataInputStream(fstream);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuffer buffer = new StringBuffer();
		String strLine;
		while ((strLine = br.readLine()) != null) {
			buffer.append(strLine);
			buffer.append("\n");
		}
		in.close();
		return buffer.toString();
	}

}
