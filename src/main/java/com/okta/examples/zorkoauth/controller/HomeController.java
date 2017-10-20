package com.okta.examples.zorkoauth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
public class HomeController {

    @Value("#{ @environment['okta.oauth.clientId'] }")
    String clientId;

    @Value("#{ @environment['okta.oauth.issuer'] }")
    String issuer;

    @RequestMapping("/")
    public String home(HttpServletRequest req, Model model) {
        model.addAttribute("clientId", clientId);
        model.addAttribute("userAuthorizationUri", issuer + "/v1/authorize");
        model.addAttribute("redirectUri", req.getRequestURL());
        model.addAttribute("nonce", UUID.randomUUID().toString());
        model.addAttribute("state", UUID.randomUUID().toString());

        return "home";
    }
}
