package org.example.parser;

import org.json.JSONObject;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Главная точка входа приложения.
 * Запускает цикл сбора данных о матчах и парсинга, а также сохраняет данные, подходящие по условиям.
 */
public class ApplicationEntryPoint {

    private static final Logger logger = Logger.getLogger(ApplicationEntryPoint.class.getName());

    private static final int MIN_DELAY_MS = 3000; // Минимальная задержка между запросами (3 сек)
    private static final int MAX_DELAY_MS = 5000; // Максимальная задержка между запросами (5 сек)

    private final ResultWriter resultWriter = new ResultWriter();
    private final HttpConnectionManager connectionManager = new HttpConnectionManager();

    private final Map<Integer, double[]> oddsMap = new HashMap<>();

    private final GameDataExtractor gameDataExtractor = new GameDataExtractor(connectionManager.getLinkProcessor(), oddsMap);
    private final JsonGameMapper jsonGameMapper = new JsonGameMapper(gameDataExtractor);

    private final List<Game> games = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException, TelegramApiException {
        ApplicationEntryPoint app = new ApplicationEntryPoint();
        app.run();
    }

    /**
     * Основной цикл сбора данных и записи результата.
     */
    public void run() throws IOException, InterruptedException, TelegramApiException {
        logger.info("Логика парсера запущена");

        resultWriter.writeStartLog(); // Отладочная запись старта программы

        while (true) {
            try {
                JSONObject gameJson = connectionManager.fetchGameJson();

                // Маппим JSON в список игр
                jsonGameMapper.mapJsonToGames(gameJson, games);

                connectionManager.disconnect();

            } catch (java.net.ConnectException e) {
                logger.warning("Ошибка подключения: " + e.getMessage());
                Thread.sleep(60 * 1000); // Подождать минуту перед повтором
            }

            // Выводим отладочную информацию
            for (Game game : games) {
                logger.info(game.toString());
            }

            // Обрабатываем сигналы на запись результатов
            handleSignal();

            // Задержка между итерациями
            int delay = new Random().nextInt(MAX_DELAY_MS - MIN_DELAY_MS + 1) + MIN_DELAY_MS;
            Thread.sleep(delay);
        }
    }

    /**
     * Обрабатывает игры, удовлетворяющие условиям, и сохраняет результат.
     * Удаляет устаревшие игры.
     */
    public void handleSignal() {
        logger.info("Запущена проверка сигналов для записи результатов");
        logger.info("Количество игр в очереди: " + games.size());

        Iterator<Game> iterator = games.iterator();
        while (iterator.hasNext()) {
            Game game = iterator.next();

            if (game.isReadyToWrite()) {
                resultWriter.writeGameResult(game);
                game.markProcessed();
            } else if (game.isExpired()) {
                iterator.remove();
            }
        }
    }
}
