/**
 * This class configures the OAuth2 Rest Template which will be used by Eureka during client registration calls to the
 * Eureka host.
 *
 * Now, using @Value is not recommended, I would suggest you use @ConfigurationProperties because it's much safer and you
 * can read the arguments about it online, but for ease of use we'll use @Value here despite it not being the safest.
 *
 * @author au5tie
 */
@Configuration
@Profile("!local")
public class OAuthTokenRetrievalConfiguration {

    @Value("${eureka.oauth.clientID}")
    private String eurekaClientID;

    @Value("${eureka.oauth.clientSecret}")
    private String eurekaClientSecret;

    @Value("${eureka.oauth.scopes}")
    private String eurekaScopes;

    @Value("${eureka.oauth.accessTokenUri}")
    private String eurekaAccessTokenUri;

    @Value("${eureka.oauth.audience}")
    private String eurekaAudience;

    /**
     * Creates and configures the Eureka Rest Template using the eureka OAuth2 specific properties which can be used to
     * make authenticated calls to the Eureka host.
     *
     * @return Rest Template for authenticated Eureka calls.
     * @author au5tie
     */
    @Bean(name = "eurekaRestTemplate")
    public RestTemplate eurekaRestTemplate() {

        ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();

        resourceDetails.setClientId(eurekaClientID);
        resourceDetails.setClientSecret(eurekaClientSecret);
        resourceDetails.setScope(Arrays.asList(eurekaScopes.split("\\s*,\\s*")));
        //resourceDetails.setGrantType(pingFederateProperties.getGrantType());
        resourceDetails.setAccessTokenUri(eurekaAccessTokenUri + "?aud=" + eurekaAudience);

        return new OAuth2RestTemplate(resourceDetails, new DefaultOAuth2ClientContext());
    }

    /**
     * This is where the magic happens, this creates the Eureka Discovery Client custom arguments which will add an
     * instance of our EurekaOAuthClientFilter, which will put our authentication (bearer) token on the outgoing Eureka
     * calls. This method specifically is what attaches the filter to the Eureka Discovery Client itself, which will
     * invoke the filter every time this client calls the Eureka host.
     *
     * @param restTemplate Eureka Rest Template.
     * @return Eureka Discovery Client Arguments.
     * @author au5tie
     */
    @Bean
    @Primary
    public DiscoveryClient.DiscoveryClientOptionalArgs discoveryClientOptionalArgs(@Qualifier(name = "EurekaOAuthClientFilter") RestTemplate restTemplate){

        DiscoveryClient.DiscoveryClientOptionalArgs args = new DiscoveryClient.DiscoveryClientOptionalArgs();
        EurekaOAuthClientFilter filter = new EurekaOAuthClientFilter((OAuth2RestTemplate) restTemplate);

        Collection<ClientFilter> additionalFilter = Collections.singletonList(filter);
        args.setAdditionalFilters(additionalFilter);

        return args;
    }
}
