package io;

import modele.TokenStack;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by nicolas on 27/02/15.
 */
public interface IOManager {
    public void feedWithToken(TokenStack t) throws SourceNotFoundException, SourceIncorrectException, URISyntaxException;
    public void feedWithTokenRTWizard(TokenStack t) throws SourceNotFoundException, SourceIncorrectException, URISyntaxException;
    public void feedWithTokenResearcher(TokenStack t) throws SourceNotFoundException, SourceIncorrectException, URISyntaxException;
    public String getDescriptionSource();
    public String getNextIdToCrawl();
    public boolean hastNextIdToCrawl();
    public void write(String m) throws IOException;
    public void writeLine(String m) throws IOException;
    public void close() throws IOException;
}
