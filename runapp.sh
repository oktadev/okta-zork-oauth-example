#!/usr/bin/env bash

export OKTA_CLIENT_TOKEN=$OKTA_API_TOKEN
export OKTA_CLIENT_ORGURL=$OKTA_ORG_URL

java $JAVA_OPTS -Dserver.port=$PORT -jar target/*.jar \
    --okta.oauth.clientId=$OKTA_CLIENT_ID \
    --okta.oauth.issuer=$OKTA_ORG_URL/oauth2/$OKTA_AUTH_SERVER_ID \
    --okta.oauth.audience=$OKTA_AUDIENCE \
    --okta.oauth.baseUrl=$OKTA_ORG_URL