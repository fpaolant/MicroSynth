package it.univaq.microsynth.generator.openapi.spring;

import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.SpringCodegen;

import java.io.File;

public class SpringOutgoingGenerator extends SpringCodegen {
    @Override
    public void processOpts() {
        super.processOpts();

        String basePackage = this.invokerPackage;
        String apiPackage = this.apiPackage;

        String invokerPath = toPackagePath(basePackage);
        String apiPath = toPackagePath(apiPackage);

        supportingFiles.add(
                new SupportingFile(
                        "OutgoingCallService.mustache",
                        "src/main/java/" + invokerPath + "/outgoing",
                        "OutgoingCallService.java"
                )
        );

        // DefaultApiImpl
        supportingFiles.add(
                new SupportingFile(
                        "DefaultApiImpl.mustache",
                        "src/main/java/" + apiPath + "/impl",
                        "DefaultApiImpl.java"
                )
        );
    }

    private String toPackagePath(String pkg) {
        return pkg.replace('.', File.separatorChar);
    }

    @Override
    public String getName() {
        return "spring";
    }
}
