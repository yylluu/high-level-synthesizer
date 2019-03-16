package edu.yuanlu.hlsyn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import edu.yuanlu.hlsyn.GraphEnum.*;

public class Blocks extends Graph{

	private ArrayList<Block> blocks = new ArrayList<Block>();
	private int totalLatency = 0;
	
	/**
	 * update the vertex.nextVertics and vertex.predVertics by using edge info
	 */
	public void updateBlockesTopology(){
		Iterator<Edge> i = this.edges.iterator();
		while (i.hasNext()){
			Edge e = (Edge)i.next();
			e.updateTopology();
		}
	}
	
	/**
	 * 
	 * @param mode
	 */
	public Blocks(ConstructionMode mode) {
		super(mode);
		//this.latencyConstrain = latencyConstrain;
		//this.graph = graph;
	}
	
	/**
	 * 
	 * @param latencyConstrain
	 * @param log
	 * @return
	 */
	public boolean systhesizeBlocks (int latencyConstrain, StringBuffer log){
		//this.latencyConstrain = latencyConstrain;
		this.updateBlockesTopology();
		this.graph2Blocks();
		this.updateBlockTopology();
		for(int index = 0; index < blocks.size(); index++){
			Block b = blocks.get(index);
			updateThenBlockParameters(b);
			b.systhesizer.setLatencyConstrain(latencyConstrain);
			if(!b.systhesizer.scheduleForceDirect(log)) return false;
			log.append("A block scheduled.\r\n\r\n");
			totalLatency += b.systhesizer.necessaryLatency;
		}
		log.append("The total necessary latency: " + totalLatency + "\r\n");
		return true;
	}
	
	/**
	 * 
	 */
	private void graph2Blocks(){
		Iterator<Vertex> iter = this.vertices.iterator(); iter.next(); iter.next();
		while(iter.hasNext()){
			Vertex u = iter.next();
			boolean notBelongToAnyExistedBlock = true;
			for (int index = 0; index < blocks.size(); index++){
				if (blocks.get(index).vertices.get(2).blockSeq == (u.blockSeq)){
					notBelongToAnyExistedBlock = false;
					blocks.get(index).vertices.add(u);
					break;
				}
			}
			if(notBelongToAnyExistedBlock){
				Block newBlock = new Block(ConstructionMode.EMPTY);//when arg==Mode.NOP, new graph contains INOP and ONOP, otherwise it is empty graph.
				newBlock.vertices.add(this.vertices.get(0));
				newBlock.vertices.add(this.vertices.get(1));
				newBlock.vertices.add(u);
				newBlock.initialBlockParameters();
				blocks.add(newBlock);
			}
		}
	}
	
	/**
	 * 
	 */
	private void updateBlockTopology(){
		Iterator<Vertex> i = this.vertices.iterator(); i.next(); i.next();
		while(i.hasNext()){ //所有的点都和INOP和ONOP连接在一起
			Vertex u = i.next();
			u.predVertices.add(vertices.get(0));
			vertices.get(0).nextVertices.add(u);
			u.nextVertices.add(vertices.get(1));
			vertices.get(1).predVertices.add(u);
		}
	}
	
	/**
	 * 
	 */
	private void connectBlocksHavingSameNameAndType(){
		for (int index = 0; index < blocks.size(); index++){
			Block b1 = blocks.get(index);
			for (int index2 = index + 1; index2 < blocks.size(); index2++){
				Block b2 = blocks.get(index2);
				if (b1.blockName.equals(b2.blockName) && b1.blockType.equals(b2.blockType)){
					b1.seqThenBlock = b2.blockSeq;
					break;
				}
			}
		}
	}
	
	/**
	 * 
	 * @param b is a block
	 * @return
	 */
	private int[] updateThenBlockParameters(Block b){
		int[] thenBlockParameters = {0,0}; 
		if(b.seqThenBlock == 0){//还没有确定THEN
			if(b.blockType.equals(BlockType.MAIN)){
				b.setNextBlockParameters(-2, 1);
				thenBlockParameters[0] = -2;	//-2 代表Then是ONOP
				thenBlockParameters[1] = 1;		//1代表去Block的第一个状态，-1代表去block的最后一个状态
			} else if (b.blockType.equals(BlockType.WHILE)){
				Block originBlock = searchBlockContainsThatVertex(b.blockOriginVertex);
				originBlock.seqWhileBlock2Go = seachMinSeqBlockHavingSameNameAndType(b).blockSeq;
				int seqNextBlock = originBlock.blockSeq;
				b.setNextBlockParameters(seqNextBlock, -1);
				thenBlockParameters[0] = seqNextBlock; //Then block的序列号
				thenBlockParameters[1] = -1; 	//1代表去Block的第一个状态，-1代表去block的最后一个状态
			} else {
				Block originBlock = searchBlockContainsThatVertex(b.blockOriginVertex);
				if (b.blockType.equals(BlockType.IF)){
					originBlock.seqIfBlock2Go = seachMinSeqBlockHavingSameNameAndType(b).blockSeq;
				} else{
					originBlock.seqElseBlock2Go = seachMinSeqBlockHavingSameNameAndType(b).blockSeq;
				}
				int seqThenBlock = originBlock.seqThenBlock;
				int go2StartOrEndStateInThenBlock = originBlock.go2StartOrEndStateInThenBlock;
				if (seqThenBlock == 0){
					thenBlockParameters = updateThenBlockParameters(originBlock);
					b.setNextBlockParameters(thenBlockParameters[0], thenBlockParameters[1]);
				} else{
					b.setNextBlockParameters(seqThenBlock, go2StartOrEndStateInThenBlock);
				}
			}
		}
		return thenBlockParameters;
	}
	
	
	/**
	 * 
	 * @param u is a vertex
	 * @return
	 */
	private Block searchBlockContainsThatVertex(Vertex u){
		Iterator<Block> i = blocks.iterator();
		while(i.hasNext()){
			Block b = i.next();
			if (b.isVertexBelongsToThisBlock(u)){
				return b;
			}
		}
		return new Block(ConstructionMode.ERROR);
	}
	
	/**
	 * 
	 * @param b is a block
	 * @return is a block
	 */
	private Block seachMinSeqBlockHavingSameNameAndType(Block b){
		Iterator<Block> i = blocks.iterator();
		while(i.hasNext()){
			Block b1 = i.next();
			if (b1.blockName.equals(b.blockName)&& b1.blockType.equals(b.blockType)&& b1.blockSeq < b.blockSeq){
				b = b1;
			}
		}
		return b;		
	}
	
	
	private Block seachBlockHavingThisSeq(int seq){
		Iterator<Block> i = blocks.iterator();
		Block b = null;
		while(i.hasNext()){
			b = i.next();
			if (b.blockSeq == seq){
					return b;
			}
		}
		return b;		
	}
	
	/**
	 * 
	 * @param bElse
	 * @return
	 */
	private Block searhIfBlockForThisElseBlock (Block bElse){
		Iterator<Block> i = blocks.iterator();
		while(i.hasNext()){
			Block bIf = i.next();
			if (bIf.blockName.equals(bElse.blockName)){
				return bIf;
			}
		}
		return bElse;
	}
	
	
	/**
	 * @throws IOException 
	 * 
	 */
	public boolean printVerilogFSM (String outFileName){

		File outFile = new File(outFileName);
		BufferedWriter bufferedWriter;
		String verilogCode = "";
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(outFile));
			//Verilog entity statement
			verilogCode += "`timescale 1 ns/1 ps\r\n\r\nmodule HLSM (Clk, Rst, Start, Done";
			for (int i = 0; i < edges.size(); i++){
				if (edges.get(i).type == EdgeType.INPUT){
					verilogCode += ", " + edges.get(i).label;
				} else if (edges.get(i).type == EdgeType.OUTPUT){
					verilogCode += ", " + edges.get(i).label;
				}
			}
			verilogCode += ");\r\n\r\ninput Clk, Rst, Start;\r\noutput reg Done;\r\n\r\n";
			//Verilog signals declaration
			//Input declaration
			for (int i = 0; i < edges.size(); i++){
				if (edges.get(i).type == EdgeType.INPUT){
					if (edges.get(i).busSign == EdgeSign.UNSIGN){
						switch (edges.get(i).busWidth){
						case 1: 
							verilogCode += "input " + edges.get(i).label + ";\r\n"; 
							break;
						default: 
							verilogCode += "input [" + (edges.get(i).busWidth - 1) + ":0] " + edges.get(i).label + ";\r\n"; 
							break;
						}
					} else {
						switch (edges.get(i).busWidth){
						case 1: 
							verilogCode += "input signed " + edges.get(i).label + ";\r\n"; 
							break;
						default: 
							verilogCode += "input signed [" + (edges.get(i).busWidth - 1) + ":0] " + edges.get(i).label + ";\r\n"; 
							break;
						}
					}
				}
			}
			verilogCode += "\r\n";
			//Output declaration
			for (int i = 0; i < edges.size(); i++){
				if (edges.get(i).type == EdgeType.OUTPUT){
					if (edges.get(i).busSign == EdgeSign.UNSIGN){
						switch (edges.get(i).busWidth){
						case 1: 
							verilogCode += "output reg " + edges.get(i).label + ";\r\n"; 
							break;
						default: 
							verilogCode += "output reg [" + (edges.get(i).busWidth - 1) + ":0] " + edges.get(i).label + ";\r\n"; 
							break;
						}
					} else {
						switch (edges.get(i).busWidth){
						case 1: 
							verilogCode += "output reg signed " + edges.get(i).label + ";\r\n"; 
							break;
						default: 
							verilogCode += "output reg signed [" + (edges.get(i).busWidth - 1) + ":0] " + edges.get(i).label + ";\r\n"; 
							break;
						}
					}
				}
			}
			verilogCode += "\r\n";
			//Variable declaration
			for (int i = 0; i < edges.size(); i++){
				if (edges.get(i).type == EdgeType.VARIABLE){
					if (edges.get(i).busSign == EdgeSign.UNSIGN){
						switch (edges.get(i).busWidth){
						case 1: 
							verilogCode += "reg " + edges.get(i).label + ";\r\n"; 
							break;
						default: 
							verilogCode += "reg [" + (edges.get(i).busWidth - 1) + ":0] " + edges.get(i).label + ";\r\n"; 
							break;
						}
					} else {
						switch (edges.get(i).busWidth){
						case 1: 
							verilogCode += "reg signed " + edges.get(i).label + ";\r\n"; 
							break;
						default: 
							verilogCode += "reg signed [" + (edges.get(i).busWidth - 1) + ":0] " + edges.get(i).label + ";\r\n"; 
							break;
						}
					}
				}
			}
			verilogCode += "\r\n";
			//Declare the state parameters for the Finite State Machine 
			verilogCode += "parameter Wait = 0, ";
			for (int i = 1; i <= this.totalLatency; i++){
				verilogCode += "S" + i + " = " + i + ", ";
			}
			verilogCode += "Final" + " = " + (totalLatency + 1) + ";\r\n\r\n";
			verilogCode += "reg [" + (Integer.toBinaryString(totalLatency).length() - 1) + ":0] " + "State;\r\n\r\n";
			verilogCode += "// High-level state machine\r\n";
			//Start describe the Verilog FSM
			verilogCode += "always @(posedge Clk) begin\r\n\t";
			//Reset
			verilogCode += "if (Rst == 1) begin\r\n\t\tState <= Wait; //Reset state register\r\n\t\tDone <= 0; //Reset output register\r\n";
			for (int i = 0; i < edges.size(); i++){
				if(edges.get(i).type==EdgeType.OUTPUT){
					verilogCode += "\t\t" + edges.get(i).label + " <= 0; //Reset output register\r\n";
				}
				else if (edges.get(i).type==EdgeType.VARIABLE){
					verilogCode += "\t\t" + edges.get(i).label + " <= 0; //Reset inner register\r\n";
				}
			}
			//State transition
			//Wait state
			verilogCode += "\tend //end if\r\n\telse begin\r\n\t\tcase (State)\r\n";
			verilogCode += "\t\tWait: begin\r\n\t\t\tDone <= 0;\r\n\t\t\tif (Start == 1)\r\n\t\t\t\tState <= S1;\r\n\t\t\telse\r\n\t\t\t\tState <= Wait;\r\n\t\tend //end Wait\r\n";
			//
			for (int blockIndex = 0; blockIndex<this.blocks.size(); blockIndex++){
				boolean hasIfWhileFlag = false;
				Block b = blocks.get(blockIndex);
				//
				int latencyIndex;
				//States (Except the last state) of each Block
				for (latencyIndex = 1; latencyIndex < b.systhesizer.necessaryLatency; latencyIndex++){
					verilogCode += "\t\tS" + blockStateIndex2GlobalStateIndex(b.blockSeq,latencyIndex) + ": begin\r\n";
					for (int vertexIndex = 2; vertexIndex < b.vertices.size(); vertexIndex++){
						Vertex u = blocks.get(blockIndex).vertices.get(vertexIndex);
						if (u.forceDirectStart == latencyIndex && latencyIndex < b.systhesizer.necessaryLatency){
							verilogCode += "\t\t\t" + u.printVertexAsVerilogOperation() + ";\r\n";
						}
					}
					verilogCode += "\t\t\tState <= S" + blockStateIndex2GlobalStateIndex(b.blockSeq,latencyIndex + 1) + ";\r\n";
					verilogCode += "\t\tend // end S" + blockStateIndex2GlobalStateIndex(b.blockSeq,latencyIndex) + ";\r\n";
				}
				//Last State of each Block
				latencyIndex = b.systhesizer.necessaryLatency;
				verilogCode += "\t\tS" + blockStateIndex2GlobalStateIndex(b.blockSeq,latencyIndex) + ": begin\r\n";
					for (int vertexIndex = 2; vertexIndex < b.vertices.size(); vertexIndex++){
						Vertex u = blocks.get(blockIndex).vertices.get(vertexIndex);
						if (u.forceDirectStart == latencyIndex && u.type == VertexType.IF){
							hasIfWhileFlag = true;
							verilogCode += "\t\t\tif(" + u.inEdges.get(0).label + " != 0)\r\n"+"\t\t\t\tState <= S" + blockStateIndex2GlobalStateIndex(b.seqIfBlock2Go,1) + ";  //GO TO IF ENTITY\r\n";
							if (b.seqElseBlock2Go != 0){
								verilogCode += "\t\t\telse\r\n" + "\t\t\t\tState <= S" + + blockStateIndex2GlobalStateIndex(b.seqElseBlock2Go,1) + ";  //GO TO ELSE ENTITY\r\n";
							} else {
								if (b.seqThenBlock == -2){
									verilogCode += "\t\t\telse\r\n"+"\t\t\t\tState <= " + "Final" + ";  //GO TO THEN ENTITY\r\n";
								} else if (b.go2StartOrEndStateInThenBlock == 1){
									verilogCode += "\t\t\telse\r\n" + "\t\t\t\tState <= S" + + blockStateIndex2GlobalStateIndex(b.seqThenBlock, 1) + ";  //GO TO THEN ENTITY\r\n";
								} else if (b.go2StartOrEndStateInThenBlock == -1){
									verilogCode += "\t\t\telse\r\n" + "\t\t\t\tState <= S" + + blockStateIndex2GlobalStateIndex(b.seqThenBlock + 1, 0) + ";  //GO TO THEN ENTITY\r\n";
								}
							}
							break;
						} else if (u.forceDirectStart == latencyIndex && u.type == VertexType.WHILE){
							hasIfWhileFlag = true;
							verilogCode += "\t\t\tif(" + u.inEdges.get(0).label + " != 0)\r\n"+"\t\t\t\tState <= S" + blockStateIndex2GlobalStateIndex(b.seqWhileBlock2Go,1) + ";  //GO TO LOOP ENTITY\r\n";
							if (b.seqThenBlock == -2){
								verilogCode += "\t\t\telse\r\n"+"\t\t\t\tState <= " + "Final" + ";  //BREAK LOOP ENTITY\r\n";
							} else if (b.go2StartOrEndStateInThenBlock == 1){
								verilogCode += "\t\t\telse\r\n" + "\t\t\t\tState <= S" + + blockStateIndex2GlobalStateIndex(b.seqThenBlock, 1) + ";  //BREAK LOOP ENTITY\r\n";
							} else if (b.go2StartOrEndStateInThenBlock == -1){
								verilogCode += "\t\t\telse\r\n" + "\t\t\t\tState <= S" + + blockStateIndex2GlobalStateIndex(b.seqThenBlock + 1, 0) + ";  //BREAK LOOP ENTITY\r\n";
							}
							break;
						} else if (u.forceDirectStart == latencyIndex){
							verilogCode += "\t\t\t" + u.printVertexAsVerilogOperation() + ";\r\n";
						}
					}
					if (!hasIfWhileFlag){
						if (b.seqThenBlock == -2){//This is the last state of FSM, go to Final state.
							verilogCode += "\t\t\tState <= Final;\r\n";
						} else if (b.go2StartOrEndStateInThenBlock == 1){
							//verilogCode += "\t\t\tState <= S" + blockStateIndex2GlobalStateIndex(b.blockSeq,latencyIndex + 1) + ";  //1\r\n";
							verilogCode += "\t\t\tState <= S" + blockStateIndex2GlobalStateIndex(b.seqThenBlock, 1) + ";  //GO To THEN of IF-ELSE\r\n";
						} else if (b.go2StartOrEndStateInThenBlock == -1){
							//verilogCode += "\t\t\tState <= S" + blockStateIndex2GlobalStateIndex(b.blockSeq + 1, -1) + ";  //GO TO LOOP JUDGEMENT\r\n";
							verilogCode += "\t\t\tState <= S" + blockStateIndex2GlobalStateIndex(b.seqThenBlock + 1, 0) + ";  //GO TO LOOP JUDGEMENT\r\n";
						}
					}
					verilogCode += "\t\tend // end S" + blockStateIndex2GlobalStateIndex(b.blockSeq,latencyIndex)+ ";\r\n";
			}
			
			verilogCode += "\t\tFinal: begin\r\n\t\t\tDone <= 1;\r\n\t\t\tState <= Wait;\r\n\t\tend //end Final\r\n";
			verilogCode += "\t\tendcase\r\n";
			verilogCode += "\tend // end else\r\n";
			verilogCode += "end // end always\r\n";
			verilogCode += "endmodule // end HLSM\r\n ";
			
			bufferedWriter.write(verilogCode);
			bufferedWriter.flush();
			bufferedWriter.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}

	}
	
	
	/**
	 * Convert the state index (inside a certain block) into the state index of the entire FSM 
	 * @param blockSeq: the block sequence number
	 * @param blockStateIndex: the state index inside the block
	 * @return
	 */
	int blockStateIndex2GlobalStateIndex(int blockSeq, int blockStateIndex){
		int globalStateIndex = 0;
		Iterator <Block> iter = this.blocks.iterator();
		while (iter.hasNext()){
			Block b = iter.next();
			if (b.blockSeq < blockSeq)
				globalStateIndex += b.systhesizer.necessaryLatency;
			else
				break;
		}
		globalStateIndex += blockStateIndex;
		return globalStateIndex;
	}
	
	
	/**
	 * 
	 * @author Administrator
	 *
	 */
	private class Block extends Graph {

		//Block Properties Parameters
		public String blockName;
		public BlockType blockType;
		public int blockSeq;
		public int seqIfBlock2Go = 0; //0 代表不知道去哪里
		public int seqElseBlock2Go = 0; //0 代表不知道去哪里
		public int seqWhileBlock2Go = 0; //0 代表不知道去哪里
		public int seqThenBlock = 0; //0 代表不知道去哪里, -2代表去Final状态
		public int go2StartOrEndStateInThenBlock = 0; //1代表去Block的第一个状态，-1代表去block的最后一个状态
		Vertex blockOriginVertex;
		
		public Block(ConstructionMode mode) {
			super(mode);
		}
		
		public boolean hasSameBlockName(String blockName){
			if(this.blockName.equals(blockName)) {
				return true;
			}
			else {
				return false;
			}
		}
		
		public boolean isVertexBelongsToThisBlock(Vertex u){
			if(this.blockSeq == u.blockSeq){
				return true;
			}
			return false;
		}
		
		public void initialBlockParameters(){
			this.blockName = vertices.get(2).blockName;
			this.blockType = vertices.get(2).blockType;
			this.blockSeq = vertices.get(2).blockSeq;
			this.blockOriginVertex = vertices.get(2).blockOriginVertex;
		}
		
		public void setNextBlockParameters(int seqThenBlock, int go2StartOrEndStateInThenBlock){
			this.seqThenBlock = seqThenBlock;
			this.go2StartOrEndStateInThenBlock = go2StartOrEndStateInThenBlock;
		}
	}
	
	
}