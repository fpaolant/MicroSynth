package it.univaq.microsynth.generator.openapi.node;

import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.NodeJSExpressServerCodegen;


/**
 * Custom generator for Node.js Express, extending the default NodeJSExpressServerCodegen and adding custom supporting files for outgoing calls
 */
public class NodeOutgoingGenerator extends NodeJSExpressServerCodegen {

    /**
     * Override the processOpts method to add custom supporting files for outgoing calls
     */
    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.add(
                new SupportingFile(
                        "outgoingClient.mustache",
                        "",
                        "outgoingClient.js"
                )
        );
    }

    /**
     * Override the getName method to return the name of the generator
     * @return The name of the generator
     */
    @Override
    public String getName() {
        return "nodejs-express";
    }
}
