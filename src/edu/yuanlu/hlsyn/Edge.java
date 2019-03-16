package edu.yuanlu.hlsyn;
import java.util.*;

import edu.yuanlu.hlsyn.GraphEnum.*;


public class Edge {

	//Basic parameters
	EdgeType type = null;
	String label = null;

	//Bus parameters
	int busWidth;
	EdgeSign busSign;
	
	//Topo parameters
	Vertex startVertex;
	Collection<Vertex> endVertex =  new ArrayList<Vertex>();

	/**
	 * Construction Method, every edge must have one start-vertex and one end-vertex;
	 * @param label
	 */
	public Edge(String label) {
		super();
		this.label = label;
	}
	
	public Edge(String label, EdgeType type) {
		super();
		this.label = label;
		this.type = type;
	}
	
	/**
	 * Construction Method, every edge must have one start-vertex and one end-vertex;
	 * @param label
	 * @param startVertex
	 * @param endVertex
	 */
	public Edge(String label, Vertex startVertex, Vertex endVertex) {
		super();
		this.label = label;
		this.startVertex = startVertex;
		this.endVertex.add(endVertex);
	}
	
	/**
	 * Construction Method, every edge must have one start-vertex and one end-vertex;
	 * @param label
	 * @param startVertex
	 * @param endVertex
	 */
	public Edge(String label, Vertex startVertex, Vertex endVertex1, Vertex endVertex2) {
		super();
		this.label = label;
		this.startVertex = startVertex;
		this.endVertex.add(endVertex1);
		this.endVertex.add(endVertex2);
	}
	
	/**
	 * 
	 * @param startVertex
	 * @return
	 */
	public boolean addStartVertex(Vertex startVertex){
		this.startVertex = startVertex;
		return true;
	}
	
	/**
	 * 
	 * @param endVertex
	 * @return
	 */
	public boolean addEndVertex(Vertex endVertex){
		this.endVertex.add(endVertex);
		return true;
	}
	
	
	/**
	 * Judge whether the edge's label is same to the input string;
	 * @param str
	 * @return
	 */
	public boolean isThisEdge (Edge edge){
		if (edge.label.equals(label)){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param label
	 * @return
	 */
	public boolean isThisEdge (String label){
		if (this.label.equals(label)){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 */
	public void updateTopology(){
		Vertex u = this.startVertex;
		Iterator<Vertex> i = this.endVertex.iterator();
		while (i.hasNext()){
			Vertex v = (Vertex)i.next();
			if ( u.blockName.equals(v.blockName) && u.blockType.equals(v.blockType)){
				v.predVertices.add(u);
				u.nextVertices.add(v);
			}
		}
	}
	
	/**
	 * 
	 * @param bus
	 * @return
	 */
	public boolean setBus(String bus){
		if(bus.equals("Int1") || bus.equals("bool")){
			this.busWidth = 1;
			this.busSign = EdgeSign.SIGN;
		} else if(bus.equals("Int8") || bus.equals("char")){
			this.busWidth = 8;
			this.busSign = EdgeSign.SIGN;
		} else if(bus.equals("Int16") || bus.equals("short")){
			this.busWidth = 16;
			this.busSign = EdgeSign.SIGN;
		} else if(bus.equals("Int32") || bus.equals("int")){
			this.busWidth = 32;
			this.busSign = EdgeSign.SIGN;
		} else if(bus.equals("Int64") || bus.equals("long")){
			this.busWidth = 64;
			this.busSign = EdgeSign.SIGN;
		} else if(bus.equals("UInt1") || bus.equals("ubool")){
			this.busWidth = 1;
			this.busSign = EdgeSign.UNSIGN;
		} else if(bus.equals("UInt8") || bus.equals("uchar")){
			this.busWidth = 8;
			this.busSign = EdgeSign.UNSIGN;
		} else if(bus.equals("UInt16") || bus.equals("ushort")){
			this.busWidth = 16;
			this.busSign = EdgeSign.UNSIGN;
		} else if(bus.equals("UInt32") || bus.equals("uint")){
			this.busWidth = 32;
			this.busSign = EdgeSign.UNSIGN;
		} else if(bus.equals("UInt64") || bus.equals("ulong")){
			this.busWidth = 64;
			this.busSign = EdgeSign.UNSIGN;
		} else {
			return false;
		}
		return true;
	}
}
