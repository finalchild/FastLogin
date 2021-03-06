package com.github.games647.fastlogin.bungee.tasks;

import com.github.games647.fastlogin.bungee.BungeeLoginSession;
import com.github.games647.fastlogin.bungee.BungeeLoginSource;
import com.github.games647.fastlogin.bungee.FastLoginBungee;
import com.github.games647.fastlogin.core.PlayerProfile;
import com.github.games647.fastlogin.core.shared.JoinManagement;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.connection.InitialHandler;

public class AsyncPremiumCheck extends JoinManagement<ProxiedPlayer, CommandSender, BungeeLoginSource>
        implements Runnable {

    private final FastLoginBungee plugin;
    private final AsyncEvent<?> preLoginEvent;

    private final PendingConnection connection;

    public AsyncPremiumCheck(FastLoginBungee plugin, AsyncEvent<?> preLoginEvent, PendingConnection connection) {
        super(plugin.getCore(), plugin.getCore().getAuthPluginHook());

        this.plugin = plugin;
        this.preLoginEvent = preLoginEvent;
        this.connection = connection;
    }

    @Override
    public void run() {
        plugin.getSession().remove(connection);

        InitialHandler initialHandler = (InitialHandler) connection;
        String username = initialHandler.getLoginRequest().getData();
        try {
            super.onLogin(username, new BungeeLoginSource(connection));
        } finally {
            preLoginEvent.completeIntent(plugin);
        }
    }

    @Override
    public void requestPremiumLogin(BungeeLoginSource source, PlayerProfile profile,
                                    String username, boolean registered) {
        source.setOnlineMode();
        plugin.getSession().put(source.getConnection(), new BungeeLoginSession(username, registered, profile));

        String ip = source.getAddress().getAddress().getHostAddress();
        plugin.getCore().getPendingLogin().put(ip + username, new Object());
    }

    @Override
    public void startCrackedSession(BungeeLoginSource source, PlayerProfile profile, String username) {
        plugin.getSession().put(source.getConnection(), new BungeeLoginSession(username, false, profile));
    }
}
