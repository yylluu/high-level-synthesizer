package edu.yuanlu.hlsyn;
import java.util.*;

import edu.yuanlu.hlsyn.GraphEnum.*;

public class Vertex {
	
	//General Properties
	String label = "";
	int latency = 0;
	int dataWidth = 0;
	VertexType type = null;
	VertexColor color = GraphEnum.VertexColor.WHITE;
	
	//Topological Properties
	public Edge outEdges = null;
	public ArrayList<Edge> inEdges = new ArrayList<Edge>();
	public Collection<Vertex> predVertices = new ArrayList<Vertex>();
	public Collection<Vertex> nextVertices = new ArrayList<Vertex>();
	
	//Schedule Flags
	boolean asapScheduled = false;
	boolean alapScheduled = false;
	boolean forceDirectScheduled = false;
	
	//Schedule Parameters
	int asapStart = 0;
	int alapStart = 0;
	int asapEnd = 0;
	int alapEnd = 0;
	int forceDirectStart = 0;
	int forceDirectEnd = 0;
	
	//Force Direct Parameters
	int[] timeFrame = {0, 0};
	Vector<Double> probabilityDistribution = null;
	Vector<Double> probabilityDistributionWhenFixed = null;
	
	//Block Parameters
	String blockName;
	int blockSeq;
	BlockType blockType;
	Vertex blockOriginVertex;

	/**
	 * 
	 * @param label
	 * @param type
	 */
	public Vertex(String label, VertexType type) {
		super();
		this.label = label;
		this.type = type;
	}

	
	/**
	 * 
	 * @param label
	 * @param type
	 * @param bus
	 */
	public Vertex(String label, VertexType type, int dataWidth) {
		super();
		this.label = label;
		this.type = type;
		this.dataWidth = dataWidth;
	}
	
	
	/**
	 * 
	 * @param label
	 * @param type
	 * @return
	 */
	public boolean isThisVertex(String label, GraphEnum.VertexType type){
		if (this.label.compareToIgnoreCase(label) == 0 && this.type.equals(type)) 
			return true;
		return false;
	}
	
	
	
	public String printVertexAsVerilogOperation (){
		String verilogOperationCode = "";
		switch (this.type){
		case ADD:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " + " + this.inEdges.get(0).label;
			break;
		case SUB:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " - " + this.inEdges.get(0).label;
			break;
		case DIV:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " / " + this.inEdges.get(0).label;
			break;
		case MUL:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " * " + this.inEdges.get(0).label;
			break;
		case MOD:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " % " + this.inEdges.get(0).label;
			break;
		case MUX:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(2).label + " ? " + this.inEdges.get(1).label + " : " + this.inEdges.get(0).label;
			break;
		case SHL:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " << " + this.inEdges.get(0).label;
			break;
		case SHR:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " >> " + this.inEdges.get(0).label;
			break;
		case REG:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(0).label;
			break;
		case COMPGT:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " > " + this.inEdges.get(0).label;
			break;
		case COMPLT:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " < " + this.inEdges.get(0).label;
			break;
		case COMPEQ:
			verilogOperationCode = this.outEdges.label + " <= " + this.inEdges.get(1).label + " == " + this.inEdges.get(0).label;
			break;
		}
		return verilogOperationCode;
	}
	
}





