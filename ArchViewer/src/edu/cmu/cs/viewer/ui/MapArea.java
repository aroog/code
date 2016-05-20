package edu.cmu.cs.viewer.ui;

/**
 * Adapted from mozilla/lib/layout/layout.h from
 * http://ftp.mozilla.org/pub/mozilla.org/mozilla/source/mozilla-19980603.tar.gz RFC:
 * ftp://ftp.isi.edu/in-notes/rfc1980.txt For a rectangle, the coordinates are given as "left,top,right,bottom". The
 * rectangular region defined includes the lower-right corner specified, i.e. to specify the entire area of a 100x100
 * image, the coordinates would be "0,0,99,99". For a circular region, the coordinates are given as
 * "center_x,center_y,radius", specifying the center and radius of the ircle. All points up to and including those at a
 * distance of "radius" points from the center are included. For example, the coordinates "4,4,2" would specify a circle
 * which included the coordinates (2,4) (6,4) (4,2) and (4,6).
 * 
 * @author mabianto
 */
public class MapArea {
	int type;

	int[] coords;

	int coord_cnt;

	String anchor;

	String title;

	static String S_AREA_SHAPE_DEFAULT = "default";

	static String S_AREA_SHAPE_RECT = "rect";

	static String S_AREA_SHAPE_CIRCLE = "circle";

	static String S_AREA_SHAPE_POLY = "poly";

	static String S_AREA_SHAPE_POLYGON = "polygon"; /* maps to AREA_SHAPE_POLY */

	public static final int AREA_SHAPE_UNKNOWN = 0;

	public static final int AREA_SHAPE_DEFAULT = 1;

	public static final int AREA_SHAPE_RECT = 2;

	public static final int AREA_SHAPE_CIRCLE = 3;

	public static final int AREA_SHAPE_POLY = 4;

	public MapArea() {
		super();

	}

}
