package de.tudarmstadt.tk.pm.graph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.io.GraphMLWriter;

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
		Graph<HNNode, HNEdge<? extends HNNode, ? extends HNNode>> graph = new DirectedSparseGraph<>();

		// convert nodes
		for (HNNode node : heuristicGraph.getNodes()) {
			graph.addVertex(node);
		}
		for (HNEdge<? extends HNNode, ? extends HNNode> edge : heuristicGraph.getEdges()) {
			graph.addEdge(edge, edge.getSource(), edge.getTarget());
		}

		// export graph
		FileOutputStream fos = new FileOutputStream(outputFile);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);

		GraphMLWriter<HNNode, HNEdge<? extends HNNode, ? extends HNNode>> exporter = new GraphMLWriter<>();
		exporter.save(graph, outputStreamWriter);

		outputStreamWriter.close();
		fos.close();

		context.getProgress().setMaximum(100);

	}

}
