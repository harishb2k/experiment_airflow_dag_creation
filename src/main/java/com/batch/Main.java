package com.batch;

import com.batch.graph.DgGraph;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.Charset;

@SuppressWarnings("deprecation")
public class Main {
    public static void main(String[] args) throws Exception {
        String content = Files.toString(new File("/Users/harishbohara/workspace/personal/test/src/main/resources/graph.json"), Charset.defaultCharset());
        // System.out.println(content);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        Graph graph = objectMapper.readValue(content, Graph.class);
        // System.out.println(graph);

        DgGraph g = graph.buildGraph();
        // System.out.println(objectMapper.writeValueAsString(g));

        DgGraph.GraphNode last = g.lastNode;
       // System.out.println(" " + last);

        StringBuffer sb = new StringBuffer();
        last.travers(last, sb, 1);


        String file = g.createFile();
        file += sb.toString();

        // System.out.println(file);

        Files.write(file.getBytes(), new File("out.py"));
    }

}
