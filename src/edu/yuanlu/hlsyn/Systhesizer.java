package edu.yuanlu.hlsyn;
import java.util.*;

import edu.yuanlu.hlsyn.GraphEnum.VertexType;

public class Systhesizer {
	
	//Graph and Constrain
	Graph graph = null;
	public int latencyConstrain = 0;
	public int necessaryLatency = 0;
	
	//Various Counts
	private int verticesCount = 0;
	private int addsubCount = 0;
	private int divmodCount = 0;
	private int mulCount = 0;
	private int logicCount = 0;
	private int asapScheduledVerticesCount = 0;
	private int alapScheduledVerticesCount = 0;

	
	
	//Probability distribution of various kinds of vertices
	private Vector<Double> addsubProbabilityDistribution = null;
	private Vector<Double> mulProbabilityDistribution = null;
	private Vector<Double> divmodProbabilityDistribution = null;
	private Vector<Double> logicProbabilityDistribution = null;	
	
	
	
	/**
	 * Construction method
	 * @param graph
	 */
	public Systhesizer(Graph graph) {
		super();
		this.graph = graph;
	}
	
	
	
	/**
	 * Set the latency constrain for graph, and initial the latency of each vertex at the same time;
	 * @param latencyConstrain
	 */
	public void setLatencyConstrain(int latencyConstrain) {
		this.latencyConstrain = latencyConstrain;
		this.initialLatency();
	}
	
	
	
	/**
	 * Initial the latency
	 */
	private String initialLatency(){
		Iterator<Vertex> i = this.graph.vertices.iterator();
		while (i.hasNext()){
			this.verticesCount++;
			Vertex vertex = (Vertex)i.next();
			switch (vertex.type){
			case ADD: vertex.latency = 1; this.addsubCount++; break;
			case SUB: vertex.latency = 1; this.addsubCount++; break;
			case MUL: vertex.latency = 2; this.mulCount++; break;
			case DIV: vertex.latency = 3; this.divmodCount++; break;
			case MOD: vertex.latency = 3; this.divmodCount++; break;
			case COMPGT: vertex.latency = 1; this.logicCount++; break;
			case COMPLT: vertex.latency = 1; this.logicCount++; break;
			case COMPEQ: vertex.latency = 1; this.logicCount++; break;
			case REG: vertex.latency = 1; this.logicCount++; break;
			case MUX: vertex.latency = 1; this.logicCount++; break;
			case SHL: vertex.latency = 1; this.logicCount++; break;
			case SHR: vertex.latency = 1; this.logicCount++; break;
			case IF: vertex.latency = 1; this.logicCount++; break;
			case WHILE: vertex.latency = 1; this.logicCount++; break;
			}
		}
		return "Latency initialed!";
	}
	
	
	
	/**
	 * Check whether there is a cycle or not
	 */
	private boolean isCyclicGraph(){
		
		boolean cyclic = false;
		Iterator<Vertex> i = this.graph.vertices.iterator();
		while (i.hasNext()){
			Vertex u = (Vertex)i.next();
			u.color = GraphEnum.VertexColor.WHITE;
		}
		Vertex u = this.graph.vertices.get(0);
		cyclic = dfsVisit(u);
		return cyclic;

	}

	
	/**
	 * Depth First Visit Algorithm for Directional Graph;
	 * @return true when cyclic graph, false when non-cyclic graph;
	 */
	private boolean dfsVisit(Vertex u){
		
		u.color = GraphEnum.VertexColor.GREY;
		Iterator<Vertex> i = u.nextVertices.iterator();
		while (i.hasNext()){
			Vertex v = (Vertex)i.next();
			if (v.color == GraphEnum.VertexColor.WHITE){
				dfsVisit(v);
			} else if (v.color == GraphEnum.VertexColor.WHITE){
				return true; //has a cycle;
			}
		}
		u.color = GraphEnum.VertexColor.BLACK;
		return false;//has no cycle;
		
	}

	
	
	/**
	 * as soon as possible algorithm
	 */
	private int asapSchedule(){
		
		int minRequiredLatency = 0;
		
		if (isCyclicGraph()){
			System.out.println("Bad graph, there is cycle, which cannot be scheduled.");
			System.exit(1);
		} else {
			System.out.println("Good graph, the cycle check passed.");
		}
		
		Iterator<Vertex> i = this.graph.vertices.iterator();
		while (i.hasNext()){
			Vertex u = (Vertex)i.next();
			u.asapStart = 0;
			u.asapEnd = 0;
			u.asapScheduled = false;
		}
		
		graph.vertices.get(0).asapStart = 0;
		graph.vertices.get(0).asapEnd = 0;
		graph.vertices.get(0).asapScheduled = true;
		this.asapScheduledVerticesCount = 1;
		
		while(this.graph.vertices.size() - 1 > this.asapScheduledVerticesCount){
			i = this.graph.vertices.iterator();
			while(i.hasNext()){
				Vertex u = (Vertex)i.next();
				if(u.asapScheduled) {
					continue;
				} else if(u.forceDirectScheduled) {
					//u.asapStart = u.forceDirectStart;
					//u.asapEnd = u.forceDirectEnd;
					u.asapStart = u.timeFrame[0];
					u.asapEnd = u.timeFrame[0] + u.latency -1;
					u.asapScheduled = true;
					this.asapScheduledVerticesCount++;
					continue;
				}
				boolean asapAllowed = true;
				int asapStart = 0;
				Iterator<Vertex> iterPred = u.predVertices.iterator();
				while(iterPred.hasNext()){
					Vertex v = (Vertex)iterPred.next();
					if(!v.asapScheduled){
						asapAllowed = false; 
						break;
					} else if (asapStart <= v.asapEnd) {
						asapStart = v.asapEnd + 1;
					}
				}
				if (asapAllowed){
					u.asapStart = asapStart;
					u.asapEnd = asapStart + u.latency -1;
					u.asapScheduled = true;
					this.asapScheduledVerticesCount++;
					if (minRequiredLatency < u.asapEnd){
						minRequiredLatency = u.asapEnd;
					}
				}				
			}
		}
		System.out.println("ASAP secheuled, the minimum required Latency is: " + minRequiredLatency);
		return minRequiredLatency;
	}
	
	
	
	/**
	 * as late as possible algorithm
	 */
	private boolean alapSchedule(){
		
		if (this.latencyConstrain < this.asapSchedule()){
			System.out.println("The latency constrain is too small to be realized.");
			return false;
		}
		
		Iterator<Vertex> i = this.graph.vertices.iterator();
		while (i.hasNext()){
			Vertex u = (Vertex)i.next();
			u.alapStart = 0;
			u.alapEnd = 0;
			u.alapScheduled = false;
		}
		
		graph.vertices.get(1).alapStart = this.latencyConstrain + 1;
		graph.vertices.get(1).alapEnd = this.latencyConstrain + 1;
		graph.vertices.get(1).alapScheduled = true;
		this.alapScheduledVerticesCount = 1;
		int test = 0;
		while(this.graph.vertices.size() - 1 > this.alapScheduledVerticesCount){
			System.out.println(test++);
			i = this.graph.vertices.iterator();
			while(i.hasNext()){
				Vertex u = (Vertex)i.next();
				if(u.alapScheduled) {
					continue;
				} else if(u.forceDirectScheduled) {
					u.alapStart = u.timeFrame[0];
					u.alapEnd = u.timeFrame[0] + u.latency -1;
					u.alapScheduled = true;
					this.alapScheduledVerticesCount++;
					continue;
				}
				boolean alapAllowed = true;
				int alapEnd = this.latencyConstrain + 1;
				Iterator<Vertex> iterNext = u.nextVertices.iterator();
				while(iterNext.hasNext()){
					Vertex v = (Vertex)iterNext.next();
					if(!v.alapScheduled){
						alapAllowed = false; 
						break;
					} else if (alapEnd >= v.alapStart) {
						alapEnd = v.alapStart - 1;
					}
				}
				if (alapAllowed){
					u.alapStart = alapEnd - u.latency + 1;
					u.alapEnd = alapEnd;
					u.alapScheduled = true;
					this.alapScheduledVerticesCount++;
				}				
			}
		}
		System.out.println("ALAP secheuled.");
		return true;
	}
	
	
	
	/**
	 * force direct schedule algorithm
	 */
	public boolean scheduleForceDirect(StringBuffer log){
		//this.updateTopology();
		if (!this.alapSchedule()){
			return false;
		};
		this.resetVerticesFlagWithInitialProbability();
		for (int index = 0; index < this.graph.vertices.size() - 2; index++){
			this.updateAllVerticesTimeFrame();
			this.calculateAllVerticesProbabilityVectors();
			this.calculateAllTypesProbabilityDistribution();
			double minForce = 1.7976931348623157e+308;
			int minForceVertexFixedTime = 0;
			Vertex minForceVertex = null;	
			Iterator<Vertex> i = this.graph.vertices.iterator(); i.next(); i.next();
			while (i.hasNext()){
				Vertex u = (Vertex)i.next();
				System.out.println(u.label);
				if(u.forceDirectScheduled) continue;
				for (int item = u.timeFrame[0]; item <= u.timeFrame[1]; item++){
					double totalForce = this.calculateTotalForce(u, item);
					System.out.println(totalForce + "  " + minForce);
					if (totalForce <= minForce){
						minForce = totalForce;
						minForceVertex = u;
						System.out.println(u.label);
						minForceVertexFixedTime = item;
					}
				}
			}
			if(minForce >= 5000.0){
				log.append("Warning: Force is too large, not reasonable.\r\n");
			}
			System.out.println(minForceVertex.label + " has been scheduled at time #" + minForceVertexFixedTime + ", having force as " + minForce + "\r\n");			
			//log.append(minForceVertex.label + " has been scheduled at time #" + minForceVertexFixedTime + ", having force as " + minForce + "\r\n");
			minForceVertex.forceDirectScheduled = true;
			minForceVertex.timeFrame[0] = minForceVertexFixedTime;
			minForceVertex.timeFrame[1] = minForceVertexFixedTime + minForceVertex.latency - 1;
			this.alapSchedule();
		}
		
		Iterator<Vertex> i = this.graph.vertices.iterator(); i.next(); i.next();
		while(i.hasNext()){
			Vertex u = (Vertex)i.next();
			if(u.type==GraphEnum.VertexType.IF || u.type==GraphEnum.VertexType.WHILE){
				u.forceDirectStart = this.latencyConstrain + 1;
				u.forceDirectEnd = this.latencyConstrain + 1;
				this.latencyConstrain++;
			} else {
				u.forceDirectStart = u.timeFrame[0];
				u.forceDirectEnd = u.timeFrame[0] + u.latency - 1;
			}
		}
		
		this.removeEmptyStates();
		this.printLog(log);
		
		log.append("Force Direct secheuled.\r\n");
		System.out.println(log);
		return true;
	}
	
	
	
	/**
	 * reset  vertices flags with all "false", and initial the probability vectors at the same time.
	 */
	private void resetVerticesFlagWithInitialProbability(){
		Iterator<Vertex> i = this.graph.vertices.iterator();
		while (i.hasNext()){
			Vertex u = (Vertex)i.next();
			u.alapScheduled = false;
			u.asapScheduled = false;
			u.forceDirectScheduled = false;
			if(u.type != VertexType.INOP && u.type != VertexType.ONOP){
				u.probabilityDistribution = new Vector<Double>(this.latencyConstrain);
				u.probabilityDistribution.setSize(this.latencyConstrain);
				u.probabilityDistributionWhenFixed = new Vector<Double>(this.latencyConstrain);
				u.probabilityDistributionWhenFixed.setSize(this.latencyConstrain);
			}
		}
		this.addsubProbabilityDistribution = new Vector<Double>(this.latencyConstrain);
		this.addsubProbabilityDistribution.setSize(this.latencyConstrain);
		this.mulProbabilityDistribution = new Vector<Double>(this.latencyConstrain);
		this.mulProbabilityDistribution.setSize(this.latencyConstrain);
		this.divmodProbabilityDistribution = new Vector<Double>(this.divmodCount);
		this.divmodProbabilityDistribution.setSize(this.latencyConstrain);
		this.logicProbabilityDistribution = new Vector<Double>(this.logicCount);
		this.logicProbabilityDistribution.setSize(this.latencyConstrain);
		
	}
	
	
	
	/**
	 * Update Time Frame of all Vertices based on the ASAP and ALAP results
	 */
	private void updateAllVerticesTimeFrame(){
		Iterator<Vertex> i = this.graph.vertices.iterator();
		i.next();
		i.next();
		while (i.hasNext()){
			Vertex u = (Vertex)i.next();
			u.timeFrame[0] = u.asapStart;
			u.timeFrame[1] = u.alapStart;
		}
	}
	
	
	
	/**
	 * calculate the probability vector of a certain vertex by using the timeFrame parameter
	 */
	private void calculateOneVertexProbabilityVector(Vertex u){
		int timeFrameLength = u.timeFrame[1] - u.timeFrame[0] + u.latency;
		for (int index = 0; index < this.latencyConstrain; index++){
			if ( index >= (u.timeFrame[0]-1) && index <= (u.timeFrame[0] + timeFrameLength - 2)){
				Double p = (Double)(1/(double)timeFrameLength);
				u.probabilityDistribution.set(index, p);
			} else {
				u.probabilityDistribution.set(index, (Double)(double)0);
			}
		}
	}
	
	
	/**
	 * for every vertex except INOP and ONOP, calculate the probability vector
	 */
	private void calculateAllVerticesProbabilityVectors(){
		Iterator<Vertex> i = this.graph.vertices.iterator();
		i.next();
		i.next();
		while (i.hasNext()){
			Vertex u = (Vertex)i.next();
			this.calculateOneVertexProbabilityVector(u);
		}
	}
	
	
	/**
	 * For each different type of vertices (addsub, mul, moddiv, logic), calculate their probability distribution vector
	 */
	private void calculateAllTypesProbabilityDistribution(){//Types indicate add_sub, mul, mod_div, logic
		
		for (int index = 0; index < this.latencyConstrain; index++){
			Double element = (Double)0.0;
			this.addsubProbabilityDistribution.set(index, element);
			this.mulProbabilityDistribution.set(index, element);
			this.divmodProbabilityDistribution.set(index,element);
			this.logicProbabilityDistribution.set(index, element);
		}
		
		Iterator<Vertex> i = this.graph.vertices.iterator(); i.next(); i.next();
		while (i.hasNext()){
			Vertex u = (Vertex)i.next();
			this.verticesCount++;
			switch (u.type){
			case INOP: case ONOP:
				System.exit(1); break;
			case ADD: case SUB: 
				for (int index = 0; index < this.latencyConstrain; index++){
					Double element = this.addsubProbabilityDistribution.get(index) + u.probabilityDistribution.get(index);
					this.addsubProbabilityDistribution.set(index, element);
				}
				break;
			case MUL: 
				for (int index = 0; index < this.latencyConstrain; index++){
					Double element = this.mulProbabilityDistribution.get(index) + u.probabilityDistribution.get(index);
					this.mulProbabilityDistribution.set(index, element);
				}
				break;
			case DIV: case MOD:
				for (int index = 0; index < this.latencyConstrain; index++){
					Double element = this.divmodProbabilityDistribution.get(index) + u.probabilityDistribution.get(index);
					this.divmodProbabilityDistribution.set(index, element);
				}
				break;
			default:
				for (int index = 0; index < this.latencyConstrain; index++){
					Double element = this.logicProbabilityDistribution.get(index) + u.probabilityDistribution.get(index);
					this.logicProbabilityDistribution.set(index, element);
				}
				break;
			}
		}
		
	}
	
	
	/**
	 * 
	 * @param u
	 * @param scheduleTime
	 * @return
	 */
	private double calculateTotalForce(Vertex u, int scheduleTime){
		double selfForce = this.calculateSelfForce(u, scheduleTime);
		double predForce = this.calculatePredForce(u, scheduleTime);
		double succForce = this.calculateSuccForce(u, scheduleTime);
		return (selfForce + predForce + succForce);
	}
	
	
	/**
	 * 
	 * @param u
	 * @param scheduleTime
	 * @return
	 */
	private double calculateSelfForce(Vertex u, int timeFixedAt){
		double selfForce = 0;
		for (int index = 0; index < this.latencyConstrain; index++) {
			if(index >= (timeFixedAt - 1) && index <= (timeFixedAt + u.latency -2)){
				Double element = (Double)(1/(double)u.latency);
				u.probabilityDistributionWhenFixed.set(index, element);
			} else {
				u.probabilityDistributionWhenFixed.set(index, (Double)(double)0);
			}
		}
		switch (u.type){
		case INOP: case ONOP:
			System.exit(1); break;
		case ADD: case SUB: 
			for (int index = 0; index < this.latencyConstrain; index++){
				selfForce += (u.probabilityDistributionWhenFixed.get(index) - u.probabilityDistribution.get(index)) * this.addsubProbabilityDistribution.get(index);
			}
			break;
		case MUL: 
			for (int index = 0; index < this.latencyConstrain; index++){
				selfForce += (u.probabilityDistributionWhenFixed.get(index) - u.probabilityDistribution.get(index)) * this.mulProbabilityDistribution.get(index);
			}
			break;
		case DIV: case MOD:
			for (int index = 0; index < this.latencyConstrain; index++){
				selfForce += (u.probabilityDistributionWhenFixed.get(index) - u.probabilityDistribution.get(index)) * this.divmodProbabilityDistribution.get(index);
			}
			break;
		default:
			for (int index = 0; index < this.latencyConstrain; index++){
				selfForce += (u.probabilityDistributionWhenFixed.get(index) - u.probabilityDistribution.get(index)) * this.logicProbabilityDistribution.get(index);
			}
			break;
		}
		return selfForce;
	}
	
	
	/**
	 * 
	 * @param u
	 * @param scheduleTime
	 * @return
	 */
	private double calculatePredForce(Vertex u, int scheduleTime){
		double predForce = 0;
		if (u.timeFrame[0] == scheduleTime){
			Iterator<Vertex> i = u.predVertices.iterator();
			while (i.hasNext()){
				Vertex v = (Vertex)i.next();
				try {
					if (v.type != GraphEnum.VertexType.INOP && v.timeFrame[0] + v.latency == scheduleTime){
						predForce += this.calculateSelfForce(v, v.asapStart);
					}
				} catch (Exception e){
					break;
				}
			}
		}
		return predForce;
	}
	
	
	/**
	 * 
	 * @param u
	 * @param scheduleTime
	 * @return
	 */
	private double calculateSuccForce(Vertex u, int scheduleTime){
		double nextForce = 0;
		if (u.timeFrame[1] == scheduleTime){
			Iterator<Vertex> i = u.predVertices.iterator();
			while (i.hasNext()){
				Vertex v = (Vertex)i.next();
				if (v.type != GraphEnum.VertexType.ONOP && scheduleTime + u.latency == v.timeFrame[1]){
					nextForce += this.calculateSelfForce(v, v.alapStart);
				}
			}
		}
		return nextForce;
	}
	
	
	
	/**
	 * 
	 */
	private void removeEmptyStates(){
		int timeIndex = 1;
		this.necessaryLatency = this.latencyConstrain;
		while(timeIndex <= this.necessaryLatency){
			boolean isTimeIndexBusy = false;
			Iterator<Vertex> i = this.graph.vertices.iterator();
			while(i.hasNext()){
				Vertex u = i.next();
				if( timeIndex >= u.forceDirectStart && timeIndex <= u.forceDirectEnd){
					isTimeIndexBusy = true;
				}
			}
			if (!isTimeIndexBusy){
				(this.necessaryLatency)--;
				Iterator<Vertex> i1 = this.graph.vertices.iterator();
				while(i1.hasNext()){
					Vertex v = i1.next();
					if( v.forceDirectStart > timeIndex){
						(v.forceDirectStart)--;
						(v.forceDirectEnd)--;
					}
				}
				timeIndex--;
			}
			timeIndex++;
		}
	}
	
	/**
	 * 
	 * @param log
	 */
	private void printLog (StringBuffer log){
		log.append("Schedule a block:\r\n");
		Iterator<Vertex> i = this.graph.vertices.iterator(); i.next(); i.next();
		while(i.hasNext()){
			Vertex u = i.next();
			log.append(u.label + " is scheduled at time #" + u.forceDirectStart + ", its block name is " + u.blockName + ", its block seq is " + u.blockSeq + ", its block type is " + u.blockType + "\r\n");
		}
		log.append("Necessary latency: " + this.necessaryLatency + "\r\n");
	}
	
	
}
