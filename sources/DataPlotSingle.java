
/* DataPlotSingle Usage Start
        GeneralUtility gu = new GeneralUtility();
        double[][] xydata = GeneralUtility.readDataFile("_VSRAFit.csv",(new int[]{3,1000,0}),(new int[]{0,1}),false);
        DataPlotSingle dpsc = new DataPlotSingle(xydata,"Test",null,null,null,null);

        GeneralUtility.wait_ms(1000);
        double[][] xydata2 = GeneralUtility.readDataFile("_VSRAFit.csv",(new int[]{3,1000,0}),(new int[]{0,3}),false);
        dpsc.plotData(xydata2,true);
// DataPlotSingle Usage End */
// Format of xydata
//
// xydata[0][i]: X-data for the first set
// xydata[1][i]: Y-data for the first set
// xydata[2][i]: X-data for the second set
// xydata[3][i]: Y-data for the second set
// .
// .
// .
// xydata[m][i]: m is even number, X-data for the (m/2+1)th set
// xydata[m+1][i]: m is even number, Y-data for the (m/2+1)th set
//

import java.text.DecimalFormat;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;

public class DataPlotSingle extends JFrame{

    DKPlot dkp;
    private double [] selectedXYPosition = null;
    
    public DataPlotSingle(double[][] xydata,String title,int [] plottype
    		,int [] circlesize,double [] xrange,double[] yrange){
        int ndataset = xydata.length/2;

        if (plottype != null){
            if (plottype.length != ndataset) plottype = null;
        }

        if (circlesize != null){
            if (circlesize.length != ndataset) circlesize = null;
        }

        if(plottype == null){
            circlesize = null;

            plottype = new int[ndataset];
            circlesize = new int[ndataset];
            for (int i = 0; i < ndataset;i++){
                plottype[i] = 1;
                circlesize[i] = 5;
            }
        }
        else if (circlesize == null){
            circlesize = new int[ndataset];
            for (int i = 0; i < ndataset;i++) circlesize[i] = 5;
        }
        else{
            for (int i = 0; i < ndataset;i++){
                if((plottype[i] != 1) && (circlesize[i] <= 0))
                        circlesize[i] = 5;
            }
        }

        this.setSize(960,720);
        this.setTitle("DataPlot by Daewon Kwon");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dkp = new DKPlot(ndataset,plottype,circlesize,xydata,xrange,yrange);
        setTitleText(title,null,null);
        this.add(dkp,BorderLayout.CENTER);
        this.setVisible(true);
    }
    
    public double [] getSelectedXYPosition() {
    	return selectedXYPosition;
    }
    
    public void plotData(double[][] xydata,double [] xrange,double[] yrange){
    	boolean autoscale;
    	if((xrange == null) && (yrange == null)) {
    		autoscale = true;
    	}
    	else {
        	dkp.setXYrange(xrange, yrange);
    		autoscale = false;
    	}
    	
    	dkp.plotData(xydata,autoscale); 
    }
    
    public void plotData(double[][] xydata,boolean autoscale){
    	dkp.plotData(xydata,autoscale); 
    }
    
    public void setTitleText(String title,String subtitle,String subtitle2){
    	dkp.setTitleText(title,subtitle,subtitle2);
    }
    
    public double [] getXrange(){
    	return dkp.getXrange();
    }

    public double [] getYrange(){
    	return dkp.getYrange();
    }

    private class DKPlot extends JComponent{
    	int ndataset = 1;
    	int [] plottype;
    	int [] circlesize;
    	
    	double[][] xydata;
    	double[] xrange;
    	double[] yrange;

        double[] xymin0 = new double[2];
        double[] xymax0 = new double[2];
        double[] xymin = new double[2];
        double[] xymax = new double[2];

        int[] gxymin = new int[2];
        int[] gxymax = new int[2];

        double[] drag1 = new double[2];
        double[] drag2 = new double[2];

        Point startDrag, endDrag;
        double[] cursorxy = new double[2];
        boolean updatexy = false,firstplotdata = true;
        
        String title = "Main title will be shown here",subtitle = "Average values will be shown here";

        public DKPlot(int ndataset,int [] plottype,int[] circlesize,double[][] xydata,double[] xrange,double[] yrange){
        	this.ndataset = ndataset; // this value will never change again
        	this.plottype = plottype;
// plottype 0 - scatter, 1 - line, 2 - line & scatter (plottype.length = xydata.length/2)
        	this.circlesize = circlesize;
        	this.xrange = xrange;
        	this.yrange = yrange;
        	plotData(xydata,false);
        	
            this.addMouseListener(
                new MouseAdapter(){
                    public void mousePressed(MouseEvent e){
                        int[] gcrd = new int[2];
                        gcrd[0] = e.getX();
                        gcrd[1] = e.getY();
                        drag1 = gcrdtoxy(gcrd);
                        startDrag = new Point(e.getX(),e.getY());
                        endDrag = startDrag;
                        repaint();
                    }

                    public void mouseClicked(MouseEvent e){
                        if(e.getClickCount()==2){
                        	selectedXYPosition = cursorxy;
                        }
                    }
                    
                    public void mouseReleased(MouseEvent e){
                        int[] gcrd = new int[2];
                        gcrd[0] = e.getX();
                        gcrd[1] = e.getY();
                        drag2 = gcrdtoxy(gcrd);

                        if( Math.abs(endDrag.getX() - startDrag.getX()) > 10){
                            updatexy = false;
                            xymin[0] = Math.min(drag1[0],drag2[0]);
                            xymin[1] = Math.min(drag1[1],drag2[1]);
                            xymax[0] = Math.max(drag1[0],drag2[0]);
                            xymax[1] = Math.max(drag1[1],drag2[1]);
                        }
                        else if((endDrag.getX() >= gxymin[0]) && (endDrag.getX() <= gxymax[0])&&
                        (endDrag.getY() <= gxymin[1]) && (endDrag.getY() >= gxymax[1])){
                            updatexy = true;
                            int[] sgxy = new int[2];
                            sgxy[0] = (int)endDrag.getX();
                            sgxy[1] = (int)endDrag.getY();
                            cursorxy = gcrdtoxy(sgxy);
                            selectedXYPosition = cursorxy;
                        }
                        else{
                            updatexy = false;
                            xymin[0] = xymin0[0]; xymin[1] = xymin0[1];
                            xymax[0] = xymax0[0]; xymax[1] = xymax0[1];
                        }

                        DecimalFormat df;
                        String showstat = "", showstat2 = "";

                        String logStr = "Average,STD,min,max,range,ndata\n";

                        for (int m=0;m<ndataset;m++){
                            int ndata =  xydata[2*m].length;
                            int idata = 0;
                            double sum = 0.0,sumsq = 0.0,avg,std,min=0.0,max=0.0;
                            for (int i = 0; i < ndata; i++){
                                if(((xydata[2*m][i] >= xymin[0]) && (xydata[2*m][i] <= xymax[0]))
                                        && ((xydata[2*m+1][i] >= xymin[1]) && (xydata[2*m+1][i] <= xymax[1]))){
                                        idata++;

                                        if (idata == 1){
                                            min = xydata[2*m+1][i];
                                            max = xydata[2*m+1][i];
                                        }
                                        else if (xydata[2*m+1][i] < min) min = xydata[2*m+1][i];
                                        else if (xydata[2*m+1][i] > max) max = xydata[2*m+1][i];

                                        sum += xydata[2*m+1][i];
                                        sumsq = sumsq + xydata[2*m+1][i] * xydata[2*m+1][i];
                                }
                            }
                            if(idata == 0){
                                avg = 0.0;
                                std = 0.0;
                            }
                            else{
                                avg = sum/idata;
                                if(idata == 1){
                                    std = 0.0;
                                }
                                else{
                                    std = Math.sqrt((sumsq - idata * avg * avg)/(idata - 1));
                                }
                            }

                            if((avg == 0) || (std == 0)){
                                df = new DecimalFormat("0.000E0"); //Scientific representation
                            }
                            else if((Math.log10(Math.abs(avg)) < -3.001)||(Math.log10(Math.abs(std)) < -3.001)){
                                df = new DecimalFormat("0.000E0"); //Scientific representation
                            }
                            else if((Math.log10(Math.abs(avg)) > 5.999)||(Math.log10(Math.abs(std)) > 5.999)){
                                df = new DecimalFormat("0.000E0"); //Scientific representation
                            }
                            else{
                                df = new DecimalFormat("0.000");
                            }
                            if(showstat == ""){
                                logStr = logStr + avg+","+std+","+min+","+max+","+(max-min)+","+idata +"\n";
                                showstat = df.format(avg);
                                showstat2 = df.format(std);
                            }
                            else{
                                logStr = logStr + avg+","+std+","+min+","+max+","+(max-min)+","+idata +"\n";
                                showstat = showstat + " / " + df.format(avg);
                                showstat2 = showstat2 + " / " + df.format(std);
                            }
                        }
//                        GeneralUtility.saveToFile("_stat.csv",logStr,false);

                        subtitle = showstat;

                        startDrag = null;
                        endDrag = null;
                        repaint();
                    }
                });

            this.addMouseMotionListener(
                new MouseMotionAdapter(){
                    public void mouseDragged(MouseEvent e){
                        endDrag = new Point(e.getX(),e.getY());
                        repaint();
                    }
                });
        }
        
        public double[] getXrange(){
        	return new double[]{xymin[0],xymax[0]};
        }
        
        public double[] getYrange(){
        	return new double[]{xymin[1],xymax[1]};
        }
        
        public void setTitleText(String title,String subtitle,String subtitle2){
        	if(title != null) this.title = title;
        	if(subtitle != null) this.subtitle = subtitle;
        }
        
        public void plotData(double [][] xydata,boolean autoScale){
            if(xydata.length != 2*ndataset) return;
            this.xydata = xydata;
            
            if(autoScale){
                xrange = null;
                yrange = null;
            }
            else if(!firstplotdata){
            	xrange = new double[]{xymin[0],xymax[0]};
            	yrange = new double[]{xymin[1],xymax[1]};
            }

            setXYMinMax(xrange,yrange);

            firstplotdata = false;
            repaint();
        }

        private double[] stat(double[] var){
            if(var.length < 1) return null;
            int ndata = var.length,mini=0,maxi=0;
            double avg=0.0,std=0.0,min=var[0],max=var[0],sum=0.0,sumsq=0.0;

            for(int i = 0 ; i < ndata; i++){
                if(var[i] < min){ min = var[i]; mini = i; }
                if(var[i] > max){ max = var[i]; maxi = i; }
                sum = sum + var[i];
                sumsq = sumsq + var[i] * var[i];
            }

            avg = sum/ndata;
            std = Math.sqrt((sumsq - ndata * avg * avg)/(ndata - 1));

            return (new double[]{avg,std,min,max,(ndata+0.1),(mini+0.1),(maxi+0.1)});
        }

        private void setXYMinMax(double[] xrange,double[] yrange){
        	//sets the following values: xymin0,xymax0,xymin,xymax
            xymin0 = new double[2]; xymax0 = new double[2];
            xymin = new double[2]; xymax = new double[2];

            double[] statx,staty;

            for (int i = 0; i < ndataset; i++){
                statx = stat(xydata[2*i]);
                staty = stat(xydata[2*i+1]);

                if(i == 0){
                    xymin0[0] = statx[2];
                    xymax0[0] = statx[3];

                    xymin0[1] = staty[2];
                    xymax0[1] = staty[3];
                }
                else{
                    if(statx[2] < xymin0[0]) xymin0[0] = statx[2];
                    if(statx[3] > xymax0[0]) xymax0[0] = statx[3];

                    if(staty[2] < xymin0[1]) xymin0[1] = staty[2];
                    if(staty[3] > xymax0[1]) xymax0[1] = staty[3];
                }
            }

            if((xrange == null) || (xrange[0] >= xrange[1])){
                xymin[0] = xymin0[0]; xymax[0] = xymax0[0];
            }
            else{
                xymin[0] = xrange[0]; xymax[0] = xrange[1];
            }

            if((yrange == null) || (yrange[0] >= yrange[1])){
                xymin[1] = xymin0[1]; xymax[1] = xymax0[1];
            }
            else{
                xymin[1] = yrange[0]; xymax[1] = yrange[1];
            }
        }
        
        public void setXYrange(double[] xrange,double[] yrange){
            if(xrange != null) {
            	if(xrange[0] < xrange[1]){
            		xymin[0] = xrange[0]; xymax[0] = xrange[1];
            	}
            }

            if(yrange != null) {
            	if(yrange[0] < yrange[1]){
            		xymin[1] = yrange[0]; xymax[1] = yrange[1];
            	}
            }
        }
        
        public double[] gcrdtoxy(int[] gcrd){
            double[] xy = new double[2];

            xy[0] = xymin[0] + ((double)gcrd[0] - gxymin[0])*(xymax[0] - xymin[0]) / (gxymax[0] - gxymin[0]);
            xy[1] = xymin[1] + ((double)gcrd[1] - gxymin[1])*(xymax[1] - xymin[1]) / (gxymax[1] - gxymin[1]);

            return xy;
        }

        public int[] xytogcrd(double[] xy){
            int[] gcrd = new int[2];

            gcrd[0] = gxymin[0] + (int)((xy[0] - xymin[0])*(gxymax[0] - gxymin[0]) / (xymax[0] - xymin[0]));
            gcrd[1] = gxymin[1] + (int)((xy[1] - xymin[1])*(gxymax[1] - gxymin[1]) / (xymax[1] - xymin[1]));

            return gcrd;
        }

        public void paint(Graphics g){
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            gxymin[0] = getSize().width / 6;
            gxymin[1] = 5 * getSize().height / 6;
            gxymax[0] = 5 * getSize().width / 6;
            gxymax[1] = getSize().height / 6;

            int[] gxy1 = xytogcrd(xymin);
            int[] gxy2 = xytogcrd(xymax);

            if(Math.abs(gxy2[0] - gxy1[0]) < 2){
                xymin[0] = xymin[0] - 0.1;
                xymax[0] = xymax[0] + 0.1;
            }
            if(Math.abs(gxy2[1] - gxy1[1]) < 2){
                xymin[1] = xymin[1] - 0.1;
                xymax[1] = xymax[1] + 0.1;
            }

            g2.setStroke(new BasicStroke(2));
            g2.setPaint(Color.BLACK);
            Shape plotarea = makeRectangle(gxymin[0],gxymin[1],gxymax[0],gxymax[1]);
            g2.draw(plotarea);
            g2.setPaint(Color.WHITE);
            g2.fill(plotarea);
            g2.setPaint(Color.BLACK);

            int[] txy = new int[2];
            int maxlabelsize = 20;
            int minlabelsize = 8;
            int defaultlabelsize = 10;
            int labelsize = (int)((double)getSize().width * defaultlabelsize / 640.0);
            if(labelsize > maxlabelsize) labelsize = maxlabelsize;
            if(labelsize < minlabelsize) labelsize = minlabelsize;
            int labelheight = labelsize;
            int labelwidth = labelsize / 2;

            setFont(new Font("Ariel", Font.PLAIN,labelsize));
            int titlex = getSize().width / 2 - labelwidth * title.length() / 2;
            g2.drawString(title, titlex, (gxymax[1] - labelheight)/2);
            subtitle = "Unzoom - left mouse click on outside of plotting area";
            titlex = getSize().width / 2 - labelwidth * subtitle.length() / 2;
            g2.drawString(subtitle, titlex, (gxymax[1] + 2* labelheight)/2);

            DecimalFormat df;

            if((cursorxy[0]==0) || (cursorxy[1]==0)){
                df = new DecimalFormat("0.000E0"); //Scientific representation
            }
            else if((Math.log10(Math.abs(cursorxy[0])) < -3.001) || (Math.log10(Math.abs(cursorxy[1])) < -3.001)){
                df = new DecimalFormat("0.000E0"); //Scientific representation
            }
            else if((Math.log10(Math.abs(cursorxy[0])) > 5.999) || (Math.log10(Math.abs(cursorxy[1])) > 5.999)){
                df = new DecimalFormat("0.000E0"); //Scientific representation
            }
            else{
                df = new DecimalFormat("0.000");
            }
            g2.setStroke(new BasicStroke(1));

            if(updatexy){
                double radius = Math.sqrt(cursorxy[0]*cursorxy[0]+cursorxy[1]*cursorxy[1]);
                double angle = Math.abs(180.0/Math.PI*Math.asin(cursorxy[0]/radius));
//                if((cursorxy[0]>=0.0)&&(cursorxy[1]<=0.0)) angle = angle;
                if((cursorxy[0]>=0.0)&&(cursorxy[1]>=0.0)) angle = 180 - angle;
                if((cursorxy[0]<=0.0)&&(cursorxy[1]>=0.0)) angle = angle + 180.0;
                if((cursorxy[0]<=0.0)&&(cursorxy[1]<=0.0)) angle = 360.0 - angle;
// Represent the point r-theta conf: DKwon
//                String label1 = "( " + df.format(radius) + " , " + df.format(angle) + " )";
                String label1 = "( " + df.format(cursorxy[0]) + " , " + df.format(cursorxy[1]) + " )";
                txy[0] = gxymax[0] - labelwidth * label1.length();
                txy[1] = gxymax[1] - labelheight / 2;
                g2.drawString(label1, txy[0], txy[1]);

                g2.setPaint(Color.BLACK);
                int[] cxy = xytogcrd(cursorxy);
                g2.draw(new Line2D.Float((gxymin[0]+1),cxy[1],(gxymax[0]-1),cxy[1]));
                g2.draw(new Line2D.Float(cxy[0],(gxymax[1]+1),cxy[0],(gxymin[1]-1)));
            }

            DecimalFormat dfl;
            double[] inidx;
            double[] gridpos = new double[]{0,0};

            // Vertical grid
            inidx = tickpos(xymin[0],xymax[0]);

            if((xymax[0] - xymin[0]) <= 0){
                dfl = new DecimalFormat("0.000E0");
            }
            else if(Math.log10(xymax[0]-xymin[0]) < -3.001)
                dfl = new DecimalFormat("0.000E0");
            else if(Math.log10(xymax[0]-xymin[0]) > 5.999)
                dfl = new DecimalFormat("0.000E0");
            else
                dfl = new DecimalFormat("0.000");

            gridpos[0] = inidx[0];
            int icount = 0,igridpos;
            boolean onoff = false;
            do{
                igridpos = xytogcrd(gridpos)[0];
                g2.setPaint(Color.BLACK);

                String label = dfl.format(gridpos[0]) +"";

                txy[0] = igridpos - labelwidth * label.length() / 2;
                if(onoff){
                    txy[1] = gxymin[1] + (3 * labelheight) / 2;
                    onoff = false;
                }
                else{
                    txy[1] = gxymin[1] + (5 * labelheight) / 2;
                    onoff = true;
                }
                g2.drawString(label, txy[0], txy[1]);
                g2.setPaint(Color.LIGHT_GRAY);

                Shape line = new Line2D.Float(igridpos,(gxymax[1] + 1),igridpos,(gxymin[1] -1));
                g2.draw(line);
                icount++;
                gridpos[0] = inidx[0] + inidx[1] * icount;
            }while(gridpos[0] < xymax[0]);

            //Horizontal grid
            inidx = tickpos(xymin[1],xymax[1]);

            if((xymax[1]-xymin[1]) <= 0){
                dfl = new DecimalFormat("0.000E0");
            }
            else if(Math.log10(xymax[1]-xymin[1]) < -3.001)
                dfl = new DecimalFormat("0.000E0");
            else if(Math.log10(xymax[1]-xymin[1]) > 5.999)
                dfl = new DecimalFormat("0.000E0");
            else
                dfl = new DecimalFormat("0.000");

            gridpos[1] = inidx[0];
            icount = 0;
            do{
                igridpos = xytogcrd(gridpos)[1];
                g2.setPaint(Color.BLACK);
                String label = dfl.format(gridpos[1]) +"";

                txy[0] = gxymin[0] - labelwidth * label.length() - 15;
                txy[1] = igridpos + labelheight / 2;
                g2.drawString(label, txy[0], txy[1]);
                g2.setPaint(Color.LIGHT_GRAY);
                Shape line = new Line2D.Float((gxymin[0] +1),igridpos,(gxymax[0] -1),igridpos );
                g2.draw(line);
                icount++;
                gridpos[1] = inidx[0] + inidx[1] * icount;
            }while(gridpos[1] < xymax[1]);

            g2.setStroke(new BasicStroke(1));
//            g2.setStroke(new BasicStroke(2)); //dkwon
                Color[] colors = {Color.RED,Color.BLUE,Color.MAGENTA,Color.CYAN,Color.PINK,
                        Color.LIGHT_GRAY,Color.GREEN,Color.DARK_GRAY,Color.BLACK,Color.ORANGE};
            for(int j = 0; j < ndataset; j++){

                g2.setPaint(colors[j%10]);
                for(int i = 0; i < (xydata[2*j].length-1); i++ ){
                    double[] xy1 = new double[2];
                    double[] xy2 = new double[2];
                    xy1[0] = xydata[2*j][i]; xy1[1] = xydata[2*j+1][i];
                    xy2[0] = xydata[2*j][i+1]; xy2[1] = xydata[2*j+1][i+1];

                    if((plottype[j] == 1) ||(plottype[j] == 2)){
                        double[][] xy = linecoords(xy1,xy2);
                        if(xy != null){
                            int[][] crd = new int[2][2];
                            crd[0] = xytogcrd(xy[0]);
                            crd[1] = xytogcrd(xy[1]);
                            g2.draw(new Line2D.Float(crd[0][0],crd[0][1],crd[1][0],crd[1][1]));
                        }
                    }

                    if((plottype[j] == 0) ||(plottype[j] == 2)){
//                    else{
                        int[] crd = new int[2];
                        int csize = circlesize[j];

                        if(i==0){
                            if(((xy1[0] >= xymin[0]) && (xy1[0] <= xymax[0]))
                                    && ((xy1[1] >= xymin[1]) && (xy1[1] <= xymax[1]))){
                                crd = xytogcrd(xy1);
                                Shape circle = new Ellipse2D.Float(crd[0]-csize/2,crd[1]-csize/2,csize,csize);
                                g2.draw(circle);
                            }
                        }

                        if(((xy2[0] >= xymin[0]) && (xy2[0] <= xymax[0]))
                                && ((xy2[1] >= xymin[1]) && (xy2[1] <= xymax[1]))){
                            crd = xytogcrd(xy2);
                            Shape circle = new Ellipse2D.Float(crd[0]-csize/2,crd[1]-csize/2,csize,csize);
                            g2.draw(circle);
                        }
                    }

                }
            }

            if(startDrag != null && endDrag != null){
                g2.setStroke(new BasicStroke(1));
                g2.setPaint(Color.RED);
                Shape r = makeRectangle(startDrag.x,startDrag.y,endDrag.x,endDrag.y);
                g2.draw(r);
            }
        }

        public double[] tickpos(double min, double max){
            if(max <= min){
                return new double[]{max,0.1};
            }
            double order = Math.log10(max-min);
            if(order < 0) order = order - 1.0;
            double dx = Math.pow(10.0,(int)order);
//            if((max - min) < 5 * dx)
  //              dx = dx/2;
            if((max - min) < 2.5 * dx)
                dx = dx/4.0;

            double xini;
            if((min/dx) > (int)(min/dx))
                xini = dx * (1 + (int)(min/dx));
            else
                xini = dx * (int)(min/dx);

            double[] inidx = new double[]{xini,dx};

            return inidx;
        }

        public double[][] linecoords(double[] xy1,double[] xy2){
            double[][] la = new double[2][2];

            boolean in1 = false, in2 = false;
            if((xy1[0] >= xymin[0])&&(xy1[0] <= xymax[0])
                    &&(xy1[1] >= xymin[1])&&(xy1[1] <= xymax[1])) in1 = true;
            if((xy2[0] >= xymin[0])&&(xy2[0] <= xymax[0])
                    &&(xy2[1] >= xymin[1])&&(xy2[1] <= xymax[1])) in2 = true;

            if(in1 && in2){
                for(int i = 0; i < 2; i++){
                    la[0][i] = xy1[i];
                    la[1][i] = xy2[i];
                }
            }
            else {
                la = fittoscreen(xy1,in1,xy2,in2);
            }
            return la;
        }

        public double[] xyint(double[] a1,double[] a2,double[] b1,double[] b2){
            double numerator = (b1[0]-a1[0])*(b2[1]-b1[1])-(b1[1]-a1[1])*(b2[0]-b1[0]);
            double denominator = (a2[0]-a1[0])*(b2[1]-b1[1])-(a2[1]-a1[1])*(b2[0]-b1[0]);

            if(denominator == 0){
                return null;
            }

            double u =  numerator/denominator;

            if( (u < 0) || (u > 1.0) ){
                return null;
            }

            double[] xy = new double[2];
            for(int i = 0; i < 2; i++){
                xy[i] = a1[i] + u * (a2[i] - a1[i]);
            }

            return xy;
        }

        public double[][] fittoscreen(double[] xy1,boolean in1,double[] xy2,boolean in2){
            double[][] la = new double[2][2];
            double[] xy;
            double xmin = xymin[0];
            double xmax = xymax[0];
            double ymin = xymin[1];
            double ymax = xymax[1];


            int ipt = 0;
            if(in1){
                la[ipt] = xy1;
                ipt++;
                if(xy1[0] > xy2[0]) xmax = xy1[0];
                else xmin = xy1[0];

                if(xy1[1] > xy2[1]) ymax = xy1[1];
                else ymin = xy1[1];
            }
            else if(in2){
                la[ipt] = xy2;
                ipt++;

                if(xy2[0] > xy1[0]) xmax = xy2[0];
                else xmin = xy2[0];

                if(xy2[1] > xy1[1]) ymax = xy2[1];
                else ymin = xy2[1];
            }

            double[][] lb1 = new double[4][];
            double[][] lb2 = new double[4][];
            lb1[0] = new double[]{xymin[0],xymin[1]};
            lb2[0] = new double[]{xymax[0],xymin[1]};
            lb1[1] = new double[]{xymax[0],xymin[1]};
            lb2[1] = new double[]{xymax[0],xymax[1]};
            lb1[2] = new double[]{xymax[0],xymax[1]};
            lb2[2] = new double[]{xymin[0],xymax[1]};
            lb1[3] = new double[]{xymin[0],xymax[1]};
            lb2[3] = new double[]{xymin[0],xymin[1]};

            double[] xytest = new double[2];
            int test1,test2;

            xy = xyint(xy1,xy2,lb1[0],lb2[0]);
            if(xy != null){
                test1 = xytogcrd(xy)[1];
                xytest[1] = ymin;
                test2 = xytogcrd(xytest)[1];
                if(((xy[0] >= xmin)&&(xy[0] <= xmax)) && (Math.abs(test1-test2)<2)){
                    la[ipt] = xy;
                    ipt++;
                }
            }

            xy = xyint(xy1,xy2,lb1[1],lb2[1]);
            if(xy != null){
                test1 = xytogcrd(xy)[0];
                xytest[0] = xmax;
                test2 = xytogcrd(xytest)[0];
                if((Math.abs(test1-test2)<2) && ((xy[1] >= ymin)&&(xy[1] <= ymax))){
                    la[ipt] = xy;
                    ipt++;
                }
            }

            xy = xyint(xy1,xy2,lb1[2],lb2[2]);
            if(xy != null){
                test1 = xytogcrd(xy)[1];
                xytest[1] = ymax;
                test2 = xytogcrd(xytest)[1];
                if(((xy[0] >= xmin)&&(xy[0] <= xmax)) && (Math.abs(test1-test2)<2)){
                    la[ipt] = xy;
                    ipt++;
                }
            }

            xy = xyint(xy1,xy2,lb1[3],lb2[3]);
            if(xy != null){
                test1 = xytogcrd(xy)[0];
                xytest[0] = xmin;
                test2 = xytogcrd(xytest)[0];
                if((Math.abs(test1-test2)<2) && ((xy[1] >= ymin)&&(xy[1] <= ymax))){
                    la[ipt] = xy;
                    ipt++;
                }
            }

            if(ipt == 2)
                return la;
            else
                return null;
        }

/*        public boolean cp(int[] v1,int[] v2){
            int product = v1[0]*v2[1] - v1[1]*v2[0];
            if(product > 0){
                return false;
            }
            else{
                return true;
            }
        }
*/
        private Rectangle2D.Float makeRectangle(int x1,int y1,int x2,int y2){
            int x = Math.min(x1,x2);
            int y = Math.min(y1,y2);
            int width = Math.abs(x1 - x2);
            int height = Math.abs(y1 - y2);
            return new Rectangle2D.Float(x,y,width,height);
        }
    }
}
