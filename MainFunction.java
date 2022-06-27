package uebung;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainFunction {	
	static boolean ret = false;
	
	public static void main(String[] args) throws Exception {
		File in_file = new File("C:\\Users\\hanno\\OneDrive\\Desktop\\Informatik\\Semester IX\\ImplementierungVonProgrammiersprachen\\Uebung\\byteCodeGenerierung\\test.txt");
		Scanner scanner = new Scanner(in_file);
		Parser parser = new Parser(scanner);
		parser.check_synt();
		
		//Symboltable mainSymtab = getMainSymtab(parser.mainClass);
		//mainSymtab.printMain();
		
		System.out.println("*********************");
		// writeTree(parser.start);
		
		writeClass(parser.start, "test.java");
		displayClass("byteCodeGenerierung\\Test.class");
		
		System.out.println("\n****************\n");
		
		displayClass("DotFiles\\Test.class");
	}
	
	public static Symboltable getMainSymtab(Object mainClass) {
		Symboltable symtab = new Symboltable();
		symtab.insert(mainClass);
		
		mainClass.nextSymboltable = mainClass.finalVar;
		Symboltable currentSymtab = mainClass.nextSymboltable;
		currentSymtab.enclose = symtab;
		
		Object pointer = mainClass.varDef.head;
		if (pointer.next != null) {
			pointer = pointer.next;
			currentSymtab.insert(pointer);
			currentSymtab.length += mainClass.varDef.length - 1;
		}
		
		pointer = mainClass.methodDef.head;
		if (pointer.next != null) {
			pointer = pointer.next;
			currentSymtab.insert(pointer);
			while (pointer.next != null) {
				pointer.nextSymboltable = pointer.varDef;
				pointer = pointer.next;
			}
			pointer.nextSymboltable = pointer.varDef;
		}
		return symtab;
	}
	
	public static void writeTree(Node n) throws IOException {
		FileWriter myWriter = new FileWriter("DotFiles/tree.dot");
	    myWriter.write("digraph G {\n");
	    
		String otherStrings = "";
		String linkString = "";
		
		String allLabels = n.uniqueIndex + " [label=" + n.getNodeName() + "];\n";
		linkString += n.uniqueIndex;
		
		if (n.left != null) {
			allLabels += n.left.uniqueIndex + " [label=" + n.left.getNodeName() + "];\n";
			otherStrings += n.uniqueIndex + "->" + n.left.uniqueIndex + ";\n";
			otherStrings += rekTree(n.left); 
		}
		if (n.right != null) {
			allLabels += n.right.uniqueIndex + " [label=" + n.right.getNodeName() + "];\n";
			otherStrings += n.uniqueIndex + "->" + n.right.uniqueIndex + ";\n";
			otherStrings += rekTree(n.right); 
		}
	    
	    while (n.link != null) {
	    	n = n.link;
	    	allLabels += n.uniqueIndex + " [label=" + n.getNodeName() + "];\n";
	    	linkString += "->" + n.uniqueIndex;

	    	if (n.left != null) {
				allLabels += n.left.uniqueIndex + " [label=" + n.left.getNodeName() + "];\n";
				otherStrings += n.uniqueIndex + "->" + n.left.uniqueIndex + ";\n";
				otherStrings += rekTree(n.left); 
			}
			if (n.right != null) {
				allLabels += n.right.uniqueIndex + " [label=" + n.right.getNodeName() + "];\n";
				otherStrings += n.uniqueIndex + "->" + n.right.uniqueIndex + ";\n";
				otherStrings += rekTree(n.right); 
			}
	    }
	    
	    linkString += ";\n";
	    
	    myWriter.write(allLabels);
	    myWriter.write(linkString);
	    myWriter.write(otherStrings);
	    
	    myWriter.write("}");
	    
	    myWriter.close();
	}
	
	public static String rekTree(Node n) {
		String ret = "";
		if (n.left != null) {
			ret += n.left.uniqueIndex + " [label=" + n.left.getNodeName() + "];\n";
			ret += n.uniqueIndex + "->" + n.left.uniqueIndex + ";\n";
						
			ret += rekTree(n.left);
		}
		if (n.right != null) {
			ret += n.right.uniqueIndex + " [label=" + n.right.getNodeName() + "];\n";
			ret += n.uniqueIndex + "->" + n.right.uniqueIndex + ";\n";
			
			ret += rekTree(n.right);
		}
		if (n.link != null) {
			ret += n.link.uniqueIndex + " [label=" + n.link.getNodeName() + "];\n";
			ret += n.uniqueIndex + "->" + n.link.uniqueIndex + ";\n";
			
			ret += rekTree(n.link);
		}
		return ret;
	}
	
	public static void displayClass(String name) throws IOException {
		FileInputStream fin = new FileInputStream(name);
		int len;
	    byte data[] = new byte[16];
	    
	    int row_counter = 0;
	    int mod_row = 0;	
	    	    
	    do {
	    	len = fin.read(data);
	    	System.out.printf("%d:    ", mod_row);
	    	for (int j = 0; j < len; j++) {
	    		System.out.printf("%02X ", data[j]);
	      		row_counter++;
	      		if (row_counter == 16) {
	      			row_counter = 0;
	      			System.out.println("");
	      		}
	    	}
	      	mod_row++;
	    } while (len != -1);
	    fin.close();
	}
	
	public static void displayByteCode(byte[] b) throws IOException {
		int row_counter = 0;
	    int mod_row = 16;
		for (int i = 0; i < b.length; i++) {
			if (mod_row == 16) {
				System.out.print("\n" + row_counter + ": ");
				row_counter++;
				mod_row = 0;
			}
	    	System.out.printf("%02X ", b[i]);
	    	mod_row++;
	    }
	}
	
	public static void writeClass(Node n, String el) throws IOException {
		// Verweise speichern
		ArrayList<FieldSaver> Methoden = new ArrayList<FieldSaver>();
		// Save Everything of Methods and FinalVariablen
		ArrayList<FieldSaver> FinalVariablen = new ArrayList<FieldSaver>();
		// Save Everything of Methods and FinalVariablen
		ArrayList<FieldSaver> GlobalVariablen = new ArrayList<FieldSaver>();
		// Verweis auf "I"
		byte[] saveInt = new byte[] {0x00, 0x00};
		// Verweis auf klasse
		byte[] classSaved = new byte[] {0x00, 0x07};
		// There Constant value begin
		byte[] Constants = new byte[] {0x00, 0x00};
		// There "ConstantValue" is written
		byte[] ConstantValue = new byte[] {0x00, 0x00};
		
		// save Constants
		ArrayList<Integer> ConstantValues = new ArrayList<Integer>();
		//Start of global vars (not final)
		byte[] globalVars = new byte[] {0x00, 0x00};
		// save there code is stored
		byte[] saveCode = new byte[] {0x00, 0x00};
		// save counter
		byte[] counter = new byte[] {0x00, 0x00};
		// save init
		byte[] saveInit = new byte[] {0x00, 0x05};
		// save ()V
		byte[] saveInitReturn = new byte[] {0x00, 0x06};
		// save pos of sourcecode
		byte[] saveSourcecode = new byte[] {0x00, 0x00};
		// save pos of textfile name
		byte[] saveText = new byte[] {0x00, 0x00};
		
		// Part 1: Constants TODO
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] B = new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, 
				(byte) 0xBE, 0x00, (byte) 0x00, (byte) 0x00, 
				0x3C, 0x00, (byte) computeLength(n), 0x0A,
				(byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x03, 
				(byte) 0x07, (byte) 0x00, (byte) 0x04, (byte) 0x0C, 
				(byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x06, 
				(byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x6A,
				(byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2F,
				(byte) 0x6C, (byte) 0x61, (byte) 0x6E, (byte) 0x67,
				(byte) 0x2F, (byte) 0x4F, (byte) 0x62, (byte) 0x6A, 
				(byte) 0x65, (byte) 0x63, (byte) 0x74, (byte) 0x01,
				(byte) 0x00, (byte) 0x06, (byte) 0x3C, (byte) 0x69, 
				(byte) 0x6E, (byte) 0x69, (byte) 0x74, (byte) 0x3E,
				(byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x28, 
				(byte) 0x29, (byte) 0x56, (byte) 0x07, (byte) 0x00, 
				(byte) 0x08};
		bout.write(B);
		String className = n.varName;

		bout.write(new byte[] {0x01, 0x00, (byte) className.length()});
		bout.write(className.getBytes());
		
		// write Integer
		counter = incrementByteList(counter, 9);
		
		bout.write(new byte[] {0x01, 0x00, 0x01, 0x49});
		// save number there I is stored
		saveInt = counter;
		counter = incrementByteList(counter, 1);
				
		// first append methods
		Node curNode = n.link;
		
		// first add Functions to Class file
		while (curNode != null) {
			bout.write(createFunctionCall(curNode, counter, classSaved));
			// save there function name is stored
			// Function parameters are in the next field						
			FieldSaver FS = new FieldSaver();

			int countLocals = countLocals(curNode) + curNode.parameterLength;

			byte[] stackSize = ByteBuffer.allocate(2).putChar((char) stackSize(curNode.right)).array();
			
			FS.setValuesMethode(counter, curNode.varName, curNode.parameterLength, 
					countLocals, stackSize, curNode);
			Methoden.add(FS);
			
			counter = incrementByteList(counter, 4);
			
			if (curNode.link == null) { break; }
			curNode = curNode.link;
		}	
		curNode = n.left;
		// save values which are already defined
		int varCounter = 0;
		
		while (curNode != null) {
			if (curNode.left == null) { break; }
			varCounter++;
			String varName = curNode.left.varName;
			byte[] c1 = incrementByteList(counter, 1);
			byte[] c2 = incrementByteList(c1, 1);
			bout.write(new byte[] {0x09, classSaved[0], classSaved[1], c1[0], c1[1], 
				0x0C, c2[0], c2[1], saveInt[0], saveInt[1], 
				0x01, 0x00, (byte) varName.length()});
			bout.write(varName.getBytes());
			// save Number there constant name is saved
			FieldSaver FS = new FieldSaver();
			FS.setValuesConstants(counter, varName);
			FinalVariablen.add(FS);
			
			counter = incrementByteList(counter, 3);
			
			if (curNode.right.sst_class == Node.CONST) {
				ConstantValues.add(curNode.right.varVal);
			}
			
			// finished
			if (curNode.link == null) {break;}
			curNode = curNode.link;
		}
				
		// all constant values are written to write "constantval"
		bout.write(new byte[] {0x01, 0x00, 0x0D, 0x43, 0x6F, 
				0x6E, 0x73, 0x74, 0x61, 0x6E, 0x74, 0x56, 0x61,
				0x6C, 0x75, 0x65});
		ConstantValue = counter;
		counter = incrementByteList(counter, 1);
		// write constants
		
		for (int i = 0; i < ConstantValues.size(); i++) {
			bout.write(0x03);
			bout.write(ByteBuffer.allocate(4).putInt(ConstantValues.get(i)).array());
			
			// save start of constant variables
			// save constant values
			if (i == 0) { Constants = counter; }
			counter = incrementByteList(counter, 1);
		}
		// write globals (undefined)
		globalVars = counter;
		while (curNode != null) {
			if (curNode.sst_class == Node.ASSIGN) { break; }
			varCounter++;
			String varName = curNode.varName;
			
			FieldSaver FS = new FieldSaver();
			FS.setValuesConstants(counter, varName);
			GlobalVariablen.add(FS);
			
			byte[] c1 = incrementByteList(counter, 1);
			byte[] c2 = incrementByteList(c1, 1);
			bout.write(new byte[] {0x09, classSaved[0], classSaved[1], c1[0], c1[1], 
					0x0C, c2[0], c2[1], saveInt[0], saveInt[1], 
					0x01, 0x00, (byte) varName.length()});
			
			//bout.write(new byte[] {0x01, 0x00, (byte) varName.length()});
			bout.write(varName.getBytes());
			
			counter = incrementByteList(counter, 3);
			
			if (curNode.link == null) {break;}
			curNode = curNode.link;
		}
		// write "code"
		saveCode = counter;
		bout.write(new byte[] {0x01, 0x00, 0x04, 0x43, 0x6F, 0x64, 0x65});
		counter = incrementByteList(counter, 1);
		// write "LineNumberTable"
		// lnt = counter;
		bout.write(new byte[] {0x01, 0x00, 0x0F, 0x4C, 0x69, 0x6E,
				0x65, 0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72, 0x54, 0x61, 
				0x62, 0x6C, 0x65});
		counter = incrementByteList(counter, 1);
		// write "StackMapeable"
		bout.write(new byte[] {0x01, 0x00, 0x0D, 0x53, 0x74, 0x61,
				0x63, 0x6B, 0x4D, 0x61, 0x70, 0x54, 0x61, 0x62, 0x6C, 0x65});
		counter = incrementByteList(counter, 1);
		// write "sourcefile"
		saveSourcecode = counter;
		bout.write(new byte[] {0x01, 0x00, 0x0A, 0x53, 0x6F, 0x75, 0x72,
				0x63, 0x65, 0x46, 0x69, 0x6C, 0x65});
		counter = incrementByteList(counter, 1);
		
		// write name of file
		saveText = counter;
		bout.write(new byte[] {0x01, 0x00, (byte) el.length()});
		bout.write(el.getBytes());
		counter = incrementByteList(counter, 1);
		
		// Part 2: Fields TODO
		
		// Merkmale -> synchronized
		bout.write(new byte[] {0x00, 0x01});
		// Klasse -> pointer auf Konstanteneintrag 07
		bout.write(new byte [] {classSaved[0], classSaved[1]});
		// Superklasse
		bout.write(new byte[] {0x00, 0x02});
		// Anzahl Interfaces = 0
		bout.write(new byte[] {0x00, 0x00});
		// Anzahl Felder (8)
		bout.write(ByteBuffer.allocate(2).putChar((char) varCounter).array());
		// Felder Final: 20!
		/*
		 * 1. Merkmale 0010 -> Final short
		 * 2. Namensindex -> verweis auf namen in Konstantenpool short
		 * 3. Signatur Index -> Verweis auf Typ 000C short
		 * 4. Anzahl Attribute short
		 * 5. Attributeintrag:
		 * 	a) Attributname-Index short
		 *  b) Attributlänge int
		 *  c) Konstantenindex short
		 */
		curNode = n.left;
		int constCounter = 0;
		// write final variables
		for (int i = 0; i < FinalVariablen.size(); i++) {
			// merkmale final
			bout.write(0x00);
			bout.write(0x10);
			// Name index
			byte[] value = FinalVariablen.get(i).val01;
			bout.write(new byte[] {value[0], value[1]});
			// Signaturindex
			bout.write(new byte[] {saveInt[0], saveInt[1]});
			// Anzahl Attribute
			if (curNode.right.sst_class == Node.CONST) {
				// we have Attributes (always one)
				bout.write(0x00);
				bout.write(0x01);
				// Attributename Index -> constant value
				bout.write(new byte[] {ConstantValue[0], ConstantValue[1]});
				// Attributlänge
				bout.write(new byte[] {0x00, 0x00, 0x00, 0x02});
				// short -> Konstantenpoolverweise
				byte[] temp = incrementByteList(Constants, constCounter);
				bout.write(new byte[] {temp[0], temp[1]});
				constCounter++;
			}
			else {
				// No Attributes
				bout.write(0x00);
				bout.write(0x00);
			}
			curNode = curNode.link;
		}
		// write Normal Variables
		// count which var we are at for fast access
		int count = 2;
		while (curNode != null) {
			// Merkmale sind 0000
			bout.write(0x00);
			bout.write(0x00);
			// Namensindex
			byte[] temp = incrementByteList(globalVars, count);
			bout.write(new byte[] {temp[0], temp[1]});
			// Signaturindex -> "I"
			bout.write(new byte[] {saveInt[0], saveInt[1]});
			// keine Attribute
			bout.write(0x00);
			bout.write(0x00);
			if (curNode.link == null) { break; }
			curNode = curNode.link;
			count += 3;
		}
		curNode = n.link;
		
		// Part 3: Methods TODO
		
		// Anzahl Methoden
		int anzahlMethoden = Methoden.size() + 1;
		bout.write(ByteBuffer.allocate(2).putChar((char) anzahlMethoden).array());
		// Methoden
		// Init
		// 1. Merkmale:
		bout.write(0x00);
		bout.write(0x01);
		// 2. Verweis auf init
		bout.write(saveInit);
		// 3. Signatur ()V
		bout.write(saveInitReturn);
		// 4. AttributAnzahl
		bout.write(0x00);
		bout.write(0x01);
		// Attribut:
		// a) "Code" Verweis
		bout.write(saveCode);
		// b) Attributlänge -> erst nach Codeerzeugung feststellbar
		byte[] byteCode = generateInitByteCode(n.left, FinalVariablen, Methoden);
		// 8 wegen 2 stack, 2 lok vars, 4 codelaenge schon drin
		// 2 wegen Ausnahmetabelle und 2 weitere wegen Anzahl Attribute
		int attributSize = byteCode.length + 4;
		// write Attributsize
		bout.write(ByteBuffer.allocate(4).putInt(attributSize).array());
		// c) stack, lok Vars, code...
		bout.write(byteCode);
		// Ausnahmetabellegröße
		bout.write(0x00); bout.write(0x00);
		// Anzahl Attribute
		bout.write(0x00); bout.write(0x00);
		
		// Normal Methods
		for (int i = 0; i < Methoden.size(); i++) {
			FieldSaver curMethod = Methoden.get(i);
			// 1. Merkmale
			bout.write(0x00);
			bout.write(0x01);
			// 2. Methoden name
			byte[] pos = curMethod.val01;
			bout.write(new byte[] {pos[0], pos[1]});
			// 3. Methoden Signatur
			byte[] pos1 = curMethod.para01;

			bout.write(new byte[] {pos1[0], pos1[1]});
			// 4. Anzahl Attribute
			bout.write(new byte[] {0x00, 0x01});
			
			// a) Attributnameindex -> Verweis auf Code
			bout.write(new byte[] {saveCode[0], saveCode[1]});

			byte[] field = FunctionCode(curNode, curMethod, FinalVariablen, Methoden, GlobalVariablen);
			// b) Attributlänge 4 + codelänge (int)
			int length = field.length + 4;
			bout.write(ByteBuffer.allocate(4).putInt(length).array());
			// DONE c) Stackgröße -> curMethod.stackSize
			// DONE d) lokale Variablen -> curMethod.localVars
			// DONE e) Codelänge (int)
			// compute code
			// f) Code
			bout.write(field);
			
			// g) Ausnahmetabelle größe
			bout.write(new byte[] {0x00, 0x00});
			// h) Anzahl Attribute
			bout.write(new byte[] {0x00, 0x00});
			
			curNode = curNode.link;
		}
		
		// write Attribut of file:
		// length of Attribute
		bout.write(0x00); bout.write(0x01);
		// sourcecode
		bout.write(saveSourcecode);
		// save argument length
		bout.write(new byte[] {0x00, 0x00, 0x00, 0x02});
		// save textfile location
		bout.write(saveText);
		
		bout.toByteArray();
		
		try(OutputStream outputStream = new FileOutputStream("ByteCodeGenerierung//Test.class")) {
		    bout.writeTo(outputStream);
		}
	}
	
	static byte[] createFunctionCall(Node n, byte[] counter, byte[] classSaved) throws IOException {
		String methName = n.varName;
		
		byte[] c1 = incrementByteList(counter, 1);
		byte[] c2 = incrementByteList(counter, 2);
		byte[] c3 = incrementByteList(counter, 3);
		
		byte[] temp1 = new byte[] {0x0A, classSaved[0], classSaved[1], c1[0], c1[1],
				0x0C, c2[0], (byte) c2[1], c3[0], c3[1],
				0x01, 0x00, (byte) methName.length()};
		byte[] temp2 = methName.getBytes();
		byte[] fin = mergeByteArrays(temp1, temp2);
		String t = "(";
		for (int i = 0; i < n.parameterLength; i++) { t += "I"; }
		t += ")";
		if (n.returnValue == 1) { t += "I"; }
		else { t += "V"; }
		
		return mergeByteArrays(
				mergeByteArrays(fin, new byte[] {0x01, 0x00, (byte) t.length()}), t.getBytes());
	}
	
	public static byte[] generateInitByteCode(Node n, ArrayList<FieldSaver> FinalVariablen, ArrayList<FieldSaver> Methoden) throws IOException {
		/*
		 * n is Startnode of final (constant) Variables
		 * Generate Bytecode for init function
		 */
		// Temporärer ByteStream, der später angehängt werden muss
		ByteArrayOutputStream codeBout = new ByteArrayOutputStream();
		// header contains stack size, lokal vars and code size
		ByteArrayOutputStream headerBout = new ByteArrayOutputStream();
		// Attributlänge, Codelänge und Stackgröße wird benötigt
		// Stackgröße ist Größe des längsten rechten Teilbaums + 1
		int stackSize = stackSize(n);
		// write stack size
		headerBout.write(ByteBuffer.allocate(2).putChar((char) stackSize).array());
		// write lokal vars
		headerBout.write(new byte[] {0x00, 0x01});
		
		//tempBout.write(ByteBuffer.allocate(2).putChar((char) stackSize).array());
		// lokale Variablen -> init also eine
		//tempBout.write(new byte[] {0x00, 0x01});
		// CodeLänge -> int
		
		// Always load a0 at the beginning
		codeBout.write(0x2A);
		// invoke special on java lang object
		codeBout.write(new byte[] {(byte) 0xB7, 0x00, 0x01});

		while (n != null) {
			if (n.right == null || n.left == null) { break; }
			// this assignement since left is always a final var
			codeBout.write(0x2A);
			// check right path for function calls or vars
			byte[] temp = initCode(n.right, FinalVariablen, Methoden);
			codeBout.write(temp);
			// actual Assignement
			// get pos of Final value
			byte[] pos = searchFinals(n.left.varName, FinalVariablen);
			// this operator
			// codeBout.write(0x2A);
			codeBout.write(0xB5);
			codeBout.write(pos);
			
			if (n.link == null) { break; }
			n = n.link;
		}
		codeBout.write(0xB1);
		// write Code length
		headerBout.write(ByteBuffer.allocate(4).putInt(codeBout.toByteArray().length).array());
		
		// Code counter has to be appended
		// Merge Code counter and rest
		return mergeByteArrays(headerBout.toByteArray(), codeBout.toByteArray());
	}
	
	public static int countThis(Node n) {
		/*
		 * count how many times we need to call aload0
		 */
		int counter = 0;
		if (n.sst_class == Node.VAR || n.sst_class == Node.PROC_CALL) {
			counter += 1;
		}
		if (n.right != null) {
			counter += countThis(n.right);
		}
		if (n.left != null) {
			counter += countThis(n.left);
		}
		return counter;
	}
	
	public static byte[] initCode(Node n, ArrayList<FieldSaver> FinalVariablen, ArrayList<FieldSaver> Methoden) throws IOException {
		/*
		 * n is the right Node of Assignement
		 * Generate the code for the init methode
		 */
		ByteArrayOutputStream tempBout = new ByteArrayOutputStream();
		if (n.sst_class == Node.CONST) {
			tempBout.write(0x10); // bipush
			tempBout.write((byte) n.varVal); // push constant value
			// can be in parameter section so link might exist
			if (n.link != null) {
				tempBout.write(initCode(n.link, FinalVariablen, Methoden));
			}
		}
		else if (n.sst_class == Node.VAR) {
			// Final Value -> search name in final Variablen
			// this operator
			// this operator
			tempBout.write(0x2A);
			tempBout.write(0xB4); // getField
			tempBout.write(searchFinals(n.varName, FinalVariablen)); // Final Var Field pos
			// can be in parameter section so link might exist
			if (n.link != null) {
				tempBout.write(initCode(n.link, FinalVariablen, Methoden));
			}
		}
		else if (n.sst_class == Node.BINOP){
			tempBout.write(initCode(n.left, FinalVariablen, Methoden));
			tempBout.write(initCode(n.right, FinalVariablen, Methoden));
			if (n.operation.equals("+")) { tempBout.write(0x60); }
			else if (n.operation.equals("-")) { tempBout.write(0x64); }
			else if (n.operation.equals("*")) { tempBout.write(0x68); }
			else if (n.operation.equals("/")) { tempBout.write(0x6C); }
			else {
				System.out.println("Unknown Operation!");
			}
		}
		else if (n.sst_class == Node.PROC_CALL) {
			// count parameter
			int parameterCount = 0;
			// Method call
			tempBout.write(0x2A);
			if (n.left != null) {
				// we have parameters
				parameterCount++;
				Node temp = n.left;
				while (temp.link != null) {
					parameterCount++;
					temp = temp.link;
				}
				tempBout.write(initCode(n.left, FinalVariablen, Methoden));
			}
			tempBout.write(0xB6); // invoke special
			tempBout.write(searchMethods(n.varName, Methoden, parameterCount));
		}
		else {
			System.out.println("What is that?");
			System.out.println(n.varName);
			System.out.println(n.sst_class);
		}
		return tempBout.toByteArray();
	}
	
	public static byte[] FunctionCode(Node n, FieldSaver Method, 
			ArrayList<FieldSaver> FinalVariablen, ArrayList<FieldSaver> Methoden,
			ArrayList<FieldSaver> GlobalVariablen) throws IOException {
		ByteArrayOutputStream headerBout = new ByteArrayOutputStream();
		
		ret = false;
		
		FieldSaver curMethod = Method;
		// Stackgröße
		headerBout.write(curMethod.stackSize);
		// local vars
		headerBout.write(ByteBuffer.allocate(2).putChar((char) curMethod.localVars).array());
		
		byte[] body = FunctionByteCode(n, Method, FinalVariablen, Methoden, GlobalVariablen);
		
		
		// no return in body
		if (ret == false) { 
			headerBout.write(ByteBuffer.allocate(4).putInt(body.length + 1).array());
			return mergeByteArrays(mergeByteArrays(headerBout.toByteArray(), body), new byte[] {(byte) 0xB1});
		}
		headerBout.write(ByteBuffer.allocate(4).putInt(body.length).array());
		return mergeByteArrays(headerBout.toByteArray(), body);
	}
	
	public static byte[] FunctionByteCode(Node n, FieldSaver Method, 
			ArrayList<FieldSaver> FinalVariablen, ArrayList<FieldSaver> Methoden,
			ArrayList<FieldSaver> GlobalVariablen) throws IOException {
		ByteArrayOutputStream tempBout = new ByteArrayOutputStream();
		
		Node curNode = n.right;
		if (curNode == null) { return new byte[] {}; }
		
		while (curNode != null) {
			// case 1: Assignement
			if (curNode.sst_class == Node.ASSIGN) {
				// search for "this" command (left)
				int aload = searchALoad(curNode.left, FinalVariablen, GlobalVariablen, Methoden);
				
				for (int i = 0; i < aload; i++) { 
					tempBout.write(0x2A); 
				}
				
				// compute right Assign
				// can use parameters, locals, globals
				byte[] ra = computeRightAssign(curNode.right, Method, FinalVariablen, Methoden, GlobalVariablen);
				tempBout.write(ra);
								
				// compute left assign
				// can be global or local
				// check global:
				String varName = curNode.left.varName;
				FieldSaver g = searchArrayList(GlobalVariablen, varName);
				if (g == null) {
					// check local
					byte l = Method.returnParPos(varName);
					if (l == -1) {
						System.out.println(varName + " seems to not exist, FunctionCode - Test.java");
					}
					else {
						// local var -> store
						tempBout.write(0x36);
						// access number
						tempBout.write(l);
					}
				}
				else {
					// global var - this -> allready at the top
					// global var -> store
					// putfield
					tempBout.write(0xB5);
					tempBout.write(g.val09);
				}
			}
			else if (curNode.sst_class == Node.RETURN) {
				ret = true;
				// this statement will be done in computeRightAssign()
				
				tempBout.write(computeRightAssign(curNode.right, Method, FinalVariablen, Methoden, GlobalVariablen));
				if (curNode.right != null) { 
					// return int 
					tempBout.write(0xAC); 
				}
				else { tempBout.write(0xB1); }
			}
			else if (curNode.sst_class == Node.PROC_CALL) {				
				byte[] pc = computeRightAssign(curNode, Method, FinalVariablen, Methoden, GlobalVariablen);
				tempBout.write(pc);
			}
			else if (curNode.sst_class == Node.IFELSE) {
				// Werte left left aus
				Node cond = curNode.left.left;
				
				byte[] ifConditionL = computeRightAssign(cond.left, Method, FinalVariablen, Methoden, GlobalVariablen);
				byte[] ifConditionR = computeRightAssign(cond.right, Method, FinalVariablen, Methoden, GlobalVariablen);
				
				// werte right of if aus
				byte[] ifBlock = FunctionByteCode(curNode.left, Method, 
						FinalVariablen, Methoden, GlobalVariablen);
								
				// werte right of ifelse aus
				byte[] elseBlock = FunctionByteCode(curNode, Method, 
						FinalVariablen, Methoden, GlobalVariablen);
								
				tempBout.write(ifConditionL);
				tempBout.write(ifConditionR);
				if (cond.operation.equals("==")) {tempBout.write(0xA0);}
				else if (cond.operation.equals("<")) {tempBout.write(0xA2);}
				else if (cond.operation.equals("<=")) {tempBout.write(0xA3);}
				else if (cond.operation.equals(">")) {tempBout.write(0xA4);}
				else if (cond.operation.equals(">=")) {tempBout.write(0xA1);}
				else {System.out.println("UNKNOWN OPERATION! - IF in Function Call");}
				// jump
				tempBout.write(ByteBuffer.allocate(2).putChar((char) (ifBlock.length + 6)).array());

				// write if code
				tempBout.write(ifBlock);
				// goto 
				tempBout.write(0xA7);
				// jump				
				tempBout.write(ByteBuffer.allocate(2).putChar((char) (elseBlock.length + 3)).array());
				// write else
				tempBout.write(elseBlock);
			}
			else if (curNode.sst_class == Node.WHILE) {	
				Node cond = curNode.left;

				byte[] whileCondL = computeRightAssign(cond.left, Method, FinalVariablen, Methoden, GlobalVariablen);
				byte[] whileCondR = computeRightAssign(cond.right, Method, FinalVariablen, Methoden, GlobalVariablen);
				
				byte[] whileBlock = FunctionByteCode(curNode, Method, FinalVariablen, Methoden, GlobalVariablen);
				
				tempBout.write(whileCondL);
				tempBout.write(whileCondR);
				
				if (cond.operation.equals("==")) {tempBout.write(0xA0);}
				else if (cond.operation.equals("<")) {tempBout.write(0xA2);}
				else if (cond.operation.equals("<=")) {tempBout.write(0xA3);}
				else if (cond.operation.equals(">")) {tempBout.write(0xA4);}
				else if (cond.operation.equals(">=")) {tempBout.write(0xA1);}
				else {System.out.println("UNKNOWN OPERATION! - IF in Function Call");}
				
				// first jump
				tempBout.write(ByteBuffer.allocate(2).putChar((char) (whileBlock.length + 6)).array());
				// while code
				tempBout.write(whileBlock);
				
				// goto
				tempBout.write(0xA7);
				// jump to -> (length while + 3 + whileCondL + whileCondR + 1)
				int offset = -(whileBlock.length + 3 + whileCondL.length + whileCondR.length);
				
				tempBout.write(ByteBuffer.allocate(2).putChar((char) (offset)).array());
			}
			if (curNode.link == null) { break; }
			curNode = curNode.link;
		}
		return tempBout.toByteArray();
	}
	
	public static int searchALoad(Node n, ArrayList<FieldSaver> FinalVariablen, 
			ArrayList<FieldSaver> GlobalVariablen, ArrayList<FieldSaver> Methoden) {
		/*
		 * count for a given node how many this commands will be needed
		 * check only left and right branch
		 */
		int count = 0;
		// final var
		if (n == null) {return count;}
		if (n.varName != null) {
			int g = searchGlobals(GlobalVariablen, n.varName);

			if (searchFinals(n.varName, FinalVariablen) != null) {
				// final
				count += 1;
			}
			else if (g == 1){
				// global var
				count += 1;
			}
			else if (n.sst_class == Node.PROC_CALL) {
				count += 1;
			}
		}
		if (n.left != null) {
			count += searchALoad(n.left, FinalVariablen, GlobalVariablen, Methoden);
		}
		if (n.right != null) {
			count += searchALoad(n.right, FinalVariablen, GlobalVariablen, Methoden);
		}
		return count;
	}
	
	public static byte[] computeRightAssign(Node n, FieldSaver Method, 
			ArrayList<FieldSaver> FinalVariablen, ArrayList<FieldSaver> Methoden,
			ArrayList<FieldSaver> GlobalVariablen) throws IOException {
		ByteArrayOutputStream tempBout = new ByteArrayOutputStream();
		
		// check right of assign, Node can be operation, constant, var or method call
		if (n == null) { return tempBout.toByteArray(); }
		if (n.sst_class == Node.CONST) {
			tempBout.write(0x10); // bipush
			tempBout.write((byte) n.varVal); // push constant value
			// can be in parameter section so link might exist
			if (n.link != null) {
				tempBout.write(computeRightAssign(n.link, Method, FinalVariablen, Methoden, GlobalVariablen));
			}
		}
		else if (n.sst_class == Node.VAR) {
			// can be global, local or final
			// Final Value -> search name in final Variablen
			String varName = n.varName;
			byte[] sf = searchFinals(varName, FinalVariablen);
			if (sf == null) {
				// no final var
				// can be global
				FieldSaver g = searchArrayList(GlobalVariablen, varName);
				if (g == null) {
					// local var
					byte l = Method.returnParPos(varName);
					if (l == -1) {
						System.out.println(varName + " not known computeRightAssign - Test.java");
					}
					else {
						// iload
						tempBout.write(0x15);
						// load pos
						tempBout.write(l);
					}
				}
				else {
					tempBout.write(0x2A);
					tempBout.write(0xB4); // getField
					// load element
					tempBout.write(g.val09);
				}
			}
			else {
				// final var
				tempBout.write(0x2A);
				tempBout.write(0xB4); // getField
				tempBout.write(searchFinals(n.varName, FinalVariablen)); // Final Var Field pos
				
			}
			// can be in parameter section so link might exist
			if (n.link != null) {
				tempBout.write(computeRightAssign(n.link, Method, FinalVariablen, Methoden, GlobalVariablen));
			}
		}
		else if (n.sst_class == Node.BINOP){
			tempBout.write(computeRightAssign(n.left, Method, FinalVariablen, Methoden, GlobalVariablen));
			tempBout.write(computeRightAssign(n.right, Method, FinalVariablen, Methoden, GlobalVariablen));
			if (n.operation.equals("+")) { tempBout.write(0x60); }
			else if (n.operation.equals("-")) { tempBout.write(0x64); }
			else if (n.operation.equals("*")) { tempBout.write(0x68); }
			else if (n.operation.equals("/")) { tempBout.write(0x6C); }
			else { System.out.println("Unknown Operation!"); }
		}
		else if (n.sst_class == Node.PROC_CALL) {
			// count parameter
			int parameterCount = 0;
			
			// this operator
			tempBout.write(0x2A);
			
			// Method call			
			if (n.left != null) {
				// we have parameters
				parameterCount++;
				Node temp = n.left;
				while (temp.link != null) {
					parameterCount++;
					temp = temp.link;
				}
				tempBout.write(computeRightAssign(n.left, Method, FinalVariablen, Methoden, GlobalVariablen));
			}
			// this access
			// tempBout.write(0x2A);
			tempBout.write(0xB6); // invoke special
						
			tempBout.write(searchMethods(n.varName, Methoden, parameterCount));
		}
		else {
			System.out.println("What is that?");
			System.out.println(n.varName);
			System.out.println(n.sst_class);
		}
		return tempBout.toByteArray();
	}
	
	public static int stackSize(Node n) {
		int size = 0;
		int tempSize = 0;
						
		while (n != null) {
			if (n.sst_class == Node.WHILE || n.sst_class == Node.IF) {
				// Check left tree as well as right
				tempSize = stackSize(n.right);
				if (tempSize > size) { size = tempSize; }
				
				// check length of compop
				if (n.left != null) { tempSize = rekNodeSize(n.left); }
				if (tempSize > size) { size = tempSize; }
			}
			else if (n.sst_class == Node.IFELSE) {
				if (n.link != null) {
					tempSize = stackSize(n.link);
					if (tempSize > size) { size = tempSize; }
				}
				tempSize = stackSize(n.left);
				if (tempSize > size) { size = tempSize; }
			}
			if (n.right != null) { tempSize = rekNodeSize(n.right); }
			// get maximum
			if (tempSize > size) { size = tempSize; }
			
			if (n.link == null) { break; }
			n = n.link;
		}
		
		return 2*(size + 1);
	}
	
	public static int rekNodeSize(Node n) {
		int size = 1;
		if (n.left != null) {
			size += rekNodeSize(n.left);
		}
		if (n.right != null) {
			size += rekNodeSize(n.right);
		}
		return size;
	}
	
	public static byte[] searchFinals(String name, ArrayList<FieldSaver> FinalVariablen) {		
		for (int i = 0; i < FinalVariablen.size(); i++) {
			if (name.equals(FinalVariablen.get(i).name)) {
				return FinalVariablen.get(i).val09;
			}
		}
		// System.out.println(name + "   Not found in searchFinals - Test.java");
		return null;
	}
	
	public static byte[] searchMethods(String name, ArrayList<FieldSaver> Methoden, int parameters) {		
		for (int i = 0; i < Methoden.size(); i++) {
			if (name.equals(Methoden.get(i).name) && parameters == Methoden.get(i).paraCount) {
				return Methoden.get(i).val0A;
			}
		}
		return null;
	}
	
	public static int countLocals(Node n) {
		// count local Variables for given function Node
		int c = 1;
				
		Node curNode = n.right.link;
		
		while (curNode != null && curNode.sst_class == Node.VAR) {
			// should work
			c++;
			if (curNode.link == null) { break; }
			curNode = curNode.link;
		}
		return c;
	}
	
	public static FieldSaver searchArrayList(ArrayList<FieldSaver> FS, String givenName) {
		/*
		 * return Entry in Arraylist with name "givenName"
		 * if not found return null
		 */	
		for (int i = 0; i < FS.size(); i++) {
			if (FS.get(i).name.equals(givenName)) {
				return FS.get(i);
			}
		}
		return null;
	}
	
	public static int searchGlobals(ArrayList<FieldSaver> FS, String givenName) {
		/*
		 * return Entry in Arraylist with name "givenName"
		 * if not found return null
		 */
		for (int i = 0; i < FS.size(); i++) {
			if (FS.get(i).name.equals(givenName)) {
				return 1;
			}
		}
		return 0;
	}
	
	static int computeLength(Node n) {
		// get Length for Arguments list
		int counter = 10;
		// functions
		Node curNode = n.link;
		// first add Functions to Class file
		if (curNode != null) {
			while (curNode.link != null) {
				counter += 4;
				curNode = curNode.link; 
			}
			if (curNode != null) { counter += 4; }
		}
		// add var space
		curNode = n.left;
		// finals
		while (curNode != null) {
			if (curNode.left == null) { break; }
			counter += 3;
			if (curNode.right.sst_class == Node.CONST) { counter += 1; }
			// finished
			if (curNode.link == null) {break;}
			curNode = curNode.link;
		}
				
		while (curNode != null) {
			if (curNode.sst_class == Node.ASSIGN) { break; }
			counter += 3;
			if (curNode.link == null) { break; }
			curNode = curNode.link;
		}
		counter += 6;
		
		return counter;
	}
	
	static byte[] mergeByteArrays(byte[] a, byte[] b) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(a);
		outputStream.write(b);

		return outputStream.toByteArray( );
	}
	
	public static byte[] incrementByteList(byte[] b, int l) {
		byte[] bt = new byte[] {b[0], b[1]};
		for (int i = 0; i < l; i++) {
			if (bt[1] == 0xFF) { bt[0] += 1; bt[1] = 0x00; }
			else { bt[1] += 1; }
		}
		return bt;
	}
	
	public static void displayCounter(byte[] c) {
		System.out.print("COUNTER: ");
		System.out.printf("%02X ", c[0]);
		System.out.printf("%02X ", c[1]);
		System.out.println("");
	}
	
	public static void displayByteList(byte[] c) {
		System.out.println("Byte List:");
		
		for (int i = 0; i < c.length; i++) {
			System.out.printf("%02X ", c[i]);
		}
		
		System.out.println("END!");
	}
}
