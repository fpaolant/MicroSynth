package it.univaq.microsynth.generator.openapi.python;

import org.openapitools.codegen.languages.PythonFlaskConnexionServerCodegen;
import org.openapitools.codegen.SupportingFile;


/**
 *  Custom generator for Python Flask, extending the default PythonFlaskConnexionServerCodegen and adding custom supporting files
 *  for outgoing calls and delegate implementation
 */
public class PythonOutgoingGenerator extends PythonFlaskConnexionServerCodegen {

    /**
     * Override the processOpts method to add custom supporting files for outgoing calls and delegate implementation
     */
    @Override
    public void processOpts() {
        super.processOpts();

        // Scrive openapi_server/outgoing_client.py
        supportingFiles.add(
                new SupportingFile(
                        "outgoing_client.mustache",
                        "openapi_server",
                        "outgoing_client.py"
                )
        );
    }

    /**
     * Override the getName method to return the name of the generator
     * @return The name of the generator
     */
    @Override
    public String getName() {
        return "python-flask";
    }
}