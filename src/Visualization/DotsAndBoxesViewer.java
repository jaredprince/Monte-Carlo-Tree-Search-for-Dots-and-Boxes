package Visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import javax.swing.JFrame;

public class DotsAndBoxesViewer extends JFrame {

	public int topHeight = getInsets().top;
	public Polygon[] shapes;
	
	public static void main(String[] args) {
		new DotsAndBoxesViewer();
	}
	
	public DotsAndBoxesViewer(){
		setVisible(true);
		setTitle("Dots and Boxes");
		setResizable(false);
		
		setSize(500, 500 + topHeight);
		repaint();
	}

	
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, topHeight, 500, 500);

    	paintDots(g);	
    	paintEdges(g);
    	paintBoxes(g);
    }
	
	public void paintDots(Graphics g){
		
	}
	
	public void paintEdges(Graphics g){
		
	}

	public void paintBoxes(Graphics g){
		
	}
}
