package com.legoproject.backend;

import com.github.scribejava.core.builder.api.DefaultApi10a;

public class BrickLinkApi extends DefaultApi10a {

    private static final BrickLinkApi INSTANCE = new BrickLinkApi();

    public static BrickLinkApi instance() {
        return INSTANCE;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "";
    }
}