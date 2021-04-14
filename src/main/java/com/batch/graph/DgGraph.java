package com.batch.graph;

import com.batch.Graph;
import com.batch.Node;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class DgGraph {

    @JsonIgnore
    public Map<String, GraphNode> nodes = new HashMap<>();
    public Map<String, GraphNode> roots = new HashMap<>();
    public GraphNode lastNode;

    public void initGraph(Graph graph) {
        graph.getSource().forEach(node -> {
            GraphNode graphNode = new GraphNode();
            graphNode.setId(node.getId());
            graphNode.setNode(node);
            nodes.put(graphNode.getId(), graphNode);
        });

        graph.getTransformation().forEach(node -> {
            GraphNode graphNode = new GraphNode();
            graphNode.setId(node.getId());
            graphNode.setNode(node);
            nodes.put(graphNode.getId(), graphNode);
        });

        graph.getSink().forEach(node -> {
            GraphNode graphNode = new GraphNode();
            graphNode.setId(node.getId());
            graphNode.setNode(node);
            nodes.put(graphNode.getId(), graphNode);
        });

        Node end = graph.getEnd();
        GraphNode graphNode = new GraphNode();
        graphNode.setId(end.getId());
        graphNode.setNode(end);
        nodes.put(graphNode.getId(), graphNode);

        lastNode = graphNode;

        Node start = graph.getStart();
        graphNode = new GraphNode();
        graphNode.setId(start.getId());
        graphNode.setNode(start);
        nodes.put(graphNode.getId(), graphNode);


    }

    public void fix() {
        nodes.forEach((id, graphNode) -> {
            if (graphNode.node.getDependsOn() == null || graphNode.node.getDependsOn().isEmpty()) return;

            graphNode.node.getDependsOn().forEach(parentId -> {
                try {
                    nodes.get(parentId).vertices.add(graphNode);
                    graphNode.parents.add(nodes.get(parentId));
                } catch (Exception e) {
                    System.out.println("Error on " + parentId);
                }
            });
        });
        System.out.println();
    }

    public String createFile() {
        // # Step 5 - Create tasks
        //greaterThan15 = BashOperator(
        //  task_id= 'greater_Than_equal_to_15',
        //  bash_command="echo value is greater than or equal to 15",
        //  dag=dag
        //)

        StringBuffer sb = new StringBuffer();
        sb.append("from airflow import DAG\n" +
                "from airflow.operators.bash_operator import BashOperator\n" +
                "from airflow.operators.python_operator import PythonOperator, BranchPythonOperator\n" +
                "from datetime import datetime, timedelta\n" +
                "from airflow.models import Variable\n" +
                "from airflow.utils.trigger_rule import TriggerRule \n \n") ;

        sb.append("# Step 1 - define the default parameters for the DAG\n" +
                "default_args = {\n" +
                "  'owner': 'airflow',\n" +
                "  'depends_on_past': False,\n" +
                "  'start_date': datetime(2019, 7, 20),\n" +
                "  'email': ['vipin.chadha@gmail.com'],\n" +
                "  'email_on_failure': False,\n" +
                "  'email_on_retry': False,\n" +
                "  'retries': 1,\n" +
                "  'retry_delay': timedelta(minutes=5),\n" +
                "\n" +
                "}");
        sb.append("\n\n");

        sb.append("dag = DAG(  'hello_harish',\n" +
                "        schedule_interval='0 0 * * *' ,\n" +
                "        default_args=default_args\n" +
                "    )");
        sb.append("\n\n");

        nodes.forEach((name, graphNode) -> {
            String script =
                    "%s = BashOperator(\n" +
                            "  task_id= '%s',\n" +
                            "  bash_command=\"echo do it %s\",\n" +
                            "  dag=dag\n" +
                            ")";
            script = String.format(script, name, name, name);
            script += "\n\n";
            sb.append(script);
        });

        return sb.toString();
    }

    @Data
    public static class GraphNode {
        private String id;

        @JsonIgnore
        private Node node;

        // @JsonIgnore
        private Set<GraphNode> vertices = new HashSet<>();

        @JsonIgnore
        private Set<GraphNode> parents = new HashSet<>();

        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GraphNode graphNode = (GraphNode) o;
            return id.equals(graphNode.id);
        }


        public void travers(DgGraph.GraphNode root, StringBuffer sb, int level) {
            String tab = "";
            for (int i = 0; i < level - 1; i++) {
                tab += "  ";
            }
            List<GraphNode> parents = new ArrayList<>();
            String text = "";
            if (root.parents != null && root.parents.size() == 1) {
                parents = new ArrayList<>(root.getParents());
                text = root.id + ".set_upstream(" + parents.get(0).id + ")\n";


            } else if (root.parents != null && root.parents.size() > 1) {
                parents = new ArrayList<>(root.getParents());
                text = root.id + ".set_upstream([";
                text += parents.stream().map(graphNode -> graphNode.id).collect(Collectors.joining(", "));
                text += "]) \n";
            } else {

            }
            // sb.append(tab).append(text);
            sb.append(text);

            int l = level + 1;
            parents.forEach(graphNode -> {
                travers(graphNode, sb, l);
            });
        }

    }
}
