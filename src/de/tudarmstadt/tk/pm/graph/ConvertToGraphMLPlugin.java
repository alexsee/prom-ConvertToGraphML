/*
 * Copyright (c) 2016-2017 TK - Telecooperation Lab
 *
 * This file is part of ProM GraphMLExporter.
 * 
 * ProM GraphMLExporter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ProM GraphMLExporter. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tudarmstadt.tk.pm.graph;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.heuristics.impl.HNSet;
import org.processmining.models.heuristics.impl.HNSubSet;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class ConvertToGraphMLPlugin {

	private static final String PROPERTY_ACTIVITY = "Activity";
	
	private static final String PROPERTY_COUNT = "Count";

	@Plugin(name = "Export to GraphML format (GXL)", parameterLabels = {
			"HeuristicNet" }, help = "Converts a HeuristicNet into a GraphML format.", userAccessible = true, returnLabels = {}, returnTypes = {}, mostSignificantResult = -1)
	@UITopiaVariant(affiliation = "TU Darmstadt", author = "Alexander Seeliger", email = "seeliger@tk.tu-darmstadt.de")
	public void main(UIPluginContext context, HeuristicsNet net) throws UserCancelledException, IOException {

		// show file chooser
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new FileNameExtensionFilter("GraphML format (.graphml)", "graphml"));

		int result = fc.showSaveDialog(null);
		if (result == JFileChooser.CANCEL_OPTION) {
			throw new UserCancelledException();
		}

		File outputFile = fc.getSelectedFile();
		exportToFile2(net, outputFile);

		context.getProgress().setMaximum(100);
		
	}

	private void exportToFile2(HeuristicsNet net, File outputFile) throws IOException {

		XEventClass[] events = net.getActivitiesMappingStructures().getActivitiesMapping();

		TinkerGraph graph = new TinkerGraph();

		// add start and end node
		Vertex startVertex = graph.addVertex("START");
		startVertex.setProperty(PROPERTY_ACTIVITY, "START");

		Vertex endVertex = graph.addVertex("END");
		endVertex.setProperty(PROPERTY_ACTIVITY, "END");

		// add nodes
		Map<Integer, Vertex> nodeToBlueVertex = new LinkedHashMap<>();

		for (XEventClass event : events) {
			Vertex vertex = graph.addVertex(event.getIndex());
			vertex.setProperty(PROPERTY_ACTIVITY, event.getId());

			nodeToBlueVertex.put(event.getIndex(), vertex);

			if (net.getStartActivities().contains(event.getIndex())) {
				graph.addEdge("START->" + event.getIndex(), startVertex, vertex, "START->" + event.getIndex());
			}

			if (net.getEndActivities().contains(event.getIndex())) {
				graph.addEdge(event.getIndex() + "->END", vertex, endVertex, event.getIndex() + "->END");
			}
		}

		// add outgoing edges
		for (XEventClass event : events) {
			HNSet sets = net.getOutputSet(event.getIndex());

			for (int i = 0; i < sets.size(); i++) {
				HNSubSet subset = sets.get(i);

				for (int j = 0; j < subset.size(); j++) {
					try {
						
						Edge edge = graph.addEdge(
								event.getIndex() + "->" + subset.get(j), 
								nodeToBlueVertex.get(event.getIndex()),
								nodeToBlueVertex.get(subset.get(j)), 
								event.getIndex() + "->" + subset.get(j));
						
						edge.setProperty(PROPERTY_COUNT, net.getArcUsage().get(event.getIndex(), subset.get(j)));
						
					} catch (Exception ex) {
						// no idea why edges occur multiple times
					}
				}
			}
		}

		// export graph
		GraphMLWriter writer = new GraphMLWriter(graph);
		writer.setNormalize(true);
		writer.outputGraph(outputFile.getAbsolutePath());

	}
	
}
