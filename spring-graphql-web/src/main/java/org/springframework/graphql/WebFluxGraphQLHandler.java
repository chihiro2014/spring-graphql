/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graphql;

import java.util.List;

import graphql.GraphQL;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * GraphQL handler to expose as a WebFlux.fn endpoint via
 * {@link org.springframework.web.reactive.function.server.RouterFunctions}.
 */
public class WebFluxGraphQLHandler implements HandlerFunction<ServerResponse> {

	private final WebInterceptorExecution executionChain;


	public WebFluxGraphQLHandler(GraphQL graphQL, List<WebInterceptor> interceptors) {
		this.executionChain = new WebInterceptorExecution(graphQL, interceptors);
	}


	public Mono<ServerResponse> handle(ServerRequest request) {
		return request.bodyToMono(WebInput.MAP_PARAMETERIZED_TYPE_REF)
				.flatMap(body -> {
					WebInput webInput = new WebInput(request.uri(), request.headers().asHttpHeaders(), body);
					return this.executionChain.execute(webInput);
				})
				.flatMap(output -> ServerResponse.ok().bodyValue(output.toSpecification()));
	}

}
