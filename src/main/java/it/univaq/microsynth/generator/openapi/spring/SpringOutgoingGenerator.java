package it.univaq.microsynth.generator.openapi.spring;

import it.univaq.microsynth.generator.model.DelegateOperationModel;
import it.univaq.microsynth.generator.model.DelegateParamModel;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.SpringCodegen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SpringOutgoingGenerator extends SpringCodegen {
//    @Override
//    public void processOpts() {
//        super.processOpts();
//
//        String basePackage = this.invokerPackage;
//        String apiPackage  = this.apiPackage;
//
//        String invokerPath = toPackagePath(basePackage);
//        String apiPath     = toPackagePath(apiPackage);
//
//
//
//        //Outgoing service
//        supportingFiles.add(
//                new SupportingFile(
//                        "OutgoingCallService.mustache",
//                        "src/main/java/" + invokerPath + "/outgoing",
//                        "OutgoingCallService.java"
//                )
//        );
//
//        Object raw = additionalProperties.get("delegateImpl");
//        if (!(raw instanceof Map<?, ?> delegateImpl)) {
//            throw new IllegalStateException("delegateImpl missing or not a Map");
//        }
//
//        additionalProperties.put("packageName", delegateImpl.get("packageName"));
//        additionalProperties.put("className",  delegateImpl.get("className"));
//        additionalProperties.put("operations", delegateImpl.get("operations"));
//
//
//        //Delegate impl
//        supportingFiles.add(
//                new SupportingFile(
//                        "apiDelegateImpl.mustache",
//                        "src/main/java/" + apiPath + "/impl",
//                        "DefaultApiDelegateImpl.java"
//                )
//        );
//
//        log.error("SpringOutgoingGenerator supportingFiles = {}",
//                supportingFiles.stream()
//                        .map(SupportingFile::getDestinationFilename)
//                        .toList());
//    }


    @Override
    public void processOpts() {
        super.processOpts();

        String basePackage = this.invokerPackage;

        String invokerPath = toPackagePath(basePackage);

        //supportingFiles.clear();

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

    private String toPackagePath(String pkg) {
        return pkg.replace('.', File.separatorChar);
    }

    @Override
    public String getName() {
        return "spring";
    }
}
