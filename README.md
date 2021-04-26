# Eureka OAuth2 Client to Host Authentication
Netflix's [Eureka](https://github.com/Netflix/eureka) is a service discovery registry. Client applications self register themselves with the Eureka host(s) which allow for dyanmic discovery of all deployed/healthy nodes of a service. I found myself wanting to add an extra layer of security to the clients authenticating with the host and there was, at the time, absolutely no mention of doing this online. 

## Client Registration
When a client application utilizing Eureka boots up, it will make a registration call to the configured Eureka host via REST call. It also performs configurable periodic checkin calls to update the host of the node's status. While these calls can be made over HTTPS, there is no built in Eureka client configuration to authenticate the calls.

### Why Authenticate Clients?
While the Eureka host should only be accessible internally by the client nodes (and thus should be secure from nefarious registrations), it leaves gaps where:

- An engineer copies code from one client to another. The source client (Client A) registers itself as Service A with Eureka Host A. When the engineer forgets to update the copied Eureka configuration, Client B will register itself with Eureka Host A as a node of Service A, when in fact it is a Sevice B. It's unlikely that this would make it to production, but these mistakes cause one or more nodes in your Eureka pool to fail requests which can be tricky to diagnose.
- You're deployed in a large shared enterprise environment. Let's say you're in an old-school large scale enterprise setup where everyone shares the same deployment space. Accounts and Billing are deployed in the same area, but are distinctly different. Although it's within the access boundry of the company, you likely wouldn't want Billing applications to accedentially/nefariously register themselves cross-domain with your Eureka host. Ideally every domain of the enterprise should be in an isolated space so they can't interact this way, but your results may vary.
- You just use plain sense security. You have the perfect isolated corporate network, VPC tables are snatched, audit gives you the gold star, and nefarious actors weep at how impenetrable your setup is. If you add the authentication, it's one less thing you would have to worry about in regards to a network intrusion. Although a bad actor may be able to access your Eureka host, they wouldn't be able to register a bad host in your fleet to siphon traffic off (not that your IP rules should allow that anyway).

### Cons of Authenticating
There's always an overhead with authenitcation like OAuth2, but it's very minimal. 

Your client application obtains a token and you re-use that token for the duration of it's lifecycle. If it's TTL is 4 hours, you only make 1 API calls every 4 hours to your IDP service.

From the Eureka host side, you have to authenticate the tokens coming in. Like with the client tokens, the host shoud obtain and cache an authentication token for so many hours. Thus the host side only has to make X calls per TTL period to authenticate incoming hosts. 

## Setup
1) On the Eureka host, setup your incoming endpoint authentication scheme. That's not covered in this project as it's assumed you already possess the ability from the host side to authenticate incoming API calls. Refer to the documentation related to your Eureka host version, but clients will call the ```/eureka/``` endpoint to authenticate. This is the endpoint that you want to secure, ensuring only a valid OAuth2 token with the correct scope can call it successfully. This is all you have to do from the host side, ezpz.
2) On the client side, add the two Eureka classes contained in this repo. Those classes work together to add the filter to your Eureka client which will authenticate your outgoing calls to the Eureka host. The javadoc has all of the information you'll need and will need to provide. By default they're configured not to kick in when the application is running locally, since you likely don't want local instances regisering themselves into deployed pools (unless you're into that).
