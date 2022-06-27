package uebung;

import java.util.ArrayList;

public class FieldSaver {
	// Constant Vars:
	// Class
	byte[] val09;
	// Typ
	byte[] val0C;
	// Name
	byte[] val01;
	
	// Methods:
	// 1 Class
	byte[] val0A;
	// 2 Pointer Name & Parameters -> int val0C
	// 3 Name -> val01
	// 4 Parameters -> (II)I
	byte[] para01;
	// paraCount
	int paraCount;
	// count local variables
	int localVars;
	// Stackgröße
	byte[] stackSize;
	// Array list mit Parametern und lokalen Variablen
	ArrayList<String> parameters = new ArrayList<String> ();
	
	String name;
	
	FieldSaver(){}
	
	public void setValuesConstants(byte[] line, String n) {
		// normal setting of values
		val09 = line;
		line = incrementByteList(line, 1);
		val0C = line;
		line = incrementByteList(line, 1);
		val01 = line;
		name = n;
	}
	
	public void setValuesGlobals(byte[] line, String n) {
		// normal setting of values
		val01 = line;
		name = n;
	}
	
	public void setValuesMethode(byte[] line, String s, int para, int locVars, byte[] sSize,
			Node n) {
		// normal setting of values
		val0A = line;
		line = incrementByteList(line, 1);
		val0C = line;
		line = incrementByteList(line, 1);
		val01 = line;
		line = incrementByteList(line, 1);
		para01 = line;
		name = s;
		
		paraCount = para;
		localVars = locVars;
		stackSize = sSize;
		parameters = computeParameterList(n);
	}
	
	public static byte[] incrementByteList(byte[] b, int l) {
		byte[] bt = new byte[] {b[0], b[1]};
		for (int i = 0; i < l; i++) {
			if (bt[1] == 0xFF) { bt[0] += 1; bt[1] = 0x00; }
			else { bt[1] += 1; }
		}
		return bt;
	}
	
	public static ArrayList<String> computeParameterList(Node n){
		/*
		 * get function Node and compute Parameter List
		 * first the parameters then locals
		 */
		ArrayList<String> Parray = new ArrayList<String>();
		
		// Parameter Node left (comment)
		Node curNode = n.left;
		if (curNode.link != null) {
			curNode= curNode.link;
			while (curNode != null) {
				// add parameter
				Parray.add(curNode.varName);
				if (curNode.link == null) { break; }
				curNode = curNode.link;
			}
		}
		
		// local Vars
		curNode = n.right;
		if (curNode.link != null) {
			curNode= curNode.link;
			while (curNode != null && curNode.sst_class == Node.VAR) {
				// add localVar
				Parray.add(curNode.varName);
				if (curNode.link == null) { break; }
				curNode = curNode.link;
			}
		}		
		return Parray;
	}
	
	public byte returnParPos(String name) {
		/*
		 * search for Variable in parameters and local vars
		 * and return pos
		 */
		byte pos = (byte) 0x01;
		for (int i = 0; i < parameters.size(); i++) {
			if (parameters.get(i).equals(name)) {
				return pos;
			}
			pos += 1;
		}
		byte end = (byte) 0xFF;
		return end;
	}
 }
