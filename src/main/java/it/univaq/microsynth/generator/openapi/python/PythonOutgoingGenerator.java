package it.univaq.microsynth.generator.openapi.python;

import org.openapitools.codegen.languages.PythonFlaskConnexionServerCodegen;
import org.openapitools.codegen.SupportingFile;

public class PythonOutgoingGenerator extends PythonFlaskConnexionServerCodegen {

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

    @Override
    public String getName() {
        return "python-flask";
    }
}