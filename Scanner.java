package uebung;

import java.io.*;

public class Scanner {
	private Input input;
	private char ch = ' ';
	public char comment;
	public int sym = -1;
	public String id;
	public String num;
	public String compare;
	public static final int ident = 0, number = 1, lparen = 2, rparen = 3, lbrace = 4, rbrace = 5, sst_class = 6, sst_void = 7, sst_public = 8, 
			sst_final = 9, sst_int = 10, sst_if = 11, sst_else = 12, sst_while = 13, sst_ret = 14, mult = 15, div = 16, plus = 17, minus = 18, equal = 19,
			equal_equal = 20, smaller = 21, smaller_equal = 22, bigger = 23, bigger_equal = 24, sst_comment = 25, sst_comma = 26, sst_semicolon = 27,other = 28;
	public boolean end = false;
	
	public int line_counter = 1;
	public int last_line = 1;
	boolean only_blanks = true;
				
	public Scanner(File file) {
		input = new Input(file);
	}
	
	public void getSym_comment() throws Exception {
		if (this.end) { sym = Scanner.other; }
		else {
			while (ch <= ' ') { 
				if (ch == '\n') { 
					this.line_counter++; 
					if (!only_blanks) { last_line = line_counter; only_blanks = true; }
				}
				ch = input.next(); 
			}
			only_blanks = false;
			if ('0' <= ch && ch <= '9') {
				sym = number;
				num = "";
				do {
					num += ch;
					ch = input.next();
				} while (ch >= '0' && ch <= '9' && !this.input.end);
			}
			else {
				if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z')) {
					id = "";
					do {
						id += ch;
						ch = input.next();
					} while ((('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || (ch >= '0' && ch <= '9')) && !this.input.end);
					switch (id) {
					case "class": sym = sst_class; break;
					case "final": sym = sst_final; break;
					case "public": sym = sst_public; break;
					case "void": sym = sst_void; break;
					case "int": sym = sst_int; break;
					case "if": sym = sst_if; break;
					case "else": sym = sst_else; break;
					case "while": sym = sst_while; break;
					case "return": sym = sst_ret; break;
					default: sym = ident;
					}
				}
				else {
					switch (ch) {
					case '(': sym = lparen; ch = input.next(); break;
					case ')': sym = rparen; ch = input.next(); break;
					case '{': sym = lbrace; ch = input.next(); break;
					case '}': sym = rbrace; ch = input.next(); break;
					case ';': sym = sst_semicolon; ch = input.next(); break;
					case ',': sym = sst_comma; ch = input.next(); break;
					case '+': sym = plus; ch = input.next(); break;
					case '-': sym = minus; ch = input.next(); break;
					case '*': sym = mult; ch = input.next(); break;
					case '/': sym = div; 
							  ch = input.next();
							  if (ch == '/') {
								  while (!this.input.end && ch != '\n'){ sym = sst_comment; ch = input.next(); }
								  // increment line counter since we go in the next line
								  this.line_counter++;
							  }
							  else if (ch == '*') {
								  sym = sst_comment;
								  ch = input.next();
								  while (!(comment == '*' && ch == '/') && !this.input.end) { 
									  comment = ' ';
									  if (ch == '*') {comment = '*';}
									  else if (ch == '\n') { this.line_counter++; }
									  ch = input.next();
									  if (this.input.end) {
										  System.out.println("Comment not closed at line " + this.line_counter);
										  throw new Exception();
									  }
								  }
								  ch = input.next();
							  } break;
					case '=', '<', '>': compare = "";
										compare += ch;
										ch = input.next();
										if (ch == '=') { compare += ch; ch = input.next(); }
										switch (compare){
										case "=": sym = equal; break;
										case "==": sym = equal_equal; break;
										case "<": sym = smaller; break;
										case "<=": sym = smaller_equal; break;
										case ">": sym = bigger; break;
										case ">=": sym = bigger_equal; break;
										default: System.out.println("shouldnt happen: " + compare);
										} break;
					default: sym = other; 
						if (this.input.end) { System.out.println("End Of File in SCANNER"); this.end = true; sym = other; break; }
						else { System.out.println("Unknown Symbol " + ch + " " + input.end); System.exit(1); }
					}
				}
			}
		}
	}
	
	public void getSym() throws Exception {
		this.getSym_comment();
		while (this.sym == Scanner.sst_comment) {
			this.getSym_comment();
		}
	}
	
	public void read_all() throws Exception {
		do {
			try {
				this.getSym();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (!this.end);
		try {
			this.input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
