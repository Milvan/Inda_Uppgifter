// Copyright 2011 Stefan Nilsson. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.
package computer;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/* An application (and applet) that simulates a simple computer.
 * Based on an example from J. Glenn Brookshear. Computer science:
 * an overview, fifth edition, Addison-Wesley, 1997.
 *
 * Stefan Nilsson (snilsson@nada.kth.se)
 * Version 1.0, 2 April 1997
 * Version 1.1, 29 November 2007
 * Version 1.2, 31 January 2011
 *
 * Bug fix: 1 April 2003, Fixed Rotate and Move
 * Update: 29 August 2007, Removed deprecated features from JDK 1.0.*
 * Update: 31 August 2007, Updated to Swing user interface
 * Update: 23 September 2009, Using interrupt mechanism in runner thread
 * Update: 31 January 2011, New instructions, assembler, stand alone version, hg
 */

public class Nic extends JApplet {
    public final static String NAME = "nic";
    public final static String VERSION = "1.2-10";
    private final static String USAGE = "Usage: " + NAME + " [-v] [FILE]";
    Computer mc;

    /*
    // Writes ff, fe, ..., 2, 1, 0 to memory at address n.

            word n

            loadc   r0 -1
            loadc   r1 -1

    Loop:   store   r1 n        // write r1 to memory address n
            addc    r1 -1       // r1--
            jumpn   r1 Loop     // if r1 != -1 goto Loop

    */
    private final static String prog = "20ff21ff411c81fff1090000f00000";

    /* Executed when this program runs as an Applet. */
    public void init() {
        mc = new Computer();
        mc.RAM.load(0, prog);
        add(mc);
    }

    /* Executed when this program runs as an application. */
    public static void main(String[] args) {
        int len = args.length;
        int n = 0;
        boolean printVersion = len >= 1 && args[0].equals("-v");

        if (printVersion) {
            System.out.println(NAME + " version " + VERSION);
            n = 1;
        }
        
        if (!printVersion && len > 1 || len > 2) {
            System.out.println(USAGE);
            return;
        }

        Computer mc = new Computer();
        MainFrame frame = new MainFrame("NIC", mc);
        frame.getContentPane().add("Center", mc);
        frame.pack();

        Rectangle screenSize = frame.getGraphicsConfiguration().getBounds();
        Dimension size = frame.getPreferredSize();
        frame.setLocation((int) (screenSize.width/2 - size.getWidth()/2), 0);

        frame.setVisible(true);

        if (n < len) {
            frame.load(new File(args[n]));
        }
    }
}

/* Provides a window if this program is run as an application. */
class MainFrame extends JFrame {
    private JFileChooser chooser;
    private JMenuItem reload;
    private Computer mc;
    private File lastFile = null;

    private static class BiFilter extends javax.swing.filechooser.FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            return name.endsWith(".bi");
        }
 
        public String getDescription() {
            return "*.bi";
        }
    }

    MainFrame(String title, Computer mc) {
        super(title);
        this.mc = mc;
        
        this.chooser = new JFileChooser(System.getProperty("user.dir"));
        BiFilter filter = new BiFilter();
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem open = new JMenuItem("Open...", KeyEvent.VK_O);
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { openFile(); }
        });
        fileMenu.add(open);

        reload = new JMenuItem("Reload", KeyEvent.VK_R);
        reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        reload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (lastFile != null) load(lastFile); }
        });
        reload.setEnabled(false);
        fileMenu.add(reload);

        fileMenu.add(new JSeparator());

        JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_Q);
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { System.exit(0); }
        });
        fileMenu.add(quit);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        JMenuItem about = new JMenuItem("About NIC");
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainFrame.this,
                "The Nada Instructional Computer\nVersion " + Nic.VERSION +
                "\n\nhttp://www.nada.kth.se/~snilsson/NIC/" +
                "\n\nCopyright © 2011 Stefan Nilsson" + 
                "\nAll rights reserved",
                "About NIC",
                JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(about);

        setJMenuBar(menuBar);
    }

    private void openFile() {
        int val = chooser.showOpenDialog(this);

        if (val != JFileChooser.APPROVE_OPTION) { // cancelled
            return;
        }

        load(chooser.getSelectedFile());
    }
    
    public void load(File f) {
        String fname = f.getName();
        String prog = readFile(f);
        if (prog.equals(""))
            return;

        if (!prog.startsWith("1f1f1f1f")) {
            mc.message.setText("Wrong file format: " + fname);
            return;
        }
        prog = prog.substring(8);

        String progName = fname;
        if (fname.endsWith(".bi")) {
            progName = fname.substring(0, fname.lastIndexOf(".bi"));
        }

        int len = prog.length();
        if (len > 256) {
            mc.message.setText(progName + " too big (" + len + " bytes)");
            return;
        }

        mc.reset();
        mc.RAM.load(0, prog);
        setTitle("NIC " + progName);
        mc.message.setText("Loaded " + fname);
        mc.runButton.requestFocusInWindow();
        reload.setEnabled(true);
        reload.setText("Reload " + fname);
        lastFile = f;
        return;
    }
    
    private String readFile(File f) {
        String fname = f.getName();
        BufferedReader in = null; 
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            // in = new BufferedReader(new FileReader(f));
            String prog = in.readLine();
            if (prog == null) {
                mc.message.setText("Cannot read " + fname);
                return "";
            }
            return prog;

        } catch (FileNotFoundException e) {
            mc.message.setText("Cannot find " + fname);
 
        } catch (IOException e) {
            mc.message.setText("Cannot open " + fname);
 
        } finally {
            try {
                if (in != null) 
                    in.close();
            } catch (IOException e) {
                // mc.message.setText("Error closing " + f);
            }
        }
        return "";        
    }
}


/********************* Simulator ************************/

/**
 * A RAM memory cell consisting of 4 bits.
 * Any number outside the range 0..15 will be truncated before
 * it is written to the memory. The cell sends a message to its
 * observer (a component in the GUI) whenever it is updated.
 * The memory cell can be updated both by the CPU and the GUI
 * and hence the access methods need to be synchronized.
 */
class MemoryCell extends Observable {
    private int value;
    private boolean active;

    MemoryCell(int value) {
        this.value = value;
    }

    synchronized void set(int b) {
        value = b & 0xf;
        setChanged();      // Tell the observer in the GUI
        notifyObservers(); // that the value has been updated.
    }

    synchronized void setActive(boolean b) {
        active = b;
        setChanged();      // Tell the observer in the GUI
        notifyObservers(); // that the value has been updated.
    }

    synchronized boolean isActive() {
        return active;
    }

    synchronized int get() {
        return value;
    }
}

/* A random access memory, consisting of 256 4-bit memory cells.
 * The access methods have been declared synchronized to allow
 * more than one CPU to use the same memory.
 */
class MainMemory {
    final static int size = 256;
    final MemoryCell memory[];

    MainMemory() {
        memory = new MemoryCell[size];
        for (int adr = 0; adr < size; ++adr)
            memory[adr] = new MemoryCell(0);
    }

    synchronized void set(int adr, int b) {
        memory[adr].set(b);
    }

    synchronized int get(int adr) {
        return memory[adr].get();
    }
    
    void clear() {
        for (int adr = 0; adr < size; ++adr)
            set(adr, 0);
    }

    /* Load the hexadecimal digits of the string s into
     positions adr, adr + 1, ... of the RAM. */
    void load(int adr, String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int value = 0;
            if ('0' <= ch && ch <= '9')
                value = ch - '0';
            if ('a' <= ch && ch <= 'f')
                value = 10 + ch - 'a';
            if ('A' <= ch && ch <= 'F')
                value = 10 + ch - 'A';
            set((adr + i) % size, value);
        }
    }
}

/* A register that holds one number (either 8-bit or 16-bit).
 * The register sends a message to its observer (a component
 * in the GUI) every time the value of the register is updated.
 * The access methods are synchronized, since a register can
 * be accessed both by the CPU and the GUI.
 */
class Register extends Observable {
    protected int value;

    Register(int value) {
        this.value = value;
    }

    synchronized void set(int b) {
        value = b;
        setChanged();      // Tell the observer in the GUI
        notifyObservers(); // that the value has been updated.
    }

    synchronized int get() {
        return value;
    }
}

class PCRegister extends Register {
    private MainMemory mem;

    PCRegister(int value, MainMemory mem) {
        super(value);
        this.mem = mem;
    }

    synchronized void set(int b) {
        for (int i = value; i < value + 4; i++)
            mem.memory[i].setActive(false);
        for (int i = b; i < b + 4; i++)
            mem.memory[i].setActive(true);
        super.set(b);           
    }
}

/* A simple CPU.
 * The CPU has 16 general purpose registers, a program counter (pc),
 * and an instruction register (ir).
 * When the CPU is constructed it is connected to a RAM.
 * (It is possible to connect more than one CPU to the same RAM.)
 * There are two methods for interacting with the CPU:
 *
 *   -  fetch() fetches the next instruction from the RAM into
 *      the ir and increments the pc.
 *   -  execute() executes the command in the ir.
 *
 * The fetch() and execute() methods return the following status codes:
 *
 *  SUCCESS: the method completed successfully.
 *  HALT: a halt-instrcution was encountered.
 *  BAD_INSTRUCTION: an incorrect instruction was encounterd.
 *  BAD_ALIGNMENT: an attempt to read or write at an unaligned position.
 */
class Processor {
    // op-codes
    final static int opHalt      = 0x0;
    final static int opLoadMem   = 0x1;
    final static int opLoadConst = 0x2;
    final static int opLoadReg   = 0x3;
    final static int opStore     = 0x4;
    final static int opStoreReg  = 0x5;
    final static int opMove      = 0x6;
    final static int opAddInt    = 0x7;
    final static int opAddConst  = 0x8;
    final static int opMulInt    = 0x9;
    final static int opSubInt    = 0xa;
    final static int opRShift    = 0xb;
    final static int opAND       = 0xc;
    final static int opOR        = 0xd;
    final static int opXOR       = 0xe;
    final static int opJump      = 0xf;

    // return codes
    final static int SUCCESS             = 0x0;
    final static int HALT                = 0x1;
    final static int BAD_INSTRUCTION     = 0x2;
    final static int BAD_ALIGNMENT       = 0x3;

    // registers
    final Register reg[];   // general purpose registers
    final Register pc;      // program counter
    final Register ir;      // instruction register

    // The main memory attached to the processor
    final MainMemory mem;

    /* Create a new processor and attached it to main memory */
    public Processor(MainMemory mem) {
        this.mem = mem;
        reg = new Register[16];
        for (int i = 0; i < reg.length; ++i)
            reg[i] = new Register(0);
        pc = new PCRegister(0, mem);
        pc.set(0);
        ir = new Register(0);
    }
    
    public void reset() {
        pc.set(0);
        ir.set(0);
        for (int i = 0; i < reg.length; ++i)
            reg[i].set(0);
    }

    /* Retrieve the next operation from memory
     * and then increment the program counter.
     */
    public int fetch() {
        int adr = pc.get();
        if (adr % 4 != 0) return BAD_ALIGNMENT;
        ir.set((mem.get(adr)<<12) + (mem.get(adr+1)<<8) +
                (mem.get(adr+2)<<4) + mem.get(adr+3));
        pc.set((adr + 4) % MainMemory.size);
        return SUCCESS;
    }

    /* Decode the bit pattern in the instruction register
     * and execute the command.
     */
    public int execute() {
        int instruction = ir.get();
        int opCode = (instruction & 0xf000) >>> 12;
        int field1 = (instruction & 0x0f00) >>> 8;
        int field2 = (instruction & 0x00f0) >>> 4;
        int field3 = instruction & 0x000f;

        switch (opCode) {
        case opHalt:
            return halt();
        case opLoadMem:
            return loadMem(field1, (field2<<4) + field3);
        case opLoadConst:
            return loadConst(field1, (field2<<4) + field3);
        case opLoadReg:
            return loadReg(field2, field3);
        case opStore:
            return store(field1, (field2<<4) + field3);
        case opStoreReg:
            return storeReg(field2, field3);
        case opMove:
            return move(field2, field3);
        case opAddInt:
            return addInt(field1, field2, field3);
        case opAddConst:
            return addConst(field1, (field2<<4) + field3);
        case opMulInt:
            return mulInt(field1, field2, field3);
        case opSubInt:
            return subInt(field1, field2, field3);
        case opRShift:
            return shift(field1, field2, field3);
        case opAND:
            return AND(field1, field2, field3);
        case opOR:
            return OR(field1, field2, field3);
        case opXOR:
            return XOR(field1, field2, field3);
        case opJump:
            return jump(field1, (field2<<4) + field3);
        default:
            return BAD_INSTRUCTION;
        }
    }

    /* Load register r with the bit pattern in memory cells a and a+1. */
    int loadMem(int r, int a) {
        if (a % 2 != 0)
            return BAD_ALIGNMENT;

        int b = (mem.get(a)<<4) + mem.get(a+1);
        reg[r].set(b);

        return SUCCESS;
    }

    /* Load register r with bit pattern c. */
    int loadConst(int r, int c) {
        reg[r].set(c);

        return SUCCESS;
    }

    /* Load register s with the bit pattern in the memory cell
     * whose address is stored in t.
     */
    int loadReg(int s, int t) {
        int a = reg[t].get();
        if (a % 2 != 0)
            return BAD_ALIGNMENT;

        int b = (mem.get(a)<<4) + mem.get(a+1);
        reg[s].set(b);

        return SUCCESS;
    }

    /* Store the bit pattern in register r in memory cells a and a+1. */
    int store(int r, int a) {
        if (a % 2 != 0)
            return BAD_ALIGNMENT;

        int b = reg[r].get();
        mem.set(a, (b & 0xf0)>>>4);
        mem.set(a+1, b & 0x0f);

        return SUCCESS;
    }

    /* Store the bit pattern in register s in the memory cell
     * whose addresses is stored in t. */
    int storeReg(int s, int t) {
        int a = reg[t].get();
        if (a % 2 != 0)
            return BAD_ALIGNMENT;

        int b = reg[s].get();
        mem.set(a, (b & 0xf0)>>>4);
        mem.set(a+1, b & 0x0f);

        return SUCCESS;
    }


    /* Move the bit pattern in register s to register t. */
    int move(int s, int t) {
        reg[t].set(reg[s].get());

        return SUCCESS;
    }

    /* Add the integers in registers s and t and place
     * the result in register r.
     */
    int addInt(int r, int s, int t) {
        reg[r].set((reg[s].get() + reg[t].get()) & 0xff);

        return SUCCESS;
    }

    /* Add integer c to register r.
     */
    int addConst(int r, int c) {
        reg[r].set((reg[r].get() + c) & 0xff);

        return SUCCESS;
    }

    /* Multiply the integers in registers s and t and place
     * the result in register r.
     */
    int mulInt(int r, int s, int t) {
        reg[r].set((reg[s].get() * reg[t].get()) & 0xff);

        return SUCCESS;
    }

    /* Subtract the integer in register t from the integer in
     * register s and place the result in register r.
     */
    int subInt(int r, int s, int t) {
        reg[r].set((reg[s].get() - reg[t].get()) & 0xff);

        return SUCCESS;
    }

    /* Shift the bit patter in register s the number of
     * positions given by the integer stored in t. Positive
     * integers shift to the right, negative to the left.
     */
    int shift(int r, int s, int t) {
        int rs = reg[s].get();
        int rt = reg[t].get();

        if (rt > 0 && rt < 128) {
            if (rt <= 8)
               rs >>= rt;
            else
                rs = 0;
        }
        if (rt >= 128) {
            rt -= 256;
            if (rt >= -8)
               rs <<= -rt;
            else
                rs = 0;
        }
        reg[r].set(rs&0xff);

        return SUCCESS;
    }

    /* AND the bitpatterns in registers s and t
     * and place the result in register r.
     */
    int AND(int r, int s, int t) {
        reg[r].set(reg[s].get() & reg[t].get());

        return SUCCESS;
    }

    /* OR the bitpatterns in registers s and t
     * and place the result in register r.
     */
    int OR(int r, int s, int t) {
        reg[r].set(reg[s].get() | reg[t].get());

        return SUCCESS;
    }

    /* XOR the bitpatterns in registers s and t
     * and place the result in register r.
     */
    int XOR(int r, int s, int t) {
        reg[r].set(reg[s].get() ^ reg[t].get());

        return SUCCESS;
    }

    /* Jump to memory cell a if the bit pattern in register r
     * eq/neq/le/leq the bit pattern in register 0.
     */
    int jump(int r, int a) {
        int adr = a & 0xfc;
        int b = a % 4;
        int rx = reg[r].get();
        int r0 = reg[0].get();
        
        if (r0 >= 128)
            r0 -= 256;
        if (rx >= 128)
            rx -= 256;

        switch (b) {
        case 0:
            if (rx == r0)
                pc.set(adr);
            break;
        case 1:
            if (rx != r0)
                pc.set(adr);
            break;
        case 2:
            if (rx < r0)
                pc.set(adr);
            break;
        case 3:
            if (rx <= r0)
                pc.set(adr);
            break;
        }

        return SUCCESS;
    }

    /* Halt execution */
    int halt() {
        return HALT;
    }
}


/********************* User interface ************************/

class Colors {
    final static Color backGround = new Color(0xFF, 0xFF, 0xFF);
    final static Color memoryCell = new Color(0xB8, 0xCF, 0xE5);
    final static Color active = new Color(0x83, 0xA1, 0xD1);
    final static Color highLight = new Color(0xFF, 0xFF, 0xFF);
    final static Color registerCell = new Color(0xFF, 0xEC, 0xC0);
}

class Fonts {
    final static Font number = new Font("SansSerif", Font.PLAIN, 12);
}

class Computer extends JPanel {
    final MainMemory RAM;
    final Processor CPU;

    final JLabel message;
    final JButton runButton;
    final JButton fetchExecuteButton;
    final JSlider speed;

    Runner process;
 
    // Is the next step to be performed a fetch?
    // (If not, it's an execute.)
    boolean fetching = true; 

    // Is the machine running?
    boolean running = false;

    // The (minimum) time in ms for one fetch or execute step.
    int delay = 125; // 40 Hz
    
    synchronized void setDelay(int delay) {
        this.delay = delay;
    }
    
    synchronized int getDelay() {
        return delay;
    }

    final static String[] error = {
        "",
        "Halt",
        "Illegal instruction",
        "Bad memory alignment",
    };

    Computer() {
        RAM = new MainMemory();
        CPU = new Processor(RAM);
        
        message = new JLabel();
        message.setBackground(Colors.backGround);

        runButton = new JButton("Run");
        runButton.setBackground(Colors.backGround);
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (running)
                    stopProgram();
                else
                    runProgram();
            }
        });

        fetchExecuteButton = new JButton("Fetch");
        fetchExecuteButton.setBackground(Colors.backGround);
        fetchExecuteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (fetching)
                    fetch();
                else
                    execute();
            }
        });

        speed = new JSlider();
        Dimension d = speed.getSize();
        d.width = 120;
        speed.setPreferredSize(d);
        speed.setBackground(Colors.backGround);
        speed.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                // Qubic response, 1-1000 Hz
                int n = 100 - speed.getValue();
                int d = 1 + n*n*n/1000;
                setDelay(d);
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        buttons.setBackground(Colors.backGround);
        buttons.add(runButton);
        buttons.add(fetchExecuteButton);
        buttons.add(message);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Colors.backGround);
        bottomPanel.add("West", buttons);
        bottomPanel.add("East", speed);

        setBackground(Colors.backGround);
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        add("West", new CPUPanel(CPU));
        add("Center", new RAMPanel(RAM));
        add("South", bottomPanel); 

        setVisible(true);
        runButton.requestFocusInWindow();
    }

    /* Run the machine in a separate thread so that the GUI can
     * continue to be responsive.
     */
    void runProgram() {
        running = true;
        runButton.setText("Stop");
        fetchExecuteButton.setEnabled(false);
        message.setText("Running");
        process = new Runner(this);
        process.start();
    }

    void reset() {
        if (running)
            stopProgram();
        CPU.reset();
        RAM.clear();
        running = false;
        fetching = true;
        resetButtons();
    }

    void resetButtons() {
        runButton.setText("Run");
        fetchExecuteButton.setEnabled(true);
        if (fetching)
            fetchExecuteButton.setText("Fetch");
        else
            fetchExecuteButton.setText("Execute");
    }

    /* Kill the thread running in the background.
     */
    void stopProgram() {
        process.interrupt();
        try {
            process.join();
        } catch (InterruptedException ignored) {}
    }

    /* Fetch the next instruction */
    void fetch() {
        int err = CPU.fetch();
        message.setText(error[err]);
        fetching = false;
        fetchExecuteButton.setText("Execute");
    }

    /* Execute the instruction */
    void execute() {
        int err = CPU.execute();
        message.setText(error[err]);
        fetching = true;
        fetchExecuteButton.setText("Fetch");
    }
}

/* Runs computer in a separate thread.
 * The thread ends its life
 *   - when it encounters a halt instruction or
 *     an illegal instruction;
 *   - the thread is interrupted;
 *   - the sleep() method throws an InterruptedException.
 */
class Runner extends Thread {
    final Computer mc;

    Runner(Computer mc) {
        this.mc = mc;
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                int err;
                if (mc.fetching) {
                    sleep(mc.getDelay());
                    err = mc.CPU.fetch();
                } else {
                    err = mc.CPU.execute();
                }
                mc.fetching = !mc.fetching;
                if (err != 0) {     
                    mc.message.setText(Computer.error[err]);
                    return;
                }
            }
            mc.message.setText("Stopped");
        } catch (InterruptedException e) {
            mc.message.setText("Stopped");
        } finally {
            mc.running = false;
            mc.resetButtons();
        }
    }
}

/* A small Canvas, just large enough to display one hexadecimal digit */
class HexLabel extends JComponent {
    final static char[] hexDigit =
    {'0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private int value;

    HexLabel() {
        value = 0;
    }

    HexLabel(int value) {
        this.value = value;
    }

    void set(int value) {
        this.value = value & 0xf;
        repaint();
    }

    public void paint(Graphics g) {
        String digit = String.valueOf(hexDigit[value]);
        g.setFont(Fonts.number);
        g.setColor(Color.black);
        FontMetrics f = g.getFontMetrics();
        int width = getSize().width;
        int height =getSize().height;
        int textWidth = f.stringWidth(digit);
        int textHeight = f.getAscent();
        g.drawString(digit, (width - textWidth)/2,
                textHeight + (height - textHeight)/2 - 1);
        super.paint(g);
    }

    public Dimension getMinimumSize() { return new Dimension(20, 20); }
    public Dimension getPreferredSize() { return new Dimension(20, 20); }
}

/* A graphical component that represents a memory cell in the RAM.
 * It is possible to edit the value of the component (and hence
 * also the corresponding value in the RAM cell).
 */
class MemoryCellLabel extends HexLabel implements Observer, KeyListener {
    // The RAM memory cell that I represent.
    private final MemoryCell mem;
    private MemoryCellLabel left;
    private MemoryCellLabel right;
    private MemoryCellLabel up;
    private MemoryCellLabel down;

    void setLeft(MemoryCellLabel left) {
        this.left = left;
    }
    void setRight(MemoryCellLabel right) {
        this.right = right;
    }
    void setUp(MemoryCellLabel up) {
        this.up = up;
    }
    void setDown(MemoryCellLabel down) {
        this.down = down;
    }

    MemoryCellLabel(MemoryCell mem) {
        this.mem = mem;
        mem.addObserver(this);
        update(mem, null);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                repaint();
            }
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });

        addKeyListener(this);
    }

    /* This method is called by the memory cell that I'm observing,
     * when its value changes.
     */
    public void update(Observable o, Object x) {
        set(mem.get());
    }

    /* Extend the paint method of HexLabel to show a different
     * background when I'm active and nonactive.
     */
    public void paint(Graphics g) {
        if (isFocusOwner())
            g.setColor(Colors.highLight);
        else if (mem.isActive())
            g.setColor(Colors.active);
        else
            g.setColor(Colors.memoryCell);
        g.fillRect(0, 0, getSize().width, getSize().height);
        super.paint(g);
    }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {;
        boolean ok = false;
        char key = e.getKeyChar();
        if ('0' <= key && key <= '9') {
            mem.set(key - '0');
            ok = true;
        }
        if ('a' <= key && key <= 'f') {
            mem.set(10 + key - 'a');
            ok = true;
        }
        if ('A' <= key && key <= 'F') {
            mem.set(10 + key - 'A');
            ok = true;
        }
        if (ok) {
            right.requestFocusInWindow();
            return;
        }

        switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_KP_DOWN:
            down.requestFocusInWindow();
            break;
        case KeyEvent.VK_UP:
        case KeyEvent.VK_KP_UP:
            up.requestFocusInWindow();
            break;
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_KP_LEFT:
            left.requestFocusInWindow();
            break;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_KP_RIGHT:
            right.requestFocusInWindow();
            break;
        case KeyEvent.VK_BACK_SPACE:
            left.requestFocusInWindow();
            left.mem.set(0);
            break;
        case KeyEvent.VK_SPACE:
            mem.set(0);
            right.requestFocusInWindow();
            break;
        }
    }
}

/* A canvas that displays a RAM consisting of 16 by 16 bytes.
 * Each byte is displayed as a single digit hexadecimal number.
 * The cells are editable.
 */
class RAMPanel extends JPanel {
    final static int rows = 16, cols = 16;

    RAMPanel(MainMemory RAM) {
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.gray), "RAM"));

        JPanel memory = new JPanel(new GridLayout(rows+1, cols+1, 1, 1));
        memory.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));
        memory.setBackground(Colors.backGround);

        memory.add(new JLabel()); // top left position empty
        for (int i = 0; i < cols; ++i)  // column headings
            memory.add(new HexLabel(i));

        MemoryCellLabel[] mcl = new MemoryCellLabel[256];
        for (int i = 0; i < 256; i++) {
            mcl[i] = new MemoryCellLabel(RAM.memory[i]);
        }
        for (int i = 0; i < 256; i++) {
            if (i == 0)
                mcl[i].setLeft(mcl[i]);
            else
                mcl[i].setLeft(mcl[(i-1)&0xff]);

            if (i == 255)
                mcl[i].setRight(mcl[i]);
            else
                mcl[i].setRight(mcl[(i+1)&0xff]);

            if (i < 16)
                mcl[i].setUp(mcl[i]);
            else
                mcl[i].setUp(mcl[(i-16)&0xff]);

            if (i < 240)
                mcl[i].setDown(mcl[(i+16)&0xff]);
            else
                mcl[i].setDown(mcl[i]);
        }

        for (int i = 0; i < rows; ++i) {
            memory.add(new HexLabel(i));  // row headings
            for (int j = 0; j < rows; ++j)
                memory.add(mcl[i*cols + j]);
        }

        setLayout(new BorderLayout(0, 0));
        add("Center", memory);
        setBackground(Colors.backGround);
    }
}

/* A HexLabel with a different background color */
class RegisterCellLabel extends HexLabel {
    public void paint(Graphics g) {
        g.setColor(Colors.registerCell);
        g.fillRect(0, 0, getSize().width, getSize().height);
        super.paint(g);
    }
}

/* A register is displayed using two RegisterCellLabels */
class RegisterPanel extends JPanel implements Observer {
    private final RegisterCellLabel b1, b2;

    RegisterPanel(Register reg, int number) {
        b1 = new RegisterCellLabel();
        b2 = new RegisterCellLabel();
        setLayout(new GridLayout(1, 3, 1, 1));
        add(new HexLabel(number));  // Display the register number
        add(b1);
        add(b2);
        reg.addObserver(this);
        update(reg, null);
        setBackground(Colors.backGround);
    }

    /* This method is called by the register that I'm observing
     * when its value changes.
     */
    public void update(Observable o, Object x) {
        set(((Register) o).get());
    }

    void set(int value) {
        b1.set((value & 0xf0)>>>4);
        b2.set(value & 0x0f);
    }
}

/* A panel displaying the pc register. */
class PCPanel extends JPanel implements Observer {
    private final RegisterCellLabel b1, b2;

    PCPanel(Register reg) {
        b1 = new RegisterCellLabel();
        b2 = new RegisterCellLabel();
        setLayout(new GridLayout(1, 5, 1, 1));
        add(new JLabel());  // The three empty positions make
        add(new JLabel());  // this label line up nicely with
        add(new JLabel());  // the ir register label.
        add(b1);
        add(b2);
        reg.addObserver(this);
        setBackground(Colors.backGround);
    }

    /* This method is called by the register that I'm observing
     * when its value changes.
     */
    public void update(Observable o, Object x) {
        set(((Register) o).get());
    }

    void set(int value) {
        b1.set((value & 0xf0) >>> 4);
        b2.set(value & 0x0f);
    }
}

/* A panel displaying the ir register */
class IRPanel extends JPanel implements Observer {
    private final RegisterCellLabel b1, b2, b3, b4;

    IRPanel(Register reg) {
        b1 = new RegisterCellLabel();
        b2 = new RegisterCellLabel();
        b3 = new RegisterCellLabel();
        b4 = new RegisterCellLabel();
        setLayout(new GridLayout(1, 5, 1, 1));
        add(new JLabel());  // empty position
        add(b1);
        add(b2);
        add(b3);
        add(b4);
        reg.addObserver(this);
        setBackground(Colors.backGround);
    }

    /* This method is called by the register that I'm observing
     * when it's value changes.
     */
    public void update(Observable o, Object x) {
        set(((Register) o).get());
    }

    void set(int value) {
        b1.set((value & 0xf000) >>> 12);
        b2.set((value & 0x0f00) >>> 8);
        b3.set((value & 0x00f0) >>> 4);
        b4.set(value & 0x000f);
    }
}

class CPUPanel extends JPanel {
    CPUPanel(Processor CPU) {
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.gray), "CPU"));

        JPanel generalRegs = new JPanel(new GridLayout(17, 1, 1, 1));
        generalRegs.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 2));
        generalRegs.setBackground(Colors.backGround);
        generalRegs.add(new JLabel("r[i]", SwingConstants.RIGHT));
        for (int i = 0; i < 16; ++i) {
            generalRegs.add(new RegisterPanel(CPU.reg[i], i));
        }

        JPanel specRegs = new JPanel(new GridLayout(17, 1, 1, 1));
        specRegs.setBorder(BorderFactory.createEmptyBorder(2, 2, 10, 10));
        specRegs.setBackground(Colors.backGround);
        specRegs.add(new JLabel("pc", SwingConstants.RIGHT));
        specRegs.add(new PCPanel(CPU.pc));
        specRegs.add(new JLabel());    // empty position
        specRegs.add(new JLabel("ir", SwingConstants.RIGHT));
        specRegs.add(new IRPanel(CPU.ir));
        for (int i = 4; i < 16; ++i) {  // empty positions
            specRegs.add(new JLabel());
        }

        setLayout(new BorderLayout(0, 0));
        add("West", generalRegs);
        add("East", specRegs);
        setBackground(Colors.backGround);
    }
}
