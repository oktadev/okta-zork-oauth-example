## Play Zork and Learn OpenID Connect

This is an example app that let's you play the Infocom classic: [Zork]().

But, there's a catch: All interactions with the game must be done using an [Access Token]().

## Jump In

1. Browse to the [OIDC Playground](https://https://okta-oidc-fun.herokuapp.com)    
2. Click off `code` and click on `token` for `response_type`

    ![oidc](images/oidc_step1.png)

3. Click the `Link` at the bottom of the page

    ![oidc](images/oidc_step1.png)
    
4. Copy the `Access Token`
5. From the command line, execute the following (uses [HTTPie](https://httpie.org)):

    ```
    http POST \
    https://okta-oauth-zork.herokuapp.com/v1/c \
    Authorization:"Bearer <access token>"
    ```  

6. You'll see a response like this:

    ```
    {
        "gameInfo": [
            "ZORK I: The Great Underground Empire",
            "Copyright (c) 1981, 1982, 1983 Infocom, Inc. All rights reserved.",
            "ZORK is a registered trademark of Infocom, Inc.",
            "Revision 88 / Serial number 840726"
        ],
        "look": [
            "",
            "West of House",
            "You are standing in an open field west of a white house, with a boarded front door.",
            "There is a small mailbox here."
        ],
        "status": "SUCCESS"
    }
    ```
    
7. You can give it commands too. For instance, to `go north`:

    ```
    http POST \
    https://okta-oauth-zork.herokuapp.com/v1/c \
    Authorization:"Bearer <access token>"
    command="go north"
    ```
    
## Learn More

Curious about what's going on here? Dive into our `OIDC Primer` series:

* [Identity, Claims, & Tokens – An OpenID Connect Primer, Part 1](https://developer.okta.com/blog/2017/07/25/oidc-primer-part-1)
* [OIDC in Action – An OpenID Connect Primer, Part 2](https://developer.okta.com/blog/2017/07/25/oidc-primer-part-2)
* [What’s in a Token? – An OpenID Connect Primer, Part 3](https://developer.okta.com/blog/2017/08/01/oidc-primer-part-3)
