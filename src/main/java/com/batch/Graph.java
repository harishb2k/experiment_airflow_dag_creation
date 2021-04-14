package com.batch;

import com.batch.graph.DgGraph;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Graph {
    List<Node> source;
    List<Node> transformation;
    List<Node> sink;
    Node end;
    Node start;

    public DgGraph buildGraph() {
        DgGraph g = new DgGraph();
        g.initGraph(this);
        for (int i = 0; i < 1; i++) {
            g.fix();
        }
        return g;
    }
}
