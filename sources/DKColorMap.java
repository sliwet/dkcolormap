import java.io.*;
import java.text.DecimalFormat;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class DKColorMap  extends JFrame{
	private int xaddpix = 4,yaddpix = 32,labelheight = 12; //labelheight
    private int labelwidth = labelheight / 2,infowidth = labelheight + 4,colorgradbandwidth = labelheight*3;
    private ColorMapData cmdt = null;
    private ColorMapDraw cmdraw = null;
    private final DecimalFormat twoDigit = new DecimalFormat("0.00");
    
    public DKColorMap(String fullpath) {
    	initialize(readColorMapData(fullpath));
    }
    
    public DKColorMap(ColorMapData cmdt) {
    	initialize(cmdt);
    }
    
    public void initialize(ColorMapData cmdt) {
    	this.cmdt = cmdt;
    	
		int nxpix = cmdt.xcoord.length;
		int nypix = cmdt.ycoord.length;

        this.setSize(nxpix+xaddpix,nypix+colorgradbandwidth+infowidth+yaddpix);
        this.setResizable(false);
        this.setTitle("DKColorMap By Daewon Kwon");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        cmdraw = new ColorMapDraw(cmdt);
        
        getContentPane().add(cmdraw,BorderLayout.CENTER);
        this.setVisible(true);
    }

    public ColorMapData readColorMapData(String fullpath) {
		if(!(new File(fullpath)).exists()) return null;

    	double [] xcoord = null;
    	double [] ycoord = null;
    	double [][] matrix = null;
    	double [] zminmax = new double [2];
    	boolean xdesc = false,ydesc = false;
		try{
			Queue<Double> tq = new Queue<Double>();
			Queue<Double> zq = new Queue<Double>();
			double [] dblarr;

			Scanner scan = new Scanner(new File(fullpath));
			double [] tmp = strToDoubleArr(scan.nextLine());
			if(tmp[tmp.length - 1] < tmp[0]) {
				xdesc = true;
				xcoord = new double[tmp.length];
				for(int i = 0;i<xcoord.length;i++) {
					xcoord[i] = tmp[tmp.length - 1 - i];
				}
			}
			else {
				xcoord = tmp;
			}

			while (scan.hasNextLine())
			{
				if(tq.size() > 1000) {
					JOptionPane.showMessageDialog(null,"File size is too big!!"
							,"readData", JOptionPane.WARNING_MESSAGE);
					scan.close();
					return null;
				}

				dblarr = strToDoubleArr(scan.nextLine());
				if(dblarr == null) {
					break;
				}
				else{
					if((dblarr.length - 1) != xcoord.length) break;
					tq.enqueue(dblarr[0]);

					for(int i = 1;i<dblarr.length;i++) {
						zq.enqueue(dblarr[i]);
					}
				}
			}
			scan.close();

			tmp = new double[tq.size()];
			for(int i = 0; i < tmp.length;i++) {
				tmp[i] = tq.dequeue();
			}

			if(tmp[tmp.length - 1] < tmp[0]) {
				ydesc = true;
				ycoord = new double[tmp.length];
				for(int i = 0;i<ycoord.length;i++) {
					ycoord[i] = tmp[tmp.length - 1 - i];
				}
			}
			else {
				ycoord = tmp;
			}
			
			matrix = new double[ycoord.length][xcoord.length];
			int ii,jj;
			for(int i=0;i<ycoord.length;i++) {
				if(ydesc) {
					ii = ycoord.length - 1 - i;
				}
				else {
					ii = i;
				}
				
				for(int j=0;j<xcoord.length;j++) {
					if(xdesc) {
						jj = xcoord.length - 1 - j;
					}
					else {
						jj = j;
					}
					
					matrix[ii][jj] = zq.dequeue();
					
					if((i==0)&&(j==0)) {
						zminmax[0] = matrix[i][j];
						zminmax[1] = matrix[i][j];
					}
					else if(matrix[i][j] < zminmax[0]) {
						zminmax[0] = matrix[i][j];
					}
					else if(matrix[i][j] > zminmax[1]) {
						zminmax[1] = matrix[i][j];
					}
				}
			}
		} catch(Exception ee) {
			JOptionPane.showMessageDialog(null,"Reading Error","readColorMapData", JOptionPane.WARNING_MESSAGE);
			return null;
		}

		return (new ColorMapData(xcoord,ycoord,matrix,zminmax));
    }

    private class ColorMapDraw extends JComponent{
    	private ColorMapData cmdt;
	    private BufferedImage BI;
        private int nxpix,nypix;
        private double [] xdbl;
        private double [] ydbl;
        private int [] ixr;
        private int [] iyr;
        private double [] xyratio = new double[2];
        private int [] dragStart = new int[2];
        private int [] dragStop = new int[2];
        private int [] xymark;
        private int [] Mij = null;

        private boolean isPressedInside = false,stillPressed = false;

        public ColorMapDraw(ColorMapData cmdt){
        	this.cmdt = cmdt;
        	this.nxpix = cmdt.xcoord.length;
        	this.nypix = cmdt.ycoord.length;
        	
        	initializeBI();

            this.addMouseListener(
            		new MouseAdapter(){
                        public void mousePressed(MouseEvent e){
                        	xymark = null;
                        	if(((e.getX() >=0) && (e.getX() < nxpix))
                        			&& ((e.getY() >=0) && (e.getY() < nypix))) {
                        		dragStart[0] = e.getX();
                        		dragStart[1] = e.getY();

                        		dragStop[0] = dragStart[0];
                        		dragStop[1] = dragStart[1];
                        		isPressedInside = true;
                        		stillPressed = true;
                        	}
                        }

                        public void mouseReleased(MouseEvent e){
                        	stillPressed = false;
                        	if(((e.getX() >=0) && (e.getX() < nxpix))
                        			&& ((e.getY() >=0) && (e.getY() < nypix))) {
                        		dragStop[0] = e.getX();
                        		dragStop[1] = e.getY();

                        		if(isPressedInside) {
                                    if((Math.abs(dragStop[0] - dragStart[0])> 10) &&
                                            (Math.abs(dragStop[1] - dragStart[1]) > 10)){
                                    	reconfigureBI(dragStart,dragStop);
                                    }
                            		else {
                                    	xymark = dragStop;
                            		}
                        		}
                        	}
                        	else {
                        		initializeBI();
                        	}

                    		repaint();
                    		isPressedInside = false;
                        }
                    });

                this.addMouseMotionListener(
                    new MouseMotionAdapter(){
                        public void mouseDragged(MouseEvent e){
                        	dragStop[0] = e.getX();
                        	dragStop[1] = e.getY();
                            repaint();
                        }
                    });
        }
        
        private int [] getMij() {
        	return Mij;
        }

        private int [] Gij_Mij(int [] Gij) {
        	int [] Mij = new int [2];
        	// Elapsed Time
        	Mij[0] = (int)(xyratio[1] * (nypix - 1 - Gij[1])) + iyr[0];
        	// Wavelength
        	Mij[1] = (int)(xyratio[0] * Gij[0]) + ixr[0];
        	return Mij;
        }

        private void initializeBI() {
        	this.xdbl = new double[] {cmdt.xcoord[0],cmdt.xcoord[cmdt.xcoord.length-1]};
        	this.ydbl = new double[] {cmdt.ycoord[0],cmdt.ycoord[cmdt.ycoord.length-1]};
        	configureBI();
        }

        private void configureBI() {
        	// index range
        	ixr = new int[] {(int)((xdbl[0] - cmdt.xcoord[0])* (nxpix - 1) / (cmdt.xcoord[cmdt.xcoord.length-1] - cmdt.xcoord[0]))
        			, (int)((xdbl[1] - cmdt.xcoord[0])* (nxpix - 1) / (cmdt.xcoord[cmdt.xcoord.length-1] - cmdt.xcoord[0]))};
        	iyr = new int[] {(int)((ydbl[0] - cmdt.ycoord[0])* (nypix - 1) / (cmdt.ycoord[cmdt.ycoord.length-1] - cmdt.ycoord[0]))
        			, (int)((ydbl[1] - cmdt.ycoord[0])* (nypix - 1) / (cmdt.ycoord[cmdt.ycoord.length-1] - cmdt.ycoord[0]))};

        	if(ixr[0] < 0) ixr[0] = 0;
        	if(ixr[1] >= nxpix) ixr[1] = nxpix - 1;

        	if(iyr[0] < 0) iyr[0] = 0;
        	if(iyr[1] >= nypix) iyr[1] = nypix - 1;

            xyratio[0] = (double)(ixr[1] - ixr[0]) / (nxpix -1);
            xyratio[1] = (double)(iyr[1] - iyr[0]) / (nypix -1);

            int [] Gij = new int[2];
            BI = new BufferedImage(nxpix, (nypix + infowidth + colorgradbandwidth), BufferedImage.TYPE_INT_RGB);
            for (Gij[0] = 0;Gij[0] < nxpix; Gij[0]++) for(Gij[1] = 0; Gij[1] < nypix; Gij[1]++){
            	Mij = Gij_Mij(Gij);
                BI.setRGB(Gij[0],Gij[1], val_rgb(cmdt.zminmax[0],cmdt.zminmax[1],cmdt.matrix[Mij[0]][Mij[1]])); // Tan packground
            }
            for (int i = 0; i<nxpix; ++i) for(int j=nypix; j<(nypix+infowidth); j++){
                BI.setRGB(i, j, 0); // Tan packground
            }
            for (int i = 0; i<nxpix; ++i) for(int j=(nypix+infowidth); j<(nypix+infowidth+colorgradbandwidth); j++){
                BI.setRGB(i, j, val_rgb(0,(double)nxpix,(double)i)); // Tan packground
            }
            Mij = null;
        }

        private void reconfigureBI(int[] dragStart,int[] dragStop) {
        	int xi,xf,yi,yf;

        	if(dragStop[0] > dragStart[0]) {
        		xi = dragStart[0];
        		xf = dragStop[0];
        	}
        	else {
        		xi = dragStop[0];
        		xf = dragStart[0];
        	}


        	if(dragStop[1] > dragStart[1]) {
        		yi = dragStart[1];
        		yf = dragStop[1];
        	}
        	else {
        		yi = dragStop[1];
        		yf = dragStart[1];
        	}

        	double ratio = (xdbl[1] - xdbl[0]) / (nxpix - 1);
        	double [] newwl = new double[2];
        	newwl[0] = xi * ratio + xdbl[0];
        	newwl[1] = xf * ratio + xdbl[0];

        	ratio = (ydbl[1] - ydbl[0]) / (nypix - 1);
        	double [] newet = new double[2];
        	newet[1] = ydbl[1] - yi * ratio;
        	newet[0] = ydbl[1] - yf * ratio;

        	this.xdbl = newwl;
        	this.ydbl = newet;

        	configureBI();
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(BI, 0, 0, Color.red, null);

            g2.setPaint(Color.YELLOW);
            setFont(new Font("Ariel", Font.PLAIN,labelheight));

            String minwl = twoDigit.format(xdbl[0]);
            String maxwl = twoDigit.format(xdbl[1]);
            g2.drawString(minwl, labelwidth, nypix - labelheight);
            g2.drawString(maxwl, (nxpix - labelwidth*(maxwl.length()+2)), nypix - labelheight);

            String minstr = twoDigit.format(cmdt.zminmax[0]);
            String maxstr = twoDigit.format(cmdt.zminmax[1]);
            g2.drawString(minstr, labelwidth, (nypix+infowidth+2*labelheight));
            g2.drawString(maxstr, (nxpix - labelwidth*(maxstr.length()+2)), (nypix+infowidth+2*labelheight));

            if(xymark != null) {
                g2.setPaint(Color.BLACK);
                g2.draw(new Line2D.Float(0,xymark[1],nxpix,xymark[1]));
                g2.draw(new Line2D.Float(xymark[0],0,xymark[0],nypix));

                g2.setPaint(Color.YELLOW);
                Mij = Gij_Mij(xymark);
                String infoString =  "X: " + twoDigit.format(cmdt.xcoord[Mij[1]]) + " , Y: "
                + twoDigit.format(cmdt.ycoord[Mij[0]]) + " , Z: " + twoDigit.format(cmdt.matrix[Mij[0]][Mij[1]]);

                g2.drawString(infoString, labelwidth, (nypix+labelheight+2));
            }

            if(isPressedInside && stillPressed){
                g2.setStroke(new BasicStroke(1));
                g2.setPaint(Color.RED);
                g2.draw(makeRectangle(dragStart[0],dragStart[1],dragStop[0],dragStop[1]));
            }
        }

        private Rectangle2D.Float makeRectangle(int x1,int y1,int x2,int y2){
            int x = Math.min(x1,x2);
            int y = Math.min(y1,y2);
            int width = Math.abs(x1 - x2);
            int height = Math.abs(y1 - y2);
            return new Rectangle2D.Float(x,y,width,height);
        }
    }

	public String getSelectedYInfo() {
		if(cmdraw.getMij() == null) return "";
		return "Z vs X at Y = " + cmdt.ycoord[cmdraw.getMij()[0]];
	}

	public String getSelectedXInfo() {
		if(cmdraw.getMij() == null) return "";
		return "Z vs Y at X = " + cmdt.xcoord[cmdraw.getMij()[1]];
	}

	public double[][] getYdataAtX(){
		if(cmdraw.getMij() == null) {
			JOptionPane.showMessageDialog(null,"Pick first!"
					,"getElapsedTimeData", JOptionPane.WARNING_MESSAGE);
			return null;
		}

		double [][] dataAtFixedX = new double[2][cmdt.matrix.length];

		for(int i =0;i<cmdt.matrix.length;i++) {
			dataAtFixedX[0][i] = cmdt.ycoord[i];
			dataAtFixedX[1][i] = cmdt.matrix[i][cmdraw.getMij()[1]];
		}
		return dataAtFixedX;
	}

	public double[][] getXDataAtY(){
		if(cmdraw.getMij() == null) {
			JOptionPane.showMessageDialog(null,"Pick first!"
					,"getSpecificTimeData", JOptionPane.WARNING_MESSAGE);
			return null;
		}

		double [][] dataAtFixedY = new double[2][cmdt.matrix[0].length];

		for(int i =0;i<cmdt.matrix[0].length;i++) {
			dataAtFixedY[0][i] = cmdt.xcoord[i];
			dataAtFixedY[1][i] = cmdt.matrix[cmdraw.getMij()[0]][i];
		}
		return dataAtFixedY;
	}

	
    private int val_rgb (double min,double max,double val){
        if (val < min) val = min;
        if (val > max) val = max;
        double h = 240.0*(max - val)/(max - min);
        //Hue [0,360], 0 for Red, 120 for Green, 240 for Blue
        double s = 1.0; //Saturation [0,1], 1 for pure color
        double v = 1.0; // Brightness [0,1], 0 for black
        double r,g,b;

        if(s==0) { //achromatic(grey)
            r = g = b = v;
        }

	h /= 60;			// sector 0 to 5
	double i = Math.floor(h);
	double f = h - i;			// factorial part of h
	double p = v * ( 1 - s );
	double q = v * ( 1 - s * f );
	double t = v * ( 1 - s * ( 1 - f ) );
	switch( (int)(i +0.1) ) {
		case 0:
			r = v;
			g = t;
			b = p;
			break;
		case 1:
			r = q;
			g = v;
			b = p;
			break;
		case 2:
			r = p;
			g = v;
			b = t;
			break;
		case 3:
			r = p;
			g = q;
			b = v;
			break;
		case 4:
			r = t;
			g = p;
			b = v;
			break;
		default:		// case 5:
			r = v;
			g = p;
			b = q;
			break;
	}

        r = r * 255;
        g = g * 255;
        b = b * 255;

        int rgb = 65536 * (int)r + 256 * (int)g + (int)b;

        return rgb;
    }
	
    private double [] strToDoubleArr(String str){
    	
    	Queue<Double> doubleq = new Queue<Double>();
    	
    	StringTokenizer st = new StringTokenizer(str," \t\",<>()[]{}=:$");
    	String tokenStr;
		while(st.hasMoreTokens()){
			tokenStr = st.nextToken();
			if(tokenStr != null){
				try {
					doubleq.enqueue(Double.parseDouble(tokenStr));
				}
				catch(Exception ee) {}
			}
		}

		if(doubleq.size() > 0) {
	    	double [] retdouble = new double[doubleq.size()];
	    	
			for(int i = 0; i< retdouble.length;i++) {
				retdouble[i] = doubleq.dequeue();
			}
	    	
	        return retdouble;
		}
		else return null;
    }
}

class ColorMapData{
	double [] xcoord;
	double [] ycoord;
	double [][] matrix;
	double [] zminmax;

	public ColorMapData(double [] xcoord,double [] ycoord,double[][] matrix,double [] zminmax) {
		this.xcoord = xcoord;
		this.ycoord = ycoord;
		this.matrix = matrix;
		this.zminmax = zminmax;
	}
}

class Queue<T> {
	class CLLNode<E> {
		E data; CLLNode<E> next;
		CLLNode(E data) { 
			this.data = data; next = null;
		}
	}
	
	/**
	 * Rear of the queue.
	 */
	CLLNode<T> rear; 
	
	/**
	 * Number of entries in the queue.
	 */
	int size;
	
	/**
	 * Initializes a new queue instance to empty.  
	 */
	public Queue() {
		rear = null; size=0;
	}
	
	/**
	 * Enqueues a given item into the queue.
	 * 
	 * @param item Item to be enqueued.
	 */
	public void enqueue(T item) {
		CLLNode<T> temp = new CLLNode<T>(item);
		if (size == 0) {
			temp.next = temp;
		} else {
			temp.next = rear.next;
			rear.next = temp;
		}
		rear = temp;
		size++;
	}
	
	/**
	 * Dequeues the front item of queue and returns it.
	 * 
	 * @return Item that is dequeued.
	 * @throws NoSuchElementException if item is not in queue.
	 */
	public T dequeue() throws NoSuchElementException { 
		if (size == 0) throw new NoSuchElementException();
		T o = rear.next.data;
		if (size == 1) {
			rear = null; 
		} else {
			rear.next = rear.next.next;
		}
		size--;
		return o;
	}
	
	/**
	 * Returns the first item in the queue without deleting it.
	 * 
	 * @return First item in the queue.
	 * @throws NoSuchElementException if queue is empty.
	 */
	public T peek() 
	throws NoSuchElementException { 
		if (size == 0) throw new NoSuchElementException();
		return rear.next.data;
	}
	
	/**
	 * Tells whether the queue is empty.
	 * 
	 * @return True if empty, false otherwise.
	 */
	public boolean isEmpty(){
		return size == 0;
	}
	
	/**
	 * Returns the number of items in the queue.
	 * 
	 * @return Number of items.
	 */
	public int size(){
		return size;
	}
}
