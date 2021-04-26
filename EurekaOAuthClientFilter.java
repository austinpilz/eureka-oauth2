/**
 * This client filter is an interceptor which will intercept outgoing calls from this client to the Eureka host. It will
 * apply the bearer token from our Eureka-specific OAuth2 Rest Template which adds our security claim to the outgoing call.
 * This filter is how the outgoing call is actually populated with our grant information for the Eureka host to validate
 * our calls/claims.
 *
 * PS This uses Lombok to create the constructor with the specific names rest template bean. You have to add some Lombok
 * configuration, if you haven't already, which allows it to copy the qualified annotations during constructor generation.
 * See https://stackoverflow.com/a/50287955 on how to do that.
 *
 * @author au5tie
 */
@AllArgsConstructor
@Slf4j
@Profile("!local")
public class EurekaOAuthClientFilter extends ClientFilter {

    @Qualifier(name = "eurekaRestTemplate")
    private final OAuth2RestTemplate restTemplate;

    /**
     * Adds the rest template's bearer token to the outgoing Eureka request.
     *
     * @param request Eureka request.
     * @return Client Response.
     * @throws ClientHandlerException If an issue occurs during filtering.
     * @author au5tie
     */
    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {

        request.getHeaders().add(HttpHeaders.AUTHORIZATION, OAuth2AccessToken.BEARER_TYPE.concat(StringUtils.SPACE)
                .concat(restTemplate.getAccessToken()
                        .getValue()));

        return getNext() != null ? getNext().handle(request) : null;
    }
}