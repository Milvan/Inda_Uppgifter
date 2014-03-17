// Copyright 2011 Stefan Nilsson. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.
package assembler;

import java.io.*;
import java.util.*;

/**
 * Assembly for NIC
 *
 * Stefan Nilsson (snilsson@nada.kth.se)
 * Version 1.0, 31 January 2011
 */
public final class Nas {
    public final static String NAME = "nas";
    public final static String VERSION = "1.0-2";
    public final static String USAGE = "Usage: " + NAME + " [-v] [FILE...]";

    public final static int MAX_ERRORS = 10;

    static String fname = "";
    static boolean verbose = false;
    static int totErrors = 0;

    public static void main(String[] args) {
        int len = args.length;
        int nFiles = len;
        int n = 0;

        if (nFiles == 0) {
            System.out.println(USAGE);
            return;
        }

        if (args[0].equals("-v")) {
            verbose = true;
            n = 1;
            nFiles--;
            if (nFiles == 0)
                System.out.println(NAME + " version " + VERSION);
        }

        for (int i = n; i < len; i++) {
            fname = args[i];
            if (verbose && nFiles > 1) {
                String sep = fname.replaceAll(".", ":");
                System.out.printf("%s%n%s%n%s%n", sep, fname, sep);
            }
            try {
                assemble(fname);
            } catch (TooManyErrorsException e) {
                // Continue with next file
            }
        }

        if (nFiles > 1 && totErrors > 0) {
            System.out.printf("%d error%s in %d files%n", totErrors,
                totErrors > 1 ? "s" : "", nFiles);
        }
    }
    
    static void assemble(String fname) {
        BufferedReader in = null;
        PrintWriter out = null;
        String biname = "";
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fname), "UTF-8"));
            Asm a = new Asm(in);
            in.close();
 
            if (a.errors > 0) {
                totErrors += a.errors;
                return;
            }

            if (fname.endsWith(".as"))
                biname = fname.replaceFirst(".as\\z", ".bi");
            else
                biname = fname + ".bi";
            out = new PrintWriter(biname, "UTF-8");
            a.generate(out);
            out.close();
 
        } catch (FileNotFoundException e) {
            System.out.printf("%s: cannot find %s%n", NAME, fname);
            return;
 
        } catch (IOException e) {
            if (biname.equals(""))
                System.out.printf("%s: error reading %s%n", NAME, fname);
            else
                System.out.printf("%s: error writing %s%n", NAME, biname);
            return;

        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                System.out.printf("%s: error closing %s%n", NAME, fname);
                return;
            }
        }
    }
}

class TooManyErrorsException extends RuntimeException {}

enum Op {
    HALT("0", "halt"),
    LOAD("1", "load"),
    LOADC("2", "loadc"),
    LOADR("3", "loadr"),
    STORE("4", "store"),
    STORER("5", "storer"),
    MOVE("6", "move"),
    ADD("7", "add"),
    ADDC("8", "addc"),
    MUL("9", "mul"),
    SUB("a", "sub"),
    SHIFT("b", "shift"),
    AND("c", "and"),
    OR("d", "or"),
    XOR("e", "xor"),
    JUMP("f", "jump"),
    JUMPE("f", "jumpe"),
    JUMPN("f", "jumpn"),
    JUMPL("f", "jumpl"),
    JUMPLE("f", "jumple"),
    NOOP("f", "noop");
        
    private final String code;
    private final String name;
 
    Op(String code, String name) {
        this.code = code;
        this.name = name;
    }
 
    public String code() { return code; }
    public String toString() { return name; }
}

enum Type {
    WORD("word"),
    CODE("code"),
    LABEL("label"),
    VOID("_");
    
    private final String name;
 
    Type(String name) {
        this.name = name;
    }
 
    public String toString() { return name; }
}

class Sym {
    Type type;
    String name;
    int address;
    int[] values;
    
    Sym(Type type, String name, int... values) {
        this.type = type;
        this.name = name;
        this.values = values;
    }
    
    void setAdr(int i) {
        address = i;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);

        fmt.format("%s %s 0x%02x", type, name, address);
 
        if (type == Type.LABEL) {
            return sb.toString();
        }

        fmt.format(" [");
        for (int n: values) {
            switch (type) {
            case WORD:
                fmt.format("%d ", n >= 128 ? n - 256 : n);
                break;
            case CODE:
                fmt.format("%04x ", n);
                break;
            }
        }
        sb.setLength(sb.length()-1); // Remove trailing " ".
        sb.append("]");

        return sb.toString();        
    }
}

class Value {
    String name;
    int n;
    int offset;
    boolean isHex;
    
    Value(String name, int n, int offset) {
        this.name = name;
        this.n = n;
        this.offset = offset;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);

        if (isHex) {
            fmt.format("0x%02x", n - offset);
        } else {
            int no = n - offset;
            fmt.format("%d", no >= 128 ? no - 256 : no);
        }

        if (!name.equals(""))
            fmt.format("(%s)", name);
    
        String offs = offset > 0 ? "+" : "";
        if (offset != 0) {
            offs += offset;
        }
        sb.append(offs);

        return sb.toString();
    }
}

class Instr {
    int linenr;
    Op code;
    int r, s, t;
    Value val;
    
    Instr(int linenr, Op code, int r, int s, int t, Value val) {
        this.linenr = linenr;
        this.code = code;
        this.r = r;
        this.s = s;
        this.t = t;
        this.val = val;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);

        fmt.format("%-7s", code);
        switch (code) {
        case HALT:
        case NOOP:
            break;
        case JUMP:
            fmt.format("%s", val);
            break;
        case LOAD:
        case LOADC:
        case STORE:
        case ADDC:
        case JUMPE:
        case JUMPN:
        case JUMPL:
        case JUMPLE:
            fmt.format("r%x %s", r, val);
            break;
        case LOADR:
        case STORER:
        case MOVE:
            fmt.format("r%x r%x", s, t);
            break;
        default:
            fmt.format("r%x r%x r%x", r, s, t);
            break;
        }

        return sb.toString();
    }
}

class Asm {
    final static int INT_ERR = Integer.MIN_VALUE;

    final static Map<String, Op> codes = new HashMap<String, Op>();
    static {
        for (Op o : Op.values()) {
            codes.put(o.toString(), o);
        }    
    }

    int errors = 0;

    List<String> lines = new ArrayList<String>();
    List<Instr> instrs = new ArrayList<Instr>();
    List<Sym> syms = new ArrayList<Sym>();
    Map<String, Integer> symIndex = new HashMap<String, Integer>();

    Sym get(String name) {
        Integer i = symIndex.get(name);
        if (i != null)
            return syms.get(i);
        return null;
    }
    
    void put(Sym sym) {
        symIndex.put(sym.name, syms.size());
        syms.add(sym);
    }

    Asm(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            lines.add(line);
            parse(line);
        }

        computeAdr();

        if (Nas.verbose && syms.size() > 0) {
            for (Sym s: syms) {
                System.out.println(s);
            }
            System.out.println();
        }

        fillinAdr();

        if (Nas.verbose && errors == 0 && instrs.size() > 0) {
            int addr = 0;
            for (Instr i: instrs) {
                System.out.printf("%02x: ", addr);
                addr += 4;
                System.out.println(i);
            }
            System.out.println();
        }

        if (errors > 0) {
            System.out.printf("%d error%s in %s%n%n",
                errors, errors > 1 ? "s" : "", Nas.fname);
        }
    }
    
    void computeAdr() {
        int memp = 4*instrs.size() + 8; // program + halt + jump to 0

        for (Sym s: syms) {
            if (s.type == Type.WORD) {
                s.setAdr(memp);
                memp += 2*s.values.length;
            }
            if (s.type == Type.CODE) {
                if (memp%4 != 0) // padding for 4 byte alignment
                    memp += 2;
                s.setAdr(memp);
                memp += 4*s.values.length;
            }
        }
    }
    
    void fillinAdr() {
        for (Instr i: instrs) {
            int res = 0;
            int addr = 0;
            String name = "";
            boolean align2 = false;
            boolean align4 = false;

            switch (i.code) {
            case JUMP:
            case JUMPE:
            case JUMPN:
            case JUMPL:
            case JUMPLE:
                align4 = true;
                // fall through
            case LOAD:
            case STORE:
                align2 = true;
                // fall through
            case LOADC:
            case ADDC:
                res = i.val.n + i.val.offset;
                name = i.val.name;
                Sym sym = get(name);

                if (sym != null) {
                    addr = sym.address;
                    res += addr;
                } else if (!name.equals("")) {
                    printErr(i.linenr, "Undefined name", name);
                    break;
                }

                i.val.n = res;

                if (res < 0 || res > 0xff) {
                    String s = align2 ? "Adress " : "Number ";
                    printErr(i.linenr, s + i.val + " is out of range.");
                }

                if (align4 && res % 4 != 0 || align2 && res % 2 != 0) {
                    String s = "Address " + i.val + " is not aligned on ";
                    s += (align4 ? "4" : "2") + "-byte boundary.";
                    printErr(i.linenr, s);
                }
                break;
            }
        } 
    }

    void generate(PrintWriter out) {
        printCode(out, 0x1f1f); // Magic word
        printCode(out, 0x1f1f);

        for (Instr i: instrs) {
            int w = 0;
            if (i.val != null) {
                w = i.val.n;
            }
            int b = 0;

            printOp(out, i.code);

            switch (i.code) {
            case JUMPLE:
                b++;
                // fall through
            case JUMPL:
                b++;
                // fall through
            case JUMPN:
                b++;
                // fall through
            case JUMPE:
            case JUMP:
            case LOAD:
            case LOADC:
            case STORE:
            case ADDC:
                printByte(out, i.r);
                printWord(out, w+b);
                break;
            default:
                printByte(out, i.r);
                printByte(out, i.s);
                printByte(out, i.t);
                if (Nas.verbose)
                    System.out.print(" ");
                break;
            }
        }

        printCode(out, 0x0000); // halt
        printCode(out, 0xf000); // jump to 0

        // This must mirror the addressing traversal.
        int adr = 0; // Keep track of alignment.
        boolean first = true;
        for (Sym s: syms) {
            if (Nas.verbose && first && s.type != Type.LABEL) {
                System.out.println();
                first = false;
            }

            switch (s.type) {
            case WORD:    
                for (int n: s.values) {
                    printWord(out, n);
                    adr += 2;
                }
                break;
            case CODE:
                if (adr%4 != 0) { // padding for 4 byte alignment
                    printWord(out, 0x00);
                    adr += 2;
                }
                for (int n: s.values) {
                    printCode(out, n);
                    adr += 4;
                }
                break;
            }
        }
        printFlush(out);
        if (Nas.verbose) {
            System.out.println();
        }
    }

    void printFlush(PrintWriter out) {
        out.println();
        if (Nas.verbose)
            System.out.println();
    }

    void printOp(PrintWriter out, Op op) {
        out.print(op.code());
        if (Nas.verbose)
            System.out.print(op.code());
    }

    void printByte(PrintWriter out, int b) {
        if (b < 0 || b > 15) {
            internalError("byte", b);
        } else {
            out.printf("%x", b);
            if (Nas.verbose) {
                System.out.printf("%x", b);
            }
        }
    }
    
    void printWord(PrintWriter out, int n) {
        if (n < 0 || n > 0xff) {
            internalError("word", n);
        } else {
            out.printf("%02x", n);
            if (Nas.verbose)
                System.out.printf("%02x ", n);
        }        
    }
    
    void printCode(PrintWriter out, int n) {
        if (n < 0 || n > 0xffff) {
            internalError("code", n);
        } else {
            out.printf("%04x", n);
            if (Nas.verbose)
                System.out.printf("%04x ", n);
        }        
    }
    
    void internalError(String msg, int n) {
        throw new Error("internal error: " + msg + "=0x" + Integer.toHexString(n));
    }

    void parse(String line) {
        int i = line.indexOf("//");
        if (i >= 0)
            line = line.substring(0, i);
        String[] tok = line.trim().split("\\s+");
        
        if (tok[0].equals("")) {
            return;
        }

        String s = tok[0];
        if (s.endsWith(":")) {
            parseLabel(s.replaceFirst(":\\z", ""));

            int len = tok.length;
            if (len == 1)
                return;

            // Make the label disappear.
            String[] tmp = new String[len-1];
            for (i = 0; i < len-1; i++)
                tmp[i] = tok[i+1];
            tok = tmp;
            s = tok[0];
        }
        
        if (s.equals("word")) {
            parseVar(Type.WORD, tok);
            return;
        }

        if (s.equals("code")) {
            parseVar(Type.CODE, tok);
            return;
        }
        
        if (codes.containsKey(s)) {
            Op op = codes.get(s);
            parseOp(op, tok);
            return;
        }

        printErr("Unknown instruction", s);
    }
    
    void parseVar(Type type, String[] tok) {
        if (tok.length < 2) {
            printErr("Need name after " + tok[0] + ".");
            return;
        }

        boolean ok = true;
        String name = tok[1];
        int[] val;
        if (tok.length == 2) {
            val = new int[1];
        } else {
            val = new int[tok.length - 2];
        }
        Sym sym = null;
        switch (type) {
        case WORD:
            sym = new Sym(Type.WORD, name, val);
            break;
        case CODE:
            sym = new Sym(Type.CODE, name, val);
            break;
        default:
            sym = new Sym(Type.VOID, name, val);
        }

        if (get(name) != null) {
            ok = false;
            printErr("Name already defined", name);
        }

        if (ok)
            put(sym);

        if (!isId(name)) {
            ok = false;
            printErr("Invalid name", name);
        }

        for (int i = 2; i < tok.length; i++) {
            String s = tok[i];
            int n = 0;
            if (s.startsWith("0x")) {
                n = parseHex(s);
            } else {
                n = parseDec(s);
                if (n != INT_ERR && type == Type.WORD) {
                    if (n < -128 || n > 127) {
                        printErr("Value out of range", s);
                        n = INT_ERR;
                    } else if (n < 0 && n >= -128) {
                        n += 256;
                    }
                }
            }
            if (n == INT_ERR) {
                ok = false;
            } else if (type == Type.WORD && (n < 0 || n > 0xff) ||
                    type == Type.CODE && (n < 0 || n > 0xffff)) {
                ok = false;
                printErr("Value out of range", s);
            } else {
                val[i-2] = n;
            }
        }
    }

    void parseLabel(String name) {
        if (!isId(name)) {
            printErr("Invalid name", name);
            return;
        }
    
        if (get(name) != null) {
            printErr("Name already defined", name);
        } else {
            Sym sym = new Sym(Type.LABEL, name);
            sym.setAdr(4*instrs.size());
            put(sym);
        }
    }
    
    void parseOp(Op op, String[] tok) {
        switch (op) {
        case NOOP:
        case HALT:
            parseNoArgs(op, tok);
            break;
        case JUMP:
            parseValue(op, tok);
            break;
        case LOAD:
        case LOADC: 
        case STORE:
        case ADDC:
        case JUMPE:
        case JUMPN:
        case JUMPL:
        case JUMPLE:
            parseRegValue(op, tok);
            break;
        case LOADR:
        case STORER:
        case MOVE:
            parseRegReg(op, tok);
            break;
        default:
            parseRegRegReg(op, tok);
            break;
        }
    }
    
    void parseNoArgs(Op op, String[] tok) {
        if (tok.length > 1) {
            printErr("Unexpected operand", tok[1]);
            return;
        }

        switch (op) {
        case HALT:
            instrs.add(new Instr(lines.size(), op, 0, 0, 0, null));
            break;
        case NOOP:
            instrs.add(new Instr(lines.size(), op, 0, 0, 1, null));
            break;
        }
    }

    void parseValue(Op op, String[] tok) {
        if (tok.length != 2) {
            printErr("Need value after " + op + ".");
            return;
        }

        Value v = parseValue(tok[1]);
        if (v == null) {
            return;
        }
        v.isHex = true;

        instrs.add(new Instr(lines.size(), op, 0, 0, 0, v));
    }

    void parseRegValue(Op op, String[] tok) {
        if (tok.length != 3) {
            printErr("Need register and value after " + op + ".");
            return;
        }

        boolean ok = true;
        int r = parseReg(tok[1]);
        if (r == INT_ERR) {
            ok = false;
        }

        Value v = parseValue(tok[2]);
        if (v == null) {
            return;
        }

        switch (op) {
        case LOAD:
        case STORE:
        case JUMPE:
        case JUMPN:
        case JUMPL:
        case JUMPLE:
            v.isHex = true;
            break;
        }

        if (ok)
            instrs.add(new Instr(lines.size(), op, r, 0, 0, v));
    }

    void parseRegReg(Op op, String[] tok) {
        if (tok.length != 3) {
            printErr("Need two registers after " + op + ".");
            return;
        }

        boolean ok = true;
        int r = parseReg(tok[1]);
        if (r == INT_ERR) {
            ok = false;
        }

        int s = parseReg(tok[2]);
        if (s == INT_ERR) {
            ok = false;
        }

        if (ok)
            instrs.add(new Instr(lines.size(), op, 0, r, s, null));
    }

    void parseRegRegReg(Op op, String[] tok) {
        if (tok.length != 4) {
            printErr("Need three registers after " + op + ".");
            return;
        }

        boolean ok = true;
        int r = parseReg(tok[1]);
        if (r == INT_ERR) {
            ok = false;
        }

        int s = parseReg(tok[2]);
        if (s == INT_ERR) {
            ok = false;
        }

        int t = parseReg(tok[3]);
        if (t == INT_ERR) {
            ok = false;
        }

        if (ok)
            instrs.add(new Instr(lines.size(), op, r, s, t, null));
    }
    
    int parseReg(String s) {        
        if (!s.startsWith("r")) {
            printErr("Invalid register name", s);
            return INT_ERR;
        }
        
        if (s.matches("r[a-f]")) {
            return 10 + s.charAt(1) - 'a';
        }

        int n = 0;
        try {
            n = Integer.parseInt(s.substring(1));
        } catch (NumberFormatException e) {
            printErr("Invalid register name", s);
            return INT_ERR;
        }
               
        if (n < 0 || n > 15) {
            printErr("Invalid register number", s);
            return INT_ERR;
        }

        return n;
    }

    Value parseValue(String s) {
        String name = "";
        int n = 0;
        int offset = 0;

        if (s.startsWith("0x")) {
            n = parseHex(s);
            if (n != INT_ERR) {
                if (n < 0 || n > 0xff) {
                    printErr("Hex value out of range", s);
                    n = INT_ERR;
                }
            }
        } else if (s.matches("\\d.*|-.*"))  {
            n = parseDec(s);
            if (n != INT_ERR) {
                if (n < -128 || n > 127) {
                    printErr("Value out of range", s);
                    n = INT_ERR;
                }
                else if (n < 0 && n >= -128) {
                    n += 256;
                }
            }
        } else if (s.contains("+"))  {
            name = s.substring(0, s.indexOf("+"));
            offset = parseDec(s.substring(s.indexOf("+") + 1));
        } else if (s.contains("-"))  {
            name = s.substring(0, s.indexOf("-"));
            offset = -parseDec(s.substring(s.indexOf("-") + 1));
        } else {
            name = s;
        }

        Value v = new Value(name, n, offset);
        
        if (!name.equals("") && !isId(name)) {
            printErr("Invalid name", name);
            v = null;
        }
            
        if (n == INT_ERR) {
            v = null;
        }

        if (offset == INT_ERR) {
            return null;
        } 

        return v;
    }
    
    boolean isId(String s) {
        return s.matches("[a-zA-Z][\\w|\\d]*");
    }
    
    int parseDec(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            printErr("Invalid number", s);
            return INT_ERR;
        }        
    }
    
    int parseHex(String s) {
        try {
            return Integer.parseInt(s.substring(2), 16);
        } catch (NumberFormatException e) {
            printErr("Invalid hex number", s);
            return INT_ERR;
        }
    }

    void printErr(String... msg) {
        printErr(lines.size(), msg);
    }

    void printErr(int linenr, String... msg) {
        int msgs = msg.length;
    
        errors++;
        if (errors > Nas.MAX_ERRORS) {
            System.out.printf("More than %d errors in %s%n%n",
                Nas.MAX_ERRORS, Nas.fname);
            Nas.totErrors += Nas.MAX_ERRORS;
            throw new TooManyErrorsException();
        }
        
        int n = linenr;
        System.out.printf("Line %d in %s%n\"%s\"%n",
            n, Nas.fname, lines.get(n-1).trim());

        if (msgs >= 1) {
            System.out.print(msg[0]);
        }

        if (msgs >= 2) {
            System.out.printf(": \"%s\"%n", msg[1]);
        } else {
            System.out.println();
        }

        System.out.println();
    }
}
