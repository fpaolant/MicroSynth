package it.univaq.microsynth.service.impl;

import it.univaq.microsynth.domain.Connection;
import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.DiagramData;
import it.univaq.microsynth.domain.Node;
import it.univaq.microsynth.domain.Payload;
import it.univaq.microsynth.domain.dto.GenerationParamsDTO;
import it.univaq.microsynth.service.GeneratorService;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Override
    public Diagram generate(GenerationParamsDTO params) throws IllegalArgumentException {
        int n = params.getNodes();
        int r = params.getRoots();
        double d = params.getDensity();

        if (r > n) {
            throw new IllegalArgumentException("Number of roots cannot be greater than number of nodes.");
        }

        List<Node> nodes = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();
        Random rand = new Random();

        // 1. Create nodes with UUIDs as IDs
        List<String> nodeIds = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String id = UUID.randomUUID().toString();
            nodeIds.add(id);

            nodes.add(new Node(
                    id,
                    "S " + (i + 1),
                    "circle",
                    this.generateRandomPayload(),
                    0L
            ));
        }

        // 2. Select roots (by index)
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

            String edgeKey = from + "->" + to;
            if (existingEdges.contains(edgeKey)) continue;

            existingEdges.add(edgeKey);

            String sourceId = nodeIds.get(from);
            String targetId = nodeIds.get(to);

            connections.add(new Connection(
                    UUID.randomUUID().toString(),
                    sourceId,
                    targetId,
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

    @Override
    public ByteArrayOutputStream exportDockerCompose(Diagram diagram) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        // 1. Add docker-compose.yml
        String dockerCompose = getDockerCompose(diagram);
        zos.putNextEntry(new ZipEntry("docker-compose.yml"));
        zos.write(dockerCompose.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 2. Add microservices with Dockerfile and code
        for (Node node : diagram.getData().getNodes()) {
            String serviceName = node.getLabel().toLowerCase().replace(" ", "_");
            String folder = serviceName + "/";

            // 2.1. Dockerfile
            String dockerfile = generateDockerfileFromTemplate(node.getPayload());
            zos.putNextEntry(new ZipEntry(folder + "Dockerfile"));
            zos.write(dockerfile.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 2.2. Main file (e.g. Main.java, main.py, index.js)
            String filename = mainFilename(node.getPayload().getLanguage());
            zos.putNextEntry(new ZipEntry(folder + filename));
            zos.write(node.getPayload().getCode().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        zos.close();
        return baos;
    }

    private String getDockerCompose(Diagram diagram) throws IOException {
        StringBuilder servicesSection = new StringBuilder();

        String netName = diagram.getName().toLowerCase().replace(" ", "_") + "-net";

        for (Node node : diagram.getData().getNodes()) {
            String serviceName = node.getLabel().toLowerCase().replace(" ", "_");
            Payload payload = node.getPayload();
            String image = getDockerImageFromPayload(payload);
            int internalPort = getExposedPortFromPayload(payload);
            int externalPort = 8000 + Math.abs(serviceName.hashCode() % 1000);

            servicesSection.append("  ").append(serviceName).append(":\n");
            servicesSection.append("    image: ").append(image).append("\n");
            servicesSection.append("    container_name: ").append(serviceName).append("\n");
            servicesSection.append("    ports:\n");
            servicesSection.append("      - \"").append(externalPort).append(":").append(internalPort).append("\"\n");
            servicesSection.append("    networks:\n");
            servicesSection.append("      - ").append(netName).append("\n");
        }

        // Load Docker compose template
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("templates/docker/docker-compose.tpl")) {
            if (is == null) throw new FileNotFoundException("Template not found: templates/docker/docker-compose.tpl");

            String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("{{services}}", servicesSection.toString())
                            .replace("{{network_name}}", netName);
        }
    }

    /**
     * Return ImageName
     * @param payload conntaining language and code
     * @return String of imagename
     */
    private String getDockerImageFromPayload(Payload payload) {
        if (payload == null) return "alpine";

        return switch (payload.getLanguage().toLowerCase()) {
            case "java" -> "openjdk:17";
            case "python" -> "python:3.11";
            case "javascript", "node", "typescript" -> "node:20";
            default -> "alpine";
        };
    }

    private int getExposedPortFromPayload(Payload payload) {
        if (payload == null) return 8080;

        return switch (payload.getLanguage().toLowerCase()) {
            case "java" -> 8080;
            case "python" -> 5000;
            case "javascript", "typescript" -> 3000;
            default -> 8080;
        };
    }

    private String generateDockerfileFromTemplate(Payload payload) throws IOException {
        String language = payload.getLanguage().toLowerCase();
        String templatePath = "templates/docker/" + language + ".dockerfile.tpl";

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (is == null) throw new FileNotFoundException("Template not found: " + templatePath);

            String template = StreamUtils.copyToString(is, StandardCharsets.UTF_8);

            Map<String, String> vars = switch (language) {
                case "java" -> Map.of(
                        "main_file", "Main.java",
                        "main_class", "Main"
                );
                case "python" -> Map.of(
                        "main_file", "main.py"
                );
                case "javascript", "node" -> Map.of(
                        "main_file", "index.js"
                );
                default -> Map.of();
            };

            return interpolate(template, vars);
        }
    }

    private String interpolate(String template, Map<String, String> vars) {
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }

    private String mainFilename(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> "Main.java";
            case "python" -> "main.py";
            case "javascript", "node" -> "index.js";
            case "typescript" -> "index.ts";
            default -> "main.txt";
        };
    }

    private Payload generateRandomPayload() {
        Payload payload = new Payload();
        Random rand = new Random();

        String[] languages = {"java", "python", "javascript"};
        String chosen = languages[rand.nextInt(languages.length)];
        payload.setLanguage(chosen);

        String code = switch (chosen) {
            case "java" -> """
                    public class Main {
                        public static void main(String[] args) {
                            System.out.println("Hello from Java!");
                        }
                    }
                    """;
            case "python" -> """
                    def main():
                        print("Hello from Python!")

                    if __name__ == "__main__":
                        main()
                    """;
            case "javascript" -> """
                    function main() {
                        console.log("Hello from JavaScript!");
                    }

                    main();
                    """;
            default -> "// No code";
        };

        payload.setCode(code);
        return payload;
    }


}
