package org.example.parser;

import data.LinkProcessor;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Класс отвечает за установку HTTP-соединения, отправку запроса и получение JSON-ответа от сервера.
 */
public class HttpConnectionManager {

    private static final Logger logger = Logger.getLogger(HttpConnectionManager.class.getName());

    private static final String BASE_URL =
            "https://1xstavka.ru/LiveFeed/Get1x2_VZip?sports=3&count=50&antisports=188&mode=4";
    private static final String QUERY_PARAMS =
            "&country=1&partner=51&getEmpty=true&noFilterBlockEvent=true";

    private final LinkProcessor linkProcessor = new LinkProcessor();
    private HttpURLConnection connection;

    /**
     * Устанавливает соединение с сервером и возвращает JSON-объект с ответом.
     *
     * @return JSONObject — ответ от сервера.
     * @throws IOException если возникли проблемы с сетевым подключением.
     */
    public JSONObject connectAndGetJson() throws IOException {
        URL fullUrl = new URL(BASE_URL + linkProcessor.getLink() + QUERY_PARAMS);
        connection = (HttpURLConnection) fullUrl.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

        int responseCode = connection.getResponseCode();
        logger.info("Ответ сервера: " + responseCode);

        StringBuilder responseBuilder = new StringBuilder();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

            }
        } else {
            logger.warning("Не удалось получить ответ от сервера");
        }

        String responseString = responseBuilder.toString();
        logger.info("Ответ сервера (JSON): " + responseString);
        return new JSONObject(responseString);
    }

    /**
     * Закрывает текущее соединение с сервером.
     */
    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    /**
     * Возвращает обработчик ссылок.
     *
     * @return LinkProcessor — экземпляр обработчика.
     */
    public LinkProcessor getLinkProcessor() {
        return linkProcessor;
    }
}
