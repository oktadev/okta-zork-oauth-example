#!/usr/bin/env bash

cat<<EOF
about to run:

java $JAVA_OPTS -Dserver.port=$PORT -jar target/*.jar \\
    --okta.client.orgUrl=$OKTA_ORG_URL \\
    --okta.client.token=$OKTA_API_TOKEN \\
    --security.oauth2.client.clientId=$OKTA_CLIENT_ID \\
    --security.oauth2.client.clientSecret=$OKTA_CLIENT_SECRET \\
    --security.oauth2.client.accessTokenUri=$OKTA_ORG_URL/oauth2/$OKTA_AUTHORIZATION_SERVER_ID/v1/token \\
    --security.oauth2.client.userAuthorizationUri=$OKTA_ORG_URL/oauth2/$OKTA_AUTHORIZATION_SERVER_ID/v1/authorize \\
    --security.oauth2.resource.userInfoUri=$OKTA_ORG_URL/oauth2/$OKTA_AUTHORIZATION_SERVER_ID/v1/userinfo \\
    --security.oauth2.resource.openIdConfigUri=$OKTA_ORG_URL/oauth2/$OKTA_AUTHORIZATION_SERVER_ID/.well-known/openid-configuration \\
    --security.oauth2.client.clientAuthenticationScheme=form \\
    --security.oauth2.client.scope="openid profile email" \\
    --security.oauth2.resource.preferTokenInfo=false


EOF

java $JAVA_OPTS -Dserver.port=$PORT -jar target/*.jar \
    --okta.client.orgUrl=$OKTA_ORG_URL \
    --okta.client.token=$OKTA_API_TOKEN \
    --security.oauth2.client.clientId=$OKTA_CLIENT_ID \
    --security.oauth2.client.clientSecret=$OKTA_CLIENT_SECRET \
    --security.oauth2.client.accessTokenUri=$OKTA_ORG_URL/oauth2/$OKTA_AUTHORIZATION_SERVER_ID/v1/token \
    --security.oauth2.client.userAuthorizationUri=$OKTA_ORG_URL/oauth2/$OKTA_AUTHORIZATION_SERVER_ID/v1/authorize \
    --security.oauth2.resource.userInfoUri=$OKTA_ORG_URL/oauth2/$OKTA_AUTHORIZATION_SERVER_ID/v1/userinfo \
    --security.oauth2.resource.openIdConfigUri=$OKTA_ORG_URL/oauth2/$OKTA_AUTHORIZATION_SERVER_ID/.well-known/openid-configuration \
    --security.oauth2.client.clientAuthenticationScheme=form \
    --security.oauth2.client.scope="openid profile email" \
    --security.oauth2.resource.preferTokenInfo=false
