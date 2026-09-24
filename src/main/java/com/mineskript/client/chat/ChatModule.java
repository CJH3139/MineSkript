package com.mineskript.client.chat;

import com.mineskript.client.chat.elements.ChatEvents;
import com.mineskript.client.chat.elements.EffCancelEvent;
import com.mineskript.client.chat.elements.EffCommand;
import com.mineskript.client.chat.elements.EffMakeSay;
import com.mineskript.client.chat.elements.EffSend;
import com.mineskript.client.chat.elements.ExprMessage;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

/** Chat messages and commands, received and sent. */
public final class ChatModule implements SyntaxModule {
    @Override
    public String name() {
        return "chat";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        ChatEvents.register(registry);

        ExprMessage.register(registry);

        EffCancelEvent.register(registry);
        EffMakeSay.register(registry);
        EffCommand.register(registry);
        EffSend.register(registry);
    }
}
