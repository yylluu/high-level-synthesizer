package edu.yuanlu.hlsyn;

import edu.yuanlu.hlsyn.GraphEnum.*;

import java.util.*;
import java.io.*;

public class Parser {
	
	private Vector<String[]> tokens = new Vector<String[]>();
	private boolean failure = false;
	
	private File file;
	private String fileFullPath = null;
	private String dirPath = null;
	private String fileName = null;
	private BufferedReader bufferedReader;
	private Graph graph = null;
//	private BlocksGraph blocks = null;
	
	private int inCount = 0;
	private int outCount = 0;
	private int variableCount = 0;
	private int signalCount = 0;
	
	private int verticesCount = 0;
	private int addCount = 0;
	private int subCount = 0;
	private int divCount = 0;
	private int modCount = 0;
	private int shrCount = 0;
	private int shlCount = 0;
	private int mulCount = 0;
	private int gtCount = 0;
	private int ltCount = 0;
	private int eqCount = 0;
	private int muxCount = 0;
	private int regCount = 0;

	private Stack<String> blocksLabelStack = new Stack<String>();
	private Stack<GraphEnum.BlockType> blocksTypeStack = new Stack<GraphEnum.BlockType>();
	private int ifElseBranchCount = 1;
	private int whileBranchCount = 1;
	private int blocksCount = 1;
	Vertex originalVertexOfCurrentBlock = null;
	
	String lastPoppedBlockLabel = null;
	String currentBlockLabel = null;
	String toPushBlockLabel = null;
	BlockType lastPoppedBlockType = null;
	BlockType currentBlockType = null;
	BlockType toPushBlockType = BlockType.MAIN;
	boolean isLeftBracketExpectedInNextLine = false;
/*
	public Parser(String fileFullPath, BlocksGraph block) {
		super();
		this.fileFullPath = fileFullPath;
		this.blocks = blocks;
		this.graph = new Graph();
	}
*/
	public Parser(String dirPath, String fileName, Graph graph) {
		super();
		this.dirPath = dirPath;
		this.fileName = fileName;
		this.fileFullPath = dirPath + fileName;
		this.graph = graph;
		this.blocksLabelStack.push("main");
		this.blocksTypeStack.push(BlockType.MAIN);
		this.currentBlockType = BlockType.MAIN;
		this.currentBlockLabel = "main";
	}
	
	public boolean parse() {
		
		try {
		System.out.println(fileFullPath);
			file = new File(fileFullPath);
			InputStreamReader read = new InputStreamReader(new FileInputStream(file));
			bufferedReader = new BufferedReader(read);
			System.out.println("File opened.");
			c2tokens();
			tokens2graph();
			if (this.failure){
				System.out.println("Error: unsupported grammer.");
				return false;
			}
		} catch (IOException e){
			System.out.println("Cannot open (read) file.");
			return false;
		}
		
		System.out.println("Parse!");
		return true;
	}
	
	public void c2tokens()throws IOException{
		String line = null;		
		StringBuffer processedStringBuffer = new StringBuffer("");
		while ((line = bufferedReader.readLine()) != null){
			line = line.replaceAll("\t", "");////////////////////////////////this bug is confused;
			if(line.indexOf("}") != -1){
				line = line.replaceFirst("\\}", "\r\n}\r\n");
			}
			if(line.indexOf("{") != -1){
				line = line.replaceFirst("\\{", "\r\n{\r\n");
			}
			processedStringBuffer.append(line + "\r\n");
		}
		bufferedReader.close();
		File tempFile = new File(fileFullPath + "~");
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
		bufferedWriter.write(processedStringBuffer.toString());
		bufferedWriter.flush();
		bufferedWriter.close();
		InputStreamReader read = new InputStreamReader(new FileInputStream(tempFile));
		bufferedReader = new BufferedReader(read);
		while ((line = bufferedReader.readLine()) != null){
			//Delete comment
			int commnetStart = line.indexOf("//");
			if (commnetStart != -1){
				line = line.substring(0, commnetStart);
			}
			//
			if(line.indexOf("=") != -1){
				line = line.replaceFirst("=", " = ");
			}
			if(line.indexOf("+") != -1){
				line = line.replace("+", " + ");
			} 
			if(line.indexOf("*") != -1){
				line = line.replace("*", " * ");
			}
			if(line.indexOf("/") != -1){
				line = line.replace("/", " / ");
			}
			if(line.indexOf(":") != -1){
				line = line.replace(":", " : ");
			}
			if(line.indexOf("-") != -1){
				line = line.replace("-", " - ");
			}
			if(line.indexOf("%") != -1){
				line = line.replace("%", " % ");
			}
			if(line.indexOf("?") != -1){
				line = line.replace("?", " ? ");
			}
			if(line.indexOf(">") != -1 && line.indexOf(">>") == -1){
				line = line.replace(">", " > ");
			}
			if(line.indexOf("<") != -1 && line.indexOf("<<") == -1){
				line = line.replace("<", " < ");
			}
			if(line.indexOf("==") != -1){
				line = line.replace("==", " == ");
			}
			if(line.indexOf(">>") != -1){
				line = line.replace(">>", " >> ");
			}
			if(line.indexOf("<<") != -1){
				line = line.replace("<<", " << ");
			}
			if(line.indexOf("(") != -1){
				line = line.replace("(", " ( ");
			}
			if(line.indexOf(")") != -1){
				line = line.replace(")", " ) ");
			}
			if(line.indexOf(";") != -1){
				line = line.replace(";", "");
			}
			if(line.indexOf("unsigned") != -1){
				line = line.replace("unsigned", "");
				line = line.replace("bool", "ubool");
				line = line.replace("char", "uchar");
				line = line.replace("short", "ushort");
				line = line.replace("int", "uint");
				line = line.replace("long", "ulong");
			}
			String[] strArray = line.split("[ [,;\r\n\t]]+");
			if (strArray.length!=0  && strArray[0] != null && strArray[0].length() > 0){
				tokens.add(strArray);
			}		
		}
		bufferedReader.close();
		System.out.println(tempFile.delete());
	}
	
	public boolean tokens2graph(){
		boolean stmtFlag = true;

		this.originalVertexOfCurrentBlock = graph.vertices.get(0);
		for (int i = 0; i < tokens.size(); i++){
			if (this.failure){
				return false;
			}
			if(stmtFlag){
				boolean isStmt = parseStatements (tokens.get(i));
				if (!isStmt){
					stmtFlag = false;
					boolean isValidOperation = parseOperations (tokens.get(i));
					if(!isValidOperation){
						this.failure = true;
						return false;
					}
				}
			} else {
				boolean isValidOperation = parseOperations (tokens.get(i));
				if(!isValidOperation){
					this.failure = true;
					return false;
				}
			}
		}		
		return true;
	}
	
	
	public boolean parseStatements (String[] lineTokens){
			
		if (lineTokens[0].equals("input")){
			for (int j =2; j<lineTokens.length; j++){
				Edge newInput = new Edge(lineTokens[j],EdgeType.INPUT);
				if(!this.graph.hasRepeatingEdge(newInput)){
					if (!newInput.setBus(lineTokens[1])) {
						System.out.println("Error: Invalid bus statement.");
						this.failure = true;
					}
					newInput.addStartVertex(this.graph.vertices.get(0));//vertices.get(0) is INOP
					this.graph.addEdge(newInput); 
					inCount++; signalCount++;
				} else {
					System.out.println("Error: repeating declare of " + lineTokens[j]);
					this.failure = true;
				}
			}
		} else if (lineTokens[0].equals("output")){
			for (int j =2; j<lineTokens.length; j++){
				Edge newOutput = new Edge(lineTokens[j],EdgeType.OUTPUT);
				if(!this.graph.hasRepeatingEdge(lineTokens[j])){
					if (!newOutput.setBus(lineTokens[1])) {
						System.out.println("Error: Invalid bus statement.");
						this.failure = true;
					}
					newOutput.addEndVertex(this.graph.vertices.get(1));//vertices.get(1) is ONOP
					this.graph.addEdge(newOutput); 
					outCount++; signalCount++;
				} else {
					System.out.println("Error: repeating declare of " + lineTokens[j]);
					this.failure = true;
				}
			}
		} else if (lineTokens[0].equals("variable") ){//variable
			for (int j =2; j<lineTokens.length; j++){
				Edge newVariable = new Edge(lineTokens[j],EdgeType.VARIABLE);
				if(!this.graph.hasRepeatingEdge(newVariable)){
					if (!newVariable.setBus(lineTokens[1])) {
						System.out.println("Error: Invalid bus statement.");
						this.failure = true;
					}
					this.graph.addEdge(newVariable); 
					variableCount++; signalCount++;
				} else {
					System.out.println("Error: repeating declare of " + lineTokens[j]);
					this.failure = true;
				}
			}
		} else {
			System.out.println("No statement entry.");
			return false;
		}
		return true;
		
	}
	
	
	
	public boolean parseOperations (String[] lineTokens){
		
		currentBlockLabel = this.blocksLabelStack.peek();
		currentBlockType = this.blocksTypeStack.peek();
		
		if (isLeftBracketExpectedInNextLine){
			if(!lineTokens[0].equals("{") || lineTokens.length != 1){
				this.failure = true;
				return false;
			} else {
				this.blocksLabelStack.push(toPushBlockLabel);
				this.blocksTypeStack.push(toPushBlockType);
				isLeftBracketExpectedInNextLine = false;
			}	
		} else if (lineTokens[0].equals("}") && lineTokens.length == 1){
			if (this.blocksLabelStack.size()==1){
				this.failure = true;
				return false;
			} else if (this.blocksLabelStack.size()==2){
				originalVertexOfCurrentBlock = this.graph.vertices.get(0);
			}
			lastPoppedBlockLabel = this.blocksLabelStack.pop();
			lastPoppedBlockType = this.blocksTypeStack.pop();
			this.blocksCount++;
		} else if (lineTokens[0].equals("if") && lineTokens.length==4 && lineTokens[1].equals("(") && lineTokens[3].equals(")")){
			isLeftBracketExpectedInNextLine = true;
			Vertex newVertex = new Vertex("IFELSE" + ifElseBranchCount, VertexType.IF);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.vertices.add(newVertex);
			Edge inEdge = graph.searchEdge(lineTokens[2]);
			if(inEdge != null) {
				inEdge.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge);
			}
			toPushBlockLabel = "ifelse" + ifElseBranchCount;
			toPushBlockType = BlockType.IF;
			originalVertexOfCurrentBlock = newVertex;
			this.ifElseBranchCount++; this.blocksCount++;
		} else if (lineTokens[0].equals("while") && lineTokens.length==4 && lineTokens[1].equals("(") && lineTokens[3].equals(")")){
			isLeftBracketExpectedInNextLine = true;
			Vertex newVertex = new Vertex("WHILE" + whileBranchCount, VertexType.WHILE);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.vertices.add(newVertex);
			Edge inEdge = graph.searchEdge(lineTokens[2]);
			if(inEdge != null) {
				inEdge.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge);
			}
			toPushBlockLabel = "while" + whileBranchCount;
			toPushBlockType = BlockType.WHILE;
			originalVertexOfCurrentBlock = newVertex;
			this.whileBranchCount++; this.blocksCount++;
		} else if (lineTokens[0].equals("else") && lineTokens.length == 1 ){
			isLeftBracketExpectedInNextLine = true;
			toPushBlockLabel = lastPoppedBlockLabel;
			toPushBlockType = BlockType.ELSE;
			if (lastPoppedBlockType != BlockType.IF){
				this.failure = true;
				return false;
			} else {
				originalVertexOfCurrentBlock = graph.searchVertex(lastPoppedBlockLabel, VertexType.IF);
				if (originalVertexOfCurrentBlock==null){
					System.err.println("Error: Wrong search");
				}
			}
			this.blocksCount++; //this.ifelseBlocksCount++; 
		} else if (lineTokens.length<3 || lineTokens.length>7 || lineTokens.length==6 || !lineTokens[1].equals("=")){
			printline(lineTokens);
			System.out.println("Error: wrong tokens number");
			this.failure = true;
			return false;
		} else if (lineTokens.length==3){
			regCount++;
			Vertex newVertex = new Vertex("REG" + regCount, VertexType.REG);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge != null) {
				inEdge.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				this.failure = true;
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				this.failure = true;
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals("+")){
			addCount++;
			Vertex newVertex = new Vertex("ADD" + addCount, VertexType.ADD);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				this.failure = true;
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				this.failure = true;
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges= outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				this.failure = true;
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals("-")){
			subCount++;
			Vertex newVertex = new Vertex("SUB" + subCount, VertexType.SUB);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				this.failure = true;
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				this.failure = true;
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				this.failure = true;
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals("*")){
			mulCount++;
			Vertex newVertex = new Vertex("MUL" + mulCount, VertexType.MUL);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals("/")){
			divCount++;
			Vertex newVertex = new Vertex("DIV" + divCount, VertexType.DIV);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals("%")){
			modCount++;
			Vertex newVertex = new Vertex("MOD" + modCount, VertexType.MOD);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals("<")){
			ltCount++;
			Vertex newVertex = new Vertex("COMPLT" + ltCount, VertexType.COMPLT);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals(">")){
			gtCount++;
			Vertex newVertex = new Vertex("COMPGT" + gtCount, VertexType.COMPGT);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals("==")){
			eqCount++;
			Vertex newVertex = new Vertex("COMPEQ" + eqCount, VertexType.COMPEQ);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals("<<")){
			shlCount++;
			Vertex newVertex = new Vertex("SHL" + shlCount, VertexType.SHL);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else if (lineTokens.length==5 && lineTokens[3].equals(">>")){
			shrCount++;
			Vertex newVertex = new Vertex("SHR" + shrCount, VertexType.SHR);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge inEdge1 = graph.searchEdge(lineTokens[4]);
			Edge inEdge2 = graph.searchEdge(lineTokens[2]);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else if (lineTokens.length==7 && lineTokens[3].equals("?") && lineTokens[5].equals(":")){
			muxCount++;
			Vertex newVertex = new Vertex("MUX" + muxCount, VertexType.MUX);
			newVertex.blockSeq = blocksCount;
			newVertex.blockType = currentBlockType;
			newVertex.blockName = currentBlockLabel;
			newVertex.blockOriginVertex = originalVertexOfCurrentBlock;
			graph.addVertex(newVertex);
			Edge outEdge = graph.searchEdge(lineTokens[0]);
			Edge inEdge1 = graph.searchEdge(lineTokens[2]);
			Edge inEdge2 = graph.searchEdge(lineTokens[4]);
			Edge inEdge3 = graph.searchEdge(lineTokens[6]);
			if(inEdge1 != null) {
				inEdge1.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge1);
			} else {
				System.out.println("Error: " + lineTokens[2] +" not declared.");
				return false;
			}
			if(inEdge2 != null) {
				inEdge2.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge2);
			} else {
				System.out.println("Error: " + lineTokens[4] +" not declared.");
				return false;
			}
			if(inEdge3 != null) {
				inEdge3.addEndVertex(newVertex);
				newVertex.inEdges.add(inEdge3);
			} else {
				System.out.println("Error: " + lineTokens[6] +" not declared.");
				return false;
			}
			if(outEdge != null) {
				outEdge.addStartVertex(newVertex);
				newVertex.outEdges = outEdge;
			} else {
				System.out.println("Error: " + lineTokens[0] +" not declared.");
				return false;
			}
		} else {
			printline(lineTokens);
			this.failure = true;
			return false;
		}
		return true;
	}
	
	
	private void printline(String[] lineTokens){
		for(int i= 0; i<lineTokens.length; i++){
			System.out.println(lineTokens[i]);
		}
	}

}
