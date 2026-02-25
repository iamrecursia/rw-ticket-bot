package com.kozitskiy.rwticket.telegrambotservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String botName;
    private final TicketService ticketService;

    private final List<String> CITIES = List.of("Минск", "Пинск", "Брест", "Гомель");

    public TelegramBot(@Value("${bot.token}") String botToken,
                       @Value("${bot.name}") String botName,
                       TicketService ticketService) {
        super(botToken);
        this.botName = botName;
        this.ticketService = ticketService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")){
                sendCitySelection(chatId, "Привет! Выбери станцию ОТПРАВЛЕНИЯ:", "from_");
            }else {
                sendMessage(chatId, "Пожалуйста, используйте меню или команду /start");
            }
        }
        else if (update.hasCallbackQuery()){
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            handleCallback(chatId, callBackData);
        }
    }

    private void handleCallback(long chatId, String callbackData){
        if (callbackData.startsWith("from_")){
            String fromCity = callbackData.replace("from_", "");
            sendCitySelection(chatId, "✅ Откуда: " + fromCity + "\n🏁 Выберите станцию ПРИБЫТИЯ:", "to_" + fromCity + "_");
        }
        else if (callbackData.startsWith("to_")) {
            String route = callbackData.replace("to_", "");
            sendDateSelection(chatId, route);
        }
        else if (callbackData.startsWith("date_")) {
            String[] parts = callbackData.split("_");
            // parts[0] = "date", parts[1] = "Минск", parts[2] = "Пинск", parts[3] = "2026-02-23"

            String from = parts[1];
            String to = parts[2];
            String date = parts[3];

            sendMessage(chatId, "⏳ Ищу билеты:\n" + from + " ➡️ " + to + "\nНа дату: " + date + "\nПодождите пару секунд...");

            String response = ticketService.findTickets(from, to, date);
            sendMessage(chatId, response);
        }
    }

    private void sendCitySelection(long chatId, String text, String callbackPrefix) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (String city : CITIES) {
            if (callbackPrefix.endsWith(city + "_")) continue;

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(city);
            button.setCallbackData(callbackPrefix + city); // Пример: from_Минск или to_Минск_Пинск

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        executeMessage(message);
    }

    private void sendDateSelection(long chatId, String route) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        String[] cities = route.split("_");
        message.setText("✅ Маршрут: " + cities[0] + " ➡️ " + cities[1] + "\n📅 Выберите дату:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 3; i++) {
            LocalDate targetDate = today.plusDays(i);
            String dateString = targetDate.toString(); // Формат YYYY-MM-DD

            String buttonText = dateString;
            if (i == 0) buttonText += " (Сегодня)";
            if (i == 1) buttonText += " (Завтра)";

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);
            button.setCallbackData("date_" + route + "_" + dateString);

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        executeMessage(message);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
