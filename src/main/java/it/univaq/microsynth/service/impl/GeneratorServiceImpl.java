package it.univaq.microsynth.service.impl;

import it.univaq.microsynth.domain.Connection;
import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.DiagramData;
import it.univaq.microsynth.domain.Node;
import it.univaq.microsynth.domain.Payload;
import it.univaq.microsynth.domain.dto.GenerationParamsDTO;
import it.univaq.microsynth.service.GeneratorService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Override
    public Diagram generate(GenerationParamsDTO params) {

        int n = params.getNodes();
        int r = params.getRoots();
        double d = params.getDensity();

        if (r > n) {
            throw new IllegalArgumentException("Number of roots cannot be greater than number of nodes.");
        }

        List<Node> nodes = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();
        Random rand = new Random();

        // 1. Create nodes
        for (int i = 0; i < n; i++) {
            String id = "Service" + i;
            nodes.add(new Node(
                    id,
                    id,
                    "circle",
                    new Payload(),
                    0L
            ));
        }

        // 2. Select roots
        Set<Integer> rootIndices = new HashSet<>();
        while (rootIndices.size() < r) {
            rootIndices.add(rand.nextInt(n));
        }

        // 3. Generate connections
        int maxConnections = n * (n - 1);
        int targetConnections = (int) Math.round(d * maxConnections);
        Set<String> existingEdges = new HashSet<>();

        while (connections.size() < targetConnections) {
            int from = rand.nextInt(n);
            int to = rand.nextInt(n);

            if (from == to) continue;
            if (rootIndices.contains(to)) continue;

            String key = from + "->" + to;
            if (existingEdges.contains(key)) continue;

            existingEdges.add(key);

            connections.add(new Connection(
                    UUID.randomUUID().toString(),
                    "Service" + from,
                    "Service" + to,
                    false,
                    0L,
                    "calls",
                    new Payload()
            ));
        }

        // 4. Build diagram
        Diagram diagram = new Diagram();
        diagram.setId(UUID.randomUUID().toString());
        diagram.setName("Generated Diagram");
        diagram.setData(new DiagramData(nodes, connections));

        return diagram;
    }

    public String exportDockerCompose(Diagram diagram) {
        StringBuilder sb = new StringBuilder();
        sb.append("version: '3.8'\n");
        sb.append("services:\n");

        for (Node node : diagram.getData().getNodes()) {
            String serviceName = node.getId().toLowerCase();
            sb.append("  ").append(serviceName).append(":\n");
            sb.append("    image: openjdk:17\n");
            sb.append("    container_name: ").append(serviceName).append("\n");
            sb.append("    ports:\n");
            sb.append("      - \"").append(8000 + node.getId().hashCode() % 1000).append(":8080\"\n"); // porta fittizia
            sb.append("    networks:\n");
            sb.append("      - microsynth-net\n");
        }

        sb.append("\nnetworks:\n");
        sb.append("  microsynth-net:\n");
        sb.append("    driver: bridge\n");

        return sb.toString();
    }


}
