package com.mineskript.client.server;

import com.mineskript.client.server.elements.EffDisconnect;
import com.mineskript.client.server.elements.ExprOnlinePlayerNames;
import com.mineskript.client.server.elements.ExprPing;
import com.mineskript.client.server.elements.ExprPlayersOnline;
import com.mineskript.client.server.elements.ExprServerAddress;
import com.mineskript.client.server.elements.ExprServerBrand;
import com.mineskript.client.server.elements.ServerEvents;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

/** The server you are connected to and what it sends: players, the tab list, titles, sounds, particles and chunks. */
public final class ServerModule implements SyntaxModule {
    @Override
    public String name() {
        return "server";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        ServerEvents.register(registry);

        ExprPlayersOnline.register(registry);
        ExprServerAddress.register(registry);
        ExprServerBrand.register(registry);
        ExprPing.register(registry);
        ExprOnlinePlayerNames.register(registry);

        EffDisconnect.register(registry);
    }
}
