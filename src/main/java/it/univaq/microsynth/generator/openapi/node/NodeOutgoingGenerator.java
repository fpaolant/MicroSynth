package it.univaq.microsynth.generator.openapi.node;

import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.NodeJSExpressServerCodegen;

public class NodeOutgoingGenerator extends NodeJSExpressServerCodegen {

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.add(
                new SupportingFile(
                        "outgoingClient.mustache",
                        "",                       // root di generated/
                        "outgoingClient.js"
                )
        );
    }

    @Override
    public String getName() {
        return "nodejs-express";
    }
}
