package edu.cmu.cs.viewer.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Code to handle client-side image maps Adapted from mozilla/lib/layout/laymap.c from
 * http://ftp.mozilla.org/pub/mozilla.org/mozilla/source/mozilla-19980603.tar.gz
 * 
 * @author mabianto
 */
public class Map {

	private List<MapArea> areas = new ArrayList<MapArea>();

	public Map() {
		super();
	}

	public static void main(String[] args) {

		Map map = new Map();
		map.build(args[0]);
	}

	/*
	 * Check if a point is in a rectable specified by upper-left and lower-right corners.
	 */
	private boolean lo_is_location_in_rect(int x, int y, int[] coords) {
		int x1, y1, x2, y2;

		x1 = coords[0];
		y1 = coords[1];
		x2 = coords[2];
		y2 = coords[3];
		if ((x1 > x2) || (y1 > y2)) {
			return (false);
		}
		if ((x >= x1) && (x <= x2) && (y >= y1) && (y <= y2)) {
			return (true);
		}
		else {
			return (false);
		}
	}

	/*
	 * Check if a point is within the radius of a circle specified by center and radius.
	 */
	private boolean lo_is_location_in_circle(int x, int y, int[] coords) {
		int x1, y1, radius;
		int dx, dy, dist;

		x1 = coords[0];
		y1 = coords[1];
		radius = coords[2];
		if (radius < 0) {
			return (false);
		}

		dx = x1 - x;
		dy = y1 - y;

		dist = (dx * dx) + (dy * dy);

		if (dist <= (radius * radius)) {
			return (true);
		}
		else {
			return (false);
		}
	}

	/*
	 * Check if the passed point is withing the area described in the area structure.
	 */
	private boolean lo_is_location_in_area(MapArea area, int x, int y) {
		boolean ret_val;

		if (area == null) {
			return (false);
		}

		ret_val = false;
		switch (area.type) {
		case MapArea.AREA_SHAPE_RECT:
			if (area.coord_cnt < 4) {
				ret_val = false;
			}
			else {
				ret_val = lo_is_location_in_rect(x, y, area.coords);
			}
			break;

		case MapArea.AREA_SHAPE_CIRCLE:
			if (area.coord_cnt < 3) {
				ret_val = false;
			}
			else {
				ret_val = lo_is_location_in_circle(x, y, area.coords);
			}
			break;

		case MapArea.AREA_SHAPE_POLY:
			if (area.coord_cnt < 6) {
				ret_val = false;
			}
			else {
				// TODO: Implement me!
				// ret_val = lo_is_location_in_poly(x, y,
				// area.coords, area.coord_cnt);
			}
			break;

		case MapArea.AREA_SHAPE_DEFAULT:
			ret_val = true;
			break;

		case MapArea.AREA_SHAPE_UNKNOWN:
		default:
			break;
		}
		return (ret_val);
	}

	public ArrayList build(String filename) {
		ArrayList list = new ArrayList();

		File file = new File(filename);

		String s;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			if (in != null) {
				while (((s = in.readLine()) != null)) {
					parseEntry(s);
				}
				in.close();
			}
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}


		return list;
	}

	private String trim(String str) {
		str = str.replace('\"', ' ');
		return str.trim();
	}

	private void parseEntry(String line) {
		// <area shape="rect" href="32190111" title="objData: Data" alt="" coords="1671,389,1804,437">

		StringTokenizer tokenizer = new StringTokenizer(line, "< =>");
		String token = tokenizer.nextToken();
		String name = null;
		String value = null;
		String shapeType = null;
		String url = null;
		String title = null;
		String alt = null;
		String coords = null;

		if (token.equals("area")) {
			while (tokenizer.hasMoreElements()) {
				token = tokenizer.nextToken();
				value = tokenizer.nextToken();

				if (token.equals("shape")) {
					shapeType = trim(value);
				}
				else if (token.equals("href")) {
					url = trim(value);
				}
				else if (token.equals("title")) {
					title = trim(value);
				}
				else if (token.equals("alt")) {
					alt = trim(value);
				}
				else if (token.equals("coords")) {
					coords = trim(value);
				}
				else {
					System.err.println("\nWarning: Ignoring illegal line in configuration file: <" + line + ">");
				}
				// Create a new object
				if ((shapeType != null) && (coords != null)) {
					MapArea area = new MapArea();

					area.title = title;
					area.anchor = url;
					String[] split = coords.split(",");
					if (split != null) {
						area.coord_cnt = split.length;
						area.coords = new int[area.coord_cnt];
						for (int ii = 0; ii < split.length; ii++) {
							area.coords[ii] = Integer.parseInt(split[ii]);
						}
					}

					if (shapeType.compareTo(MapArea.S_AREA_SHAPE_RECT) == 0) {
						area.type = MapArea.AREA_SHAPE_RECT;
					}
					else if (shapeType.compareTo(MapArea.S_AREA_SHAPE_CIRCLE) == 0) {
						area.type = MapArea.AREA_SHAPE_CIRCLE;
					}

					areas.add(area);
				}
			}
		}

	}

	public MapArea getArea(int x, int y) {
		MapArea theArea = null;

		for (Iterator iter = areas.iterator(); iter.hasNext();) {
			MapArea area = (MapArea) iter.next();

			if (lo_is_location_in_area(area, x, y)) {
				theArea = area;
				break;
			}
		}

		return theArea;
	}
}
