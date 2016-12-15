/*
 * Copyright (c) 2016 TK - Telecooperation Lab
 *
 * This file is part of prom-GraphMLExporter.
 * 
 * prom-GraphMLExporter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with prom-GraphMLExporter.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tudarmstadt.tk.pm.graph;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.heuristics.HeuristicsNetGraph;
import org.processmining.models.heuristics.elements.HNEdge;
import org.processmining.models.heuristics.elements.HNNode;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class ConvertToGraphMLPlugin {

	@Plugin(name = "Export to GraphML format (GXL)", parameterLabels = {
			"HeuristicNet" }, help = "Converts a HeuristicNet into a GraphML format.", userAccessible = true, returnLabels = {}, returnTypes = {}, mostSignificantResult = -1)
	@UITopiaVariant(affiliation = "TU Darmstadt", author = "Alexander Seeliger", email = "seeliger@tk.tu-darmstadt.de")
	public void main(UIPluginContext context, HeuristicsNet net) throws UserCancelledException, IOException {

		// show file chooser
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new FileNameExtensionFilter("GraphML format (.gxl)", "gxl"));

		int result = fc.showSaveDialog(null);
		if (result == JFileChooser.CANCEL_OPTION) {
			throw new UserCancelledException();
		}

		File outputFile = fc.getSelectedFile();

		// convert heuristics net into graph
		HeuristicsNetGraph heuristicGraph = new HeuristicsNetGraph(net, "", false);
		TinkerGraph graph = new TinkerGraph();

		// convert nodes
		Map<HNNode, Vertex> nodeToBlueVertex = new LinkedHashMap<>();
		
		for (HNNode node : heuristicGraph.getNodes()) {
			Vertex vertex = graph.addVertex(node.getId().toString());
			nodeToBlueVertex.put(node, vertex);
			
			vertex.setProperty("activity", node.getAttributeMap().get("ProM_Vis_attr_label"));
		}
		
		for (HNEdge<? extends HNNode, ? extends HNNode> edge : heuristicGraph.getEdges()) {
			graph.addEdge(getId(edge), 
					nodeToBlueVertex.get(edge.getSource()), 
					nodeToBlueVertex.get(edge.getTarget()),
					getId(edge));
		}

		// export graph
		GraphMLWriter writer = new GraphMLWriter(graph);
		writer.setNormalize(true);
		writer.outputGraph(outputFile.getAbsolutePath());

		context.getProgress().setMaximum(100);

	}
	private static String getId(HNEdge<? extends HNNode, ? extends HNNode> edge) {
		return edge.getSource().getId().toString() + "->" + edge.getTarget().getId().toString();
	}
}
