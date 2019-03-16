package edu.yuanlu.hlsyn;
/**
 * 
 */
import java.util.*;

import edu.yuanlu.hlsyn.GraphEnum.*;

/**
 * @author Administrator
 *
 */
public class Graph {
	
	//public Graph graph = this;
	public ArrayList<Vertex> vertices = null;
	public ArrayList<Edge> edges = null;
	public Systhesizer systhesizer = null;
	public int verticesCount;
	public int edgesCount;

	/**
	 * 
	 */
	public Graph(ConstructionMode mode) {
		super();
		if(mode==ConstructionMode.ERROR){
			System.out.println("Error: unknown type.");
		}
		vertices = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		if(mode==ConstructionMode.NOP){
			addVertex(new Vertex("INOP",GraphEnum.VertexType.INOP)); 
			verticesCount++; 
			vertices.get(0).blockName = "main"; 
			vertices.get(0).blockType = BlockType.MAIN;
			addVertex(new Vertex("ONOP",GraphEnum.VertexType.ONOP)); 
			verticesCount++; 
			vertices.get(1).blockName = "main"; 
			vertices.get(1).blockType = BlockType.MAIN;
		}
		systhesizer = new Systhesizer(this);
	}
	
	public void addVertex (Vertex vertex){
		this.vertices.add(vertex);
	}
	
	public void addEdge (Edge edge){
		this.edges.add(edge);
	}
	
	public Edge searchEdge (String label){
		Iterator<Edge> i = edges.iterator();
		while(i.hasNext()){
			Edge edge = i.next();
			if (edge.isThisEdge(label)){
				return edge; 
			}
		}
		return null;
	}
	
	public Vertex searchVertex (String label, VertexType type){
		Iterator<Vertex> i = vertices.iterator();
		while(i.hasNext()){
			Vertex vertex = i.next();
			if (vertex.isThisVertex(label,type)){
				return vertex; 
			}
		}
		return null;
	}
	
	
	public boolean hasRepeatingEdge(Edge edge1){
		boolean hasRepeating = false;
		Iterator<Edge> i = edges.iterator();
		while(i.hasNext()){
			Edge edge = i.next();
			if (edge.equals(edge1)){
				hasRepeating = true;
			}
		}
		return hasRepeating;
	}
	
	
	public boolean hasRepeatingEdge(String label){
		boolean hasRepeating = false;
		Iterator<Edge> i = edges.iterator();
		while(i.hasNext()){
			Edge edge = i.next();
			if (edge.isThisEdge(label)){
				hasRepeating = true;
			}
		}
		return hasRepeating;
	}
	
	
	/**
	 * Caution: This class is only used for generating a Graph for test. Not use for other purposes.
	 */
	public void testGenerator() {
		
		Vertex v01 = new Vertex("INOP",GraphEnum.VertexType.INOP); vertices.add(v01);
		Vertex v02 = new Vertex("ONOP",GraphEnum.VertexType.ONOP); vertices.add(v02);
		Vertex v1 = new Vertex("MUL1",GraphEnum.VertexType.MUL); vertices.add(v1);
		Vertex v2 = new Vertex("MUL2",GraphEnum.VertexType.MUL); vertices.add(v2);
		Vertex v3 = new Vertex("MUL3",GraphEnum.VertexType.MUL); vertices.add(v3);
		Vertex v4 = new Vertex("MUL4",GraphEnum.VertexType.MUL); vertices.add(v4);
		Vertex v5 = new Vertex("ADD1",GraphEnum.VertexType.ADD); vertices.add(v5);
		Vertex v6 = new Vertex("ADD2",GraphEnum.VertexType.ADD); vertices.add(v6);
		Vertex v7 = new Vertex("DIV1",GraphEnum.VertexType.DIV); vertices.add(v7);
		Vertex v8 = new Vertex("SHL1",GraphEnum.VertexType.SHL); vertices.add(v8);
		
		Edge e1 = new Edge("a",v01,v1); edges.add(e1);
		Edge e2 = new Edge("b",v1,v2); edges.add(e2);
		Edge e3 = new Edge("c",v2,v3); edges.add(e3);
		Edge e4 = new Edge("d",v3,v5); edges.add(e4);
		Edge e5 = new Edge("e",v01,v4); edges.add(e5);
		Edge e6 = new Edge("f",v4,v5); edges.add(e6);
		Edge e7 = new Edge("g",v5,v6,v7); edges.add(e7);
		Edge e8 = new Edge("h",v6,v8); edges.add(e8);
		Edge e9 = new Edge("i",v7,v8,v02); edges.add(e9);
		Edge e10 = new Edge("j",v8,v02); edges.add(e10);
		
		verticesCount = vertices.size();
		edgesCount = edges.size();
		
		System.out.println("Test Graph Generated!");
	}
}
