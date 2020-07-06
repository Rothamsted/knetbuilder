package net.sourceforge.ondex.tools.tab.importer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lysenkoa
 */
public class DelimitedReader extends DataReader {

    private int lineNo = 0;
    private String thisLine = null;
    private String nextLine = null;
    private String delimiter;
    private String file;
    private BufferedReader br = null;
    private boolean isOpen = false;
    private boolean isClosed = false;
    private int lastLine = Integer.MAX_VALUE;
    private Set<ParsingCondition> conditions = new HashSet<ParsingCondition>();
    private int firstLine = 0;

    public DelimitedReader(String file, String delimiter) throws Exception {
        this.delimiter = delimiter;
        this.file = file;
    }

    public DelimitedReader(String file, String delimiter, int firstLine) throws Exception {
        this.delimiter = delimiter;
        this.file = file;
        System.err.println("Starting from line " + firstLine);
        this.firstLine = firstLine;
    }

    protected void openFile(String file) throws Exception {
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(new DataInputStream(new FileInputStream(file)));
            br = new BufferedReader(in);
            thisLine = null;
            nextLine = br.readLine();
            while (nextLine != null) {
                if (checkConditions(nextLine.split(delimiter))) {
                    break;
                }
                nextLine = br.readLine();
            }
            lineNo = 0;
            isOpen = true;
            if (nextLine == null) {
                try {
                    br.close();
                    isClosed = true;
                } catch (Exception e1) {
                }
            }
        } catch (Exception e) {
            try {
                br.close();
                isOpen = false;
                isClosed = true;
            } catch (Exception e1) {
            }
            throw new RuntimeException("Probelms encountered when attempting to open " + file + "\n Check that the file exists and is not locked.");
        }
    }

    @Override
    public boolean hasNext() {
        return (!isClosed && nextLine != null && lastLine >= lineNo);
    }

    @Override
    public String[] readLine() {
        if (!isOpen && !isClosed) {
            try {
                openFile(this.file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (firstLine > 0 && lineNo < firstLine)
            setLine(firstLine);
        if (br == null)
            throw new IllegalStateException("You have read past the end of this file!");
        try {
            if (lastLine < lineNo)
                throw new IllegalStateException("You have read past the line limit restriction!");
            thisLine = nextLine;
            nextLine = br.readLine();
            if (nextLine != null) {
                while (nextLine != null && !checkConditions(nextLine.split(delimiter))) {
                    nextLine = br.readLine();
                }
            } else
                this.close();
            lineNo++;
            return thisLine.split(delimiter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLine(int lineNumber) {
        if (lastLine < lineNo)
            lineNumber = lastLine;
        while (hasNext() != false && lineNo < lineNumber) {
            lineNo++;
            readLine();
        }
    }

    @Override
    public void close() {
        try {
            br.close();
        }
        catch (Exception e) {
        }
        br = null;
        isClosed = true;
        isOpen = false;
    }

    @Override
    public void reset() {
        try {
            if (br != null)
                br.close();
        }
        catch (Exception e) {
        }
        try {
            this.openFile(file);
            isClosed = false;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public boolean isOpen() {
        return isOpen && !isClosed;
    }


    @Override
    public void setLastLine(int lineNumber) {
        lastLine = lineNumber;
    }

    public void addCondition(ParsingCondition c) {
        conditions.add(c);
    }

    private boolean checkConditions(String[] line) {
        if (conditions.size() == 0) {
            return true;
        } else {
            boolean result = true;
            for (ParsingCondition c : conditions) {
                result = c.check(line);
                if (!result)
                    break;
            }
            return result;
        }
    }

    public static DataReader getDelimitedReader(String file, String delimiter) throws Exception {
        return new DelimitedReader(file, delimiter);
    }

    public static DataReader getDelimitedReader(String file, int fromLine) throws Exception {
        DelimitedReader result = new DelimitedReader(file, "	");
        result.setLine(fromLine);
        return result;
    }

    public static DataReader getDelimitedReader(String file, String delimiter, int fromLine) throws Exception {
        DelimitedReader result = new DelimitedReader(file, delimiter);
        result.setLine(fromLine);
        return result;
    }
}
