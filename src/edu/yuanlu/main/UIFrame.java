package edu.yuanlu.main;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class UIFrame {
	
	public String dir = null;
	public String file = null;
	public String fullPath = null;
	public int latencyConstrain = 10;
	public boolean isFileLoaded = false;
	public boolean isStarted = false;
	
	public boolean isFileLoaded() {
		return isFileLoaded;
	}

	public boolean isStarted() {
		return isStarted;
	}

	Frame mainFrame = new Frame("Yuan's high-level Systnesizer");
	Panel pUp = new Panel(new FlowLayout(FlowLayout.CENTER,20,10));
	Panel pDn = new Panel(new BorderLayout());
	Label constrainLabel = new Label("Latency Constrain:");
	TextField latencyConstrainTF = new TextField(6);
	Button importButton = new Button ("Import C-file");
	Button startButton = new Button ("Start Schedule");
	public TextArea log = new TextArea();
	public FileDialog fileFrame = new FileDialog(new Frame(""), "Select C-file:", FileDialog.LOAD );

	
	public UIFrame(){
		mainFrame.setLocation(300,300);
		mainFrame.setSize(600,450);
		mainFrame.setBackground(Color.WHITE);
		mainFrame.setResizable(false);
		mainFrame.setVisible(true);
		mainFrame.add(pUp, "North");
		mainFrame.add(pDn, "Center");
		latencyConstrainTF.setText("10");
		log.setEditable(false);
		pUp.add(this.importButton);
		pUp.add(this.startButton);
		pUp.add(this.constrainLabel);
		pUp.add(this.latencyConstrainTF);
		pDn.add(this.log,"Center");
		
		this.importButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				fileFrame.setVisible(true);
			}
		});
		
		this.startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (isFileLoaded){
					try {
						latencyConstrain = Integer.parseInt(latencyConstrainTF.getText());
						if (latencyConstrain <= 0){
							log.append("Error: latency constrain must be a positive integer number." + "\r\n");
							isStarted = false;
						} else {
							isStarted = true;
						}
					} catch (NumberFormatException e){
						isStarted = false;
						log.append("Error: latency constrain must be a positive integer number." + "\r\n");
					}
				} else {
					log.append("File is not loaded, please select a C file." + "\r\n");
					isStarted = false;
				}
			}
		});
				
		this.mainFrame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
				
	} //end UIFrame construction method
	
	
	
}
