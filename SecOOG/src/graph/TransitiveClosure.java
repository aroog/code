package graph;

import java.util.List;

import oog.itf.IObject;
import secoog.SecEdge;

/**
 * adapted from http://code.google.com/p/jbpt/
 * see TransitiveClosure class
 * invariant: once the transitive closure is computed the graph does not change
 * */
public class TransitiveClosure<E extends SecEdge,V extends IObject> {

	private List<V> verticesAsList;
	private List<E> edgesAsList;
	private boolean[][] matrix;
	
	
	public TransitiveClosure(List<V> verticesAsList, List<E> edgesAsList ) {
		this.verticesAsList = verticesAsList;
		this.edgesAsList = edgesAsList;
		this.matrix = new boolean[this.verticesAsList.size()][this.verticesAsList.size()];
		calculateMatrix();
	}

	private void calculateMatrix() {

		/*
		 * Init matrix with edges
		 */
		for (E e: this.edgesAsList) {
			int source = this.verticesAsList.indexOf(e.src);
			int target = this.verticesAsList.indexOf(e.dst);
			matrix[source][target] = true;
		}
		
		/*
		 * Compute the transitive closure
		 */
		for (int i = 0; i < matrix.length; i++) 
			for (int j = 0; j < matrix.length; j++) 
				if (matrix[j][i])
					for (int k = 0; k < matrix.length; k++)
						matrix[j][k] = matrix[j][k] | matrix[i][k];
	}
	
	/**
	 * Check if there exists a directed path between two vertices
	 * @param v1 Vertex
	 * @param v2 Vertex
	 * @return <code>true</code> if there is a directed path from v1 to v2, <code>false</code> otherwise 
	 */
	public boolean hasPath(V v1, V v2) {
		int i = this.verticesAsList.indexOf(v1);
		int j = this.verticesAsList.indexOf(v2);
		return matrix[i][j];
	}
	
	/**
	 * Check if vertex is part of a loop
	 * @param v Vertex
	 * @return <code>true</code> if vertex is part of a loop, <code>false</code> otherwise
	 */
	public boolean isInLoop(V v) {
		int index = this.verticesAsList.indexOf(v);
		return matrix[index][index];
	}
	
	@Override
	public String toString() {
		if (matrix == null)
			calculateMatrix();
		
		String result = "";
		
		result += "==================================================\n";
		result += " Transitive Closure\n";
		result += "--------------------------------------------------\n";
		for (int i=0; i<verticesAsList.size(); i++)
			result += String.format("%d : %s\n", i, verticesAsList.get(i).getInstanceDisplayName());
		result += "--------------------------------------------------\n";
		result += "    ";
		for (int i=0; i<verticesAsList.size(); i++) result += String.format("%-4d", i);
		result += "    \n";
		for (int i=0; i<verticesAsList.size(); i++) {
			result += String.format("%-4d", i);
			for (int j=0; j<verticesAsList.size(); j++) {
				result += String.format("%-4s",(matrix[i][j] ? "+" : "-"));
			}
			result += String.format("%-4d", i);
			result += "\n";
		}
		result += "    ";
		for (int i=0; i<verticesAsList.size(); i++) result += String.format("%-4d", i);
		result += "    \n";
		result += "==================================================";
		
		return result;
	}

}


