package ontherock.streaming.common;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class SwaggerOperationFilterConfig {

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            String pattern = "/api/secure/.*";

            if (handlerMethod.getMethod().getDeclaringClass().getPackageName().matches(pattern) ||
                    handlerMethod.getMethod().getName().matches(pattern)) {

                Parameter authHeader = new Parameter()
                        .in(ParameterIn.HEADER.toString())
                        .name("Authorization")
                        .required(true)
                        .description("JWT access token")
                        .example("Bearer your_access_token");

                operation.addParametersItem(authHeader);
            }

            if (handlerMethod != null) {
                java.lang.reflect.Parameter[] parameters = handlerMethod.getMethod().getParameters();

                List<Parameter> existingParameters = Optional.ofNullable(operation.getParameters())
                        .orElseGet(List::of);

                List<Parameter> filteredParameters = existingParameters.stream()
                        .filter(p -> !Arrays.stream(parameters)
                                .anyMatch(param -> param.isAnnotationPresent(ontherock.streaming.common.UserId.class) && param.getName().equals(p.getName())))
                        .collect(Collectors.toList());

                operation.setParameters(filteredParameters);
            }
            return operation;
        };
    }
}
