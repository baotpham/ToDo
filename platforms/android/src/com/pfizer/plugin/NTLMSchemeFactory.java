package com.pfizer.plugin;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;

/**
 * Project NTLMSchemeFactory
 * <p/>
 * Created by jefferysmallwood.
 * <p/>
 * Created by Pfizer on 2/21/14.
 * Copyright (c) 2014 Pfizer. All rights reserved.
 */
public class NTLMSchemeFactory implements AuthSchemeFactory{
    @Override
    public AuthScheme newInstance(HttpParams params)
    {
        return new NTLMScheme(new JCIFSEngine());
    }
}