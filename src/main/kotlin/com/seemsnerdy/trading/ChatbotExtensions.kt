package com.seemsnerdy.trading

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.HandleCommand
import com.github.kotlintelegrambot.dispatcher.handlers.HandleMessage
import com.github.kotlintelegrambot.dispatcher.message

fun Dispatcher.filteredCommand(command: String, chatId: Long, handleCommand: HandleCommand) = command(command) {
    if (chatId == message.chat.id) {
        handleCommand()
    }
}

fun Dispatcher.filteredMessage(chatId: Long, handleMessage: HandleMessage) = message {
    if (chatId == message.chat.id) {
        handleMessage()
    }
}