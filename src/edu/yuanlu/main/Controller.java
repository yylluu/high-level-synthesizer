package edu.yuanlu.main;

import java.io.IOException;

import edu.yuanlu.hlsyn.*;
import edu.yuanlu.hlsyn.GraphEnum.*;


public class Controller {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int latencyConstrain = 10;
		boolean isSuccessfullyParse = false;

		UIFrame mainWindow = new UIFrame();
		Controller controller = new Controller();
		
		FrameRun frame = new FrameRun(mainWindow);
		Thread frameThread = new Thread(frame);
		frameThread.start();
		
		while(true){// main thread
			if (mainWindow.isFileLoaded == true && mainWindow.isStarted == true){
				//BlocksGraph blocks = null;
				Graph graph = new Blocks(ConstructionMode.NOP);//when arg==NOP, new graph contains INOP and ONOP, otherwise it is empty graph.
				Parser parser = new Parser(frame.dirPath, frame.fileName, graph);
				isSuccessfullyParse = parser.parse();
				if(isSuccessfullyParse){
					latencyConstrain = frame.mainWindow.latencyConstrain;
					mainWindow.log.append("Latency constrain is: " + latencyConstrain + "\r\n");
					mainWindow.log.append(controller.schedule(graph,latencyConstrain, frame.dirPath + frame.fileName + ".v") + "\r\n");
				} else {
					mainWindow.log.append("Error: bad C source file, schedule fails.\r\n");
				}
				mainWindow.isFileLoaded = false;
				mainWindow.isStarted = false;
				frame.fullPath = null;
			}
		}
		
	}
	
	private String schedule(Graph graph, int latencyConstrain, String outFile){
		StringBuffer log = new StringBuffer("");
		if(((Blocks)graph).systhesizeBlocks(latencyConstrain,log)){
			log.append("Algorithm finished, trying to create Verilog file!\r\n");
			if(((Blocks)graph).printVerilogFSM(outFile)){
				log.append("Verilog file has been successfully created!\r\n");
			} else {
				log.append("Fail to create verilog file.\r\n");
			}
		} else {
			log.append("Error: invalid latency constrain, too small to schedule.\r\n");
		}
		return log.toString();
	}

}


class FrameRun implements Runnable {
	
	public UIFrame mainWindow;
	private boolean flag = true;
	public String dirPath = null;
	public String fileName = null;
	public String fullPath = null;
	int latencyConstrain = 10;

	public FrameRun (UIFrame mainWindow){
		super();
		this.mainWindow = mainWindow;
	}
	
	public void run(){
		while (flag){
			if(mainWindow.fileFrame.getFile() != null && mainWindow.fileFrame.getDirectory() != null){
				mainWindow.dir = mainWindow.fileFrame.getDirectory();
				mainWindow.file = mainWindow.fileFrame.getFile();
				mainWindow.fullPath = mainWindow.dir + mainWindow.file;
				this.dirPath = mainWindow.dir;
				this.fileName = mainWindow.file;
				this.fullPath = mainWindow.fullPath;
				mainWindow.log.append(this.fullPath + "\r\n");
				mainWindow.fileFrame.setFile(null);
				mainWindow.fileFrame.setDirectory(null);
				mainWindow.isFileLoaded = true;
				mainWindow.fullPath = null;
				this.latencyConstrain = mainWindow.latencyConstrain;
			}
		}
	}
	
	public void shutDown(){
		flag = false;
	}
	
}
