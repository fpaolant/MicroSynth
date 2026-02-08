package it.univaq.microsynth.generator.openapi.spring;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.SpringCodegen;

import java.io.File;

/**
 * Custom generator for Spring, extending the default SpringCodegen and adding custom supporting files for outgoing calls and delegate implementation
 */
@Slf4j
public class SpringOutgoingGenerator extends SpringCodegen {

    /**
     * Override the processOpts method to add custom supporting files for outgoing calls and delegate implementation
     */
    @Override
    public void processOpts() {
        super.processOpts();

        String basePackage = this.invokerPackage;

        String invokerPath = toPackagePath(basePackage);

        supportingFiles.add(
                new SupportingFile(
                        "OutgoingCallService.mustache",
                        "src/main/java/" + invokerPath + "/outgoing",
                        "OutgoingCallService.java"
                )
        );
        //Delegate impl
        supportingFiles.add(
                new SupportingFile(
                        "ApiDelegateImpl.mustache",
                        "src/main/java/" + invokerPath + "/impl",
                        "DefaultApiDelegateImpl.java"
                )
        );
    }

    /**
     * Utility method to convert a package name to a file path
     * @param pkg The package name to convert
     * @return The corresponding file path for the package
     */
    private String toPackagePath(String pkg) {
        return pkg.replace('.', File.separatorChar);
    }

    /**
     * Override the getName method to return the name of the generator
     * @return The name of the generator
     */
    @Override
    public String getName() {
        return "spring";
    }
}
